package org.firstinspires. ftc. teamcode. Extras;

import com. qualcomm. robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com. qualcomm. robotcore.eventloop. opmode.TeleOp;
import com. qualcomm. robotcore. hardware. DcMotor;

@TeleOp(name="MecanumDrive", group="Iterative Opmode")
public class MecanumDrive extends OpMode {
    private ElapsedTime runtime = new ElapsedTime();
    
    DcMotor RFMotor = null;
    DcMotor LFMotor = null;
    DcMotor RBMotor = null;
    DcMotor LBMotor = null;

    public void moveDriveTrain() {
        double vertical;
        double horizontal;
        double pivot;

        vertical = -gamepad1.left_stick_y;
        horizontal = gamepad1.left_stick_x;
        pivot = gamepad1.right_stick_x;

        RFMotor.setPower(pivot + (-vertical + horizontal));
        LFMotor.setPower(pivot + (-vertical - horizontal));
        LBMotor.setPower(pivot + (-vertical - horizontal));
        RBMotor.setPower(pivot + (-vertical + horizontal));
    }
    
    @Override
    public void init() {
        RFMotor = hardwareMap.get(DcMotor.class, "frontRight");
        LFMotor = hardwareMap.get(DcMotor.class, "frontLeft");
        LBMotor = hardwareMap.get(DcMotor.class, "backLeft");
        RBMotor = hardwareMap.get(DcMotor.class, "backRight");
        
        LFMotor.setDirection(DcMotorSimple.Direction.REVERSE);
        LBMotor.setDirection(DcMotorSimple.Direction.REVERSE);
    }
    
    public void start() {
        runtime.reset();
    }
    
    @Override
    public void loop() {
        moveDriveTrain();
    }
    
    @Override
    public void stop() {
    }
}