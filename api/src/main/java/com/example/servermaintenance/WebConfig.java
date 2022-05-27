package com.example.servermaintenance;

import com.example.servermaintenance.course.CourseService;
import com.example.servermaintenance.course.CourseUrlToCourseConverter;
import lombok.AllArgsConstructor;
import org.modelmapper.AbstractConverter;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@AllArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler("/webjars/**")
                .addResourceLocations("/webjars/");
    }

    private final CourseService courseService;

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new CourseUrlToCourseConverter(courseService));
    }

    @Bean
    public ModelMapper modelMapper() {
        Converter<String, String> nullToString = new AbstractConverter<>() {
            protected String convert(String source) {
                return source == null ? "" : source;
            }
        };
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.addConverter(nullToString);
        return modelMapper;
    }

}
