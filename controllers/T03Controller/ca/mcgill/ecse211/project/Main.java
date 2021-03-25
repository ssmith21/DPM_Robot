package ca.mcgill.ecse211.project;

import static ca.mcgill.ecse211.project.Movement.*;
import static ca.mcgill.ecse211.project.Resources.*;

import ca.mcgill.ecse211.playingfield.Point;
import java.lang.Thread;
import simlejos.ExecutionController;

/**
 * Main class of the program.
 *
 * TODO Describe your project overview in detail here (in this Javadoc comment).
 */
public class Main {

  /** Main entry point. */
  public static void main(String[] args) {
    ExecutionController.performPhysicsSteps(INITIAL_NUMBER_OF_PHYSICS_STEPS);
    ExecutionController.performPhysicsStepsInBackground(PHYSICS_STEP_PERIOD);

    // Start the odometer thread and update the number of threads
    new Thread(odometer).start();
    ExecutionController.setNumberOfParties(NUMBER_OF_THREADS);
    
    Point startingPoint = new Point(0, 0);
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
    
    beep(3);
    UltrasonicLocalizer.localize();
    LightLocalizer.localize_start();
    Navigation.crossingTunnel(corner);
    println("Done crossing tunnel");
    odometer.printPosition();
    Navigation.driveToFirstWayPoint(waypoint(0));
    odometer.printPositionInTileLengths();
    beep(3);
    Navigation.moveBackToStart(startingPoint, waypoint(0));

    
    System.exit(0);
  }

  
  /**
   * Example using WifiConnection to communicate with a server and receive data concerning the
   * competition such as the starting corner the robot is placed in.<br>
   * 
   * <p>Keep in mind that this class is an <b>example</b> of how to use the Wi-Fi code; you must use
   * the WifiConnection class yourself in your own code as appropriate. In this example, we simply
   * show how to get and process different types of data.<br>
   * 
   * <p>There are two variables you MUST set manually (in Resources.java) before using this code:
   * 
   * <ol>
   * <li>SERVER_IP: The IP address of the computer running the server application. This will be your
   * own laptop, until the beta beta demo or competition where this is the TA or professor's laptop.
   * In that case, set the IP to the default (indicated in Resources).</li>
   * <li>TEAM_NUMBER: your project team number.</li>
   * </ol>
   * 
   * <p>Note: You can disable printing from the Wi-Fi code via ENABLE_DEBUG_WIFI_PRINT.
   * 
   */
  public static void wifiExample() {
    // Note that we are using the Resources.println() method, not System.out.println(), to ensure
    // the team number is always printed
    println("Running...");

    // Example 1: Print out all received data
    println("Map:\n" + wifiParameters);

    // Example 2: Print out specific values
    println("Red Team: " + redTeam);
    println("Green Zone: " + green);
    println("Island Zone, upper right: " + island.ur);
    println("Red tunnel footprint, lower left y value: " + tnr.ll.y);
    println("All waypoints: " + waypoints);

    // Example 3: Compare value (simplified example)
    if (overpass.endpointA.x >= island.ll.x && overpass.endpointA.y >= island.ll.y) {
      println("Overpass endpoint A is on the island.");
    } else {
      errPrintln("Overpass endpoint A is in the water!"); // prints to stderr (shown in red)
    }
    
    // Example 4: Calculate the distance between two waypoints
    println("Distance between waypoints 3 and 5:",
        Navigation.distanceBetween(waypoint(3), waypoint(5)));

    // Example 5: Calculate the area of a region
    println("The island area is " + island.getWidth() * island.getHeight() + ".");
  }
  

}
