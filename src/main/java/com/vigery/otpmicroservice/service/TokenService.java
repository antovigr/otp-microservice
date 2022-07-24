package com.vigery.otpmicroservice.service;

import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class TokenService {

    // Generate 5 digits random Number.
    public String getRandomToken() {

        Random rnd = new Random();
        int number = rnd.nextInt(99999);

        return String.format("%06d", number);
    }

    // Generate 9 digits random Number.
    public String getRandomSessionId() {

        Random rnd = new Random();
        int number = rnd.nextInt(999999999);

        return String.format("%06d", number);
    }

}
