package ca.mcgill.ecse211.project;

import static ca.mcgill.ecse211.project.Resources.*;

import ca.mcgill.ecse211.playingfield.Point;
import java.util.Arrays;
import simlejos.ExecutionController;

/**
 * The AvoidObstacle helps the robot to avoid the obstacle using ultrasonic and wall follower.
 * @author Narry Zendehrooh
 * @author Sean Smith
 *
 */
public class AvoidObstacle {
  
  /** The left and right motor speeds, respectively. */
  private static int[] motorSpeeds = new int[2];
  /** The difference between the actual and ideal distance of robot from the wall. */
  private static int distance_error;
  /** The distance remembered by the filter() method. */
  private static int prevDistance;
  /** The actual distance from obstacle. */
  private static int distance;
  /** The number of invalid samples seen by filter() so far. */
  private static int invalidSampleCount;
  /** Buffer (array) to store US samples. */
  private static float[] usData = new float[usSensor.sampleSize()];
  /** A flag to check if the robot joined the slope again. */
  private static boolean notReturningFlag = true;
  /** An arbitrary value for difference margin. */
  private static final double EPSILON = 0.012;
  /** A flag deciding turning right or left. */
  private static boolean turningRight = false;
  /** Total rotation of US motor. */
  private static int totalMotorRotation = 0;
  
  /** This is the default constructor of this class. It cannot be accessed externally. */
  private AvoidObstacle() {}
  
  /**
   * The starting point of obstacle avoidance.
   * When detects the obstacle stops and applies wall follower
   * until it joins back the path.
   * @param startPoint starting point right before the obstacle
   * @param endPoint the destination point of the waypoint
   */
  public static void avoid(Point startPoint, Point endPoint) {
    Movement.stopMotors();
    double initialAngle = odometer.getXyt()[2];
    int decision = decideLeftRight();
    
    if (decision == LEFT) {
      turningRight = false;
    } else {
      turningRight = true;
    }
    
    double[] params = getLinearSlope(startPoint, endPoint);
    
    wallFollow(params, startPoint, endPoint);
    Navigation.turnTo(initialAngle);
    
    notReturningFlag = true;
  }
  
  /**
   * Decides whether the robot should go left or right.
   * uses the distance from left and right wall and chooses
   * the direction which is further from the wall.
   * @return decision going left or right
   */
  public static int decideLeftRight() {
    turnUsMotor(-90);
    int distL = readUsDistance();
    
    turnUsMotor(180);
    int distR = readUsDistance();
    
    int decision;
    if (distL > distR) {
      System.out.println("Safer to turn left");
      decision = LEFT;
    } else {
      System.out.println("Safer to turn right");
      decision = RIGHT;
    }
    
    turnUsMotor(-90);
    return decision;
  }
  
  /**
   * A modified version of wall follower from lab 1 
   * which terminates when the robot passed the obstacle.
   * The robot follows the wall until it joins back to the waypoint path.
   * @param slopeParams the slope and y-intercept of the path
   * @param startPoint the point before starting the obstacle avoidance
   */
  public static void wallFollow(double[] slopeParams, Point startPoint, Point endPoint) {
    System.out.println("m = " + slopeParams[0] + "\tb = " + slopeParams[1]);
    while (true) {
      controller(readUsDistance(), motorSpeeds);
      setMotorSpeeds();
      Movement.drive();
      Point curPoint = Navigation.getCurrentPoint_feet();
      if (checkIfPointOnSlope(startPoint, curPoint, slopeParams)) {
        Movement.stopMotors();
        usReturnToDefault();
        break;
      }
    }
  }
  
  /**
   * Checking if the robot joined back the path using equation of the line.
   * @param start starting point before going off the path
   * @param curr the current position of the robot
   * @return true if it is back to the path
   */
  public static boolean checkIfPointOnSlope(Point start, Point curr, double[] params) {
    // y = mx + b;
    double tolerance = 0.09;
    double m = params[0];
    
    // if slope is 0, just compare x or y, otherwise continuously
    // recalculate current slope to see if we've rejoined the path
    if (m == 0) {
      double xdiff = Math.abs(start.x - curr.x);
      double ydiff = Math.abs(start.y - curr.y);
      boolean dist = distIndicator(start, curr);
      return ((xdiff < EPSILON && !notReturningFlag && dist)
          || (ydiff < EPSILON && !notReturningFlag && dist));
    } else {
      double curX = curr.x;
      double curY = curr.y;
      double newM = (curY - start.y) / (curX - start.x);
      return (compareRoughly(newM, m, tolerance));
    }
  }



  /**
   * Measure the linear slope between two points.
   * @param p1 first point
   * @param p2 second point
   * @return params has the slope and y-intercept
   */
  public static double[] getLinearSlope(Point p1, Point p2) {
    double m;
    double tolerance = 0.7;
    if (compareRoughly(p1.x, p2.x, 0.7) || compareRoughly(p1.y, p2.y, tolerance)) {
      m = 0;
    } else {
      m = (p2.y - p1.y) / (p2.x - p1.x);
    }
    double b = p1.y - m * (p2.x);
    double[] params = {m, b};
    return params;
  }
  
  /**
   * Compares if two numbers are the same with a given margin.
   * @param a first number
   * @param b second number
   * @param margin the tolerated difference
   * @return true if they are close enough
   */
  private static boolean compareRoughly(double a, double b, double margin) {
    double diff = Math.abs(a - b);
    return (diff < margin); // if they're close enough return true
  }
  
  /**
   * Detects if there is enough distance between two points.
   * @param start first point
   * @param curr second point
   * @return true if distance is more than 0.5
   */
  private static boolean distIndicator(Point start, Point curr) {
    double threshold = 1.5;
    double distance = Navigation.distanceBetween(start, curr);
    if (distance > threshold) {
      return true;
    }
    return false;
  }

  /**
   * Sets the speeds of the left and right motors from the motorSpeeds array.
   */
  public static void setMotorSpeeds() {
    leftMotor.setSpeed(motorSpeeds[LEFT]);
    rightMotor.setSpeed(motorSpeeds[RIGHT]);
  }

  
  /**
   * Process a movement based on the US distance passed in Bang-Bang.
   *
   * @param distance the distance to the wall in cm
   * @param motorSpeeds output parameter you need to set
   */
  public static void controller(int distance, int[] motorSpeeds) {
    int leftSpeed = MOTOR_HIGH;
    int rightSpeed = MOTOR_HIGH;
    
    correctController();
    
    distance_error = turningRight ? WALL_DIST_RIGHT - distance : WALL_DIST_LEFT - distance;

    //When the sensor detects no obstacles the robot goes straight
    if (Math.abs(distance_error) <= WALL_DIST_ERR_THRESH) {
      leftSpeed = MOTOR_HIGH;
      rightSpeed = MOTOR_HIGH;
      //When the sensor detects the robot is getting too close to the wall it goes away from wall
    } else if (distance_error > 0) {
      rightSpeed = turningRight ? MOTOR_LOW :  MOTOR_HIGH;
      leftSpeed = turningRight ? MOTOR_HIGH : MOTOR_LOW;
      //When the sensor detects the robot is getting too far from the wall it goes towards the wall
    } else if (distance_error < 0) {
      rightSpeed = turningRight ? MOTOR_HIGH : MOTOR_LOW;
      leftSpeed = turningRight ? MOTOR_LOW : MOTOR_HIGH;
    }
    //Sets the speed of left and right motors
    motorSpeeds[LEFT] = leftSpeed;
    motorSpeeds[RIGHT] = rightSpeed;
  }
  
  /**
   * Correcting the controller depending on turning left or right.
   * The position and orientation of the robot will be adjusted depending on
   * turning left or right.
   */
  public static void correctController() {
    
    // value determined by trial and error.
    double initialMove = 0.06475;
    if (distance > 10 && notReturningFlag) {
            
      int motorRotate = 45; 
      int robotRotate = -70; 
      
      if (turningRight) {
        motorRotate = -45; 
        robotRotate = 70;
      }
      
      leftMotor.stop();
      rightMotor.stop();
      turnUsMotor(motorRotate);
      
      Movement.turnBy(robotRotate);
      Movement.moveStraightFor(initialMove);
      turnUsMotor(motorRotate);
      
      notReturningFlag = false;
    }
  }
  
  /**
   * turning the ultrasonic motor by the passed amount in degree.
   * @param amount the amount by which to turn the motor.
   */
  public static void turnUsMotor(int amount) {
    usMotor.setSpeed(100);
    usMotor.rotate(amount, false);
    totalMotorRotation += amount; // update global amount to keep track.
    usMotor.stop();
  }
  
  /**
   * return motor to be facing forwards.
   */
  public static void usReturnToDefault() {
    turnUsMotor(-totalMotorRotation);
  }
  

  /**
   *  Returns the filtered distance between the US sensor and an obstacle in cm.
   */
  public static int readUsDistance() {
    int[] filterArr = new int[21]; 
    for (int i = 0; i < filterArr.length; i++) {
      usSensor.fetchSample(usData, 0);
      filterArr[i] = (int) (usData[0] * 100); 
      ExecutionController.sleepFor(60);
    }
    return filter(filterArr);
  }
  

  /**
   * Rudimentary filter - toss out invalid samples corresponding to null signal.
   *
   * @param arr raw distance array measured by the sensor in cm
   * @return distance filtered distance in cm
   */
  public static int filter(int[] arr) {
    Arrays.sort(arr);
    distance = arr[10];
    if (distance >= MAX_SENSOR_DIST && invalidSampleCount < INVALID_SAMPLE_LIMIT) {
      invalidSampleCount++;
      return prevDistance;
    } else {
      if (distance < MAX_SENSOR_DIST) {
        invalidSampleCount = 0;
      }
      prevDistance = distance;
      return distance;
    }
  }
}
