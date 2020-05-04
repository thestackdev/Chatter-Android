package com.firebase.chatter.models;

import android.view.View;

public class SelectedItemsModel {

    private int position;
    private View view;
    private String currentUid;
    private String fromUid;

    public SelectedItemsModel() {
    }

    public SelectedItemsModel(int position, View view, String currentUid, String fromUid) {
        this.position = position;
        this.view = view;
        this.currentUid = currentUid;
        this.fromUid = fromUid;
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
}
