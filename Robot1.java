/* Created by Phil Malone. 2023.
    This class illustrates my simplified Odometry Strategy.
    It implements basic straight line motions but with heading and drift controls to limit drift.
    See the readme for a link to a video tutorial explaining the operation and limitations of the code.
 */

 package org.firstinspires.ftc.teamcode;

 import com.qualcomm.hardware.lynx.LynxModule;
 import com.qualcomm.robotcore.hardware.Servo;
 import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
 import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
 import com.qualcomm.robotcore.hardware.DcMotor;
 import com.qualcomm.robotcore.hardware.IMU;
 import com.qualcomm.robotcore.util.ElapsedTime;
 import com.qualcomm.robotcore.util.Range;
 import com.qualcomm.robotcore.eventloop.opmode.Disabled;
 import com.qualcomm.robotcore.eventloop.opmode.OpMode;
 import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
 
 
 import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
 import org.firstinspires.ftc.robotcore.external.navigation.AngularVelocity;
 import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;
 
 import java.util.List;
 
 public class Robot1 {
     // Adjust these numbers to suit your robot.
     private final double ODOM_INCHES_PER_COUNT   = 0.002969;   //  GoBilda Odometry Pod (1/226.8)
     private final boolean INVERT_DRIVE_ODOMETRY  = true;       //  When driving FORWARD, the odometry value MUST increase.  If it does not, flip the value of this constant.
     private final boolean INVERT_STRAFE_ODOMETRY = false;       //  When strafing to the LEFT, the odometry value MUST increase.  If it does not, flip the value of this constant.
     
     private final double wristUpPosition = 1.0;
     private final int armHomePosition = 14;
     private final double armManualDeadband = 0.03;
     private final int armScorePosition = 564;
     private final double wristScoringPosition = 0.267;
     private boolean manualMode = false;
     
 
     private static final double DRIVE_GAIN          = 0.03;    // Strength of axial position control
     private static final double DRIVE_ACCEL         = 2.0;     // Acceleration limit.  Percent Power change per second.  1.0 = 0-100% power in 1 sec.
     private static final double DRIVE_TOLERANCE     = 0.5;     // Controller is is "inPosition" if position error is < +/- this amount
     private static final double DRIVE_DEADBAND      = 0.2;     // Error less than this causes zero output.  Must be smaller than DRIVE_TOLERANCE
     private static final double DRIVE_MAX_AUTO      = 0.6;     // "default" Maximum Axial power limit during autonomous
 
     private static final double STRAFE_GAIN         = 0.03;    // Strength of lateral position control
     private static final double STRAFE_ACCEL        = 1.5;     // Acceleration limit.  Percent Power change per second.  1.0 = 0-100% power in 1 sec.
     private static final double STRAFE_TOLERANCE    = 0.5;     // Controller is is "inPosition" if position error is < +/- this amount
     private static final double STRAFE_DEADBAND     = 0.2;     // Error less than this causes zero output.  Must be smaller than DRIVE_TOLERANCE
     private static final double STRAFE_MAX_AUTO     = 0.6;     // "default" Maximum Lateral power limit during autonomous
 
     private static final double YAW_GAIN            = 0.018;    // Strength of Yaw position control
     private static final double YAW_ACCEL           = 3.0;     // Acceleration limit.  Percent Power change per second.  1.0 = 0-100% power in 1 sec.
     private static final double YAW_TOLERANCE       = 1.0;     // Controller is is "inPosition" if position error is < +/- this amount
     private static final double YAW_DEADBAND        = 0.25;    // Error less than this causes zero output.  Must be smaller than DRIVE_TOLERANCE
     private static final double YAW_MAX_AUTO        = 0.6;     // "default" Maximum Yaw power limit during autonomous
 
 
     // Public Members
     public double driveDistance     = 0; // scaled axial distance (+ = forward)
     public double strafeDistance    = 0; // scaled lateral distance (+ = left)
     public double heading           = 0; // Latest Robot heading from IMU
 
     // Establish a proportional controller for each axis to calculate the required power to achieve a setpoint.
     public ProportionalControl driveController     = new ProportionalControl(DRIVE_GAIN, DRIVE_ACCEL, DRIVE_MAX_AUTO, DRIVE_TOLERANCE, DRIVE_DEADBAND, false);
     public ProportionalControl strafeController    = new ProportionalControl(STRAFE_GAIN, STRAFE_ACCEL, STRAFE_MAX_AUTO, STRAFE_TOLERANCE, STRAFE_DEADBAND, false);
     public ProportionalControl yawController       = new ProportionalControl(YAW_GAIN, YAW_ACCEL, YAW_MAX_AUTO, YAW_TOLERANCE,YAW_DEADBAND, true);
 
     // ---  Private Members
        // drive motor position variables
    private int lfPos; private int rfPos; private int lrPos; private int rrPos;
    private double clicksPerInch = 87.5; // empirically measured
    private double clicksPerDeg = 21.94; // empirically measured

     // Hardware interface Objects
     private DcMotor leftFrontDrive;     //  control the left front drive wheel
     private DcMotor rightFrontDrive;    //  control the right front drive wheel
     private DcMotor leftBackDrive;      //  control the left back drive wheel
     private DcMotor rightBackDrive;     //  control the right back drive wheel
 
     private DcMotor driveEncoder;       //  the Axial (front/back) Odometry Module (may overlap with motor, or may not)
     private DcMotor strafeEncoder;      //  the Lateral (left/right) Odometry Module (may overlap with motor, or may not)
 
     private DcMotor armLeft;
    
     public DcMotor arm;
     private Servo leftGrip;
     private Servo rightGrip;
 
     private LinearOpMode myOpMode;
     private IMU imu;
     private ElapsedTime holdTimer = new ElapsedTime();  // User for any motion requiring a hold time or timeout.
 
     private int rawDriveOdometer    = 0; // Unmodified axial odometer count
     private int driveOdometerOffset = 0; // Used to offset axial odometer
     private int rawStrafeOdometer   = 0; // Unmodified lateral odometer count
     private int strafeOdometerOffset= 0; // Used to offset lateral odometer
     private double rawHeading       = 0; // Unmodified heading (degrees)
     private double headingOffset    = 0; // Used to offset heading
 
     private double turnRate           = 0; // Latest Robot Turn Rate from IMU
     private boolean showTelemetry     = false;
 
     // Robot Constructor
     public Robot1 (LinearOpMode opmode) {
         myOpMode = opmode;
     }
 
     /**
      * Robot Initialization:
      *  Use the hardware map to Connect to devices.
      *  Perform any set-up all the hardware devices.
      * @param showTelemetry  Set to true if you want telemetry to be displayed by the robot sensor/drive functions.
      */
     public void initialize(boolean showTelemetry)
     {
        // Initialize the hardware variables. Note that the strings used to 'get' each
        // motor/device must match the names assigned during the robot configuration.
 
         // !!!  Set the drive direction to ensure positive power drives each wheel forward.
        leftFrontDrive  = setupMotor("frontLeft", DcMotor.Direction.REVERSE, DcMotor.RunMode.RUN_USING_ENCODER);
        rightFrontDrive = setupMotor("frontRight", DcMotor.Direction.FORWARD, DcMotor.RunMode.RUN_USING_ENCODER);
        leftBackDrive  = setupMotor( "backLeft", DcMotor.Direction.REVERSE, DcMotor.RunMode.RUN_USING_ENCODER);
        rightBackDrive = setupMotor( "backRight",DcMotor.Direction.FORWARD, DcMotor.RunMode.RUN_USING_ENCODER);
        armLeft = setupMotor("armLift", DcMotor.Direction.FORWARD, DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        imu = myOpMode.hardwareMap.get(IMU.class, "imu");
 
        //  Connect to the encoder channels using the name of that channel.
        driveEncoder = myOpMode.hardwareMap.get(DcMotor.class, "driveEncoder");
        strafeEncoder = myOpMode.hardwareMap.get(DcMotor.class, "strafeEncoder");
 
        // Set the wrist and grippers.
        arm = setupMotor("arm", DcMotor.Direction.FORWARD, DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        rightGrip = myOpMode.hardwareMap.get(Servo.class, "rightGrip");
        leftGrip = myOpMode.hardwareMap.get(Servo.class, "leftGrip");
 
 
         // Set all hubs to use the AUTO Bulk Caching mode for faster encoder reads
         List<LynxModule> allHubs = myOpMode.hardwareMap.getAll(LynxModule.class);
         for (LynxModule module : allHubs) {
             module.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
         }
 
         // Tell the software how the Control Hub is mounted on the robot to align the IMU XYZ axes correctly
         RevHubOrientationOnRobot orientationOnRobot =
                 new RevHubOrientationOnRobot(RevHubOrientationOnRobot.LogoFacingDirection.LEFT,
                                              RevHubOrientationOnRobot.UsbFacingDirection.DOWN);
         imu.initialize(new IMU.Parameters(orientationOnRobot));
 
         // zero out all the odometry readings.
         resetOdometry();
 
         // Set the desired telemetry state
         this.showTelemetry = showTelemetry;
     }
 
     /**
      *   Setup a drive motor with passed parameters.  Ensure encoder is reset.
      * @param deviceName  Text name associated with motor in Robot Configuration
      * @param direction   Desired direction to make the wheel run FORWARD with positive power input
      * @return the DcMotor object
      */
     private DcMotor setupMotor(String deviceName, DcMotor.Direction direction, DcMotor.RunMode mode) {
         DcMotor aMotor = myOpMode.hardwareMap.get(DcMotor.class, deviceName);
         aMotor.setDirection(direction);
         aMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);  // Reset Encoders to zero
         aMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
         aMotor.setMode(mode);  // Requires motor encoder cables to be hooked up.
         return aMotor;
     }
 
     /**
      * Read all input devices to determine the robot's motion
      * always return true so this can be used in "while" loop conditions
      * @return true
      */
     public boolean readSensors() {
         rawDriveOdometer = driveEncoder.getCurrentPosition() * (INVERT_DRIVE_ODOMETRY ? -1 : 1);
         rawStrafeOdometer = strafeEncoder.getCurrentPosition() * (INVERT_STRAFE_ODOMETRY ? -1 : 1);
         driveDistance = (rawDriveOdometer - driveOdometerOffset) * ODOM_INCHES_PER_COUNT;
         strafeDistance = (rawStrafeOdometer - strafeOdometerOffset) * ODOM_INCHES_PER_COUNT;
 
         YawPitchRollAngles orientation = imu.getRobotYawPitchRollAngles();
         AngularVelocity angularVelocity = imu.getRobotAngularVelocity(AngleUnit.DEGREES);
 
         rawHeading  = orientation.getYaw(AngleUnit.DEGREES);
         heading     = rawHeading - headingOffset;
         turnRate    = angularVelocity.zRotationRate;
 
         if (showTelemetry) {
             myOpMode.telemetry.addData("Odom Ax:Lat", "%6d %6d", rawDriveOdometer - driveOdometerOffset, rawStrafeOdometer - strafeOdometerOffset);
             myOpMode.telemetry.addData("Dist Ax:Lat", "%5.2f %5.2f", driveDistance, strafeDistance);
             myOpMode.telemetry.addData("Head Deg:Rate", "%5.2f %5.2f", heading, turnRate);
         }
         return true;  // do this so this function can be included in the condition for a while loop to keep values fresh.
     }
 
     //  ########################  Mid level control functions.  #############################3#
 
     /**
      * Drive in the axial (forward/reverse) direction, maintain the current heading and don't drift sideways
      * @param distanceInches  Distance to travel.  +ve = forward, -ve = reverse.
      * @param power Maximum power to apply.  This number should always be positive.
      * @param holdTime Minimum time (sec) required to hold the final position.  0 = no hold.
      */
      
      
     public void drive(double distanceInches, double power, double holdTime) {
         resetOdometry();
 
         driveController.reset(distanceInches, power);   // achieve desired drive distance
         strafeController.reset(0);
         yawController.reset();    // Maintain zero strafe drift                       // Maintain last turn heading
         holdTimer.reset();
 
         while (myOpMode.opModeIsActive() && readSensors()){
 
             // implement desired axis powers
             moveRobot(driveController.getOutput(driveDistance), strafeController.getOutput(strafeDistance), yawController.getOutput(heading));
 
             // Time to exit?
             if (driveController.inPosition() && yawController.inPosition()) {
                 if (holdTimer.time() > holdTime) {
                     break;   // Exit loop if we are in position, and have been there long enough.
                 }
             } else {
                 holdTimer.reset();
             }
             myOpMode.sleep(10);
         }
         stopRobot();
     }
 
     /**
      * Strafe in the lateral (left/right) direction, maintain the current heading and don't drift fwd/bwd
      * @param distanceInches  Distance to travel.  +ve = left, -ve = right.
      * @param power Maximum power to apply.  This number should always be positive.
      * @param holdTime Minimum time (sec) required to hold the final position.  0 = no hold.
      */
     public void strafe(double distanceInches, double power, double holdTime) {
         resetOdometry();
 
         driveController.reset(0.0);             //  Maintain zero drive drift
         strafeController.reset(distanceInches, power);  // Achieve desired Strafe distance
         yawController.reset();                          // Maintain last turn angle
         holdTimer.reset();
 
         while (myOpMode.opModeIsActive() && readSensors()){
 
             // implement desired axis powers
             moveRobot(driveController.getOutput(driveDistance), strafeController.getOutput(strafeDistance), yawController.getOutput(heading));
 
             // Time to exit?
             if (strafeController.inPosition() && yawController.inPosition()) {
                 if (holdTimer.time() > holdTime) {
                     break;   // Exit loop if we are in position, and have been there long enough.
                 }
             } else {
                 holdTimer.reset();
             }
             myOpMode.sleep(10);
         }
         stopRobot();
     }
    
    public void turnTo(double headingDeg, double power, double holdTime) {
        yawController.reset(headingDeg, power);
        holdTimer.reset();

        while (myOpMode.opModeIsActive() && readSensors()) {
            double turnPower = yawController.getOutput(headingDeg);

            // implement desired axis powers
            moveRobot(0, 0, turnPower);

            // Time to exit?
            if (yawController.inPosition() && holdTimer.time() > holdTime) {
                break;   // Exit loop if we are in position, and have been there long enough.
            }

            myOpMode.sleep(10);
        }
        stopRobot();
    }
    
    /*
    public void turnClockwise(int whatAngle, double speed) {
        // whatAngle is in degrees. A negative whatAngle turns counterclockwise.
        double turnPower = yawController.getOutput(heading);
        // fetch motor positions
        lfPos = leftFrontDrive.getCurrentPosition();
        rfPos = rightFrontDrive.getCurrentPosition();
        lrPos = leftBackDrive.getCurrentPosition();
        rrPos = rightBackDrive.getCurrentPosition();

        // calculate new targets
        lfPos += whatAngle * clicksPerDeg;
        rfPos -= whatAngle * clicksPerDeg;
        lrPos += whatAngle * clicksPerDeg;
        rrPos -= whatAngle * clicksPerDeg;

        // move robot to new position
        leftFrontDrive.setTargetPosition(lfPos);
        rightFrontDrive.setTargetPosition(rfPos);
        leftBackDrive.setTargetPosition(lrPos);
        rightBackDrive.setTargetPosition(rrPos);
        leftFrontDrive.setPower(speed);
        rightFrontDrive.setPower(speed);
        leftBackDrive.setPower(speed);
        rightBackDrive.setPower(speed);
    /* 
    }
     /**
      * Rotate to an absolute heading/direction
      * @param headingDeg  Heading to obtain.  +ve = CCW, -ve = CW.
      * @param power Maximum power to apply.  This number should always be positive.
      * @param holdTime Minimum time (sec) required to hold the final position.  0 = no hold.
      */
 
 
     //  ########################  Low level control functions.  ###############################
 
     /**
      * Drive the wheel motors to obtain the requested axes motions
      * @param drive     Fwd/Rev axis power
      * @param strafe    Left/Right axis power
      * @param yaw       Yaw axis power
      */
     public void moveRobot(double drive, double strafe, double yaw){
 
         double lF = drive - strafe - yaw;
         double rF = drive + strafe + yaw;
         double lB = drive + strafe - yaw;
         double rB = drive - strafe + yaw;
 
         //send power to the motors
         leftFrontDrive.setPower(-lF);
         rightFrontDrive.setPower(-rF);
         leftBackDrive.setPower(-lB);
         rightBackDrive.setPower(-rB);
 
         if (showTelemetry) {
             myOpMode.telemetry.addData("Axes D:S:Y", "%5.2f %5.2f", drive, strafe);
             myOpMode.telemetry.addData("Wheels lf:rf:lb:rb", "%5.2f %5.2f %5.2f %5.2f", lF, rF, lB, rB);
             myOpMode.telemetry.update(); //  Assume this is the last thing done in the loop.
         }
     }
 
 
 
     public void pickUpPosition() {
 
         arm.setTargetPosition(0);
 
         arm.setPower(0.4);
        
 
         arm.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        
     }
 
     public void travelPosition() {
 
         arm.setTargetPosition(33);
        
 
         arm.setPower(0.4);
         
 
         arm.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        
     }
 
      public void scoreFrontPosition() {
         //armLeft.setTargetPosition(33);
         //armRight.setTargetPosition(33);
         //armLeft.setPower(0.4);
         //armRight.setPower(0.4);
         //armLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
         //armRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        
     }
 
 
     public void scoreBackPosition() {
         //armLeft.setTargetPosition(33);
         //armRight.setTargetPosition(33);
         //armLeft.setPower(0.4);
         //armRight.setPower(0.4);
         //armLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
         //armRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);
         
     }
     
     public void moveArm(double manualArmPower) {
            if (Math.abs(manualArmPower) > armManualDeadband) {
                if (!manualMode) {
                    armLeft.setPower(0.0);
                    armLeft.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
                    manualMode = true;
                }
                armLeft.setPower(manualArmPower);
            }
            else {
                if (manualMode) {
                    armLeft.setTargetPosition(armLeft.getCurrentPosition());
                    armLeft.setPower(.80);
                    armLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                    manualMode = false;
                }
            } 
     }
     
     public void startingPosition() {
         armLeft.setTargetPosition(-57);
        
 
         armLeft.setPower(0.4);
         
 
         armLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        
        
        arm.setTargetPosition(1028);
        
 
         arm.setPower(0.4);
         
 
         arm.setMode(DcMotor.RunMode.RUN_TO_POSITION);
     }
     
     public void beingAuto() {
        arm.setTargetPosition(0);
        
 
        arm.setPower(0.8);
         
 
        arm.setMode(DcMotor.RunMode.RUN_TO_POSITION);
     }
     
     public void scoringPosition() {
        armLeft.setTargetPosition(-52);
        
 
         armLeft.setPower(0.4);
         
 
         armLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        
        
        arm.setTargetPosition(500);
        
 
         arm.setPower(0.4);
         
 
         arm.setMode(DcMotor.RunMode.RUN_TO_POSITION);
     }
     
     public void boardscore() {
         arm.setTargetPosition(347);
        
 
         arm.setPower(0.4);
         
 
         arm.setMode(DcMotor.RunMode.RUN_TO_POSITION);
     }
     
 
     public void moveWrist(double manualArmPower) {
         
        if (Math.abs(manualArmPower) > armManualDeadband) {
                if (!manualMode) {
                    arm.setPower(0.0);
                    arm.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
                    manualMode = true;
                }
                arm.setPower(manualArmPower);
            }
            else {
                if (manualMode) {
                    arm.setTargetPosition(arm.getCurrentPosition());
                    arm.setPower(.80);
                    arm.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                    manualMode = false;
                }
            }
        
     }
 
 
     public void bothGrippers(double positionLeft, double positionRight) {
         leftGrip.setPosition(positionLeft);
         rightGrip.setPosition(positionRight);
     }
 
     public void rightGripper(double position) {
         rightGrip.setPosition(position);
     }
 
     public void leftGripper(double position) {
         leftGrip.setPosition(position);
     }
 
     public void telopArm(double power) {
         if (Math.abs(power) > armManualDeadband) {
             if (!manualMode) {
                 arm.setPower(0.0);
 
                 arm.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
 
                 manualMode = true;
             }
             arm.setPower(power);
             
             
         }
         else {
             if (manualMode) {
                     arm.setTargetPosition(arm.getCurrentPosition());
                     
 
                     arm.setPower(0.4);
                    
 
                     arm.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                     
 
                     manualMode = false;
             }
         }
     }
 
     /**
      * Stop all motors.
      */
     public void stopRobot() {
         moveRobot(0,0,0);
     }
 
     /**
      * Set odometry counts and distances to zero.
      */
     public void resetOdometry() {
         readSensors();
         driveOdometerOffset = rawDriveOdometer;
         driveDistance = 0.0;
         driveController.reset(0);
 
         strafeOdometerOffset = rawStrafeOdometer;
         strafeDistance = 0.0;
         strafeController.reset(0);
     }
 
     /**
      * Reset the robot heading to zero degrees, and also lock that heading into heading controller.
      */
     public void resetHeading() {
         readSensors();
         headingOffset = rawHeading;
         yawController.reset(0);
         heading = 0;
     }
 
     public double getHeading() {return heading;}
     public double getTurnRate() {return turnRate;}
 
     /**
      * Set the drive telemetry on or off
      */
     public void showTelemetry(boolean show){
         showTelemetry = show;
     }
 
     public void telemetryData() {
         // Arm Location
         myOpMode.telemetry.addData("Arm Pos:",
             "= " + 
             ((Integer)armLeft.getCurrentPosition()).toString());
             
        myOpMode.telemetry.addData("Wrist Pos:",
             "= " + 
             ((Integer)arm.getCurrentPosition()).toString());
             
         // Wrist Position
         
 
 
     }
 }
 
 //****************************************************************************************************
 //****************************************************************************************************
 
 /***
  * This class is used to implement a proportional controller which can calculate the desired output power
  * to get an axis to the desired setpoint value.
  * It also implements an acceleration limit, and a max power output.
  */
 class ProportionalControl {
    double  lastOutput;
    double  gain;
    double  accelLimit;
    double  defaultOutputLimit;
    double  liveOutputLimit;
    double  setPoint;
    double  tolerance;
    double deadband;
    boolean circular;
    boolean inPosition;
    ElapsedTime cycleTime = new ElapsedTime();

    public ProportionalControl(double gain, double accelLimit, double outputLimit, double tolerance, double deadband, boolean circular) {
        this.gain = gain;
        this.accelLimit = accelLimit;
        this.defaultOutputLimit = outputLimit;
        this.liveOutputLimit = outputLimit;
        this.tolerance = tolerance;
        this.deadband = deadband;
        this.circular = circular;
        reset(0.0);
    }

    /**
     * Determines power required to obtain the desired setpoint value based on new input value.
     * Uses proportional gain, and limits rate of change of output, as well as max output.
     * @param input  Current live control input value (from sensors)
     * @return desired output power.
     */
    public double getOutput(double input) {
        double error = setPoint - input;
        double dV = cycleTime.seconds() * accelLimit;
        double output;

        // normalize to +/- 180 if we are controlling heading
        if (circular) {
            while (error > 180)  error -= 360;
            while (error <= -180) error += 360;
        }

        inPosition = (Math.abs(error) < tolerance);

        // Prevent any very slow motor output accumulation
        if (Math.abs(error) <= deadband) {
            output = 0;
        } else {
            // calculate output power using gain and clip it to the limits
            output = (error * gain);
            output = Range.clip(output, -liveOutputLimit, liveOutputLimit);

            // Now limit rate of change of output (acceleration)
            if ((output - lastOutput) > dV) {
                output = lastOutput + dV;
            } else if ((output - lastOutput) < -dV) {
                output = lastOutput - dV;
            }
        }

        lastOutput = output;
        cycleTime.reset();
        return output;
    }

    public boolean inPosition(){
        return inPosition;
    }
    public double getSetpoint() {return setPoint;}

    /**
     * Saves a new setpoint and resets the output power history.
     * This call allows a temporary power limit to be set to override the default.
     * @param setPoint
     * @param powerLimit
     */
    public void reset(double setPoint, double powerLimit) {
        liveOutputLimit = Math.abs(powerLimit);
        this.setPoint = setPoint;
        reset();
    }

    /**
     * Saves a new setpoint and resets the output power history.
     * @param setPoint
     */
    public void reset(double setPoint) {
        liveOutputLimit = defaultOutputLimit;
        this.setPoint = setPoint;
        reset();
    }

    /**
     * Leave everything else the same, Just restart the acceleration timer and set output to 0
     */
    public void reset() {
        cycleTime.reset();
        inPosition = false;
        lastOutput = 0.0;
    }
 }