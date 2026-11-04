package com.example.emailservice.providers;

import java.util.Arrays;

public class Address {
    public Address() {}

    public static String[] split(String spt){
        if (spt == null || spt.isBlank()) return new String[0];
        return Arrays.stream(spt.split(","))
                .map(String::trim)
                .filter(s->!s.isEmpty())
                .toArray(String[]::new);
    }
}
