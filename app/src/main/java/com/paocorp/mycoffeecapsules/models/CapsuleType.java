package com.paocorp.mycoffeecapsules.models;

public class CapsuleType {

    private int id;
    private String name;

    public CapsuleType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public CapsuleType() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
