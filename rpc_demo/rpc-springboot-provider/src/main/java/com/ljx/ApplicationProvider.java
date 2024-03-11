package com.ljx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author LiuJixing
 * @Date 10/3/2024
 */
@SpringBootApplication
@RestController
public class ApplicationProvider {
    public static void main(String[] args) {
        SpringApplication.run(ApplicationProvider.class,args);
    }
    @GetMapping("test")
    public String Hello(){
        return "hello provider";
    }
}
