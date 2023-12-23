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
import org.firstinspires.ftc.teamcode.BlueAztec1;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.Range;


/*
 * This OpMode illustrates a teleop OpMode for an Omni robot.
 * An external "Robot" class is used to manage all motor/sensor interfaces, and to assist driving functions.
 * The IMU gyro is used to stabilize the heading when the operator is not requesting a turn.
 */

@TeleOp(name="Sample Teleop", group = "Mr. Phil")
public class SampleTeleop extends LinearOpMode
{
    final double SAFE_DRIVE_SPEED   = 0.8 ;
    final double SAFE_STRAFE_SPEED  = 0.9 ;
    final double SAFE_YAW_SPEED     = 0.6*2 ;
    final double HEADING_HOLD_TIME  = 10.0 ; 
 
    private DcMotor armLeft = null;
    private DcMotor armRight = null;
    private Servo gripper = null;
    private Servo wrist = null;
    private Servo plane = null;
    private DcMotor rightLift = null;
    private DcMotor leftLift = null;
    private Servo leftGrip = null;
    private Servo rightGrip = null;
    private Servo handWrist = null;
    
    // Declare Variables
    private boolean manualMode = false;
    private double armSetpoint = 0.0;
    private final double armManualDeadband = 0.03;
    private final double gripperClosedPosition = 0.0;
    private final double gripperOpenPosition = 0.3;
    private final double wristUpPosition = 1;
    private final double wristScoringPosition = 0.267;
    private final double planeLuanchPosition = 1.0;
    private final double planeRestPosition = 0.0;
    private final int armHomePosition = 14;
    private final int armScorePosition = 564;
    private final int armShutdownThreshold = 5;
    boolean hanging = false;
    

    // local parameters
    ElapsedTime stopTime   = new ElapsedTime();  // Use for timeouts.
    boolean autoHeading    = false; // used to indicate when heading should be locked.

    // get an instance of the "Robot" class.
    Robot1 robot = new Robot1(this);

    @Override public void runOpMode()
    {
        // Initialize the drive hardware & Turn on telemetry
        robot.initialize(true);
        
        armLeft  = hardwareMap.get(DcMotor.class, "armLeft");
        armRight = hardwareMap.get(DcMotor.class, "armRight");
        gripper = hardwareMap.get(Servo.class, "gripper");
        wrist = hardwareMap.get(Servo.class, "wrist");
        plane = hardwareMap.get(Servo.class, "plane");
        rightLift = hardwareMap.get(DcMotor.class, "strafeEncoder");
        leftLift = hardwareMap.get(DcMotor.class, "driveEncoder");
        
        armLeft.setDirection(DcMotor.Direction.FORWARD);
        armRight.setDirection(DcMotor.Direction.REVERSE);
        leftLift.setDirection(DcMotor.Direction.REVERSE);
        armLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        armRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        armLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        armRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        armLeft.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        armRight.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        armLeft.setPower(0.0);
        armRight.setPower(0.0);
        telemetry.addData("Status", "Initialized");
        
        


        // Wait for driver to press start
        while(opModeInInit()) {
            telemetry.addData(">", "Touch Play to drive");

            // Read and display sensor data
            robot.readSensors();
            telemetry.update();
        };

        while (opModeIsActive())
        {
            robot.readSensors();

            // Allow the driver to reset the gyro by pressing both small gamepad buttons
            if(gamepad1.options && gamepad1.share){
                robot.resetHeading();
                robot.resetOdometry();
            }

            // read joystick values and scale according to limits set at top of this file
            double manualArmPower;
             
            double drive  = gamepad1.left_stick_y * SAFE_DRIVE_SPEED;      //  Fwd/back on left stick
            double strafe = gamepad1.left_stick_x * SAFE_STRAFE_SPEED;     //  Left/Right on left stick
            double yaw    = gamepad1.right_stick_x * SAFE_YAW_SPEED;       //  Rotate on right stick
            double wristTurn  =  -gamepad1.right_stick_y;
            
            double wristPositionAnolog = Range.clip(wristTurn, -1.0, 1.0) ;
            wrist.setPosition(wrist.getPosition() + wristPositionAnolog * 0.01);
            
            manualArmPower = gamepad1.right_trigger - gamepad1.left_trigger;
            if (Math.abs(manualArmPower) > armManualDeadband) {
                if (!manualMode) {
                    armLeft.setPower(0.0);
                    armRight.setPower(0.0);
                    armLeft.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
                    armRight.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
                    manualMode = true;
                }
                armLeft.setPower(manualArmPower);
                armRight.setPower(manualArmPower);
            }
            else {
                // if (manualMode)
                if (manualMode) {
                        armLeft.setTargetPosition(armLeft.getCurrentPosition());
                        armRight.setTargetPosition(armRight.getCurrentPosition());
                        armLeft.setPower(0.4);
                        armRight.setPower(0.4);
                        armLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                        armRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                        manualMode = false;
                }
                
                //preset buttons
                if (gamepad1.a) {
                    wrist.setPosition(wristUpPosition);
                    armLeft.setTargetPosition(armHomePosition);
                    armRight.setTargetPosition(armHomePosition);
                    armLeft.setPower(0.4);
                    armRight.setPower(0.4);
                    armLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                    armRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                }
                else if (gamepad1.b) {
                    wrist.setPosition(wristScoringPosition);
                }
                else if (gamepad1.y) {
                    wrist.setPosition(wristUpPosition);
                    armLeft.setTargetPosition(armScorePosition);
                    armRight.setTargetPosition(armScorePosition);
                    armLeft.setPower(0.4);
                    armRight.setPower(0.4);
                    armLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                    armRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                }
                else if (gamepad1.right_bumper) {
                    for(int i = 0; i < 200000; i++)
                    {
                        rightLift.setPower(0.6);
                        leftLift.setPower(0.6);
                    }
                    rightLift.setPower(0);
                    leftLift.setPower(0);
                }
                else if (gamepad1.left_bumper) {
                    for(int i = 0; i < 200000; i++)
                    {
                        rightLift.setPower(-0.6);
                        leftLift.setPower(-0.6);
                    }
                    rightLift.setPower(-0);
                    leftLift.setPower(-0);
                    
                }
            } 
            
            if (!manualMode &&
            armLeft.getMode() == DcMotor.RunMode.RUN_TO_POSITION &&
            armLeft.getTargetPosition() <= armShutdownThreshold &&
            armLeft.getCurrentPosition() <= armShutdownThreshold
            ) {
                armLeft.setPower(0.0);
                armRight.setPower(0.0);
                armLeft.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
                armRight.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            }
            
             //GRIPPER
            if (gamepad1.right_bumper) {
                gripper.setPosition(gripperOpenPosition);
            }
            if (gamepad1.left_bumper) {
                gripper.setPosition(gripperClosedPosition);
            }
            
            if (gamepad1.dpad_right) {
                plane.setPosition(planeLuanchPosition);
            }
            else if (gamepad1.dpad_left) {
                plane.setPosition(planeRestPosition);
            }

            // If auto heading is on, override manual yaw with the value generated by the heading controller.
            if (autoHeading) {
                yaw = robot.yawController.getOutput(robot.getHeading());
            }

            //  Drive the wheels based on the desired axis motions
            robot.moveRobot(drive, strafe, yaw);
            

            telemetry.addData("Manual Power", manualArmPower);
            telemetry.addData("Arm Pos:",
                "left = " + 
                ((Integer)armLeft.getCurrentPosition()).toString() + 
                ", right = " +
                ((Integer)armRight.getCurrentPosition()).toString());
            telemetry.addData("Wrist Position", wrist.getPosition());

            // If the robot has just been sitting here for a while, make heading setpoint track any gyro drift to prevent rotating.
            if ((drive == 0) && (strafe == 0) && (yaw == 0)) {
                if (stopTime.time() > HEADING_HOLD_TIME) {
                    robot.yawController.reset(robot.getHeading());  // just keep tracking the current heading
                }
            } else {
                stopTime.reset();
            }
        }
    }
}