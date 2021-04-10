package ca.mcgill.ecse211.project;

import static ca.mcgill.ecse211.project.Resources.*;

import ca.mcgill.ecse211.playingfield.Point;
import java.util.List;


/**
 * The Navigation class is used to make the robot navigate around the playing field.
 */
public class Navigation {

  /** the error margin when comparing two numbers to see if they are the same. */
  private static double smallTolerance = 0.2;
  /** a larger error margin when comparing two numbers to see if they are the same.**/
  private static double bigTolerance = 1.1;
  /** horizontal offset of robots' position for tunnel. */
  public static double horizontalOffset = (BASE_WIDTH / 2);
  /** vertical offset of robots' position for tunnel. */
  public static double verticalOffset = (BASE_WIDTH / 2.5);
  /** horizontal tunnel orientation while crossing the tunnel at the beginning. */
  public static int horizontalOrientation = (corner == 0 || corner == 3) ? 90 : 270;
  /** vertical tunnel orientation while crossing the tunnel at the beginning. */
  public static int verticalOrientation = (corner == 2 || corner == 3) ? 180 : 0;
  /** horizontal tunnel orientation while crossing the tunnel at the end. */
  public static int fHorizontalOrientation = (corner == 0 || corner == 3) ? 270 : 90;
  /** vertical tunnel orientation while crossing the tunnel at the end. */
  public static int fVerticalOrientation = (corner == 2 || corner == 3) ? 0 : 180;


  /** Do not instantiate this class. */
  private Navigation() {}

  /**
   * Perform 1 lap, where the track is a combination of all the waypoints.
   * Drives from the first waypoint in the list to the last waypoint in the list.
   * This method calls an overpass handling method for driving over the overpass
   * and also accounts for obstacle avoidance. Note that this method
   * @param waypoints : A list of all the waypoints the robot must travel to, usually passed from an XML file.
   */
  public static void doLap(List<Point> waypoints) {    
    driveToFirstWayPoint(waypoints.get(0));
    LightLocalizer.localize_waypoint();    
    boolean overpassExists = false;
    for(int i = 0; i < waypoints.size()-1; i++) { // i is the current waypoint index.
      try {
        overpassExists = checkForOverpass(waypoints.get(i),waypoints.get(i+1));
      } catch(ArrayIndexOutOfBoundsException e) {}
      if(overpassExists) {
        println("Driving over overpass then travelling to "+waypoints.get(i+1));
        driveOverpass();
        travelTo(waypoints.get(i+1));
      } else {
        println("Travelling to "+waypoints.get(i+1));
        travelTo(waypoints.get(i+1));
      }
    }
  }
  
  /**
   * Drive over an overpass from overpass point A to overpass point B or vice-versa,
   * depending on which overpass point is closer to the waypoint which the robot is 
   * currently at. This method accounts for the slip between the wheel and the surface
   * of the playground and wooden overpass and sets the odometer to the endpoint of the
   * overpass after the robot has passed over the overpass and arrived at the last endpoint.
   * 
   */
  private static void driveOverpass() {
    double distA = distanceBetween(getCurrentPoint_feet(), overpass.endpointA);
    double distB = distanceBetween(getCurrentPoint_feet(), overpass.endpointB);
    Point overpassStart = (distA < distB) ? (overpass.endpointA) : (overpass.endpointB);
    Point overpassEnd = (distA < distB) ? (overpass.endpointB) : (overpass.endpointA);
    
    /* Step 1: arrive at first overpass endpoint */
    double destTheta = getDestinationAngle(getCurrentPoint_feet(), overpassStart);
    turnTo(destTheta);
    directTravelTo(overpassStart);
    
    /* Step 2: drive up the overpass slightly */
    destTheta = getDestinationAngle(getCurrentPoint_feet(), overpassEnd);
    turnTo(destTheta);
    Movement.moveStraightFor(0.2);
    
    /* Step 3: drive over the overpass carefully */
    destTheta = getDestinationAngle(overpassStart, overpassEnd);
    turnTo(destTheta);
    directTravelTo(overpassEnd);
    odometer.setX(toMeters(overpassEnd.x));
    odometer.setY(toMeters(overpassEnd.y));
    
    /* step 4: drive slightly forwards, since the robot always arrives slightly short */
    Movement.moveStraightFor(0.25);    
    
  }
  
  /**
   * Checks if there is an overpass between the current waypoint and destination
   * waypoint based on the slope between the current waypoint and destination
   * waypoint, and the slope between the endpoints of the overpass.
   * 
   * @param cur The current waypoint which the robot is at.
   * @param dest The destination waypoint which the robot must travel to.
   * @return boolean indicator for if there's a slope between current and destination point.
   */
  private static boolean checkForOverpass(Point cur, Point dest) {
    double[] overpassSlope = getOverPassSlope();
    double[] waypointSlope = getCurDestSlope(cur,dest);
    boolean m_equal = roughlySame(overpassSlope[0], waypointSlope[0], bigTolerance);
    boolean b_equal = roughlySame(overpassSlope[1], waypointSlope[1], bigTolerance);
    return (m_equal && b_equal);
  }
  
  /**
   * The slope y=mx+b formed by the endpoints of the overpass, since the overpass
   * forms a straight line which can be depicted as a slope with relation
   * to the playing field.
   * @return Parameters m (mb[0]) and b (mb[1]) in the slope y=mx+b in an array of length 2.
   */
  private static double[] getOverPassSlope() {
    Point p1 = overpass.endpointA;
    Point p2 = overpass.endpointB;
    double m = (p2.y - p1.y) / (p2.x - p1.x);
    double b = p1.y - m*p1.x;
    double[] mb = {m,b};
    return mb;
  }
  
  /**
   * The slope y=mx+b formed by the current waypoint the robot is at, and the destination
   * waypoint which the robot must travel to.
   * @return Parameters m (mb[0]) and b (mb[1]) in the slope y=mx+b in an array of length 2.
   */
  private static double[] getCurDestSlope(Point cur, Point dest) {
    double m = (dest.y - cur.y) / (dest.x - cur.x);
    double b = dest.y - m*dest.x;
    double[] mb = {m,b};
    return mb;
  }
  

  /**
   * Using the starting corner to cross the tunnel using a helper method and set the odometer.
   * Divides the field into 4 different starting corner and crosses the tunnel from each corner.
   * @param startingCorner The starting corner of the robot.
   */
  public static void crossingTunnel(int startingCorner) {
    switch (startingCorner) {
      case(0):
        println("Bottom left");
        odometer.setXyt(toMeters(1), toMeters(1), 0);
        odometer.printPosition();
        moveToTunnel(startingCorner);
        if (verticalTunnel) {
          odometer.setXyt(
              toMeters(tunnel.ll.x + 0.5), toMeters(tunnel.ur.y + (TILE_SIZE / 3) + 1), 0);
        } else {
          odometer.setXyt(
              toMeters(tunnel.ur.x + (TILE_SIZE / 3) + 1), toMeters(tunnel.ll.y + 0.5), 90);
        }
        break;
      case(1):
        println("Bottom right");
        odometer.setXyt(toMeters(14), toMeters(1), 270);
        odometer.printPosition();
        moveToTunnel(startingCorner);
        if (verticalTunnel) {
          odometer.setXyt(
              toMeters(tunnel.ll.x + 0.5), toMeters(tunnel.ur.y + (TILE_SIZE / 3) + 1), 0);
        } else {
          odometer.setXyt(
              toMeters(tunnel.ll.x - (TILE_SIZE / 3) - 1), toMeters(tunnel.ll.y + 0.5), 270);
        }
        break;
      case(2):
        println("Top right");
        odometer.setXyt(toMeters(14), toMeters(8), 180);
        odometer.printPosition();
        moveToTunnel(startingCorner);
        if (verticalTunnel) {
          odometer.setXyt(
              toMeters(tunnel.ll.x + 0.5), toMeters(tunnel.ll.y - (TILE_SIZE / 3) - 1), 180);
        } else {
          odometer.setXyt(
              toMeters(tunnel.ll.x - (TILE_SIZE / 3) - 1), toMeters(tunnel.ll.y + 0.5), 270);
        }
        break;
      case(3):
        println("Top left");
        odometer.setXyt(toMeters(1), toMeters(8), 90);
        odometer.printPosition();
        moveToTunnel(startingCorner);
        if (verticalTunnel) {
          odometer.setXyt(
              toMeters(tunnel.ll.x + 0.5), toMeters(tunnel.ll.y - (TILE_SIZE / 3) - 1), 180);
        } else {
          odometer.setXyt(
              toMeters(tunnel.ur.x + (TILE_SIZE / 3) + 1), toMeters(tunnel.ll.y + 0.5), 90);
        }
        break;
      default:
        errPrintln("Error getting starting corner");
    }
  }
  //Problems:
  //World9(Horizontal tunnel Bottom right corner): Top Tunnel
  //World8(Horizontal tunnel Bottom left corner): Top Tunnel

  /**
   * Helps the robot to travel through the tunnel depending on what corner the robot is placed at.
   * The movement depends on the corner and orientation of the tunnel.
   * 
   */
  private static void moveToTunnel(int startingCorner) {
    if (verticalTunnel) {
      /* step 0 : preliminary calculations */
      Point cur = getCurrentPoint_feet();
      double destX = tunnel.ll.x;
      double destY = cur.y;
      Point dest = new Point(destX, destY);
      double destTheta = getDestinationAngle(cur, dest);
      double distance = toMeters(distanceBetween(cur, dest));

      /* step 1 : get to tunnels' x position if not already inline */
      if (!roughlySame(cur.x, destX, smallTolerance)) {
        turnTo(destTheta);
        if (cur.x > destX) {
          Movement.moveStraightFor(distance - (TILE_SIZE / 2));
          if (startingCorner == 2 || startingCorner == 1 || startingCorner == 0) {
            Movement.moveStraightFor(-verticalOffset);
          }
        } else {
          Movement.moveStraightFor(distance);
        }
      }
      println("Done Step 1");
      
      /* step 2 : correct position and move to center of the tunnel */
      cur = getCurrentPoint_feet();
      if (cur.x < tunnel.ll.x) { // check if tunnel is alone x-axis boundary
        turnTo(90);
        LightLocalizer.alignWithLine();
        Movement.moveStraightFor(verticalOffset);
      } else {
        turnTo(270);
        if (startingCorner != 1) {
          Movement.moveStraightFor(verticalOffset);
        }
      }
      println("Done Step 2");
      
      /* step 3 : point towards tunnel and ensure we're point straight through the tunnel */
      turnTo(verticalOrientation);
      LightLocalizer.alignWithLine();
      println("Done Step 3");

      /* step 4 : travel through tunnel while constantly correcting position at each tile */
      cur = getCurrentPoint_feet();
      destX = cur.x;
      destY = (startingCorner == 0 || startingCorner == 1) ? tunnel.ur.y : tunnel.ll.y;
      selfCorrectingPath(destX, destY);
      println("Done Step 4");

      /* step 5 : move for 90% of one additional tile and align with line. */
      Movement.moveStraightFor(TILE_SIZE - TILE_SIZE / 10);
      LightLocalizer.alignWithLine();
      println("Done Step 5");

    } else {
      /* step 0 : preliminary calculations */
      Point cur = getCurrentPoint_feet();
      double destX = cur.x;
      double destY = tunnel.ur.y;
      Point dest = new Point(destX, destY);
      double destTheta = getDestinationAngle(cur, dest);
      double distance = toMeters(distanceBetween(cur, dest));

      /* step 1 : get to tunnels' y position if not already inline */
      if (!roughlySame(cur.y, tunnel.ll.y, smallTolerance)) {
        turnTo(destTheta);
        if (cur.y < destY) {
          Movement.moveStraightFor(distance - TILE_SIZE);
        } else {
          Movement.moveStraightFor(distance);
        }   
      }
      Movement.pause(5);
      println("Done Step 1");      

      /* step 2 : correct position and move to center of the tunnel */
      if (!roughlySame(cur.y, tunnel.ll.y, smallTolerance)) {
        if ((startingCorner == 0 || startingCorner == 1) && cur.y < destY) {
          turnTo(0);
        } else {
          turnTo(180);
        }
        LightLocalizer.alignWithLine();
        Movement.moveStraightFor(verticalOffset);
      } else {
        turnTo(0);
        LightLocalizer.alignWithLine();
        Movement.moveStraightFor(verticalOffset);
      }
      println("Done Step 2");

      /* step 3 : point towards tunnel and ensure we're point straight through the tunnel */
      turnTo(horizontalOrientation);
      LightLocalizer.alignWithLine();
      println("Done Step 3");

      /* step 4 : travel through tunnel while constantly correcting position at each tile */
      cur = getCurrentPoint_feet();
      destX = (startingCorner == 1 || startingCorner == 2) ? tunnel.ll.x : tunnel.ur.x;
      destY = cur.y;
      selfCorrectingPath(destX, destY);
      println("Done Step 4");

      /* step 5 : move for 90% of one additional tile and align with line. */
      Movement.moveStraightFor(TILE_SIZE - TILE_SIZE / 10);
      LightLocalizer.alignWithLine();
      println("Done Step 5");
      
    }
    Movement.moveStraightFor(-TILE_SIZE / 5); // ensure we're behind black line for light localization

  }

  /**
   * Traveling back from the waypoint to the starting point.
   * The robot crosses the tunnel and goes back to the starting point.
   * @param start The starting point of the robot.
   */
  public static void moveBackToStart(Point start, Point wayPoint) {
    travelTo(wayPoint); // drive to waypoint(0)
    
    if (verticalTunnel) {
      /* step 0: localize at waypoint */
      turnTo(0);
      Movement.moveStraightFor(-TILE_SIZE / 4);
      LightLocalizer.localize_waypoint_2();
      odometer.setXyt(toMeters(wayPoint.x), toMeters(wayPoint.y), 270);

      /* step 1: align with x-axis of right of tunnel*/
      Point cur = getCurrentPoint_feet();
      double destX = tunnel.ur.x;
      double destY = tunnel.ll.y - toFeet(TILE_SIZE);
      Point dest = new Point(destX, destY);
      double destTheta = getDestinationAngle(cur, dest);
      double distance = toMeters(distanceBetween(cur, dest));
      turnTo(destTheta);
      Movement.moveStraightFor(distance);
      println("Done step 1. Odometer : ");
      odometer.printPosition();
      
      /* step 2: Correct position and move to x-center of the tunnel*/
      turnTo(270);
      LightLocalizer.alignWithLine();
      Movement.moveStraightFor(verticalOffset);
      println("Done step 2. Odometer : ");
      odometer.printPosition();

      /* step 3: turn to tunnel and approach tunnel */
      turnTo(fVerticalOrientation);
      LightLocalizer.alignWithLine();
      cur = getCurrentPoint_feet();
      destX = cur.x;
      destY = tunnel.ll.y;
      selfCorrectingPath(destX, destY);
      println("Done step 3. Odometer : ");
      odometer.printPosition();

      /* step 4: travel through tunnel */
      cur = getCurrentPoint_feet();
      destX = cur.x;
      destY = tunnel.ur.y;
      selfCorrectingPath(destX, destY);
      println("Done step 4. Odometer : ");
      odometer.printPosition();

      /* step 5 : move for 90% of one additional tile and align with line. */
      Movement.moveStraightFor(TILE_SIZE - TILE_SIZE / 10);
      LightLocalizer.alignWithLine();
      println("Done step 5. Odometer : ");
      odometer.printPosition();

      /* Step 6: Return to starting point */
      cur = getCurrentPoint_feet();
      destTheta = getDestinationAngle(cur, start);
      distance = toMeters(distanceBetween(cur, start));
      turnTo(destTheta);
      Movement.moveStraightFor(distance);
      println("Done step 6. Odometer : ");
      odometer.printPosition();

      
    } else {
      Point cur = getCurrentPoint_feet();
      double destX;
      double destY;
      double distance;

      /* step 1: Align with y-axis of tunnel */
      if (tunnel.ur.y == 9) { // if tunnel is along the top wall of the playground
        turnTo(0);
        destX = cur.x;
        destY = tunnel.ll.y;
        Point dest = new Point(destX, destY);
        LightLocalizer.alignWithLine();
        cur = getCurrentPoint_feet();
        distance = toMeters(distanceBetween(cur, dest));
        Movement.moveStraightFor(distance - (TILE_SIZE / 10));
        LightLocalizer.localize_waypoint_2();
        odometer.setXyt(toMeters(wayPoint.x), toMeters(tunnel.ll.y), 270);
        println("Done step 1. Odometer : ");
        odometer.printPosition();

      } else {
        turnTo(0);
        destX = cur.x;
        destY = tunnel.ur.y;
        Point dest = new Point(destX, destY);
        LightLocalizer.alignWithLine();
        cur = getCurrentPoint_feet();
        double destTheta = getDestinationAngle(cur, dest);
        distance = toMeters(distanceBetween(cur, dest));
        turnTo(destTheta);
        Movement.moveStraightFor(distance - (TILE_SIZE / 10));
        LightLocalizer.localize_waypoint_2();
        odometer.setXyt(toMeters(cur.x), toMeters(tunnel.ur.y), 270);
        println("Done step 1. Odometer : ");
        odometer.printPosition();

      }

      /* step 2: Align with center of the tunnel */
      if (tunnel.ur.y == 9) {
        turnTo(0);
        LightLocalizer.alignWithLine();
        Movement.moveStraightFor(verticalOffset);
      } else {
        turnTo(180);
        LightLocalizer.alignWithLine();
        Movement.moveStraightFor(verticalOffset);
      }
      println("Done step 2. Odometer : ");
      odometer.printPosition();

      /* step 3: Turn to tunnel and assure we're going straight on */
      turnTo(fHorizontalOrientation);
      LightLocalizer.alignWithLine();
      println("Done step 3. Odometer : ");
      odometer.printPosition();

      /* step 4: Drive towards tunnel */
      cur = getCurrentPoint_feet();
      destX = tunnel.ur.x;
      destY = cur.y;
      selfCorrectingPath(destX, destY);
      println("Done step 4. Odometer : ");
      odometer.printPosition();

      /* step 5: drive through the tunnel */
      cur = getCurrentPoint_feet();
      destX = tunnel.ll.x;
      destY = cur.y;
      selfCorrectingPath(destX, destY);
      println("Done step 5. Odometer : ");
      odometer.printPosition();

      /* step 6: move for 90% of one additional tile and align with line. */
      Movement.moveStraightFor(TILE_SIZE - TILE_SIZE / 10);
      LightLocalizer.alignWithLine();
      println("Done step 6. Odometer : ");
      odometer.printPosition();

      /* Step 7: Return to starting point */
      cur = getCurrentPoint_feet();
      double destTheta = getDestinationAngle(cur, start);
      distance = toMeters(distanceBetween(cur, start));
      turnTo(destTheta);
      Movement.moveStraightFor(distance);
      println("Done step 7. Odometer : ");
      odometer.printPosition();

    }
  }

  /**
   * Travel along the x or y axis direction of the playing field while constantly
   * correcting its' path, to make sure that we're traveling directly along the 
   * x or y axis.
   * We do this by traveling 90% of a tile length, then align ourselves with the 
   * black line of the tile perpendicular to our path.
   * 
   * @param destX x-axis of the destination point
   * @param destY y-axis of the destination point
   */
  private static void selfCorrectingPath(double destX, double destY) {
    Point cur = getCurrentPoint_feet();
    Point dest = new Point(destX, destY);
    int nrTiles = (int) Math.round(distanceBetween(cur, dest));
    for (int i = 0; i < nrTiles; i++) {
      Movement.moveStraightFor(TILE_SIZE - TILE_SIZE / 10);
      LightLocalizer.alignWithLine();
    }
  }

  /**
   * Get to the main island by crossing the bridge depending on the starting corner of the robot.
   * @param corner The starting corner of the robot.
   */
  public static void getToIsland(int corner) {
    Point startingPoint = new Point(-1,-1);
    switch (corner) {
      case(0):
        startingPoint = new Point(0.5, 0.5);
        break;
      case(1):
        startingPoint = new Point(14.5, 0.5);
        break;
      case(2):
        startingPoint = new Point(14.5, 8.5);
        break;
      case(3):
        startingPoint = new Point(0.5, 8.5);
        break;
      default:
        errPrintln("Error getting starting corner");
    }
    crossingTunnel(corner);
  }
  

  

  /**
   * Travel to the first point after passing the tunnel.
   * @param destination The first waypoint
   */
  public static void driveToFirstWayPoint(Point destination) {
    Point startPoint = getCurrentPoint_feet();
    double travelDist = toMeters(distanceBetween(startPoint, destination));
    double destTheta = getDestinationAngle(startPoint, destination);
    turnTo(destTheta);
    Movement.moveStraightFor(travelDist);
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
    if (travelDist < smallTolerance) {
      return;
    }

    // case 2: we're facing the right way
    if (angleDiff < 5.0 || angleDiff > 355.0) {
      travelToObstacle(destination);

      // case 3: we have to turn.
    } else {
      turnTo(destTheta);
      travelToObstacle(destination);
    }

    double tolerance = 0.5;
    if ((roughlySame(startPoint.x, destination.x, tolerance)
        || roughlySame(startPoint.y, destination.y, tolerance))
        ) {
      LightLocalizer.localize_waypoint();
      odometer.setX(toMeters(destination.x));
      odometer.setY(toMeters(destination.y));
      //odometer.setTheta(getDestinationAngle(startPoint, destination));
    }
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
  }

  /**
   * Moves forward and when it detects an obstacle calls avoid method from AvoidObstalce Class.
   * When it passes the obstacle or it is close to destination calls directTravelTo.
   * @param destination
   */
  private static void travelToObstacle(Point destination) {
    int noiseTolerance = 2;
    Movement.setMotorSpeeds(200);
    while (true) {
      Movement.drive();
      Point cur = getCurrentPoint_feet();
      if (comparePoints(cur, destination, smallTolerance)) {
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
  public static boolean roughlySame(double a, double b, double tolerance) {
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
