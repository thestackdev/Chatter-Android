package com.firebase.chatter.models;

import java.io.Serializable;

public class Chat implements Serializable {

    private boolean seen;
    private String messageNode;
    private long timeStamp;
    private boolean watching;

    public int getUnSeen() {
        return unSeen;
    }

    public void setUnSeen(int unSeen) {
        this.unSeen = unSeen;
    }

    private int unSeen;

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public boolean getWatching() {
        return watching;
    }

    public void setWatching(boolean watching) {
        this.watching = watching;
    }

    public String getMessageNode() {
        return messageNode;
    }

    public void setMessageNode(String messageNode) {
        this.messageNode = messageNode;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }
}
