package com.cx.auto.build.util;

import com.cx.auto.build.model.GeneratorVo;
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
	private static final String SRC_GROUP_ID = "com.cx";
	private static final String SRC_ARTIFACT_ID = "template-project";
	private static final String SRC_BASE_PACKAGE = "com/cx";

	public static void makeDirectoryAndFileByRecursion(GeneratorVo generatorVo, String path) {
		File[] fileAndDirs = getFileAndDirListFromSourceDir(path);
		if (null == fileAndDirs) {
			return;
		}
		for (File file : fileAndDirs) {
			if (file.isDirectory()) {
				String sourceDirPath = getReplacedSourceDirPath(file, false);
				String targetDirPath = getReplacedTargetDirPath(generatorVo, sourceDirPath, file.getName(), false);
				makeTargetDirectory(targetDirPath);
				makeDirectoryAndFileByRecursion(generatorVo, sourceDirPath);
			} else if (file.isFile()) {
				String sourceDirPath = getReplacedSourceDirPath(file, true);
				String targetDirPath = getReplacedTargetDirPath(generatorVo, sourceDirPath, file.getName(), true);
				String targetFileName = file.getName();
				makeDirectoryAndFile(generatorVo, sourceDirPath, file.getName(), targetDirPath, targetFileName);
			}
		}
	}

	/**
	 * 获取目标目录路
	 *
	 * @param sourceDirPath
	 * @param sourceFileName
	 * @param isFile
	 * @return
	 */
	private static String getReplacedTargetDirPath(GeneratorVo vo, String sourceDirPath, String sourceFileName, boolean isFile) {
		String targetDriPath;
		/**如果是文件*/
		if (isFile) {
			/**如果是读取的是java文件,由于需要根据java文件第一行的包路径来得到最终路径，所以需要单独处理*/
			if (isJavaFileDir(vo, sourceDirPath)) {
				targetDriPath = replacedSourceDirPath(vo, sourceDirPath) + File.separator + getPackageDir(vo, sourceDirPath, sourceFileName);

			} else {/**如果是非java文件，则直接根据源路径进行替换后得到目标路径*/
				targetDriPath = replacedSourceDirPath(vo, sourceDirPath);
			}
		} else {/**如果是目录*/
			targetDriPath = replacedSourceDirPath(vo, sourceDirPath);
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
	private static boolean isJavaFileDir(GeneratorVo vo, String sourceDirPath) {
		String regex = vo.getSourceBasePath1() + "\\\\(web|service|dao|rpc|domain|common|client|cache)\\\\src\\\\main\\\\java";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(sourceDirPath);
		if (m.find()) {
			return true;
		}
		return false;
	}

	private static String replacedSourceDirPath(GeneratorVo vo, String sourceDirPath) {
		String packagePrefix = vo.getGroupId().replace(".", "/");
		String packageSuffix = vo.getArtifactId().replace("-", "/").replace("_", "/");
		String result = sourceDirPath
				.replace(vo.getSourceBasePath(), vo.getTargetBasePath()).replace(SRC_BASE_PACKAGE, packagePrefix).replace("template", vo.getArtifactId());
		if (result.contains("src/main/java/" + packagePrefix + "/" + vo.getArtifactId())) {
			int index = result.lastIndexOf(vo.getArtifactId());
			return result.substring(0, index) + File.separator + packageSuffix + result.substring(index + vo.getArtifactId().length(), result.length());
		}
		return result;
	}


	private static String getReplacedSourceDirPath(File file, boolean isFile) {
		String sourceDirPath;
		String sourceAbsolutePath = file.getAbsolutePath();
		String sourceFileName = isFile ? file.getName() : null;
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
	private static void makeDirectoryAndFile(GeneratorVo vo, String sourceDirPath, String sourceFileName, String targetDirPath, String targetFileName) {
		int fileNameIndex = sourceFileName.lastIndexOf(".");
		String fileType = sourceFileName.substring(fileNameIndex + 1, sourceFileName.length());
		if (!ING_FILES.contains(fileType)) {
			String sourceContent = readContentFromSourceFile(sourceDirPath, sourceFileName);
			String newContent = getReplacedContent(vo, sourceContent, sourceFileName);
			if (makeTargetDirectory(targetDirPath)) {
				if (makeTargetFile(targetDirPath, targetFileName)) {
					writeNewContentToTargetFile(targetDirPath, targetFileName, newContent);
				}
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
	private static String getPackageDir(GeneratorVo vo, String sourceDirPath, String sourceFileName) {
		String packageDir = null;
		File file = new File(sourceDirPath + File.separator + sourceFileName);
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
			String firstLine = br.readLine();
			packageDir = getReplacedContent(vo, firstLine, null).replace(".", File.separator).replace("package ", "").replace(";", "");
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


	private static File[] getFileAndDirListFromSourceDir(String path) {
		File file = new File(path);
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
	private static String getReplacedContent(GeneratorVo vo, String sourceContent, String fileName) {
		String result = null;
		if (StringUtils.isNotBlank(fileName)) {
			if (fileName.equals("pom.xml")) {
				//pom 文件替换 group id
				result = sourceContent.replace(SRC_GROUP_ID, vo.getGroupId()).replace(SRC_ARTIFACT_ID, vo.getArtifactId()).replace("template", vo.getArtifactId());
			} else if (fileName.contains(".java") || fileName.contains(".xml")) {
				//替换java文件
				String name = vo.getArtifactId().replace("-", ".").replace("_", ".");
				result = sourceContent.replace(SRC_GROUP_ID, vo.getGroupId()).replace("template", name).replace("{artifactId}", vo.getArtifactId());
			} else {
				result = sourceContent;
			}
		}

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