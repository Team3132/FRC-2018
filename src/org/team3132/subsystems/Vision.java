package org.team3132.subsystems;

/*
 * The vision subsystem runs a server that is connected to by the vision processing external processor.
 * 
 * It runs a thread that processes status message lines as they are read from the external processor.
 * The thread also sends target selection requests to the external processor in case there are multiple cameras available.
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.regex.Pattern;

import org.strongback.components.Clock;
import org.team3132.interfaces.DashboardInterface;
import org.team3132.interfaces.DashboardUpdater;
import org.team3132.interfaces.LocationInterface;
import org.team3132.interfaces.Log;
import org.team3132.interfaces.VisionInterface;
import org.team3132.lib.Position;
import org.team3132.lib.Subsystem;

public class Vision extends Subsystem implements VisionInterface, DashboardUpdater {
	private LocationInterface location;
	private boolean hasConnection = false;
	private Clock clock;
	private Server server;
	private TargetDetails lastSeenTarget = new TargetDetails();
	private double lastContactTime = -1;
	
	public Vision(int port, LocationInterface location, DashboardInterface dashboard, Clock clock, Log log) {
		super("Vision", dashboard, log);
		this.location = location;
		this.clock = clock;
    	try {
			server = new Server(port);
	        server.start();  // Start listen socket.
		} catch (IOException e) {
			// Likely there was something already using the port.
			log.error("Failed to start vision subsystem: %s", e.getMessage());
			e.printStackTrace();
		}
		log.register(true, () -> hasConnection, "%s/hasConnection", name)
		    .register(true, () -> lastSeenTarget.location.x, "%s/curX", name)
			.register(true, () -> lastSeenTarget.location.y, "%s/curY", name)
			.register(true, () -> lastSeenTarget.location.heading, "%s/curA", name)
			.register(true, () -> clock.currentTime() - lastSeenTarget.seenAtSec, "%s/seenAt", name)
			.register(true, () -> clock.currentTime() - lastContactTime, "%s/lastContactTime", name);
	}
	
	@Override
	public boolean isConnected() {
		return hasConnection;
	}
	
	public int getPort() {
		return server.getPort();
	}
	
	/**
	 * Parses a line from the vision and calculates the target position on the field based
	 * on where the robot was at that time.
	 * Line format: <found>,<degrees>,<distance>,<skew>,<secsAgo>\n
	 * Where:
	 *   found is either "0": not found, "1": found
	 *   degrees is the angle from the middle of the camera.
	 *   distance is in inches from the camera.
	 *   secsAgo is how long ago it was seen. This is the processing time from when the image was taken.
	 */
	private void processLine(String line) {
		log.sub("Vision::processLine(%s)\n", line);
		if (line == null) return;		// Just in case the camera script drops the connection
		String[] parts = line.split(Pattern.quote(","));
        boolean targetFound = Double.parseDouble(parts[0]) == 1; 		// if the vision script has seen a suitable target recently
		if (!targetFound) return; // No point in processing any more details, keep any stale info.
		double angleDegrees = Double.parseDouble(parts[1]); // angle of the camera relative to the goal
		double distanceInches = Double.parseDouble(parts[2]); // distance of the camera relative to the goal
		double seenAtSec = clock.currentTime() - Double.parseDouble(parts[3]); // when the image was taken
		// A target was seen, update the TargetDetails in case it's asked for.
		// Fill in a new TargetDetails so it can be returned if asked for and it won't change as the
		// caller uses it.
		TargetDetails lastestTargetSeen = new TargetDetails();
		Position robotPosition = location.getHistoricalLocation(seenAtSec);
		lastestTargetSeen.location = robotPosition.addVector(distanceInches, angleDegrees);
		lastestTargetSeen.targetFound = targetFound;
		lastestTargetSeen.seenAtSec = seenAtSec;
		synchronized (this) {
			lastSeenTarget = lastestTargetSeen;
		}
		log.sub("Vision: Updated target %s", lastSeenTarget);
	}

	/**
	 * Return the details of the last target seen. Let the caller decide
	 * if the data is too old/stale.
	 */
	@Override
	public synchronized TargetDetails getTargetDetails() {
		return lastSeenTarget;
	}
	
	/**
	 * Background thread to listen for TCP socket connections from the vision
	 * processor. Complete lines are handed to parent class.
	 */
	private class Server extends Thread {
		private Socket conn = null;
		private BufferedReader input;
		private ServerSocket serverSocket;
		
		public Server(int port) throws IOException {
		    serverSocket = new ServerSocket();
		    serverSocket.setReuseAddress(true);
			serverSocket.bind(new InetSocketAddress(port));
		}
		
		public int getPort() {
			return serverSocket.getLocalPort();
		}
			
		/**
		 * Main loop.
		 */
		@Override
		public void run() {
			while (true) {
				try {
					log.info("%s: Waiting for a connection from the vision processor", name);
					try {
						conn = serverSocket.accept();
						conn.setTcpNoDelay(true); // Don't wait for large packets to reduce delay.
						input = new BufferedReader(new InputStreamReader(conn.getInputStream()));
					} catch (IOException e) {
						log.error("%s: error accepting a connection from serverSocket: %s", name, e);
						continue; // Go back and try again for a new connection.
					}
					hasConnection = true;
					log.info("%s: Accepted a connection from a vision processor", name);
					// output = new PrintStream(conn.getOutputStream());
					while (true) {
						// This will block if there isn't a line available.
						String line = input.readLine();
						if (line == null) break;
						processLine(line);
					}
				} catch (Exception e) {
					hasConnection = false;
					if (conn != null) {
						try {
							conn.close();
						} catch (IOException e1) {}
					}
					conn = null;
					log.exception("Vision: Error", e);
				}
			}
		}
	}
	
	@Override
	public void updateDashboard() {
		boolean targetFound = lastSeenTarget.targetFound;
		double lockAgeSec = clock.currentTime() - lastSeenTarget.seenAtSec;
		if (lockAgeSec > 2) targetFound = false;
		double angle = 0, distance = 0;
		if (targetFound) {
			Position robotPos = location.getCurrentLocation();
			// Where is the target relative to the current robot position?
			Position relativePos = robotPos.getRelativeToLocation(lastSeenTarget.location);
			angle = relativePos.heading;
			distance = robotPos.distanceTo(lastSeenTarget.location);
		}
		dashboard.putBoolean("Vision lock", targetFound);
		dashboard.putNumber("Vision lock age", lockAgeSec);
		dashboard.putNumber("Vision last contact", clock.currentTime() - lastContactTime);
		dashboard.putNumber("Vision X", lastSeenTarget.location.x);
		dashboard.putNumber("Vision Y", lastSeenTarget.location.y);
		dashboard.putNumber("Vision angle to target", angle);
		dashboard.putNumber("Vision distance to target", distance);
	}
}
