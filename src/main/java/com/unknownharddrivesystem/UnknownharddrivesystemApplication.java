package com.unknownharddrivesystem;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages = {"com.unknownharddrivesystem.mapper"})
public class UnknownharddrivesystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(UnknownharddrivesystemApplication.class, args);
	}

}
