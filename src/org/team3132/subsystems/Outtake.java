package org.team3132.subsystems;

import org.strongback.Executable;
import org.strongback.components.Solenoid;
import org.strongback.components.TalonSRX;
import org.team3132.interfaces.DashboardInterface;
import org.team3132.interfaces.Log;
import org.team3132.interfaces.OuttakeInterface;
import org.team3132.lib.Subsystem;

import com.ctre.phoenix.motorcontrol.ControlMode;

/**
 * Subsystem responsible for the for the outtake
 */
public class Outtake extends Subsystem implements OuttakeInterface, Executable {
	
	private TalonSRX motor;
	private Solenoid solenoid;
	
	private double positioningPower = 0;
	private double outtakePower = 0;
	
	public Outtake(TalonSRX motor, Solenoid solenoid, DashboardInterface dashboard, Log log) {
		super("Outtake", dashboard, log);
		this.motor = motor;
		this.solenoid = solenoid;
		
		log.register(false, motor::getOutputCurrent, "%s/Current", name)
		   .register(false, motor::getMotorOutputVoltage, "%s/Voltage", name)
		   .register(false, motor::getMotorOutputPercent, "%s/Percent", name)
		   .register(true, () -> outtakePower, "%s/Power", name);
	}
	
	@Override
	public boolean isClosed() {
		return solenoid.isRetracted();
	}
	
	@Override
	public boolean isOpen() {
		return solenoid.isExtended();
	}
	
	/**
	 * @return true when finished
	 */
	@Override
	public boolean openOuttake() {
		solenoid.extend();
		return isOpen();
	}
	
	/**
	 * @return true when finished
	 */
	@Override
	public boolean closeOuttake() {
		solenoid.retract();
		return isClosed();
	}
	
	@Override
	public void setOuttakeMotorOutput(double power) {
		outtakePower = power;
		motor.set(ControlMode.PercentOutput, power);
	}
	
	@Override
	public double getOuttakeMotorOutput() {
		return outtakePower;
	}

	@Override
	public void updateDashboard(){
		dashboard.putNumber("Outtake power", positioningPower);
	}
}