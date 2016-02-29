package com.xmas.config;

import com.xmas.service.TagsService;
import com.xmas.util.data.DataDirectoryService;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.Set;

@Configuration
public class MVCConfiguration extends WebMvcConfigurationSupport {

    @Autowired
    private DataDirectoryService dataDirectoryService;

    @Override
    public FormattingConversionService mvcConversionService() {
        FormattingConversionService f = super.mvcConversionService();
        f.addConverter(new TagsService.TagConverter());
        return f;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/data/**").addResourceLocations(dataDirectoryService.getDataDirectory().toString());
    }

    @Bean
    public CommonsMultipartResolver multipartResolver() {
        CommonsMultipartResolver resolver = new ExtendedMultipartResolver();
        resolver.setDefaultEncoding("utf-8");
        return resolver;
    }

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

    @Override
    public void addInterceptors(InterceptorRegistry interceptorRegistry) {

    }

    private static class ExtendedMultipartResolver extends CommonsMultipartResolver {

        public static final Set<String> ALLOWED_MULTIPART_METHODS = new HashSet<String>() {{
            add("POST");
            add("PUT");
            add("PATCH");
        }};

        @Override
        public boolean isMultipart(HttpServletRequest request) {
            return  request != null &&
                    ALLOWED_MULTIPART_METHODS.contains(request.getMethod().toUpperCase()) &&
                    FileUploadBase.isMultipartContent(new ServletRequestContext(request));
        }
    }

}
