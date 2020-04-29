package com.firebase.chatter;

public class Chat {

    public boolean seen;
    public String messageNode;

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
