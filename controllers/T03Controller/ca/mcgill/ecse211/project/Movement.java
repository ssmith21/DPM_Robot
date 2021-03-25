package ca.mcgill.ecse211.project;

import static ca.mcgill.ecse211.project.Resources.BASE_WIDTH;
import static ca.mcgill.ecse211.project.Resources.FORWARD_SPEED;
import static ca.mcgill.ecse211.project.Resources.INVALID_SAMPLE_LIMIT;
import static ca.mcgill.ecse211.project.Resources.MAX_SENSOR_DIST;
import static ca.mcgill.ecse211.project.Resources.ROTATE_SPEED;
import static ca.mcgill.ecse211.project.Resources.WHEEL_RAD;
import static ca.mcgill.ecse211.project.Resources.leftMotor;
import static ca.mcgill.ecse211.project.Resources.rightMotor;
import static ca.mcgill.ecse211.project.Resources.usMotor;
import static ca.mcgill.ecse211.project.Resources.usSensor;
import java.util.Arrays;
import simlejos.ExecutionController;
import simlejos.hardware.ev3.LocalEV3;

/**
 * Movement class covers the robot the robot movement and rotation.
 * @author Narry Zendehrooh
 * @author Sean Smith
 *
 */
public class Movement {
  
  /** This is the default constructor of this class. It cannot be accessed externally. */
  private Movement() {}
  
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
   * Moves the robot straight for the given distance.
   *
   * @param distance in feet (tile sizes), may be negative
   */
  public static void moveStraightFor(double distance) {
    //Set motor speeds and rotate them by the given distance.
    // This method will not return until the robot has finished moving.
    leftMotor.setSpeed(FORWARD_SPEED);
    rightMotor.setSpeed(FORWARD_SPEED);
    leftMotor.rotate(convertDistance(distance), true);
    rightMotor.rotate(convertDistance(distance), false);
  } 
  
  /**
   * Turns the robot by a specified angle. Note that this method is different from
   * {@code Navigation.turnTo()}. For example, if the robot is facing 90 degrees, calling
   * {@code turnBy(90)} will make the robot turn to 180 degrees, but calling
   * {@code Navigation.turnTo(90)} should do nothing (since the robot is already at 90 degrees).
   *
   * @param angle the angle by which to turn, in degrees
   */
  public static void turnBy(double angle) {
    //Similar to moveStraightFor(), but with a minus sign
    leftMotor.setSpeed(ROTATE_SPEED);
    rightMotor.setSpeed(ROTATE_SPEED);
    leftMotor.rotate(convertAngle(angle), true);
    rightMotor.rotate(-(convertAngle(angle)), false);
  }
  
  
  /**
   * Converts input angle to the total rotation of each wheel needed to rotate the robot by that
   * angle.
   *
   * @param angle the input angle in degrees
   * @return the wheel rotations necessary to rotate the robot by the angle in degrees
   */
  public static int convertAngle(double angle) {
    // Using convertDistance method to calculate constant rotation of the robot + scaling
    return convertDistance((Math.PI * BASE_WIDTH * angle / 360.0) * 100) / 100;
  }
  
  
  /**
   * sets the speed of the motors.
   * @param speed speed amount
   */
  public static void setMotorSpeeds(int speed) {
    leftMotor.setSpeed(speed);
    rightMotor.setSpeed(speed);
  }
  
  /**
   * Stops the motors.
   */
  public static void stopMotors() {
    leftMotor.stop();
    rightMotor.stop();
  }
  
  /**
   * Moves the robot forward.
   */
  public static void drive() { 
    leftMotor.forward();
    rightMotor.forward();
  }

  /**
   * This method control robot beep n times.
   * @param n number of beeps
   */
  public static void beep(int n) {
    for (int i = 0; i < n; i++) {
      LocalEV3.getAudio().beep();
      pause(1);
    }
  }
  
  /**
   * Halts the robot for a while to allow pausing the simulation to evaluate ultrasonic
   * localization.
   */
  /**
   * Pause the robot for a certain amount of time.
   * @param time : the amount of time we want to pause for, this value is roughly equal to 1 second.
   */
  public static void pause(double time) {
    leftMotor.setSpeed(0);
    rightMotor.setSpeed(0);
    for(int i=0; i<time*1000; i++) {
      ExecutionController.waitUntilNextStep();
    }
    leftMotor.setSpeed(FORWARD_SPEED);
    rightMotor.setSpeed(FORWARD_SPEED);
  }
  
}
