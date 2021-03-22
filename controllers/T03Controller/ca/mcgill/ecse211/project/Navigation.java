package ca.mcgill.ecse211.project;

import static ca.mcgill.ecse211.project.Resources.*;

import ca.mcgill.ecse211.playingfield.Point;
import simlejos.ExecutionController;

/**
 * The Navigation class is used to make the robot navigate around the playing field.
 */
public class Navigation {
  
  /** a counter to make sure two points are the same. */
  private static int counter = 0;
  
  /** Do not instantiate this class. */
  private Navigation() {}
  
  
  
  public static int getStartingPoint() {
    return (redTeam == 3) ? redCorner : greenCorner;
  }
  
  // Since now it is also crossing the bridge it is not a bad idea to put it in Navigation
  public static void initialLocalize(int startingCorner) {
    
    // TODO : localization is problematic at bottom corners
    // TODO : change the odometer angles based on the best angles to start at
    // TODO : cover the case that one team has the bridge beside the wall and the other has not.
    switch (startingCorner) {
      case(0):
        //TODO : this case hasn't been tested yet
        println("Bottom left");
        UltrasonicLocalizer.localize();
        LightLocalizer.localize_start();
        odometer.setXyt(Navigation.toMeters(1), Navigation.toMeters(1), 90);
        break;
        //TODO : needs to be improved after localization is fixed
      case(1):
        println("Bottom right");
        UltrasonicLocalizer.localize();
        LightLocalizer.localize_start();
        odometer.setXyt(Navigation.toMeters(14), Navigation.toMeters(1), 270);
        if (tng.ll.x == 13.0 || tnr.ll.x == 13.0) {
          Movement.moveStraightFor((TILE_SIZE) / 3);
        }
        if (tng.ll.x == 14.0 || tnr.ll.x == 14.0) {
          Movement.moveStraightFor((-TILE_SIZE) / 1.7);
        }
        Navigation.turnTo(0);
        Movement.moveStraightFor((TILE_SIZE * 3) + (BASE_WIDTH / 4));
        if (redTeam == 3) {
          odometer.setXyt(Navigation.toMeters(tnr.ll.x + 0.5), Navigation.toMeters(tnr.ur.y), 180);
        } else {
          odometer.setXyt(Navigation.toMeters(tng.ll.x + 0.5), Navigation.toMeters(tng.ur.y), 180);
        }
        break;
      case(2):
        println("Top right");
        UltrasonicLocalizer.localize();
        LightLocalizer.localize_start();
        odometer.setXyt(Navigation.toMeters(14), Navigation.toMeters(8), 180);
        Navigation.turnTo(90);
        // When bridge is beside the wall
        if (tng.ll.x == 14.0 || tnr.ll.x == 14.0) {
          Movement.moveStraightFor((TILE_SIZE) / 2);
        }
        // When the bridge is not beside the wall
        if (tng.ll.x == 13.0 || tnr.ll.x == 13.0) {
          Movement.moveStraightFor((-TILE_SIZE) / 2);
        }
        Navigation.turnTo(180);
        Movement.moveStraightFor((TILE_SIZE * 3));
        if (redTeam == 3) {
          odometer.setXyt(Navigation.toMeters(tnr.ll.x + 0.5), Navigation.toMeters(tnr.ll.y), 180);
        } else {
          odometer.setXyt(Navigation.toMeters(tng.ll.x + 0.5), Navigation.toMeters(tng.ll.y), 180);
        }
        break;
      case(3):
        println("Top left");
        UltrasonicLocalizer.localize();
        LightLocalizer.localize_start();
        odometer.setXyt(Navigation.toMeters(1), Navigation.toMeters(8), 90);
        if (tnr.ll.x == 0.0 || tng.ll.x == 0.0) {
          Movement.moveStraightFor((-TILE_SIZE * 1.35) / 2);
        }
        if (tnr.ll.x == 1.0 || tng.ll.x == 1.0) {
          Movement.moveStraightFor((TILE_SIZE * 0.50) / 2);
        }
        Navigation.turnTo(180);
        Movement.moveStraightFor((BASE_WIDTH / 4) + (TILE_SIZE * 3) + (BASE_WIDTH / 6));
        if (redTeam == 3) {
          odometer.setXyt(Navigation.toMeters(tnr.ll.x + 0.5), Navigation.toMeters(tnr.ll.y), 180);
        }
        else {
          odometer.setXyt(Navigation.toMeters(tng.ll.x + 0.5), Navigation.toMeters(tng.ll.y), 180);
        }
        break;
      default:
        println("Error getting starting corner");
    }
  }
  
  
  
  // no obstacles
  public static void driveToFirstWayPoint() {
    Movement.moveStraightFor(TILE_SIZE/2);
    LightLocalizer.localize_waypoint();
    Point p1 = new Point(14,1); 
    pause();
    turnTo(getDestinationAngle(getCurrentPoint_feet(), p1));
    pause();
    directTravelTo(p1);
  }
  
  
  
  
  
  
  
  /**
   * Travels to specific destination depending on whether
   * the orientation of the robot is correct and whether the path is obstacle free.
   * Turns the robot toward the destination by minimal angle. 
   * Takes the green zone and the red zone into account when it wants to travel.
   * It considers 5 different cases:
   * Case 1: We're already at the destination.
   * Case 2: We're facing the right way.
   * Case 3: We have to turn.
   * @param destination the destination point
   */
  public static void travelTo(Point destination) {
    Point startPoint = getCurrentPoint_feet();
    double travelDist = toMeters(distanceBetween(startPoint, destination));
    double destTheta = getDestinationAngle(startPoint, destination);
    double[] xyt = odometer.getXyt();
    double angleDiff = Math.abs(destTheta - xyt[2]);
    
    // case 1: we're already at the destination
    if (travelDist < 0.2) {
      System.out.println("Already at destination.");
      return;
    }
    
    // case 2: we're facing the right way
    if (angleDiff < 5.0 || angleDiff > 355.0) {
      System.out.println("Already Pointing in the right direction, might have obstacles ahead.");
      travelToObstacle(destination);
      
      // case 3: we have to turn.
    } else {
      System.out.println("Destination might have obstacles ahead.");
      turnTo(destTheta);
      travelToObstacle(destination);
    }
    
    double tolerance = 0.4;
    if ((roughlySame(startPoint.x, destination.x, tolerance)
        || roughlySame(startPoint.y, destination.y, tolerance))
        && counter < 4
        ) {
      counter++;
      LightLocalizer.localize_waypoint();
    }
    odometer.setX(toMeters(destination.x));
    odometer.setY(toMeters(destination.y));
    odometer.setTheta(getDestinationAngle(startPoint, destination) + 90);
    pause();
  }
  
  /**
   * Takes a point and moves directly toward it without concerning about the obstacles.
   * @param destination
   */
  public static void directTravelTo(Point destination) {
    Point curPoint = getCurrentPoint_feet();
    double travelDist = distanceBetween(curPoint, destination);
    Movement.moveStraightFor(toMeters(travelDist));
  }
  
  /**
   * Moves forward and when it detects an obstacle calls avoid method from AvoidObstalce Class.
   * When it passes the obstacle or it is close to destination calls directTravelTo.
   * @param destination
   */
  public static void travelToObstacle(Point destination) {
    int noiseTolerance = 2;
    Movement.setMotorSpeeds(200);
    while (true) {
      Movement.drive();
      Point cur = getCurrentPoint_feet();
      if (comparePoints(cur, destination, 0.2)) {
        System.out.println("Near destination, stop detecting obstacles.");
        break;
      }
      if (AvoidObstacle.readUsDistance() < 11) {
        noiseTolerance--;
      }
      if (noiseTolerance == 0) {
        Point startPoint = getCurrentPoint_feet();
        AvoidObstacle.avoid(startPoint, destination);
        break; 
      }
    }
    Point c = getCurrentPoint_feet();
    System.out.println("Currently at (" + c.x + "," + c.y
        + ")\tTravelling to (" + destination.x + "," + destination.y
        + ")\t Distance = " + distanceBetween(c, destination));
    
    if (distanceBetween(c, destination) < 0.5) {
      directTravelTo(destination);
    } else {
      travelTo(destination);
    }
    
  }
  
  /**
   * Turns the robot with a minimal angle towards the given input angle in degrees, no matter what
   * its current orientation is. This method is different from {@code turnBy()}.
   */
  public static void turnTo(double angle) {
    Movement.turnBy(minimalAngle(Odometer.getOdometer().getXyt()[2], angle));
  }

  /**
   * Gets the angle to the destination point.
   * @param current Current position of the robot
   * @param destination Destination point
   * @return the destination angle
   */
  public static double getDestinationAngle(Point current, Point destination) {
    return (Math.toDegrees(
        Math.atan2(destination.x - current.x, destination.y - current.y)) + 360) % 360;
  }
  
  /**
   * Calculates the minimal angle to turn in degree.
   * @param initialAngle initial angle of the robot
   * @param destAngle destination angle
   * @return toTurn the minimal angle
   */
  public static double minimalAngle(double initialAngle, double destAngle) {
    initialAngle %= 360;
    destAngle %= 360;
    double toTurn = (destAngle - initialAngle + 540) % 360 - 180;
    return toTurn;
  }
  
  /**
   * Calculates the distance between two points in feet.
   * @param p1  First point
   * @param p2  Second point
   * @return dist distance
   */
  public static double distanceBetween(Point p1, Point p2) {
    double dxSqr = Math.pow((p2.x - p1.x), 2);
    double dySqr = Math.pow((p2.y - p1.y), 2);
    double dist = Math.sqrt(dxSqr + dySqr);
    return dist;
  }
  
  /**
   * Takes current point and destination points and compare them.
   * If the difference is less than the tolerance
   * returns true
   * @param cur Current point
   * @param destination Destination point
   * @param tolerance Tolerated difference
   * @return true if points are the close
   */
  public static boolean comparePoints(Point cur, Point destination, double tolerance) {
    double distCurDest = distanceBetween(cur, destination);
    return (distCurDest < tolerance);
  }
  
  /**
   * Converts meters to feet.
   * @param meters distance in meter
   * @return feet unit
   */
  public static double toFeet(double meters) {
    return 3.28084 * meters;
  }
  
  /**
   * Converts feet to meters.
   * @param feet distance in feet
   * @return meter unit
   */
  public static double toMeters(double feet) {
    return feet / 3.28084;
  }
  
  /**
   * Takes two numbers and compare them.
   * if the difference is less than the tolerance
   * return true
   * @param a first number
   * @param b second number
   * @param tolerance tolerated difference
   * @return true if the values are roughly the same
   */
  private static boolean roughlySame(double a, double b, double tolerance) {
    double diff = Math.abs(a - b);
    return (diff < tolerance);
  }
  
  /**
   * gets the current point from odometer in feet.
   * @return Point in feet unit
   */
  public static Point getCurrentPoint_feet() {
    double[] xyt = odometer.getXyt();
    Point curPoint = new Point(toFeet(xyt[0]), toFeet(xyt[1]));
    return curPoint;
  }
  
  /**
   * gets the current point from odometer in meters.
   * @return Point in meter unit
   */
  public static Point getCurrentPoint_meters() {
    double[] xyt = odometer.getXyt();
    Point curPoint = new Point(xyt[0], xyt[1]);
    return curPoint;
  }
    
  private static void pause() {
    leftMotor.setSpeed(0);
    rightMotor.setSpeed(0);
    
    for (int i = 0; i < 3000; i++) {
      ExecutionController.waitUntilNextStep();
    }
    
    leftMotor.setSpeed(FORWARD_SPEED);
    rightMotor.setSpeed(FORWARD_SPEED);
  }
  
  /**
   * Converts input distance to the total rotation of each wheel needed to cover that distance.
   * 
   * @param distance the input distance in meters
   * @return the wheel rotations necessary to cover the distance in degrees
   */
  public static int convertDistance(double distance) {
    // Using arc length formula to calculate the distance + scaling
    return (int) ((180 * distance) / (Math.PI * WHEEL_RAD) * 100) / 100;
  }

  /**
   * Converts input angle to total rotation of each wheel needed to rotate robot by that angle.
   * 
   * @param angle the input angle in degrees
   * @return the wheel rotations (in degrees) necessary to rotate the robot by the angle
   */
  public static int convertAngle(double angle) {
    // Using convertDistance method to calculate constant rotation of the robot + scaling
    return convertDistance((Math.PI * BASE_WIDTH * angle / 360.0) * 100) / 100;
  }

}
