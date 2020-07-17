package com.dllyal;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    /**
     * API接口包路径
     */
    private String basePackage = "com.dllyal.controller";

    /**
     * API页面标题
     */
    private String title = "短信网关";

    /**
     * API描述
     */
    private String description = "通过Spring boot整合Netty，实现中移CMPP协议发送短信。（支持长短信）";

    /**
     * 服务条款地址
     */
    private String termsOfServiceUrl = "";

    /**
     * 版本号
     */
    private String version = "1.0";

    /**
     * 联系人
     */
    private Contact contact = new Contact("dllyal","","");

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage(basePackage))
                .paths(PathSelectors.any())
                .build().enable(true);
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title(title)
                .description(description)
                .termsOfServiceUrl(termsOfServiceUrl)
                .version(version)
                .contact(contact)
                .build();
    }

}