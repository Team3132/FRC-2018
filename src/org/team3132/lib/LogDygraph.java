package org.team3132.lib;

import java.io.BufferedReader;

/*
 * This class controls all logging for the system.
 * 
 * It as two main functions:
 * 1) a textual log which we can append to easily and quickly, and
 * 2) a graphical log which subsystems have to register with and will be polled for current values for registered entries.
 * 
 * A series of files are created: They are created in LOG_BASE_PATH and sub directories LOG_BASE_DATA_PATH and LOG_BASE_DATE_PATH
 * The original files are created using an incrementing instance number (The current instance number is held in the base
 * directory of the log area). They are linked to using a time stamp.
 * 
 * We can pause, continue, or restart the logging.
 * restarting the logging causes the current files to be closed and a new set of files to be created.
 * 
 * For text logs we have a series of calls: 
 * log.{logMessage,debug,info,warning,error,ctrl,sub}(String message)
 * Each message takes a varargs argument list, and prepends the message with a timestamp and type of message.
 * 
 * For graphical logs the client class creates an arraylist of type LogGraphEntry and populates it with the entries that should be logged.
 * At regular intervals the logging system will walk through this list and obtain the current value for each entry.
 * These are added to the current graphical log file.
 */

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.function.DoubleSupplier;

import org.strongback.Executable;
import org.strongback.components.Clock;
import org.team3132.interfaces.Log;

/**
 * This is the log file data logger. We split the log into two separate log systems.
 * For the text file logging we use this method.
 * 
 * We create a single text log file <basePath>/<instance>.txt
 * and provide a variety of methods to append to that log file.
 */

public class LogDygraph implements Log, Executable {
	
	private enum GraphLogState {
		INVALID,				// File has not yet been created
		CREATED,				// File has been created, but we are waiting for all logging classes to be created
		CONFIGURED,				// Logging classes are all created and have registered with the logging subsystem
		ACTIVE,					// .html files have been populated and we are ready to write records into the .csv file.
		PAUSED,					// Graphical Logging has been paused as the robot isn't doing anything
		ERRORED					// a problem has occurred. Abandon trying to write.
	}
	
	private class LogGraphElement {
		public String name;
		public DoubleSupplier sample;
		
		public LogGraphElement(DoubleSupplier sample, String name, Object... args) {
			this.name = String.format(name, args);
			this.sample = sample;
		}
	}
	
	private final String basePath;  // Where the log files live.
	private final Path logInstancePath;  // Counter for log number.
	private long logFileNumber = 0;
	private final String dateDir;
	private final String dataDir;
	// Log files.
	private LogFileWriter csvWriter;
	private LogFileWriter logWriter;
	private LogFileWriter graphWriter;
	private LogFileWriter chartWriter;
	private LogFileWriter locationWriter;
	// Internal state.
	private GraphLogState graphLogState = GraphLogState.INVALID;
	private ArrayList<LogGraphElement> logGraphElements;	// list of registered graph elements
	private String matchDescription;
	private boolean createdDateFiles;
	private Clock clock;
	private boolean onlyLocal = false;	// only log locally defined elements.

	public LogDygraph(String basePath, String dataDir, String dateDir, Path logInstancePath, boolean onlyLocal, Clock clock) {
		this.basePath = basePath;
		this.dataDir = dataDir;
		this.dateDir = dateDir;
		this.logInstancePath = logInstancePath;
		this.clock = clock;		
		this.logGraphElements = new ArrayList<LogGraphElement>();
		this.onlyLocal = onlyLocal;
		createdDateFiles = false;
		
		try {
			// Ensure the directories exist.
			Files.createDirectories(getDataPath());
			Files.createDirectories(getDatePath());
			
			logFileNumber = getNextLogFileNumber();  // Different number each start.
			
			// Open all files. Also creates Latest symlink.
			csvWriter = new LogFileWriter("data", logFileNumber, "csv", basePath, dataDir);
			logWriter = new LogFileWriter("log", logFileNumber, "txt", basePath, dataDir);
			graphWriter = new LogFileWriter("graph", logFileNumber, "html", basePath, dataDir);
			chartWriter = new LogFileWriter("chart", logFileNumber, "html", basePath, dataDir);
			locationWriter = new LogFileWriter("location", logFileNumber, "html", basePath, dataDir);
			
			// Everything was successfully created, we're good to go.
			graphLogState = GraphLogState.CREATED;
		} catch (IOException e) {
			e.printStackTrace();
			System.out.printf("Failed to create log files in %s: %s\n", basePath, e.getMessage());
			graphLogState = GraphLogState.ERRORED;
		}
	}
	
	/**
	 * Create the date based symbolic links. These create symbolic links from date stamped
	 * version of the file to the actual file. This is a separate method as we delay
	 * creating these until we are reasonably sure the date is correct.
	 * @param timestamp The date to use.
	 */
	public void createDateFiles(Calendar timestamp) {
		String timestampStr = new SimpleDateFormat("yyyyMMdd_HH:mm:ss.SSS").format(timestamp.getTime());
		try {
			// Create links based on the timestamp.
			csvWriter.createSymbolicLink(dateDir, timestampStr);
			logWriter.createSymbolicLink(dateDir, timestampStr);
			graphWriter.createSymbolicLink(dateDir, timestampStr);
			chartWriter.createSymbolicLink(dateDir, timestampStr);
			locationWriter.createSymbolicLink(dateDir, timestampStr);
			// And on event name, match type, match number, replay number, alliance and position.
			// These details should be available at the same time now that the drivers station is
			// able to talk to the robot.
			csvWriter.createSymbolicLink(dateDir, matchDescription);
			logWriter.createSymbolicLink(dateDir, matchDescription);
			graphWriter.createSymbolicLink(dateDir, matchDescription);
			chartWriter.createSymbolicLink(dateDir, matchDescription);
			locationWriter.createSymbolicLink(dateDir, matchDescription);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.printf("Error creating symlinks in %s\n", basePath);
		}
	}
	
	private void initGraphFile(String csvColumns) {
		/*
		 * Create the html file for dygraph.
		 * We can only do this once the logging classes have all been instantiated.
		 */
		String title = "Instance " + logFileNumber;
		String file = String.format("data_%05d", logFileNumber);
		writeMessageToFile(graphWriter, String.format(
				"<html>\n" +
				"<head><title>%1$s</title>\n" +
				"<script type='text/javascript' src='/scripts/dygraph-combined.js'></script>\n" +
				"</head>\n" +
				"<body onload='checkCookie()'>\n" +
				"<div id='optionDiv' style='width:100%%;height:100%%;background-color:#f0f0f0;z-index:30;display:none;position:absolute'>\n" +
				"<div id='optionHTML'>\n" +
				"</div>\n" +
				"<button id='b1' onclick='button_set_all()'>Set All</button>\n" +
				"<button id='b2' onclick='button_clear_all()'>Clear All</button>\n" +
				"<button id='b3' onclick='button_set_visible()'>Done</button>\n" +
				"<p>\n" +
				"<button id='b7' onclick='doShowAllCascade()'>Open All</button>\n" +
				"<button id='b8' onclick='doHideAllCascade()'>Collapse All</button>\n" +
				"</div>\n" +
				"<div id='graphdiv' style='width:100%%;height:90%%;display:block'></div>\n" +
				"<script>\n" +
				"var fn = '/%4$s/%2$s.csv';\n" +
				"var baseLabelsStr = '%3$s';\n" +
				"</script>\n" +
				"<script type='text/javascript' src='/scripts/dygraph-exts.js'></script>\n" +
				"<button id='b4' onclick='button_select()'>Select Traces</button>\n" +
				"<button id='b5' onclick='button_reload()'>Reload Traces</button>\n" +
				"<button id='b6' onclick='button_remaining_visible()' disabled>All Traces Shown</button>\n" +
				"</body>\n", title, file, csvColumns, dataDir));
		try {
			if (graphWriter != null) {
				graphWriter.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void initChartFile(String csvColumns) {
		/*
		 * Create the html file for the ploty chart.
		 */
		String title = "Run " + logFileNumber;
		String file = String.format("data_%05d", logFileNumber);
		writeMessageToFile(chartWriter, String.format(
                "<html>\n" + 
                "<head><title>%1$s plot.ly chart</title>\n" +
                "</head>\n" + 
                "<body>\n" + 
                "<script>\n" + 
				"var fn = '/%4$s/%2$s.csv';\n" +
				"var baseLabelsStr = '%3$s';\n" +
                "</script>\n" + 
                "<script src='/scripts/plotly.js'></script><script src='/scripts/plotly-ext.js'></script>\n" + 
                "<div id='chart1' style='width:100%%;height:90%%;'><!-- Plotly chart will be drawn inside this DIV --></div>\n" + 
                "<script> loadPlotlyFromCSV('%1$s', fn, 0);\n" + 
                "</script>\n\n" + 
                "<p>\n" + 
                "Click on the series in the legend to swap y-axis and then to turn off.\n" + 
                "<p>\n" + 
                "Trying to run this locally? Run the following in the directory containing this file:\n" + 
                "<p>\n" + 
                "<pre>\n" + 
                " python -m SimpleHTTPServer\n" + 
                "</pre>\n" + 
                "<p>\n" + 
                "Then go to <a href='http://localhost:8000/Latest_chart.html'>http://localhost:8000/Latest_chart.html</a>\n" + 
                "</body>\n", title, file, csvColumns, dataDir));
		try {
			if (chartWriter != null) {
				chartWriter.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void initCSVFile(String csvColumns) {
		/*
		 * Write the heading out to the CSV file.
		 */
		writeMessageToFile(csvWriter, csvColumns + "\n");
	}
	
	private void initLocationPlotFile() {
		/*
		 * Create the html file for the location plot.
		 * We can only do this once the logging classes have all been instantiated.
		 */
		String title = "Instance " + logFileNumber;
		String file = String.format("data_%05d", logFileNumber);
		writeMessageToFile(locationWriter, String.format(
				"<html><title>%1$s</title><head>\n" +
				"<script src='/scripts/plotly.js'></script><script src='/scripts/plotLocation.js'></script>\n" +
				"<body><div id='myDiv' style='width: 480px; height: 400px;'>\n" +
				"<!-- Plotly chart will be drawn inside this DIV --></div>\n" +
				"<script> makeplot('/%3$s/%2$s.csv');\n" +
				"</script>" +
                "<p>\n" + 
                "Trying to run this locally? Run the following in the directory containing this file:\n" + 
                "<p>\n" + 
                "<pre>\n" + 
                " python -m SimpleHTTPServer\n" + 
                "</pre>\n" + 
                "<p>\n" + 
                "Then go to <a href='http://localhost:8000/Latest_chart.html'>http://localhost:8000/Latest_chart.html</a>\n" + 
				"</body>\n", title, file, dataDir));
		try {

			if (locationWriter != null) {
				locationWriter.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Log pauseGraphLog() {
		if (graphLogState == GraphLogState.ACTIVE) {
			graphLogState = GraphLogState.PAUSED;
		}
		return this;
	}
	
	public Log resumeGraphLog() {
		if (graphLogState == GraphLogState.PAUSED) {
			graphLogState = GraphLogState.ACTIVE;
		}
		return this;
	}
		
//################     TXT file methods     ################################################################################
	
	/*
	 * Methods that handle writing to the .TXT log file. We allow freeform messages
	 * to be added to the log file. We precede each message with a time stamp and message type.
	 */
	
	private void writeLogMessage(String message) {
		writeMessageToFile(logWriter, message);
	}
	
	/**
	 * Logs to the external logger only.  Generic log messages
	 * @param message Message to Log
	 * @param args arguments to the message format string
	 */
	public Log logMessage(String message, Object... args) {
		String date = timeToLogString(getCurrentTime());
		message = String.format(date + ",L " + message + "\n", args);
		writeLogMessage(message);
		return this;
	}
	
	/**
	 * Logs to the external logger only.  For high volume debug messages
	 * @param message Message to Log
	 * @param args arguments to the message format string
	 */
	public Log debug(String message, Object... args) {
		String date = timeToLogString(getCurrentTime());
		message = String.format(date + ",D " + message + "\n", args); 
		// Don't print it to the console.
		writeLogMessage(message);
		return this;
	}
	
	/**
	 * Logs to the external logger and console.  For low volume informational messages (<1/sec).
	 * @param message Message to Log
	 * @param args arguments to the message format string
	 */
	public Log info(String message, Object... args) {
		String date = timeToLogString(getCurrentTime());
		message = String.format(date + ",I " + message + "\n", args); 
		// Print to the console.
		System.out.print(message);
		writeLogMessage(message);
		return this;
	}
	
	/**
	 * Logs to the external logger and console. For warning Messages.
	 * @param message Message to Log
	 * @param args arguments to the message format string
	 */
	public Log warning(String message, Object... args) {
		String date = timeToLogString(getCurrentTime());
		message = String.format(date + ",W " + message + "\n", args); 
		// Print to the console.
		System.err.print(message);
		writeLogMessage(message);
		return this;
	}
	
	/**
	 * Logs to the external logger and console. For important errors Messages.
	 * @param message Message to Log
	 * @param args arguments to the message format string
	 */
	public Log error(String message, Object... args) {
		String date = timeToLogString(getCurrentTime());
		message = String.format(date + ",E " + message + "\n", args); 
		// Print to the console.
		System.err.print(message);
		writeLogMessage(message);
		return this;
	}

	/**
	 * Logs Exceptions to the external logger and console.
	 * @param message Message to Log
	 * @param e Exception to log after message
	 */
	@Override
	public Log exception(String message, Exception e) {
		error(message + ": %s", e);
		for (StackTraceElement frame : e.getStackTrace()) {
			error("     at %s", frame.toString());			
		}
		return this;
	}
	
	/**
	 * Implements the classic println interface. Single string to the console!
	 * @param message Message to Log
	 */
	public Log println(String message) {
		console(message);
		return this;
	}
	
	/**
	 * Logs to the external logger and console. For important console Messages.
	 * @param message Message to Log
	 * @param args arguments to the message format string
	 */
	public Log console(String message, Object... args) {
		String date = timeToLogString(getCurrentTime());
		message = String.format(date + ",O " + message + "\n", args); 
		// Print to the console.
		System.out.print(message);
		writeLogMessage(message);
		return this;
	}
	
	/**
	 * Logs to the external logger. For command execution Messages.
	 * @param message Message to Log
	 * @param args arguments to the message format string
	 */
	public Log cmd(String message, Object... args) {
		String date = timeToLogString(getCurrentTime());
		message = String.format(date + ",C " + message + "\n", args); 
		writeLogMessage(message);
		return this;
	}
	
	/**
	 * Logs to the external logger and console. For important subsystem Messages.
	 * @param message Message to Log
	 * @param args arguments to the message format string
	 */
	public Log sub(String message, Object... args) {
		String date = timeToLogString(getCurrentTime());
		message = String.format(date + ",S " + message + "\n", args); 
		// Print to the console.
//		System.out.print(message);
		writeLogMessage(message);
		return this;
	}
	
	/**
	 * Logs to the external logger and console. For important subsystem Messages.
	 * @param message Message to Log
	 * @param args arguments to the message format string
	 */
	public Log ctrl(String message, Object... args) {
		String date = timeToLogString(getCurrentTime());
		message = String.format(date + ",T " + message + "\n", args); 
		// Print to the console.
//		System.out.print(message);
		writeLogMessage(message);
		return this;
	}
	
	//################     Graphing file methods     ################################################################################

	@Override
	public Log doRegister(boolean local, DoubleSupplier sample, String format, Object... args) {
		if (onlyLocal && !local) {
			return this;
		}
		if (graphLogState == GraphLogState.ERRORED) {
			// the log system is very broken, possibly because of a faulty USB
			return this;
		}
		if (graphLogState != GraphLogState.CREATED) {
			error("trying to add Graph elements after graph configured" + graphLogState);
			try {
				throw new Exception();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			logGraphElements.add(new LogGraphElement(sample, String.format(format, args)));
		}
		return this;
	}
	
	@Override
	public Log logCompletedElements(String matchDescription) {
		this.matchDescription = matchDescription;
		System.out.println("LogCompletedElements for match " + matchDescription);
		graphLogState = GraphLogState.CONFIGURED;
		this.debug("Log: Element additions completed");
		return this;
	}

	public String getGraphHeaders() {
		String headers = "date";
		for (LogGraphElement e: logGraphElements) {
			if (e.name != null) {
				headers = headers + "," + e.name;
			}
		}
		return headers;
	}
	
	public String getGraphValues() {
		String value = timeToLogString(getCurrentTime());
		for (LogGraphElement e: logGraphElements) {
			if (e.name != null) {
				value = value + "," + e.sample.getAsDouble();
			}
		}
		value = value + "\n";
		return value;
	}
		
	//################     Support file methods     ################################################################################
	/*
	 * These are support methods for the class. They provide useful
	 * functionality but we separate them here for ease of reading above.
	 */
	private String timeToLogString(double time) {
		/*
		 * Create the timestamp for this message. We use the robot time, so each
		 * log entry is time stamped for when it happened during the match.
		 * Question: Should we document dygraphs limits here on the limit(3?) of
		 * the number of decimal places in the timestamp?
		 */
		return String.format("%.3f", time);
	}

	private Path getDataPath() {
		return Paths.get(basePath, dataDir);
	}

	private Path getDatePath() {
		return Paths.get(basePath, dateDir);
	}

	private synchronized long getNextLogFileNumber() {
		/*
		 * Get the next Log File number. We read the logInstancePath file.
		 * Increment the number contained within the file (or create the Log
		 * File as 1 if no such file exists) and write the log file number back
		 * out.
		 */
		long logFileNumber = 0;
		
		try {
			BufferedReader br = Files.newBufferedReader(logInstancePath);
			String s = br.readLine(); // read the line into the buffer.
			logFileNumber = Integer.parseInt(s);
			br.close();
			logFileNumber++;
		} catch (IOException | NumberFormatException e) {
			System.out.println("Cannot read Log Number file. Resetting number to 1");
			logFileNumber = 1;
		}
		try {
			BufferedWriter bw = Files.newBufferedWriter(logInstancePath);
			bw.write("" + logFileNumber + "\n");
			bw.flush();
			bw.close();
			System.out.printf("Wrote to %s %d\n", logInstancePath, logFileNumber);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Cannot write log number file. Possible old file overwrite.");
		}
		return logFileNumber;
	}
	
	private double getCurrentTime()
	{
		return clock.currentTime();
	}

	private void writeMessageToFile(LogFileWriter file, String message) {
		/*
		 * Writes the message to the file (if it is open).
		 */
		if (file == null)
			return; // File logging not enabled.
		try {
			file.write(message);
		} catch (Exception e) {
			// nothing to do. If we can't write to the log file it's not a disaster.
		}
	}

	/**
	 * execute is called periodically
	 * This is the routine that performs the actual work for the graphing logs.
	 */
	@Override
	public void execute(long timeInMillis) {
		if (graphLogState == GraphLogState.CONFIGURED) {
			String csvColumns = getGraphHeaders();
			initGraphFile(csvColumns);
			initChartFile(csvColumns);
			initCSVFile(csvColumns);
			initLocationPlotFile();
			graphLogState = GraphLogState.ACTIVE;
		}
		if (graphLogState == GraphLogState.ACTIVE) {
			writeMessageToFile(csvWriter, getGraphValues());
			if (!createdDateFiles) {
				Calendar now = Calendar.getInstance();
				/*
				 * we may now have access to the correct date and time. If this is the first
				 * time through we should set the date version of the file links.
				 */
				if (now.get(Calendar.YEAR) >= 2017) {
					createDateFiles(now);
					createdDateFiles = true;
				}
			}
		}
	}

	@Override
	public Log flush() {
		csvWriter.flush();
		logWriter.flush();
		graphWriter.flush();
		chartWriter.flush();
		locationWriter.flush();
		return this;
	}
}
