package com.example.kletech;

public class StudentItem {
    private String name,status;
    private int id;

    public long getSid() {
        return sid;
    }

    public void setSid(long sid) {
        this.sid = sid;
    }

    private long sid;

    public StudentItem(long sid , int id, String name) {
        this.sid = sid;
        this.id = id;
        this.name = name;
        status = "";
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
