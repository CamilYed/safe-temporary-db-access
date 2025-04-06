package pl.pw.cyber.dbaccess;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.security.Security;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableConfigurationProperties
public class SafeTemporaryDbAccessApplication {

    public static void main(String[] args) {
        Security.setProperty("crypto.policy", "unlimited");
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        }
        SpringApplication.run(SafeTemporaryDbAccessApplication.class, args);
    }

}
