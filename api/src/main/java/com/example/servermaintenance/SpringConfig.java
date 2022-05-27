package com.example.servermaintenance;

import org.modelmapper.AbstractConverter;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringConfig {
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
