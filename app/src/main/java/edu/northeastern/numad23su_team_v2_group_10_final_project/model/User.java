package edu.northeastern.numad23su_team_v2_group_10_final_project.model;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class User {
    String avatar = ""; //user icon
    String campus = "";
    String name;
    String email;
    String userId;
    Map<String, Long> offerServicePosts = new HashMap<>();
    Map<String, Long> wantServicePosts = new HashMap<>();
    Map<String, Long> offerProductPosts = new HashMap<>();
    Map<String, Long> wantProductPosts = new HashMap<>();
    Map<String, Long> chats = new HashMap<>();

    public User() {

    }

    public User(String name, String email) {
        // Mandatory fields
        this.name = name;
        this.email = email;
    }

    public User(String id, String name, String email, String campus) {
        // Mandatory fields
        this.userId = id;
        this.name = name;
        this.email = email;
        this.campus = campus;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getCampus() {
        return campus;
    }

    public void setCampus(String campus) {
        this.campus = campus;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
