package com.doublez.backend;

import java.sql.Connection;

import javax.sql.DataSource;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@ComponentScan(basePackages = "com.doublez.backend")
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}
	
	// Uncomment to check for database connection
//	@Bean
//    CommandLineRunner testConnection(DataSource dataSource) {
//        return args -> {
//            try (Connection conn = dataSource.getConnection()) {
//                System.out.println("✔ DATABASE CONNECTION SUCCESSFUL");
//                System.out.println("✔ URL: " + conn.getMetaData().getURL());
//                System.out.println("✔ Driver: " + conn.getMetaData().getDriverName());
//            } catch (Exception e) {
//                System.err.println("✖ DATABASE CONNECTION FAILED");
//                e.printStackTrace();
//            }
//        };
//    }

}
