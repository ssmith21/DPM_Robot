package ca.mcgill.ecse211.project;

import static ca.mcgill.ecse211.project.Resources.*;

import ca.mcgill.ecse211.playingfield.Point;
import simlejos.ExecutionController;

/**
 * The Navigation class is used to make the robot navigate around the playing field.
 */
public class Navigation {
  
  /** a counter to make sure two points are the same. */
  public static int counter = 0;
  
  
  /** Do not instantiate this class. */
  private Navigation() {}
  
  
  

  
  // Since now it is also crossing the bridge it is not a bad idea to put it in Navigation
  public static void crossingTunnel(int startingCorner) {
    
    // TODO : localization is problematic at bottom corners
    // TODO : The horizontal tunnel and Bottom cases still should be tested and adjusted
    double horizontalOffset = (BASE_WIDTH / 2); // horizontal offset of robots' position for tunnel
    double verticalOffset = (BASE_WIDTH / 2.5); // vertical offset of robots' position for tunnel
    switch (startingCorner) {
      case(0):
        println("Bottom left");
        odometer.setXyt(toMeters(1), toMeters(1), 0);
        if (verticalTunnel) {
          turnTo(90);
          Movement.moveStraightFor(toMeters((tunnel.ll.x + 0.5) - toFeet(odometer.getXyt()[0])) );
          turnTo(0);
          odometer.setXyt(toMeters(tunnel.ll.x + 0.5), toMeters(1), 0);
          Movement.moveStraightFor(toMeters(toFeet(odometer.getXyt()[1]) - tunnel.ur.y));
          odometer.setXyt(toMeters(tunnel.ur.x - 0.5), toMeters(tunnel.ur.y), 0);
        } else {
          Movement.moveStraightFor(toMeters((tunnel.ll.y + 0.5) - toFeet(odometer.getXyt()[1])));
          turnTo(90);
          odometer.setXyt(toMeters(1), toMeters(tunnel.ll.y + 0.5), 90);
          Movement.moveStraightFor(toMeters(toFeet(odometer.getXyt()[0]) - tunnel.ur.x));
          odometer.setXyt(toMeters(tunnel.ur.x), toMeters(tunnel.ur.y + 0.5), 90);
        }
        break;
      case(1):
        println("Bottom right");
        odometer.setXyt(toMeters(14), toMeters(1), 270);
        if (verticalTunnel) {
          Movement.moveStraightFor(
              toMeters((tunnel.ll.x + 0.5) - toFeet(odometer.getXyt()[0])) - horizontalOffset);
          turnTo(0);
          Movement.moveStraightFor(verticalOffset);
          odometer.setXyt(toMeters(tunnel.ll.x + 0.5), toMeters(1), 0);
          Movement.moveStraightFor(toMeters(toFeet(odometer.getXyt()[1]) - tunnel.ur.y));
          odometer.setXyt(toMeters(tunnel.ll.x + 0.5), toMeters(tunnel.ur.y), 0);
        } else {
          turnTo(0);
          Movement.moveStraightFor(toMeters((tunnel.ll.y + 0.5) - toFeet(odometer.getXyt()[1])));
          turnTo(270);
          odometer.setXyt(toMeters(14), toMeters(tunnel.ll.y + 0.5), 270);
          Movement.moveStraightFor(toMeters(toFeet(odometer.getXyt()[0]) - tunnel.ll.x));
          odometer.setXyt(toMeters(tunnel.ll.x), toMeters(tunnel.ll.y + 0.5), 270);
        }
        break;
      case(2):
        println("Top right");
        odometer.setXyt(toMeters(14), toMeters(8), 180);
        if (verticalTunnel) {
          turnTo(90);
          Movement.moveStraightFor(toMeters((tunnel.ll.x + 0.5) - toFeet(odometer.getXyt()[0])));
          turnTo(180);
          odometer.setXyt(toMeters(tunnel.ll.x + 0.5), toMeters(8), 180);
          Movement.moveStraightFor(toMeters(toFeet(odometer.getXyt()[1]) - tunnel.ll.y));
          odometer.setXyt(toMeters(tunnel.ll.x + 0.5), toMeters(tunnel.ll.y), 180);
        } else {
          Movement.moveStraightFor(
              toMeters(-(tunnel.ll.y + 0.5) + toFeet(odometer.getXyt()[1])) - verticalOffset);
          turnTo(270);
          Movement.moveStraightFor(horizontalOffset);
          odometer.setXyt(toMeters(14), toMeters(tunnel.ll.y + 0.5), 270);
          Movement.moveStraightFor(toMeters(toFeet(odometer.getXyt()[0]) - tunnel.ll.x));
          odometer.setXyt(toMeters(tunnel.ll.x), toMeters(tunnel.ll.y + 0.5), 270);
        }
        break;
      case(3):
        println("Top left");
        odometer.setXyt(toMeters(1), toMeters(8), 90);
        
        // TODO: new class with just tunnel protocols?
        
        // TODO: Remove the next 3 lines
//        tnr.ll = new Point(5,6);
//        tnr.ur = new Point(8,7);
        
        tnr = tng; // to test
        println(tnr.ll);
        println(tnr.ur);
        verticalTunnel = false;
        
        if (verticalTunnel) {
//          tnr.ll = new Point(0,3);
//          tnr.ur = new Point(1,5);
          
          /* step 0 : preliminary calculations */
          Point cur = getCurrentPoint_feet();
          double destX = tnr.ll.x;
          double destY = cur.y;
          Point dest = new Point(destX, destY);
          double destTheta = getDestinationAngle(cur,dest);
          double distance = toMeters(distanceBetween(cur,dest));
          
          /* step 1 : get to tunnels' x position if not already inline */
          if(roughlySame(cur.x, destX, 0.2)) {
            turnTo(destTheta);
            Movement.moveStraightFor(distance);
            LightLocalizer.localize_waypoint();
          }
          
          /* step 2 : correct position and move to center of the tunnel */
          if(cur.x < tnr.ll.x) { // check if tunnel is alone x-axis boundary
            turnTo(90);
            LightLocalizer.alignWithLine();
            Movement.moveStraightFor(verticalOffset);
          } else {
            turnTo(270);
            LightLocalizer.alignWithLine();
            Movement.moveStraightFor(verticalOffset);
          }
          
          /* step 3 : point towards tunnel and ensure we're point straight through the tunnel */
          turnTo(180);
          LightLocalizer.alignWithLine();

          
          /* step 4 : approach tunnel while constantly correcting position at each tile */
          destX = cur.x;
          destY = tnr.ur.y;
          selfCorrectingPath(destX,destY);

          /* step 5 : travel through the tunnel, constantly correcting its' position at each tile */
          destX = cur.x;
          destY = tnr.ll.y;
          selfCorrectingPath(destX, destY);

          /* step 6 : move for 90% of one additional tile, then align with line */
          Movement.moveStraightFor(TILE_SIZE - TILE_SIZE/10);
          LightLocalizer.alignWithLine();
          
        } else {
          
          /* step 0 : preliminary calculations */
          Point cur = getCurrentPoint_feet();
          double destX = cur.x;
          double destY = tnr.ur.y;
          Point dest = new Point(destX, destY);
          double destTheta = getDestinationAngle(cur,dest);
          double distance = toMeters(distanceBetween(cur,dest));
          
          /* step 1 : get to tunnels' y position if not already inline */
          if(!roughlySame(cur.y, tnr.ll.y, 0.2)) {
            turnTo(destTheta);
            Movement.moveStraightFor(distance);
            LightLocalizer.localize_waypoint_2();
          }
          println("Done step 1");

          
          /* step 2 : correct position and move to center of the tunnel */
          if(!roughlySame(cur.y, tnr.ll.y, 0.2)) {
            turnTo(180);
            LightLocalizer.alignWithLine();
            Movement.moveStraightFor(verticalOffset);
          }else {
            turnTo(0);
            LightLocalizer.alignWithLine();
            Movement.moveStraightFor(verticalOffset);
          }
          println("Done step 2");
          
          /* step 3 : point towards tunnel and ensure we're point straight through the tunnel */
          turnTo(90);
          LightLocalizer.alignWithLine();
          
          /* step 4 : approach tunnel while constantly correcting position at each tile */
          destX = tnr.ll.x;
          destY = cur.y;
          selfCorrectingPath(destX, destY);
          
          /* step 5 : travel through the tunnel, constantly correcting its' position at each tile */
          destX = tnr.ur.x;
          destY = cur.y;
          selfCorrectingPath(destX, destY);
          
          /* step 6 : move for 90% of one additional tile, then align with line */
          Movement.moveStraightFor(TILE_SIZE - TILE_SIZE/10);
          LightLocalizer.alignWithLine();
        }
        break;
      default:
        println("Error getting starting corner");
    }
  }
  
  
  /**
   * 
   * Traval along the x or y axis direction of the playing field while constantly
   * correcting its' path, to make sure that we're travelling directly along the 
   * x or y axis.
   * We do this by travelling 90% of a tile length, then align ourselves with the 
   * black line of the tile perpendicular to our path.
   * 
   * @param destX
   * @param destY
   */
  private static void selfCorrectingPath(double destX, double destY) {
    Point cur = getCurrentPoint_feet();
    Point dest = new Point(destX, destY);
    int nrTiles = (int) Math.floor(distanceBetween(cur,dest));
    for(int i=0; i<nrTiles; i++) {
      Movement.moveStraightFor(TILE_SIZE - TILE_SIZE/10);
      LightLocalizer.alignWithLine();
    }
  }
  
  
  // no obstacles
  public static void driveToFirstWayPoint() {
    Movement.moveStraightFor(TILE_SIZE / 2);
    LightLocalizer.localize_waypoint();
    Point p1 = new Point(14, 1); 
    Movement.pause(3);
    turnTo(getDestinationAngle(getCurrentPoint_feet(), p1));
    Movement.pause(3);
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
    Movement.pause(1);
  }
  
  /**
   * Takes a point and moves directly toward it without concerning about the obstacles.
   * @param destination
   */
  public static void directTravelTo(Point destination) {
    Point curPoint = getCurrentPoint_feet();
    double travelDist = distanceBetween(curPoint, destination);
    Movement.moveStraightFor(toMeters(travelDist));
    //odometer.setX(toMeters(destination.x));
    //odometer.setY(toMeters(destination.y));
    //odometer.setTheta(getDestinationAngle(curPoint, destination) + 90);
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
