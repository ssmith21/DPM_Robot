# ECSE211 Project: Team 03 - DPM-IS-E-Z-P-Z
Welcome to group 3's repo!
![](https://github.com/mcgill-ecse211-w21/project-ecse211_t03/blob/main/Poster.png)

## Project Description
The goal of this project is to design and construct a machine that can autonomously navigate to a race track on an island and complete as many laps as possible within a 5 minute period, eventually returning to its starting point.

## Meet the Team
### Individual efforts
| Name | Role | Spent Hours |
| ---- | ---- | --------------: |
| Sean Smith | Software Lead | 66 |
| Narry Zendehrooh | Testing Lead | 68 |
| Marckly Paul | Hardware Lead | 62 |
| Nikhil Jabesh Moses | Documentation Manager | 58 |
| Atef Halwai | Project Manager | 64 |

## Purpose
The robot will start at one of the four corners of the map. It will then have to localize itself in order to update the odometer with its current position in the map. This part of the project should not be challenging since we already implemented the ultrasonic localization in lab 3. Once the odometer will have the correct coordinates, it will be able to reach the waypoints  given at the beginning of the demonstration. To reach the waypoints the odometer class will be crucial, it will tell the robot its displacement in the x and y coordinates and to which direction its heading. By following the waypoints, the robot will have to make as many laps as possible in the track of the map, and then come back to where it started before reaching the time limit. Through this process, it will have to avoid colliding the other competing robot and the obstacles. We will use an ultrasonic sensor to detect potential obstacles.

## Budget
To approach the budget, a proper evaluation of ideas was first needed, and envisioning what our hardware, software, testing and documentation approached might look like. From there, a detailed Gantt Chart was created, assigning each person based on the role they hold, to the task that they are suited to do. Hence each task is attributed to resources, which contains the personnel that are meant to take care of the task, adding the time required for the task to complete as well as the unit that each person would hold in hours. Upon doing so for each task on the gantt chart, an accumulated number of hours of each group member is made available through the resources chart, from which the budget can be seen. In addition, through the spreadsheet which represents the actual number of hours the team has put in in comparison to that of the envisioned gantt chart, one will be able to, at the end, to check the actual budget. As of this current version, the average number of hours contributed to the project relative to the gantt chart is 67.8 hours.

## Software Design
### Main
It is the entry point of the project where we set the threads and call different methods in different classes such as the TravelTo method in the Navigation class and the Localize method in the UltrasonicLocalizer and LightLocalizer classes.
This class first starts with setting the number of physical steps and step period. Then it runs the odometer as a thread and followed by Wifi class to retrieve the waypoints. Then it calls the UltrasonicLocalizer and LightLocalizer to do the localization at the beginning and sets the odometer. Finally it passes the waypoints to the Navigation class. After this process, it will terminate the program.
### Wifi
This is the class which is responsible for retrieving the waypoints from the server. The server is a jar application run from the "./server" folder. The command to run the server is <code>java -jar ev3wifiserver.jar</code>The robot will complete laps around a circuit based on the waypoints as input from the wifi class. This class also allows the robot to localize itself at the start in order to allow the robot to have its bearings. The parameters are passed to navigation. It was originally intended that all WiFi parameter handling would be done in a separate class due to the teams’ understanding of the requirements documents, however it would be too much refactoring to change the current implementation, and the resulting code would not have a tangible difference in readability or compartmentalization.
### Odometer
This class tracks the position and orientation of the robot. The position and orientation of the robot will be set and updated constantly by the navigation class. The values for the odometers’ x, y and theta position are corrected at every waypoint where the robot travels roughly along the x or y-axis of the playing field. The odometer runs in parallel on a separate thread to the main thread. 
### Resources
This class keeps the constant parameters that are being used throughout different classes.
### Movement
This class is responsible for movement of the robot which has methods for moving forward and rotation of the robot. This is a separate helper class since many classes require access to the movement logic of the robot, including ObstacleAvoidance, Navigation, LightLocalization and UltrasonicLocalization. The values for the Movement class variables are in the Resources class.
### Obstacle Avoidance
This class uses the ultrasonic sensor to do the wall follower using Bang-Bang method to avoid the obstacle and go back to the path.
We enter this class when the obstacle is detected in the Navigation class. Then this class will stop the robot and will check both the side (right and left) to decide which side to go. Then it will record the line equation y=mx+b of the path between the current position and the destination waypoint. And finally it will execute the wall follower so that it will go around the obstacle until it joins back the line again. It does so by constantly checking if the robot’s current (x,y) position as shown by the odometer is on the slope y=mx+b. Once the current (x,y) position is on the slope, it will exit the ObstacleAvoidance class, reorient itself towards the destination waypoint, then drive to the point using the Navigation class.
### Navigation
This class is being used to navigate the robot between different waypoints. It will use the AvoidObstacle class to pass the obstacles.
The destination points will be passed to the TravelTo method. This method can calculate the minimal angle to turn toward the destination. It also would use the Movement class to move the robot toward the destination. Moreover, it uses the LightLocalizer class to localize the robot at each waypoint. This method helps the robot to cross the tunnel and the bridge. The bulk of the robots' functionality is called from the Navigation class, including obstacle avoidance which is called when the robot detects an obstacle while navigating.
### Light Localization
This class uses the two color sensors to detect the black line of the tales and does the localization at the beginning and at the each waypoint. This class will be called by the Main class at the beginning and by the Navigation class at every waypoint. Once the robot arrives at the waypoint having travelled along the x or y-axis, it uses the colour sensors to align itself with one of the black lines of the tiles of the squares such that it faces 0, 90, 180 or 270 degrees theta. It then reverses and turns 90 degrees towards the waypoint and drives straight, aligning itself with the line perpendicular to the first line it aligned with. Once this is complete, it turns back 90 degrees the other way, then drives forwards such that the robot is placed directly over the waypoint. This class is extremely useful for localizing at waypoints, and driving accurately in a straight line, using the black lines to correct its' path to ensure that the robot is travelling directly along the x or y-axis.
### Ultrasonic Localization
This class is being used at the beginning to localize the robot using the corner walls.
The Ultrasonic Localizer class finds the two angles required in order for the robot to adjust itself in the environment. Two methods were designed based on the facing of the robot. When the robot was facing the walls the risingEdge method would be called. Otherwise, the fallingEdge method would be called. In both cases, the robot turned clockwise and counterclockwise to detect the angles. The sampling method was taking 20 samples, and passing them into a filtering process by using the mean value of those samples, as it represents the majority samples rejecting outliers, reducing errors.
## Hardware Design
The hardware components of the design consist of  one EV3 brick, two wheels, a set of legos that constitute the main assembly, two motors for the wheel, two color sensors, one ultrasonic sensor, and one motor for the ultrasonic sensor. The main assembly is the frame that will support all the components of the robot.
The ultrasonic and the colors sensors will allow the robot to perform ultrasonic and light localization. One limitation that we have to deal with using the ultrasonic sensor is that its range is limited and sometimes it is not able to detect when an obstacle is at the side of the robot. Moreover, the speed at which the ultrasonic sensor takes measure of the forward distance limits what else we can do with the brick since we have to  wait for the sound wave to come back and make a decision. For the color sensor we have to take samples at a precise frequency so that we do not miss when the robot is crossing a black line.


