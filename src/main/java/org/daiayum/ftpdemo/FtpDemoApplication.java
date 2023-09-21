package org.daiayum.ftpdemo;

import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FtpDemoApplication {

	public static void mains(String[] args) throws IOException {
		SpringApplication.run(FtpDemoApplication.class, args);
		new FTPService().testConnection();
	}

}
