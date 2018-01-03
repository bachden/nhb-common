package com.nhb.common.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FileSystemUtils {

	public enum SizeUnit {
		BYTE, KILOBYTE, MEGABYTE, GIGABYTES, TERABYTES, PETABYTES, EXABYTES, ZETTABYTES, YOTTABYTES;
	}

	private static String BASE_PATH;

	static final void initBasePath(Class<?> clazz) {
		if (BASE_PATH != null) {
			// throw new
			// RuntimeException("BASE_PATH cannot be init more than one times");
			return;
		}
		// System.out.println("path: " +
		// Paths.get(".").toAbsolutePath().normalize().toString());
		BASE_PATH = getBasePathForClass(clazz);
	}

	public static final String getBasePath() {
		if (BASE_PATH == null) {
			initBasePath(FileSystemUtils.class);
		}
		return BASE_PATH;
	}

	public static final String getBasePathForClass(Class<?> clazz) {
		File file;
		try {
			String basePath = null;
			file = new File(clazz.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
			if (file.isFile() || file.getPath().endsWith(".jar") || file.getPath().endsWith(".zip")) {
				basePath = file.getParent();
			} else {
				basePath = file.getPath();
			}
			// fix to run inside eclipse
			if (basePath.endsWith(File.separator + "lib") || basePath.endsWith(File.separator + "bin")
					|| basePath.endsWith("bin" + File.separator) || basePath.endsWith("lib" + File.separator)) {
				basePath = basePath.substring(0, basePath.length() - 4);
			}
			// fix to run inside netbean
			if (basePath.endsWith(File.separator + "build" + File.separator + "classes")) {
				basePath = basePath.substring(0, basePath.length() - 14);
			}
			// fix to run with maven build
			if (basePath.endsWith(File.separator + "target" + File.separator + "classes")) {
				basePath = basePath.substring(0, basePath.length() - 15);
			}
			// end fix
			if (!basePath.endsWith(File.separator)) {
				basePath = basePath + File.separator;
			}
			return basePath;
		} catch (URISyntaxException e) {
			throw new RuntimeException("Cannot firgue out base path for class: " + clazz.getName());
		}
	}

	public static final String createPathFrom(String... elements) {
		if (elements != null && elements.length > 0) {
			StringBuilder sb = new StringBuilder();
			boolean flag = false;
			for (String ele : elements) {
				sb.append(((sb.length() > 0 && !flag) ? File.separator : "") + ele);
				flag = ele.endsWith(File.separator);
			}
			return sb.toString();
		}
		return null;
	}

	public static final String createAbsolutePathFrom(String... elements) {
		StringBuilder sb = new StringBuilder();
		if (elements != null && elements.length > 0) {
			if (!elements[0].startsWith("/")) {
				sb.append(getBasePath());
			}
			boolean flag = true;
			for (String ele : elements) {
				if (flag) {
					sb.append(ele);
					flag = false;
				} else {
					sb.append(File.separator + ele);
				}
			}
			return sb.toString();
		}
		return null;
	}

	public static final void writeTextFile(String destination, String content) {
		File file = new File(destination);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				System.err.println("create file `" + file.getAbsolutePath() + "` error");
				e.printStackTrace();
				return;
			}
		}
		try {
			FileWriter fw = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			bw.close();
		} catch (Exception e) {
			System.err.println("writing file error: ");
			e.printStackTrace();
		}
	}

	public static final String readFileContent(File file) throws IOException {
		if (file != null && file.exists() && file.isFile()) {
			List<String> lines = Files.readAllLines(file.toPath());
			StringBuilder sb = new StringBuilder();
			for (String line : lines) {
				sb.append((sb.length() > 0 ? "\n" : "") + line);
			}
			return sb.toString();
		}
		return null;
	}

	public static final String readFileContent(String filePath) throws IOException {
		if (filePath != null) {
			return readFileContent(new File(filePath));
		}
		return null;
	}

	public static void delete(File file) throws IOException {
		if (file.isDirectory()) {
			// directory is empty, then delete it
			if (file.list().length == 0) {
				file.delete();
			} else {
				// list all the directory contents
				String files[] = file.list();
				for (String temp : files) {
					// construct the file structure
					File fileDelete = new File(file, temp);
					// recursive delete
					delete(fileDelete);
				}
				// check the directory again, if empty then delete it
				if (file.list().length == 0) {
					file.delete();
				}
			}
		} else {
			// if file, then delete it
			file.delete();
		}
	}

	public static String[] getFilesInFolder(String folderPath) {
		File folder = new File(folderPath);
		if (folder.exists() && folder.isDirectory()) {
			return folder.list();
		}
		return null;
	}

	public static double getFileSize(File file, SizeUnit unit) {
		if (file.exists()) {
			double bytes = file.length();
			double kilobytes = (bytes / 1024);
			double megabytes = (kilobytes / 1024);
			double gigabytes = (megabytes / 1024);
			double terabytes = (gigabytes / 1024);
			double petabytes = (terabytes / 1024);
			double exabytes = (petabytes / 1024);
			double zettabytes = (exabytes / 1024);
			double yottabytes = (zettabytes / 1024);

			switch (unit) {
			case BYTE:
				return bytes;
			case EXABYTES:
				return exabytes;
			case GIGABYTES:
				return gigabytes;
			case KILOBYTE:
				return kilobytes;
			case MEGABYTE:
				return megabytes;
			case PETABYTES:
				return petabytes;
			case TERABYTES:
				return terabytes;
			case ZETTABYTES:
				return zettabytes;
			case YOTTABYTES:
				return yottabytes;
			}
		} else {
			System.out.println("File does not exists!");
		}
		return -1;
	}

	public static List<File> scanFolder(File folder) {
		if (folder.isDirectory()) {
			List<File> results = new ArrayList<File>();
			scanFolderRecursive(folder, results);
			return results;
		}
		return null;
	}

	private static void scanFolderRecursive(File file, Collection<File> all) {
		File[] children = file.listFiles();
		if (children != null && children.length > 0) {
			for (File child : children) {
				all.add(child);
				scanFolderRecursive(child, all);
			}
		}
	}
}
