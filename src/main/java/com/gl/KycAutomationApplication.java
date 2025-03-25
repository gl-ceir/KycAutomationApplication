package com.gl;

import com.gl.service.KycService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EntityScan(basePackages = {"com.gl.entity"})
public class KycAutomationApplication {

    @Autowired
    private KycService kycService;

    public static void main(String[] args) {
        SpringApplication.run(KycAutomationApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
            if (   args.length > 0  ) {
                    kycService.processKycFile(args[0]);
            } else {
                System.err.println("Operator name not provided. Please provide operator name");
            }
            System.exit(1);
        };
    }
}
