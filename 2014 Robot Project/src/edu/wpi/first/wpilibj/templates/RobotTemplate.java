package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.SimpleRobot;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Encoder;

public class RobotTemplate extends SimpleRobot 
{
    Talon leftDrive = new Talon(2);
    Talon rightDrive = new Talon(1);
    Talon intakeRight = new Talon(5);
    Talon intakeLeft = new Talon(6);
    Talon winch = new Talon(8);
  
    Encoder leftDriveEncoder = new Encoder(2,3, true);
    Encoder rightDriveEncoder = new Encoder(4,5, true);
    Encoder winchEncoder = new Encoder(6,7, true);
    
    DigitalInput armLimitSwitch = new DigitalInput(8);
    DigitalInput ballSensor = new DigitalInput(9);     
    
    Compressor compressor = new Compressor(1,8);
    
    Solenoid autonShoot = new Solenoid(1);//True to shoot, 
    Solenoid autonLatch = new Solenoid(2);//True to latch
    Solenoid teleopShoot = new Solenoid(3);//True to latch, false to shoot
    Solenoid toroPiston = new Solenoid(4);//True is out, false retracts
    Solenoid driveShifter = new Solenoid(5);//True is low, false is high
    //Solenoid winchShifter = new Solenoid(6);
    Solenoid leftCatcher = new Solenoid(6);//True is out
    Solenoid rightCatcher = new Solenoid(7);//True is out
    
    Joystick driveStick = new Joystick(1);
    Joystick opStick = new Joystick(2);
    Joystick driverStation = new Joystick(3);
    
    double deadBand = .2;
    double circDeadBand = 0;
    double winchEnco0 = 0;
    
    boolean hasShot = false;
    boolean okToAutoRaise = false;
    
    public void autonomous() 
    {
        teleopShoot.set(false);
        autonShoot.set(true);
        Timer.delay(2000);
        leftDrive.set(.5);
        rightDrive.set(.5);
        Timer.delay(2000);
        leftDrive.stopMotor();
        rightDrive.stopMotor();
        
    }
    public void operatorControl() 
    {
        //driveLeftRight.setSafetyEnabled(true);
        autonShoot.set(true);
        while(isOperatorControl() && isEnabled())
        {
            //Timer.delay(0.01);
            double gearSpeed = 0;
            double quickTurn = 0;
            double throttle = driveStick.getRawAxis(3);
            double steering = driveStick.getRawAxis(2)*-1;
            //Circular deadband
            circDeadBand = Math.sqrt(((throttle * throttle) + (steering * steering)));
            //sets it to nothing if its below the deadband
            if(circDeadBand < .1)
            {
                throttle = 0;
                steering = 0;
            }
            //sqaured for control--throttle
            if(throttle < 0)
            {
                throttle = (throttle * throttle) * -1;
            }
            else
            {
                throttle = throttle * throttle;
            }
            //sqaured for control--steering
            if(steering < 0)
            {
                steering = (steering * steering) *-1;
            }
            else
            {
                steering = steering*steering;
            }
            double steeringSense = 0;
            if(Math.abs(throttle)>.2)
            {
                steeringSense = throttle;
                if(Math.abs(driveStick.getRawAxis(5))>deadBand)
                {//low gear
                    gearSpeed = .8;
                }
                else
                {//high gear
                    gearSpeed = 1;
                }
            }
            else
            {
                steeringSense = 1;
                gearSpeed = 1;
            }
            double drivingTotal = (gearSpeed * steeringSense * steering);
            double rightMotorValue = throttle + (drivingTotal*-1);
            double leftMotorValue = throttle + (drivingTotal);
            if(rightMotorValue > 1)
            {
                rightMotorValue = 1;
            }
            else if(rightMotorValue < -1)
            {
                rightMotorValue = -1;
            }
            if(leftMotorValue > 1)
            {
                leftMotorValue = 1;
            }
            else if(leftMotorValue < -1)
            {
                leftMotorValue = -1;
            }
            if(leftMotorValue == 0)
            {
                leftDrive.stopMotor();
            }
            else
            {
                leftDrive.set(leftMotorValue);
            }
            if(rightMotorValue == 0)
            {
                rightDrive.stopMotor();
            }
            else
            {
                rightDrive.set(rightMotorValue);
            }
            //Reseting the winches 0
            if(armLimitSwitch.get() == true && hasShot == true)
            {
                winchEnco0 = winchEncoder.get();
                hasShot = false;
                okToAutoRaise = true;  
            }
            //Lowering the intake
            if(opStick.getRawButton(1) || opStick.getRawButton(2) || opStick.getRawButton(3) || opStick.getRawButton(4) || opStick.getRawAxis(3) < -1*deadBand)
            {
                toroPiston.set(true);
            }
            else
            {
                toroPiston.set(false);
            }
            if(driveStick.getRawButton(1))
            {
                compressor.start();
            }
            //Right catcher
            if(opStick.getRawButton(1) || opStick.getRawButton(3))
            {
                rightCatcher.set(true);
            }
            else
            {
                rightCatcher.set(false);
            }
            //Left catcher
            if(opStick.getRawButton(1) || opStick.getRawButton(2))
            {
                leftCatcher.set(true);
            }
            else
            {
                leftCatcher.set(false);
            }
            //Controlling the winch
            if(opStick.getRawAxis(3) > deadBand && armLimitSwitch.get() == false)
            {
                winch.set(1);
            }
            else if(opStick.getRawButton(5) && (winchEncoder.get() <= winchEnco0 + 18500))
            {
                winch.set(-1);
            }
            //Auto raising when the time is right
            else if(okToAutoRaise == true && ballSensor.get() == false && (winchEncoder.get() <= winchEnco0 + 18500) && teleopShoot.get() == true)
            {
                winch.set(1);
            }
            else
            {
                winch.stopMotor();
            }
            //Resets auto raise functionality
            if(okToAutoRaise == true && winchEncoder.get() >= winchEnco0 + 18250)
            {
                okToAutoRaise = false;
            }
            //Rolling the intake
            if(opStick.getRawAxis(3) < -1*deadBand || opStick.getRawButton(1) || opStick.getRawButton(2) || opStick.getRawButton(3))
            {
                intakeRight.set(1);
                intakeLeft.set(-1);
            }
            //Reversing the intake
            else if(opStick.getRawButton(6))
            {
                intakeRight.set(-1);
                intakeLeft.set(1);
            }
            else
            {
                intakeRight.stopMotor();
                intakeLeft.stopMotor();
            }
            //Shooting in teleop
            if(driveStick.getRawAxis(3) < -1*deadBand)
            {
                teleopShoot.set(false);
                autonShoot.set(true);
                hasShot = true;
            }
            else
            {
                teleopShoot.set(true);
            }
            //Switching to low gear
            if(driveStick.getRawAxis(3) > deadBand)
            {
                driveShifter.set(true);
            }
            else
            {
                driveShifter.set(false);
            }
        }            
    }
    public void test() 
    {
    }
}