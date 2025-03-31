package com.soaresdev.picpaytestjr;

import org.springframework.boot.SpringApplication;

public class TestPicpayTestJrApplication {

    public static void main(String[] args) {
        SpringApplication.from(PicpayTestJrApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
