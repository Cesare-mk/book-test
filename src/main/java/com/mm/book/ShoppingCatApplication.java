package com.mm.book;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication

@EnableCaching// 使用缓存
public class ShoppingCatApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShoppingCatApplication.class, args);
    }

}
