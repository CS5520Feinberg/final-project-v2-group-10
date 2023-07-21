package edu.northeastern.numad23su_team_v2_group_10_final_project.model;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Reply {
    String userId;
    String text;
    Long Timestamp;
    Map<String, Long> replies = new HashMap<>();

    public Reply() {

    }

    public Reply(String userId, String text) {
        this.userId = userId;
        this.text = text;
    }

    //TODO: implement toMap() to add timestamp
}
