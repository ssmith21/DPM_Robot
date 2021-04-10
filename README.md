# ECSE211 Project: Team 03 - DPM-IS-E-Z-P-Z
Welcome to group 3's repo!

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
The robot will start at one of the four corners of the map. It will then have to localize itself in order to update the odometer with its current position in the map. This part of the project should not be challenging since we already implemented the ultrasonic localization in lab 3. Once the odometer will have the correct coordinates, it will be able to reach the waypoints  given at the beginning of the demonstration. To reach the waypoints the odometer class will be crucial, it will tell the robot its displacement in the x and y coordinates and to which direction its heading. By following the waypoints, the robot will have to make as many laps as possible in the track of the map, and then come back to where it started before reaching the time limit. Through this process, it will have to avoid colliding the other competing robot and the obstacles. We will use an ultrasonic sensor to detect potential obstacles. Additionally, the robot needs to pass the tunnel and the bridge using Navigation which still needs to be implemented in the future weeks.

## Budget
To approach the budget, a proper evaluation of ideas was first needed, and envisioning what our hardware, software, testing and documentation approached might look like. From there, a detailed Gantt Chart was created, assigning each person based on the role they hold, to the task that they are suited to do. Hence each task is attributed to resources, which contains the personnel that are meant to take care of the task, adding the time required for the task to complete as well as the unit that each person would hold in hours. Upon doing so for each task on the gantt chart, an accumulated number of hours of each group member is made available through the resources chart, from which the budget can be seen. In addition, through the spreadsheet which represents the actual number of hours the team has put in in comparison to that of the envisioned gantt chart, one will be able to, at the end, to check the actual budget. As of this current version, the average number of hours contributed to the project relative to the gantt chart is 67.8 hours.

## Software Requirements
### Parameter Initialization
The robot will have to receive the competition parameters via the Wi-Fi, which is made available to us. Once the parameters have been successfully transmitted, the robot will read the data received in order to visualize the competition platform, information such as tunnel positions and island location would be registered.
### Localization
The robot would have to localize and orient itself as soon as the competition begins, this has to be done after receiving the competition parameters via WiFi server. The robot will begin in one of the four corners in the platform, either top left, top right, bottom left, bottom right. The robot will direct itself in the direction appropriate to the Corner in which it started, in order to begin its navigation phase through the competition field. 
During the course of the competition, the robot is to localize to the various waypoints in order to correct the odometer. Since odometer is prone to error accumulation, to minimize this error, as the robot passes the tunnel, it will need to localize after exiting the tunnel, to ensure no errors will accumulate from that critical point.
### Navigation
During this phase the robot is to navigate through the tunnel, pass through the tunnel, and reach the island. This is achieved through a specific set of waypoints, passed by the WIFI server, in which the robot will have to drive towards them. The robot is to complete as many laps as possible under 5 minutes, from start to finish. After completing as many laps as possible and ensuring that there is indeed enough time to get back to the starting position, the robot will have to traverse back to the starting position.
### Obstacle Avoidance
The robot must be able to avoid any obstacles keyed upon its track, that includes the opponent’s team robot. This robot must be able to securely and autonomously avoid the obstacle. 
### Return To Starting Point
The robot must be able to pass the tunnel again and return to its starting corner.


