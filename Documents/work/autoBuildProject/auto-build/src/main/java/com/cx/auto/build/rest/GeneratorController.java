package com.cx.auto.build.rest;

import com.cx.auto.build.model.GeneratorVo;
import com.cx.auto.build.util.AutoBuildUtil;

import java.io.File;
import java.net.URL;

/**
 * @Description:
 * @Author: yangjie
 * @Date: 2018/11/13 下午5:36
 */
public class GeneratorController {
	private static final String SOURCE_TEMPLATE = "template-project";
	private static final String TARGET_TEMPLATE = "target-project";
	private static final String groupId = "com.guazi";
	private static final String artifactId = "sofa-mpay";

	public static void main(String[] args) {
		//
		URL url = GeneratorController.class.getResource("/");
		String bathPath = url.getPath().substring(0, url.getPath().indexOf("/auto-build"));
		String sourceBasePath = bathPath + File.separator + SOURCE_TEMPLATE;
		String targetBasePath = bathPath + File.separator + TARGET_TEMPLATE + File.separator + artifactId;
		String sourceBasePath1 = bathPath + File.separator + TARGET_TEMPLATE + File.separator + SOURCE_TEMPLATE;
		GeneratorVo vo = GeneratorVo.builder().sourceBasePath(sourceBasePath).targetBasePath(targetBasePath).sourceBasePath1(sourceBasePath1).groupId(groupId).artifactId(artifactId).build();
		AutoBuildUtil.makeDirectoryAndFileByRecursion(vo, vo.getSourceBasePath());
	}
}