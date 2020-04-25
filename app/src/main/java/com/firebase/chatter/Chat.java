package com.firebase.chatter;

public class Chat {
    int index;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getToBeSeen() {
        return toBeSeen;
    }

    public void setToBeSeen(int toBeSeen) {
        this.toBeSeen = toBeSeen;
    }

    boolean seen;
    long timeStamp;
    int toBeSeen;
}
