/**
 * Copyright (C), 2015-2018, XXX有限公司
 * <p>
 * FileName: Swagger2Config
 * <p>
 * Author: yangjie
 * <p>
 * Date: 2018/8/24 下午5:43
 * <p>
 * Description: Swagger2配置类
 */

package com.guazi.sofa.mpay.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * 〈一句话功能简述〉<br>
 * <p>
 * 〈Swagger2配置类〉
 *
 * @author yangjie
 * @create 2018/8/24
 * @since 1.0.0
 */
@Configuration
@EnableSwagger2
public class Swagger2Config {

	@Bean
	public Docket createRestApi() {
		return new Docket(DocumentationType.SWAGGER_2)
				.apiInfo(apiInfo()).enable(Boolean.TRUE)
				.select()
				.apis(RequestHandlerSelectors.basePackage("com.guazi.sofa.mpay"))
				.paths(PathSelectors.any())
				.build();
	}

	private ApiInfo apiInfo() {
		return new ApiInfoBuilder()
				.title("sofa-mpay swagger 接口文档 ")
				.description("更多相关文章请关注 sofa-mpay wiki")
				.contact("yangjie21@guazi.com")
				.version("1.0")
				.build();
	}
}
