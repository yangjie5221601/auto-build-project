package com.cx.auto.build.model;

import lombok.Builder;
import lombok.Data;

/**
 * @Description:
 * @Author: yangjie
 * @Date: 2018/11/13 下午5:51
 */
@Data
@Builder
public class GeneratorVo {
	private String groupId;
	private String artifactId;
	private String sourceBasePath;
	private String targetBasePath;
	private String sourceBasePath1;

}