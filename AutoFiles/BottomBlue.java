/* Created by Phil Malone. 2023.
    This class illustrates my simplified Odometry Strategy.
    It implements basic straight line motions but with heading and drift controls to limit drift.
    See the readme for a link to a video tutorial explaining the operation and limitations of the code.
 */

package org.firstinspires.ftc.teamcode;
import org.firstinspires.ftc.teamcode.Robot1;
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

@Autonomous(name="BottomBlue", group = "Mr. Phil")
public class BottomBlue extends LinearOpMode
{
    // get an instance of the "Robot" class.
    private Robot1 robot = new Robot1(this);
    private BlueAztec object = new BlueAztec(this);
    private ElapsedTime runtime = new ElapsedTime();
    double x_object;
    private boolean leftObject = false;
    private boolean middleObject = false;
    private boolean rightObject = false;

    
    
    @Override public void runOpMode()
    {
        // Initialize the robot hardware & Turn on telemetry
        robot.initialize(true);
        object.initializeCamera();

        robot.bothGrippers(0.3,0.3);
        justWait(5000);
        robot.bothGrippers(0,1);
        justWait(1000);
        robot.travelPosition();
        justWait(1000);
        robot.arm(160);
        
        // Wait for driver to press start
        telemetry.addData(">", "Touch Play to run Auto");
        robot.resetHeading();
        object.detectAztec();
        telemetry.update();
        justWait(10000);
        

        // Reset heading to set a baseline for Auto

        // Run Auto if stop was not pressed.
        if (opModeIsActive())
        {
            runtime.reset();
            x_object = object.get_x();
            object.stopCamera();


            //drive(double distanceInches, double power, double holdTime)
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
