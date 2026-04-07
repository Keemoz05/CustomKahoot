package com.syed.QuizYa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication // ① Tell Spring: "scan this package for components"
public class Main {

	public static void main(String[] args) {
		SpringApplication.run(Main.class, args);
	}

}
