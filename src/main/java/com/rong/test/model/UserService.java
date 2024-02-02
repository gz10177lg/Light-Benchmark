package com.rong.test.model;

import com.rong.annotation.LightBenchmark;

import java.util.Date;
import java.util.UUID;

public class UserService {
    @LightBenchmark
    public void createUUID() {
        UUID.randomUUID().toString();
    }

    @LightBenchmark
    public void createUser() {
        String uuid = UUID.randomUUID().toString();
        new User(uuid, uuid.replace("-", ""), new Date());
    }

}

class User {
    String id;
    String name;
    Date date;

    public User(String id, String name, Date date) {
        this.id = id;
        this.name = name;
        this.date = date;
    }
}
