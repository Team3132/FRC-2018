package org.team3132.lib;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Creates the files and the symbolic links for a single stream of data
 * normally on the USB flash drive. 
 *
 * 'Latest' symlink created to point to this file.
 */

public class LogFileWriter {
	
	private final String name; // eg chart
	private final String extn; // eg "html" or "csv"
	private final String basePath; // All logs are below this directory
	private Path filePath = null;
	private BufferedWriter writer = null;

	/**
	 * Write free form data to a file and create symbolic links to it. Used for csv and graphing files.
	 * 
	 * Creates
	 * <pre>
	 *    basePath/
   	 *             Latest_name.extn -> data/name_filenum.extn
   	 *             data/name_000filenum.extn
   	 *             date/timestamp_name.extn -> ../data/name_filenum.extn 
	 * </pre>
	 *        
	 * @param name  the type of data, eg "data", "chart"
	 * @param filenum  the number of the file. Incremented every start of the code.
	 * @param extn  the file extension
	 * @param basePath  Where on the file system to put these files.
	 * @throws IOException 
	 */
	public LogFileWriter(String name, long filenum, String extn, String basePath, String dataDir) throws IOException {
		this.name = name;
		this.extn = extn;
		this.basePath = basePath;
		// The absolute path to the data file so we can write to the file.
		filePath = Paths.get(basePath, dataDir, String.format("%s_%05d.%s", name, filenum, extn));
		// Ensure the parent directory exists.
		Files.createDirectories(filePath.getParent());
		// Create the file writer.
		writer = Files.newBufferedWriter(filePath);

		createSymbolicLink("Latest");
	}
	
	/**
	 * Create symbolic links to the file. Used to create Latest_x and dated
	 * symlinks. 
	 * 
	 * @param prefix as in <prefix>_chart.html
	 */
	public void createSymbolicLink(String prefix) {
		Path symlinkPath = Paths.get(basePath, String.format("%s_%s.%s", prefix, name, extn));
		Path relPath = createRelativePath(symlinkPath, filePath);
		try {
			// Ensure the parent directory exists.
			Files.createDirectories(symlinkPath.getParent());
			Files.deleteIfExists(symlinkPath);
			Files.createSymbolicLink(symlinkPath, relPath);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.printf("Failed to create symbolic link: Are we on windows?\n");
		}
	}

	/**
	 * Create symbolic links to the file. Used to create Latest_x and dated
	 * symlinks. This version also takes a directory.
	 * 
	 * @param prefix as in <prefix>_chart.html
	 */
	public void createSymbolicLink(String dir, String prefix) {
		Path path = Paths.get(dir, prefix);
		createSymbolicLink(path.toString());
	}
	
	/**
	 * Does some relative path magic to ensure that the target is
	 * relative to the file so that it will work no matter where the file system
	 * is mounted.
	 * 
	 * Examples:
	 *   prefix="Latest" creates a symlink "Latest_chart.html" -> "data/chart_00007.html"
	 *   prefix="date/20180303" creates a symlink "date/20180303_chart.html" -> "../data/chart_00007.html"
	 *   
	 * @param from the full path to the symlink.
	 * @param to the full path to the file to link to.
	 * @return the relative path to the file to link to from <code>from</code>'s perspective
	 */
	public static Path createRelativePath(Path from, Path to) {
		// Make a relative path out of this path so the symbolic link will continue to
		// work no matter where the USB flash drive is mounted.
		return from.getParent().relativize(to);
	}


	public void write(String contents) {
		if (writer == null) return; // File logging not enabled.
		try {
			writer.write(contents);
			writer.flush();
		} catch (Exception e) {
			// nothing to do. If we can't write to the log file it's not a disaster.
		}
	}
	
	public void flush() {
		try {
			writer.flush();
		} catch (IOException e) {
			// nothing to do. If we can't write to the log file it's not a disaster.
		}
	}
	
	public void close() {
		try {
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
