
package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.rev.Rev2mDistanceSensor;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;


public class Sensors1 {

    private DistanceSensor frontSensor;
    private DistanceSensor rightSensor;
    private Rev2mDistanceSensor FrontsensorTimeOfFlight; 
    private Rev2mDistanceSensor RightsensorTimeOfFlight;
    private LinearOpMode myOpMode;


    public Sensors1 (LinearOpMode opmode) {
        myOpMode = opmode;
    }


    public void initializeSensors() {
        // you can use this as a regular DistanceSensor.
        frontSensor = myOpMode.hardwareMap.get(DistanceSensor.class, "leftSensor");
        rightSensor = myOpMode.hardwareMap.get(DistanceSensor.class, "rightSensor");

        // you can also cast this to a Rev2mDistanceSensor if you want to use added
        // methods associated with the Rev2mDistanceSensor class.
        Rev2mDistanceSensor FrontsensorTimeOfFlight = (Rev2mDistanceSensor)frontSensor;
        Rev2mDistanceSensor RightsensorTimeOfFlight = (Rev2mDistanceSensor)rightSensor;
    }

    public double frontSensor(){
        return frontSensor.getDistance(DistanceUnit.INCH);
    }
    
    public double rightSensor(){
        return rightSensor.getDistance(DistanceUnit.INCH);
    }
    
    public void telemetryData() {
         // Arm Location
        myOpMode.telemetry.addData("Front Sensor:", frontSensor.getDistance(DistanceUnit.INCH));
             
        myOpMode.telemetry.addData("Right Sensor:", rightSensor.getDistance(DistanceUnit.INCH));
             
         // Wrist Position
         
     }
        
}
