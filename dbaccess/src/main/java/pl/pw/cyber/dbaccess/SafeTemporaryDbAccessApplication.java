package pl.pw.cyber.dbaccess;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.security.Security;

@EnableScheduling
@ConfigurationPropertiesScan
@EnableConfigurationProperties
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class SafeTemporaryDbAccessApplication {

    public static void main(String[] args) {
        Security.setProperty("crypto.policy", "unlimited");
        SpringApplication.run(SafeTemporaryDbAccessApplication.class, args);
    }
}
