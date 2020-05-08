package com.firebase.chatter.models;

public class Messages {

    private String message;
    private String type;
    private String from;
    private String state;
    private String times;
    private String reply_message;
    private String reply_username;

    public String getTimes() {
        return times;
    }

    public void setTimes(String times) {
        this.times = times;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Messages(String from) {
        this.from = from;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Messages(){

    }

    public String getReply_message() {
        return reply_message;
    }

    public void setReply_message(String reply_message) {
        this.reply_message = reply_message;
    }

    public String getReply_username() {
        return reply_username;
    }

    public void setReply_username(String reply_username) {
        this.reply_username = reply_username;
    }
}
