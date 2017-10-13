package com.merilonstudio.mycoffeecapsulesinventory.models;

public class DBSave {

    private String content = "";
    private int version;

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
