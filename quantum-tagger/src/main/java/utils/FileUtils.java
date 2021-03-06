package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class is part of the Java Tools (see
 * http://mpii.de/yago-naga/javatools). It is licensed under the Creative
 * Commons Attribution License (see http://creativecommons.org/licenses/by/3.0)
 * by the YAGO-NAGA team (see http://mpii.de/yago-naga)
 * 
 * Some utility methods for arrays
 */
public class FileUtils {
	/**
	 * Creates a BufferedReader for UTF-8-encoded files
	 * 
	 * @param file
	 *            File in UTF-8 encoding
	 * @return BufferedReader for file
	 * @throws FileNotFoundException
	 */
	public static BufferedReader getBufferedUTF8Reader(File file) throws FileNotFoundException {
		return new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8")));
	}

	/**
	 * Creates a BufferedReader for UTF-8-encoded files
	 * 
	 * @param fileName
	 *            Path to file in UTF-8 encoding
	 * @return BufferedReader for file
	 * @throws FileNotFoundException
	 */
	public static BufferedReader getBufferedUTF8Reader(String fileName) throws FileNotFoundException {
		return new BufferedReader(new InputStreamReader(new FileInputStream(fileName), Charset.forName("UTF-8")));
	}

	/**
	 * Creates a BufferedReader the UTF-8-encoded InputStream
	 * 
	 * @param inputStream
	 *            InputStream in UTF-8 encoding
	 * @return BufferedReader for inputStream
	 */
	public static BufferedReader getBufferedUTF8Reader(InputStream inputStream) {
		return new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
	}

	/**
	 * Creates a BufferedWriter for UTF-8-encoded files
	 * 
	 * @param file
	 *            File in UTF-8 encoding
	 * @return BufferedWriter for file
	 * @throws FileNotFoundException
	 */
	public static BufferedWriter getBufferedUTF8Writer(File file) throws FileNotFoundException {
		return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), Charset.forName("UTF-8")));
	}

	/**
	 * Creates a BufferedWriter for UTF-8-encoded files
	 * 
	 * @param fileName
	 *            Path to file in UTF-8 encoding
	 * @return BufferedWriter for file
	 * @throws FileNotFoundException
	 */
	public static BufferedWriter getBufferedUTF8Writer(String fileName) throws FileNotFoundException {
		return new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(fileName, true), Charset.forName("UTF-8")));
	}

	/**
	 * Returns the content of the (UTF-8 encoded) file as string. Linebreaks are
	 * encoded as unix newlines (\n)
	 * 
	 * @param file
	 *            File to get String content from
	 * @return String content of file.
	 * @throws IOException
	 */
	public static String getFileContent(File file) throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader reader = getBufferedUTF8Reader(file);
		for (String line = reader.readLine(); line != null; line = reader.readLine()) {
			sb.append(line);
			sb.append('\n');
		}
		reader.close();
		return sb.toString();
	}

	/**
	 * Writes the content of the string to the (UTF-8 encoded) file.
	 * 
	 * @param file
	 *            File to write String content to.
	 * @return Content of file.
	 * @throws IOException
	 */
	public static void writeFileContent(File file, String content) throws IOException {
		BufferedWriter writer = getBufferedUTF8Writer(file);
		writer.write(content);
		writer.close();
	}

	/**
	 * Collects all non-directory files in the given input directory
	 * (recursively).
	 * 
	 * @param directory
	 *            Input directory.
	 * @return All non-directory files, recursively.
	 */
	public static Collection<File> getAllFiles(File directory) {
		Collection<File> files = new LinkedList<File>();
		getAllFilesRecursively(directory, files);
		return files;
	}

	/**
	 * Helper for getAllSubdirectories(directory).
	 */
	private static void getAllFilesRecursively(File directory, Collection<File> files) {

		for (File file : directory.listFiles()) {
			if (file.isDirectory()) {
				getAllFilesRecursively(file, files);
			} else {
				files.add(file);
			}
		}
	}

	public static void checkFileExistsOrDie(File file, boolean isDirectory) throws FileNotFoundException {
		if (!file.exists()) {
			throw new FileNotFoundException(file + " does not exist.");
		} else {
			if (file.isDirectory() != isDirectory) {
				throw new IllegalStateException("Assumed " + file + " to be a " + (isDirectory ? "directory" : "file")
						+ " but is a " + (isDirectory ? "file" : "directory"));
			}
		}
	}

	public static void checkFileIsWritableOrDie(File file) throws IOException {
		checkFileExistsOrDie(file.getParentFile(), true);
		if (file.exists()) {
			if (!file.canWrite()) {
				throw new IOException("Can not write to " + file);
			}
		} else {
			try {
				file.createNewFile();
				file.delete();
			} catch (IOException e) {
				throw new IOException("Can not create " + file + ": " + e.getMessage());
			}
		}
	}

	public static Map<String, String> loadFileToMap(String file_name, String delimeter, int key_indx, int value_indx)
			throws IOException {
		BufferedReader reader = null;
		Map<String, String> data = null;
		String line;

		reader = new BufferedReader(new InputStreamReader(new FileInputStream(file_name)));
		data = new HashMap<String, String>();
		String[] temp;
		while ((line = reader.readLine()) != null) {
			temp = line.split(delimeter);
			if (temp.length > value_indx && temp.length > key_indx)
				data.put(temp[key_indx].trim(), temp[value_indx].trim());
		}
		reader.close();
		return data;
	}

	public static List<String> loadFileToList(String file_name, String delimeter, int indx) throws IOException {
		BufferedReader reader = null;
		List<String> data = null;
		String line;

		reader = new BufferedReader(new InputStreamReader(new FileInputStream(file_name)));
		data = new LinkedList<String>();
		String[] temp;
		while ((line = reader.readLine()) != null) {
			if (!delimeter.isEmpty()) {
				temp = line.split(delimeter);
				if (temp.length > indx)
					data.add(temp[indx]);
			}else{
				data.add(line.trim());
			}
		}
		reader.close();
		return data;
	}

	public static void writeListToFile(List<String> concepts, String file) throws IOException {
		BufferedWriter writer = getBufferedUTF8Writer(file);
		for (String concept : concepts) {
			writer.write(concept + "\n");
		}
		writer.close();
	}

	public static Set<String> readFile(String file_name) throws IOException {
		BufferedReader reader = null;
		Set<String> data = null;
		String line;

		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file_name)));
			data = new LinkedHashSet<String>();
			while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
				data.add(line.trim());
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			reader.close();
		}
		return data;
	}

	public static void appendToFile(String file, String string) {
		try {
			BufferedWriter writer = getBufferedUTF8Writer(file);
			writer.append(string + "\n");
			writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}