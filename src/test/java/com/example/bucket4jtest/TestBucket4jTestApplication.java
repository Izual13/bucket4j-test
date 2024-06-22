package com.example.bucket4jtest;

import org.springframework.boot.SpringApplication;

public class TestBucket4jTestApplication {

    public static void main(String[] args) {
        SpringApplication.from(Bucket4jTestApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
