/* Created by Phil Malone. 2023.
    This class illustrates my simplified Odometry Strategy.
    It implements basic straight line motions but with heading and drift controls to limit drift.
    See the readme for a link to a video tutorial explaining the operation and limitations of the code.
 */

 package org.firstinspires.ftc.teamcode;
 import org.firstinspires.ftc.teamcode.Robot1;
 import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
 import com.qualcomm.robotcore.robot.Robot;
 import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
 import com.qualcomm.robotcore.util.ElapsedTime;
 import com.qualcomm.robotcore.hardware.Servo;
 import com.qualcomm.robotcore.hardware.DcMotor;
 import com.qualcomm.robotcore.util.Range;
 
 
 /*
  * This OpMode illustrates a teleop OpMode for an Omni robot.
  * An external "Robot" class is used to manage all motor/sensor interfaces, and to assist driving functions.
  * The IMU gyro is used to stabilize the heading when the operator is not requesting a turn.
  */
 
 @TeleOp(name="Telop", group = "Mr. Phil")
 public class Telop extends LinearOpMode
 {
     private DcMotor leftLift = null;
     private DcMotor rightLift = null;
     private Servo plane = null;
 
     final double SAFE_DRIVE_SPEED   = 0.8 ;
     final double SAFE_STRAFE_SPEED  = 0.9 ;
     final double SAFE_YAW_SPEED     = 0.6*2 ;
     final double HEADING_HOLD_TIME  = 10.0 ; 
     
     // Declare Variables
     private double armSetpoint = 0.0;
     private final double gripperClosedPosition = 0.0;
     private final double gripperOpenPosition = 0.3;
 
 
     private final double wristScoringPosition = 0.267;
     private final double planeLuanchPosition = 1.0;
     private final double planeRestPosition = 0.0;
     private final int armScorePosition = 564;
     private final int armShutdownThreshold = 5;
     int b = 0;
     
 
     // local parameters
     ElapsedTime stopTime   = new ElapsedTime();  // Use for timeouts.
 
     // get an instance of the "Robot" class.
     Robot1 robot = new Robot1(this);
 
     @Override public void runOpMode()
     {
         // Initialize the drive hardware & Turn on telemetry
         robot.initialize(true);
         initializeTelop();
         telemetry.addData("Status", "Initialized");
         
 
         // Wait for driver to press start
         while(opModeInInit()) {
             telemetry.addData(">", "Touch Play to drive");
 
             // Read and display sensor data
             robot.readSensors(); // Work on this but can also wait
             telemetry.update();
 
             //Set up Wrist
             while (b == 0) {
                 robot.bothGrippers(0,1);
                 justWait(1000);
                 b++;
             }
             waitForStart();
         };
 
         while (opModeIsActive())
         {
             robot.readSensors();
             robot.moveWrist(0);
 
             // Allow the driver to reset the gyro by pressing both small gamepad buttons
             if(gamepad1.options && gamepad1.share){
                 robot.resetHeading();
                 robot.resetOdometry();
             }
 
             // read joystick values and scale according to limits set at top of this file
             double manualArmPower;
             double drive  = -gamepad1.left_stick_y * SAFE_DRIVE_SPEED;      //  Fwd/back on left stick
             double strafe = -gamepad1.left_stick_x * SAFE_STRAFE_SPEED;     //  Left/Right on left stick
             double yaw    = -gamepad1.right_stick_x * SAFE_YAW_SPEED;       //  Rotate on right stick
             //double wristTurn  =  -gamepad2.right_stick_y;
             
             //  Drive the wheels based on the desired axis motions
             telemetry.addData("Yaw:", yaw);
             telemetry.update();
             
             robot.moveRobot(drive, strafe, yaw);
 
             // Move Wrist
             
             //double wristPositionAnolog = Range.clip(wristTurn, 1, 200);
             
             if (gamepad2.dpad_up) {
                robot.moveWrist(-1);
             }
             
             else if (gamepad2.dpad_down) {
                robot.moveWrist(1); 
             }
             
             // Arm Movement
            
            manualArmPower = (gamepad2.left_trigger - gamepad2.right_trigger);
            robot.moveArm(manualArmPower);
            
            if (gamepad2.right_stick_y < 0) {
                robot.moveWrist(gamepad2.right_stick_y * -0.1);
            }
            
            else if (gamepad2.right_stick_y > 0) {
                robot.moveWrist(gamepad2.right_stick_y * -1);
            }
            
             
                 
             if (gamepad1.a) {
                 robot.travelPosition();
             }
 
             // Home Position
             if (gamepad1.b) {
                 robot.pickUpPosition();
             }
             // Scoring from the Front Position
             if (gamepad1.x) {
                 robot.scoreFrontPosition();
             }
             // Scoring from the Back Position
             if (gamepad1.y) {
                 robot.scoreBackPosition();
             }
 
 
             // Raise Poles (Get in Position to Hang)
             if (gamepad1.right_bumper) {
                 lift(1);
             }
             // Lower Poles (Hang from the Pole)
             if (gamepad1.left_bumper) {
                 lift(-1);
             }
 
 
             // Plane Ready Position
             if (gamepad2.dpad_right) {
                 plane.setPosition(0);
             }
             // Plane Luanch Position
             if (gamepad2.dpad_left) {
                 plane.setPosition(-0.3);
             }
 
 
             // Open Both Grippers
             if (gamepad2.x) {
                 robot.bothGrippers(0.3,0.3);
             }
             // Close Both Grippers
             if (gamepad2.y) {
                 robot.bothGrippers(0,1);
             }
             // Left Open
             if (gamepad2.left_bumper) {
                 robot.leftGripper(0.3);
             }  
             
             
             if (gamepad2.a) {
                 robot.Gripper(0);
             }
             
             if (gamepad2.b) {
                 robot.Gripper(1);
             }
             
             robot.telemetryData();
         }
     }
 
     public void lift(double num) {
         for(int i = 0; i < 150000; i++) {
             rightLift.setPower(num);
             leftLift.setPower(num);
         }
         rightLift.setPower(-0);
         leftLift.setPower(-0);
     }
 
     public void initializeTelop() {
         plane = hardwareMap.get(Servo.class, "plane");
         rightLift = hardwareMap.get(DcMotor.class, "strafeEncoder");
         leftLift = hardwareMap.get(DcMotor.class, "driveEncoder");
     }
     
     private void justWait(int miliseconds){
 
         double currTime = getRuntime();
         double waitUntil = currTime + (double)(miliseconds/1000);
         while (getRuntime() < waitUntil){
         }
 
     }
 
 }