package org.team3132.interfaces;

import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;

import org.strongback.Executable;
import org.strongback.components.Switch;
import org.strongback.components.ui.DirectionalAxis;

/**
 * This is the interface for the logging subsystem.
 * 
 * The logging subsystem has to implement two main functions:
 * 1) a text based log that allows us to log events, and
 * 2) a graphing log system that allows us to build a graph of tracked elements.
 * 
 * The text based log is a pure push log. Code calls its methods to write out events we wish to record.
 * 
 * The graphing log system has a set of elements registered with it, and will regularly query those
 * elements for their current values. These elements should be written out in such a way that they can be
 * viewed as a graph by the user.
 */

public interface Log extends Executable {
	/**
	 * Pause the writing of records to the graphing log. If the robot is quiescent writing out log records just
	 * takes up space and slows down the viewing of logs.
	 */
	public Log pauseGraphLog();
	
	/**
	 * resume logging after a pause.
	 */
	public Log resumeGraphLog();
		
//################     TXT file methods     ################################################################################
	
	/*
	 * Methods that handle writing to the .TXT log file. We allow freeform messages
	 * to be added to the log file. We precede each message with a time stamp and message type.
	 */
	
	/**
	 * Logs to the external logger only.  Generic log messages
	 * @param message Message to Log
	 * @param args arguments to the message format string
	 */
	public Log logMessage(String message, Object... args);
	
	/**
	 * Logs to the external logger only.  For high volume debug messages
	 * @param message Message to Log
	 * @param args arguments to the message format string
	 */
	public Log debug(String message, Object... args);
	
	/**
	 * Logs to the external logger and console.  For low volume informational messages (<1/sec).
	 * @param message Message to Log
	 * @param args arguments to the message format string
	 */
	public Log info(String message, Object... args);
	
	/**
	 * Logs to the external logger and console. For warning Messages.
	 * @param message Message to Log
	 * @param args arguments to the message format string
	 */
	public Log warning(String message, Object... args);
	
	/**
	 * Logs to the external logger and console. For important errors Messages.
	 * @param message Message to Log
	 * @param args arguments to the message format string
	 */
	public Log error(String message, Object... args);
	
	/**
	 * Logs Exceptions to the external logger and console.
	 * @param message Message to Log
	 * @param e Exception to log after message
	 */
	public Log exception(String message, Exception e);
	
	/**
	 * Implementation of the familiar println interface. A single string is passed, and is added to the log
	 * with a terminating new line.
	 * @param message Message to add to the log
	 */
	public Log println(String message);
	
	/**
	 * Logs to the external logger and console. For important console Messages.
	 * @param message Message to Log
	 * @param args arguments to the message format string
	 */
	public Log console(String message, Object... args);
	
	/**
	 * Logs to the external logger. For command execution Messages.
	 * @param message Message to Log
	 * @param args arguments to the message format string
	 */
	public Log cmd(String message, Object... args);
	
	/**
	 * Logs to the external logger and console. For important subsystem Messages.
	 * @param message Message to Log
	 * @param args arguments to the message format string
	 */
	public Log sub(String message, Object... args);
	
	/**
	 * Logs to the external logger and console. For important controller Messages.
	 * @param message Message to Log
	 * @param args arguments to the message format string
	 */
	public Log ctrl(String message, Object... args);
	
	//################     Log data collection methods     ################################################################################

	/**
	 * Register a data stream for collection to the log.
	 * The sample needs to have a method that returns a double which is the data element.
	 * The format (and args) are used to construct the name of the data stream.
	 * @param sample An object that returns a double value when called
	 * @param format a VarArgs string which evaluates to the name of the data stream
	 * @param args Any arguments required for the format varargs
	 * @return the log itself to allow chaining of the register methods.
	 */
	public default Log register(boolean local, DoubleSupplier sample, String format, Object... args) {
		return doRegister(local, sample, format, args);
	}
	
	/**
	 * Register a data stream for collection to the log.
	 * The sample needs to have a method that returns an integer which is the data element.
	 * The format (and args) are used to construct the name of the data stream.
	 * @param sample An object that returns an integer value when called
	 * @param format a VarArgs string which evaluates to the name of the data stream
	 * @param args Any arguments required for the format varargs
	 * @return the log itself to allow chaining of the register methods.
	 */
	
	public default Log register(boolean local, IntSupplier sample, String format, Object... args) {
		return doRegister(local, () -> (double)sample.getAsInt(), format, args);
	}

	/**
	 * Register a data stream for collection to the log.
	 * The sample needs to have a method that returns a DirectionalAxis which is the data element.
	 * The format (and args) are used to construct the name of the data stream.
	 * @param sample An object that returns a DirectionalAxis value when called
	 * @param format a VarArgs string which evaluates to the name of the data stream
	 * @param args Any arguments required for the format varargs
	 * @return the log itself to allow chaining of the register methods.
	 */
	public default Log register(boolean local, DirectionalAxis sample, String format, Object... args) {
		return doRegister(local, () -> sample.getDirection(), format, args);
	}
	
	/**
	 * Register a data stream for collection to the log.
	 * The sample needs to have a method that returns a Switch which is the data element.
	 * The format (and args) are used to construct the name of the data stream.
	 * @param sample An object that returns a Switch value when called
	 * @param format a VarArgs string which evaluates to the name of the data stream
	 * @param args Any arguments required for the format varargs
	 * @return the log itself to allow chaining of the register methods.
	 */
	public default Log register(boolean local, Switch sample, String format, Object... args) {
		return doRegister(local, () -> (double)(sample.isTriggered()?1:0), format, args);
	}
	
	/*
	 * Register the actual data stream. In preference one of the register() methods should be used
	 */
	public Log doRegister(boolean local, DoubleSupplier sample, String format, Object... args);
	
	/**
	 * Tells the logging subsystem that all registration events has occurred.
	 * Registration must be completed before we can start writing out logging records as the first record
	 * must be a heading record which requires all the elements to be queried 
	 */
	public Log logCompletedElements(String matchDescription);
	
	/**
	 * Flush any outstanding data for the log files
	 * @return this for chaining, never null.
	 */
	public Log flush();
}
