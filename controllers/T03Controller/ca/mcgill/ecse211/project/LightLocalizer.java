package ca.mcgill.ecse211.project;

import static ca.mcgill.ecse211.project.Movement.*;
import static ca.mcgill.ecse211.project.Resources.*;

import simlejos.ExecutionController;
import simlejos.robotics.SampleProvider;


/**
 * The light localizer.
 * This class implements the light localizer routine.
 * It uses two color sensors to first detect the horizontal line.
 * Then it turns toward the desired point and when it reaches there
 * it will turn toward 0 degree and stays there.
 * @author Narry Zendehrooh
 * @author Sean Smith
 */
public class LightLocalizer {
  
  /* Threshold value to determine if a black line is detected or not */
  private static final int THRESHOLD = 60;
 
  /** Buffer (array) to store US1 samples. */
  private static float[] sensor1_data = new float[leftColorSensor.sampleSize()];
  
  /** Buffer (array) to store US2 samples. */
  private static float[] sensor2_data = new float[rightColorSensor.sampleSize()];
  
  /** Values to operate color sensor. */
  private static int current_color_blue = 1000;
  private static int current_color_red = 1000;
  private static int current_color = 1000;
  private static boolean s1Indicator = false;
  private static boolean s2Indicator = false;

  
  /** This is the default constructor of this class. It cannot be accessed externally. */
  private LightLocalizer() {}
  
  /**
   * Localizes the robot to (x, y, theta) in two steps. e.g. (1, 1, 0)
   * Localizing in two steps by meeting horizontal and vertical black line of the tile.
   * Then it sets the odometer to (x,y,theta) for our world frame of reference.
   */
  public static void localize_start() {
    stepOne_start();  // Moving towards (y=1) black line.
    stepTwo_start();  // Moving toward (1,1)
  }
  
  
  
  /**
   * A modified version of localization using at waypoints.
   * The final position and orientation of the robot is changed from the original version. 
   */
  public static void localize_waypoint() {
    stepOne_waypoint();
    stepTwo_waypoint();
  }
  
  
  /**
   * Localizes the robot to the black line which intersects (1,1).
   * Does so by moving forwards until both sensors detect a black line.
   * When a sensor on the left or right detects a black line, the motor on the 
   * same side will stop, and allow the other motor to detect the black line.
   * The resulting position of the robot will be a robot which is perpendicular
   * to the black line which intersects (1,1).
   */
  private static void stepOne_start() {
    double backwardAdjustment = -0.0273;
    alignWithLine();
    moveStraightFor(backwardAdjustment);
    turnBy(90.0);
  }
  
  /**
   * Localizes the robot to (1,1,0)
   * Does so in the same way as step 1,
   * however it moves straight for a greater amount of time so as
   * to place the robot at (1,1,0) with reference to the odometer.
   */
  private static void stepTwo_start() {
    double backwardAdjustment = -0.0273 * 3.5;
    alignWithLine();
    moveStraightFor(backwardAdjustment);
    turnBy(-90.0);
  }
  
  /**
   * A modified version of stepOne using at waypoints.
   * Modified the position of the robot
   */
  private static void stepOne_waypoint() {
    double backwardAdjustment = -0.0273 * 3.5;
    alignWithLine();
    moveStraightFor(backwardAdjustment);
    turnBy(90.0);
  }
  
  /**
   * A modified version of stepTwo.
   * The robot will not turn in the modified version.
   */
  private static void stepTwo_waypoint() {
    double backwardAdjustment = -0.0273 * 3.5;
    alignWithLine();
    moveStraightFor(backwardAdjustment);
  }
  
  /**
   * align the robot with the line at (1,1).
   */
  public static void alignWithLine() {
    
    while (s1Indicator == false || s2Indicator == false) {   
      leftMotor.setSpeed(FORWARD_SPEED);
      rightMotor.setSpeed(FORWARD_SPEED);
      
      if(s1Indicator==false) {
        rightMotor.forward();      
      }
      if(s2Indicator==false) {
        leftMotor.forward();
      }

      //When it reaches (1,1) with sensor1 first
      if (blackLineTrigger(leftColorSensor, sensor1_data) && s1Indicator==false) {
        rightMotor.stop();
        s1Indicator = true;
        Movement.pause(0.1);
      }
      
      //When it reaches (1,1) with sensor2 first
      if (blackLineTrigger(rightColorSensor, sensor2_data) && s2Indicator==false) {
        leftMotor.stop();
        s2Indicator = true;
        Movement.pause(0.1);

      }
      
    }
    
    // reset the indicators
    s1Indicator = false;
    s2Indicator = false;
  }
  
  
  
  /**
   * The method fetches data recorded by the color sensors in RedMode 
   * and compares the most recent value to verify if the
   * robot has traveled over a black line.
   * Method makes use of a fixed threshold value which may not be reliable in
   * certain conditions, however it has been tested and conditioned to minimize false negatives.
   * @param colorSensor the color sensor
   * @param sensor the data recorded by the color sensor
   * @return true if black line is detected by both sensors.
   */
  public static boolean blackLineTrigger(SampleProvider colorSensor, float[] sensor) {
    int warmUpNoise = 5; // colour sensor will fetch several samples to "warmup" the sensor.
    for(int i=0; i<warmUpNoise; i++) {
      colorSensor.fetchSample(sensor, 0);
    }
    current_color = (int) (sensor[0]);
    return (current_color < THRESHOLD) ? true : false;
  }
  
  /**
   * drives until black line is detected by sensor one.
   */
  public static void continueUntilLine() {
    leftMotor.setSpeed(FORWARD_SPEED);
    rightMotor.setSpeed(FORWARD_SPEED);
    while (true) {
      leftMotor.forward();
      rightMotor.forward();
      if (blackLineTrigger(leftColorSensor, sensor1_data)) {
        rightMotor.stop();
        leftMotor.stop();
        break;
      }
    }
  }
  
  /**
   * stops the robot and pauses.
   */
  public static void pause() {
    System.out.println("Pause");
    leftMotor.setSpeed(0);
    rightMotor.setSpeed(0);
    
    for (int i = 0; i < 3000; i++) {
      ExecutionController.waitUntilNextStep();
    }
    
    leftMotor.setSpeed(FORWARD_SPEED);
    rightMotor.setSpeed(FORWARD_SPEED);
  }
  

}