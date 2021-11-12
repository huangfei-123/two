package com.dongyimai.oauth;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class Test {
    public static void main(String[] args) {
        String password="offcn";
        System.out.println(new BCryptPasswordEncoder().encode(password));
    }
}
