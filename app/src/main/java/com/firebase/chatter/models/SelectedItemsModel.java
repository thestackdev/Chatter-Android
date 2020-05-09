package com.firebase.chatter.models;

import android.view.View;

public class SelectedItemsModel {

    private int position;
    private View view;
    private String currentUid;
    private String fromUid;
    private String times;

    public String getTimes() {
        return times;
    }

    public void setTimes(String times) {
        this.times = times;
    }

    public String getChatUid() {
        return chatUid;
    }

    public void setChatUid(String chatUid) {
        this.chatUid = chatUid;
    }

    private String message;
    private String chatUid;

    public SelectedItemsModel() {
    }

    public SelectedItemsModel(int position , View view , String chatUid) {
        this.position = position;
        this.view = view;
        this.chatUid = chatUid;

    }

    public SelectedItemsModel(int position, View view, String currentUid, String fromUid, String message , String times) {
        this.position = position;
        this.view = view;
        this.currentUid = currentUid;
        this.fromUid = fromUid;
        this.message = message;
        this.times = times;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }

    public String getCurrentUid() {
        return currentUid;
    }

    public void setCurrentUid(String currentUid) {
        this.currentUid = currentUid;
    }

    public String getFromUid() {
        return fromUid;
    }

    public void setFromUid(String fromUid) {
        this.fromUid = fromUid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
