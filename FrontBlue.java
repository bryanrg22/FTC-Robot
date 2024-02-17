/* Created by Phil Malone. 2023.
    This class illustrates my simplified Odometry Strategy.
    It implements basic straight line motions but with heading and drift controls to limit drift.
    See the readme for a link to a video tutorial explaining the operation and limitations of the code.
 */

 package org.firstinspires.ftc.teamcode;
 import org.firstinspires.ftc.teamcode.Robot1;
 import org.firstinspires.ftc.vision.tfod.TfodProcessor;
 import org.firstinspires.ftc.vision.VisionPortal;
 import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
 import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
 import org.firstinspires.ftc.teamcode.BlueAztec;
 import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
 import com.qualcomm.robotcore.robot.Robot;
 import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
 import com.qualcomm.robotcore.util.ElapsedTime;
 
 import org.firstinspires.ftc.teamcode.BlueAztec;
 
 
 /*
  * This OpMode illustrates an autonomous opmode using simple Odometry
  * All robot functions are performed by an external "Robot" class that manages all hardware interactions.
  * Pure Drive or Strafe motions are maintained using two Odometry Wheels.
  * The IMU gyro is used to stabilize the heading during all motions
  */
 
 @Autonomous(name="FrontBlue", group = "Mr. Phil")
 public class FrontBlue extends LinearOpMode
 {
     // get an instance of the "Robot" class.
     private Robot1 robot = new Robot1(this);
     private Sensors1 sensors = new Sensors1(this);
     private BlueAztec object = new BlueAztec(this);
     private ElapsedTime runtime = new ElapsedTime();
     double x_object;
     double b = 0;
     private boolean leftObject = false;
     private boolean middleObject = false;
     private boolean rightObject = false;
 
     
     
     @Override public void runOpMode()
     {
         // Initialize the robot hardware & Turn on telemetry
         robot.initialize(true);
         sensors.initializeSensors();
 
         while (b == 0) {
             robot.bothGrippers(0.3,0.3);
             justWait(5000);
             robot.bothGrippers(0,1);
             justWait(1000);
             robot.startingPosition();
             b++;
         }
         
         robot.resetHeading();
         telemetry.addData("Waiting to Begin...",1);
         sensors.telemetryData();
         telemetry.update();
         waitForStart();
         
 
         // Reset heading to set a baseline for Auto
 
         // Run Auto if stop was not pressed.
         if (opModeIsActive())
         {
             double front = sensors.frontSensor();
             robot.beingAuto();
             justWait(3000);
             
             if (front < 25){
                 // To Place
                 robot.drive(25.5,0.8,0.25);
                 sleep(500);
                 robot.leftGripper(1);
                 sleep(500);
                 robot.leftGripper(-1);
                 
                 // To Score
                 robot.scoringPosition();
                 robot.strafe(32, 8.0,0.25);
                 turnLeft(12500);
                 justWait(3000);
                 robot.strafe(-3,0.8,0.25);
                 robot.boardscore();
                 justWait(2000);
                 robot.rightGripper(-1);
                 justWait(2000);
                 robot.drive(-2,0.8,0.25);
                 robot.rightGripper(1);
                 justWait(2000);
                 robot.strafe(23,0.8,0.25);
                 justWait(2000);
                 robot.scoringPosition();
                 robot.drive(11,0.8,0.25);
             }
             
             // If not on left, check if object is on the right.
             else {
                 robot.drive(28,1,0.25);
                 double right = sensors.rightSensor();
                 justWait(500);
                 robot.drive(-9,1,0.25);
                 
                 
                 if (right < 6) { 
                     // To Place
                     turnRight(7000); // Change Value


                     
                     justWait(500);
                     robot.leftGripper(1);
                     justWait(1000);
                     robot.leftGripper(-1);
                     


                    turnLeft(7000); // Change Value
                    justWait(500);
                    robot.drive(-3.5,1,0.25);
                     
                     
                     
                     
                     
                     
                     // To Score
                 robot.scoringPosition();
                 robot.strafe(32, 8.0,0.25);
                 turnLeft(12500);
                 justWait(3000);
                 robot.strafe(-3,0.8,0.25);
                 robot.boardscore();
                 justWait(2000);
                 robot.rightGripper(-1);
                 justWait(2000);
                 robot.drive(-2,0.8,0.25);
                 robot.rightGripper(1);
                 justWait(2000);
                 robot.strafe(23,0.8,0.25);
                 justWait(2000);
                 robot.scoringPosition();
                 robot.drive(11,0.8,0.25);
                 }

                 else{
                     // To Place
                     justWait(500);
                     robot.strafe(9, 8.0,0.25);
                     justWait(500);
                     robot.leftGripper(1);
                     justWait(1000);
                     robot.leftGripper(-1);
                     
                     
                     // To Score
                     robot.scoringPosition();
                     justWait(1000);
                     robot.strafe(23.5, 1,0.25);
                     turnLeft(12500);
                     justWait(2000);
                     robot.boardscore();
                     justWait(1000);
                     robot.rightGripper(-1);
                     justWait(500);
                     robot.drive(-3,1,0.25);
                     robot.rightGripper(1);
                     justWait(500);
                     robot.strafe(20,1,0.25);
                     justWait(500);
                     robot.scoringPosition();
                     robot.drive(11,0.8,0.25);
                 }
 
             }
             
             
         }
     }
 
     private void justWait(int miliseconds){
 
         double currTime = getRuntime();
         double waitUntil = currTime + (double)(miliseconds/1000);
         while (getRuntime() < waitUntil){
         }
 
     }
     
     private void turnLeft(int num){
         int i = 0;
         while (i < num) {
             robot.moveRobot(0,0,0.9);
             time++;
         }
         robot.moveRobot(0,0,0);
     }
     
     public void turnRight(int num) {
         int i = 0;
         while (i < num) {
             robot.moveRobot(0,0,-0.9);
             time++;
         }
         robot.moveRobot(0,0,0);
     }
 }
 