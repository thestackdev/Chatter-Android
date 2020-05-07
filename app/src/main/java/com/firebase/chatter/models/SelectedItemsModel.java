package com.firebase.chatter.models;

import android.view.View;

public class SelectedItemsModel {

    private int position;
    private View view;
    private String currentUid;
    private String fromUid;
    private String message;
    private String delete;

    public String getMessageNode() {
        return messageNode;
    }

    public void setMessageNode(String messageNode) {
        this.messageNode = messageNode;
    }

    private String messageNode;

    public String getDelete() {
        return delete;
    }

    public void setDelete(String delete) {
        this.delete = delete;
    }

    public SelectedItemsModel() {
    }

    public SelectedItemsModel(int position , View view , String messageNode) {
        this.position = position;
        this.view = view;
        this.messageNode = messageNode;
    }

    public SelectedItemsModel(int position, View view, String currentUid, String fromUid, String message , String delete) {
        this.position = position;
        this.view = view;
        this.currentUid = currentUid;
        this.fromUid = fromUid;
        this.message = message;
        this.delete = delete;
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
