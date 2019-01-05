package org.team3132.mock;

import java.util.function.DoubleSupplier;

import org.team3132.interfaces.Log;

public class MockLog implements Log {

	protected boolean logToStdOut = false;
	
	public MockLog() {
		this(false);
	}
	
	public MockLog(boolean logToStdOut) {
		this.logToStdOut = logToStdOut;
	}
	@Override
	public Log pauseGraphLog() {
		return this;
	}

	@Override
	public Log resumeGraphLog() {
		return this;
	}

	@Override
	public synchronized Log logMessage(String message, Object... args) {
		if (!logToStdOut) return this;
		System.out.printf(message, args);
		System.out.println();
		return this;
	}

	@Override
	public Log debug(String message, Object... args) {
		logMessage(message, args);
		return this;
	}

	@Override
	public Log info(String message, Object... args) {
		logMessage(message, args);
		return this;
	}

	@Override
	public Log warning(String message, Object... args) {
		logMessage(message, args);
		return this;
	}

	@Override
	public Log error(String message, Object... args) {
		logMessage(message, args);
		return this;
	}

	@Override
	public Log exception(String message, Exception e) {
		error(message + ": %s", e);
		for (StackTraceElement frame : e.getStackTrace()) {
			error("     at %s", frame.toString());			
		}
		return this;
	}

	@Override
	public Log println(String message) {
		logMessage(message);
		return this;
	}

	@Override
	public Log console(String message, Object... args) {
		logMessage(message, args);
		return this;
	}

	@Override
	public Log cmd(String message, Object... args) {
		logMessage(message, args);
		return this;	
	}

	@Override
	public Log sub(String message, Object... args) {
		logMessage(message, args);
		return this;
	}

	@Override
	public Log ctrl(String message, Object... args) {
		logMessage(message, args);
		return this;
	}

	@Override
	public Log logCompletedElements(String matchDescription) {
		return this;
	}

	@Override
	public Log doRegister(boolean local, DoubleSupplier sample, String format, Object... args) {
		return this;
	}

	@Override
	public void execute(long timeInMillis) {
	}
	
	public Log regiser(boolean local, DoubleSupplier sample, String format, Object... args) {
		return doRegister(local, sample, format, args);
	}

	@Override
	public Log flush() {
		return this;
	}
}
