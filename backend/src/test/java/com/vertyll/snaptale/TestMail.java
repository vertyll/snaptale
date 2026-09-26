package com.vertyll.snaptale;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration(proxyBeanMethods = false)
public class TestMail {

    @Bean
    @Primary
    RecordingMailSender recordingMailSender() {
        return new RecordingMailSender();
    }
}
