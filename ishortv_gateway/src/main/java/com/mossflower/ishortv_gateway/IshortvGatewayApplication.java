package com.mossflower.ishortv_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * @author z's'b
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class}, scanBasePackages = {"com.mossflower"})
public class IshortvGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(IshortvGatewayApplication.class, args);
    }

}
