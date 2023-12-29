/* Created by Phil Malone. 2023.
    This class illustrates my simplified Odometry Strategy.
    It implements basic straight line motions but with heading and drift controls to limit drift.
    See the readme for a link to a video tutorial explaining the operation and limitations of the code.
 */

package org.firstinspires.ftc.teamcode;
import org.firstinspires.ftc.teamcode.Robot1;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.robot.Robot;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.util.ElapsedTime;

/*
 * This OpMode illustrates an autonomous opmode using simple Odometry
 * All robot functions are performed by an external "Robot" class that manages all hardware interactions.
 * Pure Drive or Strafe motions are maintained using two Odometry Wheels.
 * The IMU gyro is used to stabilize the heading during all motions
 */

@Autonomous(name="Auto", group = "Mr. Phil")
public class TopRed extends LinearOpMode
{
    // get an instance of the "Robot" class.
    private Robot1 robot = new Robot1(this);
    private ElapsedTime runtime = new ElapsedTime();
    
    
    @Override public void runOpMode()
    {
        // Initialize the robot hardware & Turn on telemetry
        robot.initialize(true);

        robot.bothGrippers(0.3,0.3);
        justWait(5000);
        robot.bothGrippers(0,1);

        // Wait for driver to press start
        telemetry.addData(">", "Touch Play to run Auto");
        telemetry.update();

        waitForStart();
        robot.resetHeading();
        

        // Reset heading to set a baseline for Auto

        // Run Auto if stop was not pressed.
        if (opModeIsActive())
        {
            runtime.reset();
            
            for (int i = 0; i < 3000000; i++){
                
                robot.driveStraight(-0.5);
            }

            // Drive the path again without turning.
            
        }
    }

    private void justWait(int miliseconds){

        double currTime = getRuntime();
        double waitUntil = currTime + (double)(miliseconds/1000);
        while (getRuntime() < waitUntil){
        }

    }
}
