/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package org.usfirst.frc.team6023.robot;

import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.GenericHID.Hand;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.Timer;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.properties file in the
 * project.
 */
public class Robot extends IterativeRobot {
	private static final String kDefaultAuto = "Default";
	private static final String kCustomAuto = "My Auto";
	private String m_autoSelected;
	private SendableChooser<String> m_chooser = new SendableChooser<>();
	
	// Controller 
	private XboxController driveXbox, liftXbox;

	// Motors
	private Spark leftDrive1, leftDrive2, rightDrive1, rightDrive2;
	private SpeedControllerGroup leftDrive, rightDrive, lift;
	private Spark gripperLifter;
	private Spark liftMotor12, liftMotor11;
	
	private DoubleSolenoid gripper;
	private CameraServer camera;
	
	private Compressor comp;
	// Drive
	private DifferentialDrive drive;

	/**
	 * This function is run when the robot is first started up and should be
	 * used for any initialization code.
	 */
	@Override
	public void robotInit() {
		m_chooser.addDefault("Default Auto", kDefaultAuto);
		m_chooser.addObject("My Auto", kCustomAuto);
		SmartDashboard.putData("Auto choices", m_chooser);
		
		//Set up joy1 on port 0
		driveXbox = new XboxController(RobotMap.driveXbox);
		liftXbox = new XboxController(RobotMap.liftXbox);
		
		comp = new Compressor();
		comp.setClosedLoopControl(true);

		leftDrive1 = new Spark(RobotMap.leftMotor1);
		leftDrive2 = new Spark(RobotMap.leftMotor2);
		rightDrive1 = new Spark(RobotMap.rightMotor1);
		rightDrive2 = new Spark(RobotMap.rightMotor2);
		
		leftDrive = new SpeedControllerGroup(leftDrive1, leftDrive2);
		leftDrive.setInverted(false);
		rightDrive = new SpeedControllerGroup(rightDrive1, rightDrive2);
		rightDrive.setInverted(false);

		gripperLifter = new Spark (RobotMap.gripperLifter);
		liftMotor12= new Spark(RobotMap.lifterMotor12);
		liftMotor11 = new Spark(RobotMap.lifterMotor11);
		lift = new SpeedControllerGroup(liftMotor11, liftMotor12);
		
		gripper = new DoubleSolenoid(RobotMap.gripperForward, RobotMap.gripperReverse);
		
		drive = new DifferentialDrive(leftDrive, rightDrive);
		drive.setSafetyEnabled(true);
		
		camera = CameraServer.getInstance();
		camera.startAutomaticCapture(0);
	}

	/**
	 * This autonomous (along with the chooser code above) shows how to select
	 * between different autonomous modes using the dashboard. The sendable
	 * chooser code works with the Java SmartDashboard. If you prefer the
	 * LabVIEW Dashboard, remove all of the chooser code and uncomment the
	 * getString line to get the auto name from the text box below the Gyro
	 *
	 * <p>You can add additional auto modes by adding additional comparisons to
	 * the switch structure below with additional strings. If using the
	 * SendableChooser make sure to add them to the chooser code above as well.
	 */
	@Override
	public void autonomousInit() {
		m_autoSelected = m_chooser.getSelected();
		// autoSelected = SmartDashboard.getString("Auto Selector",
		// defaultAuto);
		System.out.println("Auto selected: " + m_autoSelected);
		
		drive.setSafetyEnabled(false);
		drive.tankDrive(-0.75, -0.75);
		Timer.delay(3.2);
		drive.tankDrive(0, 0);
		drive.setSafetyEnabled(true);
	}

	/**
	 * This function is called periodically during autonomous.
	 */
	@Override
	public void autonomousPeriodic() {
		switch (m_autoSelected) {
			case kCustomAuto:
				// Put custom auto code here
				break;
			case kDefaultAuto:
			default:
				// Put default auto code here
				break;
		}
	}
	
	double driveThrottle, driveSteering;

	/**
	 * This function is called periodically during operator control.
	 */
	@Override
	public void teleopPeriodic() {

		driveSteering = driveXbox.getX(GenericHID.Hand.kLeft);
		driveThrottle = driveXbox.getTriggerAxis(GenericHID.Hand.kRight) + (-(driveXbox.getTriggerAxis(GenericHID.Hand.kLeft)));
		
		drive.arcadeDrive(driveThrottle, driveSteering);
		
		//Winch Down
		if(liftXbox.getBumper(GenericHID.Hand.kLeft) && !liftXbox.getBumper(GenericHID.Hand.kRight)){
			lift.set(-0.3);
		} //Winch Up
		else if(liftXbox.getBumper(GenericHID.Hand.kRight) && !liftXbox.getBumper(GenericHID.Hand.kLeft)){
			lift.set(0.5);
		} //Winch Stop
		else{
			lift.set(0);
		}

		//Gripper Up
		if(driveXbox.getY(GenericHID.Hand.kRight) <= 0.07){
			gripperLifter.set(-0.1 * driveXbox.getY(GenericHID.Hand.kRight));
		} //Gripper Down
		else if(driveXbox.getY(GenericHID.Hand.kRight) >= 0.07){
			gripperLifter.set(driveXbox.getY(GenericHID.Hand.kRight));
		} //Gripper Stop
		else{
			gripperLifter.set(0);
		}
		
		//Gripper Toggle
		if(driveXbox.getAButton()){
			while(driveXbox.getAButton()){};
			if(gripper.get() == DoubleSolenoid.Value.kForward){
				gripper.set(DoubleSolenoid.Value.kReverse);
			}
			else{
				gripper.set(DoubleSolenoid.Value.kForward);
			}
		}
	}

	/**
	 * This function is called periodically during test mode.
	 */
	@Override
	public void testPeriodic() {
	}
}
