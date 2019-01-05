package org.team3132.lib;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.junit.Before;
import org.junit.Test;

public class TestLogFileWriter {
	protected Path tempDir;
	
	@Before
	public void setUp() throws IOException {
		tempDir = Files.createTempDirectory("TestLogFileWriter");
	}
	
	/**
	 * Check the relative path function for a Latest link.
	 */
	@Test
	public void createLatestSymbolicLink() {
		Path to = Paths.get("/media/sda1/data/data_007.csv");
		Path symlinkPath = Paths.get("/media/sda1/Latest_data.csv");
		Path expected = Paths.get("data/data_007.csv");
		assertThat(expected, is(equalTo(LogFileWriter.createRelativePath(symlinkPath, to))));
	}
	
	/**
	 * Check the relative path function for a date link.
	 */
	@Test
	public void createDatesSymbolicLink() {
		Path to = Paths.get("/media/sda1/data/data_007.csv");
		Path symlinkPath = Paths.get("/media/sda1/dates/20180303_data_007.csv");
		Path expected = Paths.get("../data/data_007.csv");
		assertThat(expected, is(equalTo(LogFileWriter.createRelativePath(symlinkPath, to))));
	}	
	
	/**
	 * Check file is written safely to disk.
	 * @throws IOException 
	 */
	@Test
	public void simpleLogFileWriter() throws IOException {
		LogFileWriter writer = new LogFileWriter("name", 0, "extn", tempDir.toString(), "data");
		writer.write("Hello\nworld!");
		writer.createSymbolicLink("date", "20180303");
		writer.close();
		// Check the data file is where it is expected and it contains all the data.
		{
			Path expectedFile = Paths.get(tempDir.toString(), "data", "name_00000.extn");
			BufferedReader br = Files.newBufferedReader(expectedFile);
			assertThat("Hello", is(equalTo(br.readLine())));
			assertThat("world!", is(equalTo(br.readLine())));
			br.close();
		}
		// Check that the "Latest" symbolic link can be read.
		{
			Path expectedFile = Paths.get(tempDir.toString(), "Latest_name.extn");
			BufferedReader br = Files.newBufferedReader(expectedFile);
			assertThat("Hello", is(equalTo(br.readLine())));
			assertThat("world!", is(equalTo(br.readLine())));
			br.close();
		}
		// Check that the "date/20180303" symbolic link can be read.
		{
			Path expectedFile = Paths.get(tempDir.toString(), "date", "20180303_name.extn");
			BufferedReader br = Files.newBufferedReader(expectedFile);
			assertThat("Hello", is(equalTo(br.readLine())));
			assertThat("world!", is(equalTo(br.readLine())));
			br.close();
		}
	}
}
