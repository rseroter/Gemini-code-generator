package com.seroter.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class DemoApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		System.out.println("Starting up the app generator ...");
		
		//throw an error if the user doesn't pass in an argument
		if (args == null || args.length == 0) {
            throw new IllegalArgumentException("Missing required arguments.");
        }
		else {
			System.out.println("args length is " + args.length);
		}
	}
}
