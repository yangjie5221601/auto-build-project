package com.cx.auto.build.util;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description:
 * @Author: yangjie
 * @Date: 2018/11/6 下午6:33
 */
public class AutoBuildUtil {
	private static final Set<String> ING_FILES = new HashSet<>(Arrays.asList(".impl", ".iml", ".classpath", ".project", ".class"));
	private static final Set<String> ING_DIR = new HashSet<>(Arrays.asList(".setting", "build", "bin", ".idea", "target", ".git"));
	private static final String SOURCE_TEMPLATE = "template-project";
	private static final String TARGET_TEMPLATE = "target-project";
	private static final String BASE_PACKAGE = "com/guazi";
	private static final String groupId = "com.guazi";
	private static final String BASE_PATH = "/Users/yangjie/Documents/work/autoBuildProject";
	private static final String sourceBasePath = BASE_PATH + File.separator + SOURCE_TEMPLATE;
	private static final String artifactId = "mpay";
	private static final String targetBasePath = BASE_PATH + File.separator + TARGET_TEMPLATE + File.separator + artifactId;
	private static final String sourceBasePath1 = BASE_PATH + File.separator + TARGET_TEMPLATE + File.separator + SOURCE_TEMPLATE;
	private static final String SRC_GROUP_ID = "com.cx";
	private static final String SRC_ARTIFACT_ID = "template-project";

	private static final FilenameFilter fileFilter = (dir, name) -> {
		if (dir.isDirectory() && ING_DIR.contains(name)) {
			return false;
		}
		if (dir.isFile() && ING_FILES.contains(name)) {
			return false;
		}
		int index = name.lastIndexOf(".");
		if (index <= -1) {
			if (ING_FILES.contains(name)) {
				return false;
			}
		}
		return true;
	};

	public static void main(String[] args) {
		//getFileAndDirListFromSourceDir("/Users/yangjie/Documents/work/autoBuildProject");
		makeDirectoryAndFileByRecursion(sourceBasePath);
	}

	/**
	 * 递归方式根据源目录和文件创建目标目录和文件
	 *
	 * @param path
	 */
	private static void makeDirectoryAndFileByRecursion(String path) {
		File[] fileAndDirs = getFileAndDirListFromSourceDir(path);
		if (null == fileAndDirs) {
			return;
		}
		for (File file : fileAndDirs) {
			if (file.isDirectory()) {
				String sourceAbsolutePath = file.getAbsolutePath();
				String sourceFileName = null;
				String sourceDirPath = getReplacedSourceDirPath(sourceAbsolutePath, false, sourceFileName);
				String targetDirPath = getReplacedTargetDirPath(sourceAbsolutePath, sourceDirPath, sourceFileName, false);
				makeTargetDirectory(targetDirPath);
				makeDirectoryAndFileByRecursion(sourceDirPath);
			} else if (file.isFile()) {
				String sourceAbsolutePath = file.getAbsolutePath();
				String sourceFileName = file.getName();
				String sourceDirPath = getReplacedSourceDirPath(sourceAbsolutePath, true, sourceFileName);
				String targetDirPath = getReplacedTargetDirPath(sourceAbsolutePath, sourceDirPath, sourceFileName, true);
				String targetFileName = sourceFileName;
				makeDirectoryAndFile(sourceDirPath, sourceFileName, targetDirPath, targetFileName);
			}
		}
	}

	/**
	 * 获取目标目录路径
	 *
	 * @param sourceAbsolutePath
	 * @param sourceDirPath
	 * @param sourceFileName
	 * @param isFile
	 * @return
	 */
	private static String getReplacedTargetDirPath(String sourceAbsolutePath, String sourceDirPath, String sourceFileName, boolean isFile) {
		String targetDriPath = null;
		/**如果是文件*/
		if (isFile) {
			/**如果是读取的是java文件,由于需要根据java文件第一行的包路径来得到最终路径，所以需要单独处理*/
			if (isJavaFileDir(sourceDirPath)) {
				targetDriPath = replacedSourceDirPath(sourceDirPath) + File.separator + getPackageDir(sourceDirPath, sourceFileName);

			} else {/**如果是非java文件，则直接根据源路径进行替换后得到目标路径*/
				targetDriPath = replacedSourceDirPath(sourceDirPath);
			}
		} else {/**如果是目录*/
			targetDriPath = replacedSourceDirPath(sourceDirPath);
		}
		return targetDriPath;
	}

	/**
	 * 判断此目录路径是否是java文件目录路径
	 * 引用注意：在正则表达式中的“\\”表示和后面紧跟着的那个字符构成一个转义字符（姑且先这样命名），代表着特殊的意义；所以如果你要在正则表达式中表示一个反斜杠\，应当写成“\\\\”
	 *
	 * @param sourceDirPath
	 * @return
	 */
	private static boolean isJavaFileDir(String sourceDirPath) {
		String regex = sourceBasePath1 + "\\\\(web|service|dao|rpc|domain|common|client|cache)\\\\src\\\\main\\\\java";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(sourceDirPath);
		if (m.find()) {
			return true;
		}
		return false;
	}

	private static String replacedSourceDirPath(String sourceDirPath) {
		String basePackage = StringUtils.isNoneBlank(BASE_PACKAGE) ? BASE_PACKAGE : groupId.replace(".", "/");
		String result = sourceDirPath
				.replace(sourceBasePath, targetBasePath).replace("template", artifactId).replace("com/cx", basePackage);
		return result;
	}

	/**
	 * 获取源目录路径
	 *
	 * @param sourceAbsolutePath
	 * @param isFile
	 * @param sourceFileName
	 * @return
	 */
	private static String getReplacedSourceDirPath(String sourceAbsolutePath, boolean isFile, String sourceFileName) {
		String sourceDirPath;
		if (isFile) {
			sourceDirPath = sourceAbsolutePath.replace(sourceFileName, "");
		} else {
			sourceDirPath = sourceAbsolutePath;
		}
		return sourceDirPath;
	}

	/**
	 * 创建目录及文件
	 *
	 * @param sourceDirPath
	 * @param sourceFileName
	 * @param targetDirPath
	 * @param targetFileName
	 */
	private static void makeDirectoryAndFile(String sourceDirPath, String sourceFileName, String targetDirPath, String targetFileName) {
		String sourceContent = readContentFromSourceFile(sourceDirPath, sourceFileName);
		String newContent = getReplacedContent(sourceContent);
		if (makeTargetDirectory(targetDirPath)) {
			if (makeTargetFile(targetDirPath, targetFileName)) {
				writeNewContentToTargetFile(targetDirPath, targetFileName, newContent);
			}
		}
	}

	/**
	 * 根据java文件的第一行获取包路径
	 *
	 * @param sourceDirPath
	 * @param sourceFileName
	 * @return
	 */
	private static String getPackageDir(String sourceDirPath, String sourceFileName) {
		String packageDir = null;
		File file = new File(sourceDirPath + File.separator + sourceFileName);
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
			String firstLine = br.readLine();
			packageDir = getReplacedContent(firstLine).replace(".", File.separator).replace("package ", "").replace(";", "");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return packageDir;
	}


	/**
	 * 获取文件和目录列表
	 *
	 * @param sourceDirPath
	 * @return
	 */
	private static File[] getFileAndDirListFromSourceDir(String sourceDirPath) {
		File file = new File(sourceDirPath);
		File[] fileList = file.listFiles();
		return fileList;
	}

	/**
	 * 创建目录
	 *
	 * @param dirPath
	 */
	private static boolean makeTargetDirectory(String dirPath) {
		try {
			File file = new File(dirPath);
			if (!file.exists() && !file.isDirectory()) {
				file.mkdirs();
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}


	private static boolean makeTargetFile(String targetDirPath, String targetFileName) {
		try {
			File file = new File(targetDirPath + File.separator + targetFileName);
			if (!file.exists()) {
				file.createNewFile();
			}
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	private static void writeNewContentToTargetFile(String targetDirPath, String targetFileName, String newContent) {
		FileWriter fw = null;
		try {
			fw = new FileWriter(targetDirPath + File.separator + targetFileName);
			fw.write(newContent);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 将文件中的占位符替换为需要的格式
	 *
	 * @param sourceContent
	 * @return
	 */
	private static String getReplacedContent(String sourceContent) {
		String result = sourceContent.replace(SRC_GROUP_ID, groupId).replace(SRC_ARTIFACT_ID, artifactId).replace("template", artifactId);
		return result;
	}


	/**
	 * 一次性读出文件中所有内容
	 *
	 * @param sourceDirPath
	 * @param sourceFileName
	 * @return
	 */
	private static String readContentFromSourceFile(String sourceDirPath, String sourceFileName) {
		String encoding = "utf-8";
		File file = new File(sourceDirPath + File.separator + sourceFileName);
		Long filelength = file.length();
		byte[] filecontent = new byte[filelength.intValue()];
		try {
			FileInputStream in = new FileInputStream(file);
			in.read(filecontent);
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			return new String(filecontent, encoding);
		} catch (UnsupportedEncodingException e) {
			System.err.println("The OS does not support " + encoding);
			e.printStackTrace();
			return null;
		}
	}

}