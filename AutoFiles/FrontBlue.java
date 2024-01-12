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
         //object.initializeCamera();
 
         while (b == 0) {
             robot.bothGrippers(0.3,0.3);
             justWait(5000);
             robot.bothGrippers(0,1);
             justWait(1000);
             robot.travelPosition();
             justWait(1000);
             robot.arm(10);
             justWait(1000);
             robot.moveWrist(1);
             b++;
         }
         
         // Wait for driver to press start
         robot.resetHeading();
         //telemetry.update();
         //object.detectAztec();
         //telemetry.update();
         //
         //justWait(10000);
         //telemetry.addData("waitForStart()", x_object);
         //telemetry.update();
         waitForStart();
         
 
         // Reset heading to set a baseline for Auto
 
         // Run Auto if stop was not pressed.
         if (opModeIsActive())
         {
             robot.moveWrist(-1);
             sleep(500);
             robot.drive(  26, 0.60, 0.25);
             robot.rightGripper(-1);
             sleep(500);
             robot.moveWrist(1);
             robot.rightGripper(1);
             justWait(200);
             robot.rightGripper(1);
             robot.drive(  -23, 0.60, 0.25);
             sleep(800);
             robot.strafe( 37, 0.60, 0.15);
             robot.moveWrist(-1);
             sleep(800);
             
             
             //robot.turnTo(90, 0.45, 0.5);
             //robot.drive(  71, 0.60, 0.25);
             //robot.turnTo(180, 0.45, 0.5);
             //robot.drive( 30, 0.60, 0.25);
             //robot.turnTo(270, 0.45, 0.5);
             //robot.drive(  72, 0.60, 0.25);
             //robot.turnTo(0, 0.45, 0.5);
 
             // Drive the path again without turning.
             //robot.drive(  84, 0.60, 0.15);
             //robot.strafe( -67, 0.60, 0.15);
             //sleep(1000);
             //robot.drive( -60, 0.60, 0.15);
             //sleep(1000);
             //robot.strafe( 68, 0.60, 0.15);
             //sleep(1000);
             //robot.drive( 60, 0.60, 0.15);
             //robot.strafe(-72, 0.60, 0.15);
             
             //runtime.reset();
             //int i = 0;
             //while (i < 50000000) {
             //    robot.driveStraight(0.3);
             //}
             //robot.drive(84, 0.6,0.25);
             //robot.strafe(28, 0.2, 0.25);
             
             //int first_run = 0;
             //while (first_run < 70000){
             //    robot2.RightBackDr(-0.3);
             //    robot2.RightFrontDr(-0.3);
             //    robot2.leftBackDr(-0.15);
             //    robot2.leftFrontDr(-0.15);
             //    
             //    first_run++;
             //}
             
             //int second_run = 0;
             //while (second_run < 50000){
             //
             //    robot2.driveStraight(-0.3);
             //    second_run++;
             //}
             //robot2.driveStraight(0);
             
             //x_object = object.get_x();
             //object.stopCamera();
 
 
             //strafe(double distanceInches, double power, double holdTime)
             //turnTo(double headingDeg, double power, double holdTime)
 
             
             
             /* 
 
 
             // Use camera to detect where the object is
             if (x_object < ) {
                 leftObject = true;
             }
             else if (num < x_object < num) {
                 middleObject = true;
             }
             else if (x_object > num) {
                 rightObject = true;
             }
 
 
             // Run Custom pathway
             if (leftObject) {
                 // Go to left object
 
                 // Then go to middle path
 
             }
             else if (middleObject) {
                 // Go to middle object
 
                 // Then go to middle path
             }
             else if (rightObject) {   
                 // Go to right object 
 
                 // Then go to middle path
             }
 
             // Drive to box
             
 
             */
 
             
         }
     }
 
     private void justWait(int miliseconds){
 
         double currTime = getRuntime();
         double waitUntil = currTime + (double)(miliseconds/1000);
         while (getRuntime() < waitUntil){
         }
 
     }
 }
 