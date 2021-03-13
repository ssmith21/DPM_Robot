package ca.mcgill.ecse211.project;

import static ca.mcgill.ecse211.project.Resources.*;
import static java.lang.Math.*;

import ca.mcgill.ecse211.playingfield.Point;

/**
 * The Navigation class is used to make the robot navigate around the playing field.
 */
public class Navigation {
  
  /** Do not instantiate this class. */
  private Navigation() {}
  
  /**
   * Travels to destination taking obstacles into account.
   *
   * <p>TODO Describe your algorithm here in detail (then remove these instructions).
   */
  public static void travelTo(Point destination) {
    // TODO Complete based on Lab 5
  }
  
  /** Travels directly (in a straight line) to the given destination. */
  public static void directTravelTo(Point destination) {
    // TODO Complete based on Lab 5
  }
  
  /**
   * Turns the robot with a minimal angle towards the given input angle in degrees, no matter what
   * its current orientation is. This method is different from {@code turnBy()}.
   */
  public static void turnTo(double angle) {
    // TODO Complete based on Lab 5
  }

  /**
   * Returns the angle that the robot should point towards to face the destination in degrees.
   * This function does not depend on the robot's current theta.
   */
  public static double getDestinationAngle(Point current, Point destination) {
    return 0; // TODO
  }
  
  /** Returns the signed minimal angle in degrees from initial angle to destination angle (deg). */
  public static double minimalAngle(double initialAngle, double destAngle) {
    return 0; // TODO
  }
  
  /** Returns the distance between the two points in tile lengths (feet). */
  public static double distanceBetween(Point p1, Point p2) {
    return 0; // TODO
  }
  
  // TODO Bring Navigation-related helper methods from Labs 2, 3, and 5 here
  // You can also add other helper methods here, but remember to document them with Javadoc (/**)!
  
  /**
   * Converts input distance to the total rotation of each wheel needed to cover that distance.
   * 
   * @param distance the input distance in meters
   * @return the wheel rotations necessary to cover the distance in degrees
   */
  public static int convertDistance(double distance) {
    return 0; // TODO Complete based on Lab 2
  }

  /**
   * Converts input angle to total rotation of each wheel needed to rotate robot by that angle.
   * 
   * @param angle the input angle in degrees
   * @return the wheel rotations (in degrees) necessary to rotate the robot by the angle
   */
  public static int convertAngle(double angle) {
    return 0;  // TODO Complete based on Lab 2
  }

}
