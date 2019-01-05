package org.team3132.subsystems;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import org.junit.Test;
import org.strongback.mock.Mock;
import org.strongback.mock.MockClock;
import org.team3132.interfaces.DashboardInterface;
import org.team3132.interfaces.LocationInterface;
import org.team3132.interfaces.Log;
import org.team3132.interfaces.VisionInterface.TargetDetails;
import org.team3132.lib.Position;
import org.team3132.mock.MockDashboard;
import org.team3132.mock.MockLocation;
import org.team3132.mock.MockLog;

public class TestVision {
	
	final double kProcessingTime = 0.1;  // processing time.
	
    @Test
    public void testVision() throws UnknownHostException, IOException {
    	System.out.println("testVision()");
    	MockClock clock = Mock.clock();
    	Log log = new MockLog(true);
    	// Historic locations are based on the time it is requested at.
        LocationInterface location = new MockLocation();

        DashboardInterface dashboard = new MockDashboard();
        // Listen on a port assigned by the operating system.
        Vision vision = new Vision(0, location, dashboard, clock, log);
        // Shouldn't have a target lock.
        assertThat(vision.getTargetDetails().targetFound, is(equalTo(false)));
        // Connect with a client.
        MockVisionClient client = new MockVisionClient(vision.getPort());
        // Shouldn't have a target lock.
        assertThat(vision.getTargetDetails().targetFound, is(equalTo(false)));
        // Send a line without a target lock.
        client.sendLine(false, 0, 0, kProcessingTime);
        // Shouldn't have a target lock.
        assertThat(vision.getTargetDetails().targetFound, is(equalTo(false)));
        // Tell the client to give up and reconnect
        client.disconnect();
        clock.incrementBySeconds(1);
        client = new MockVisionClient(vision.getPort());

        // Send a line with a target lock.
        final double distance = 10;
        final double angle = 15;
        client.sendLine(true, distance, angle, kProcessingTime);
        double lockTime = clock.currentTime() - kProcessingTime;
        sleep(1); // Let the other thread process it.
        Position robotPos = location.getHistoricalLocation(lockTime);
        Position expectedLocation = robotPos.addVector(distance, angle);
        // Should have a lock
        TargetDetails details = vision.getTargetDetails();
        assertThat(details.targetFound, is(equalTo(true)));
        assertThat(details.seenAtSec, is(equalTo(lockTime)));
        assertThat(details.location, is(equalTo(expectedLocation)));

        // Send a line without target lock. It should be ignored by the subsystem.
        clock.incrementBySeconds(1);
        client.sendLine(false, 0, 0, kProcessingTime);
        // Should still have the same lock position as before.
        details = vision.getTargetDetails();
        assertThat(details.targetFound, is(equalTo(true)));
        assertThat(details.seenAtSec, is(equalTo(lockTime)));
        assertThat(details.location, is(equalTo(expectedLocation)));
        
        // Send garbage, shouldn't kill the subsystem.
        client.sendLine("garbage");
        // Need to reconnect.
        client = new MockVisionClient(vision.getPort());

        // Send a real update
        clock.incrementBySeconds(1);
        client.sendLine(true, distance, angle, kProcessingTime);
        lockTime = clock.currentTime() - kProcessingTime;
        sleep(1); // Let the other thread process it.
        robotPos = location.getHistoricalLocation(lockTime);
        expectedLocation = robotPos.addVector(distance, angle);
        // Should have a lock
        details = vision.getTargetDetails();
        assertThat(details.targetFound, is(equalTo(true)));
        assertThat(details.seenAtSec, is(equalTo(lockTime)));
        assertThat(details.location, is(equalTo(expectedLocation)));
    }

    /**
     * Pretends to be an external box connecting to the robot via TCP.
     */
    private class MockVisionClient {
		protected Socket socket;
		protected PrintWriter out;
    	public MockVisionClient(int port) throws UnknownHostException, IOException {
			socket = new Socket("localhost", port);
			out = new PrintWriter(socket.getOutputStream(), true);
		}
    	
    	public void disconnect() {
    		try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    	
    	public void sendLine(boolean found, double degrees, double distance, double secsAgo) {
    		sendLine(String.format("%s,%f,%f,%f", found?"1":"0", distance, degrees, secsAgo));
    	}
    	
    	public void sendLine(String line) {
    		out.println(line);
    	}    	
    }
    
    private void sleep(double seconds) {
    	try {
			Thread.sleep((long) (seconds * 1000));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }
}
