package edu.northeastern.numad23su_team_v2_group_10_final_project.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChatMessage {
    private String message;
    private String senderId;
    private long timeStamp;
    private String currentTime;

    public ChatMessage(String message, String senderId) {
        this.message = message;
        this.senderId = senderId;
        this.timeStamp = System.currentTimeMillis();
        this.currentTime = getTimeStringFromTimestamp(this.timeStamp);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }

    private String getTimeStringFromTimestamp(long timeStamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(timeStamp));
    }
}
