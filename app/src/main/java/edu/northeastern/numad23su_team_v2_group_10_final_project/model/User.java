package edu.northeastern.numad23su_team_v2_group_10_final_project.model;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class User {
    public String avatar = ""; //user icon
    public String campus = "";
    public String name = "";
    public String email;
    public Map<String, Long> offerServicePosts = new HashMap<>();
    public Map<String, Long> wantServicePosts = new HashMap<>();
    public Map<String, Long> offerProductPosts = new HashMap<>();
    public Map<String, Long> wantProductPosts = new HashMap<>();
    public Map<String, Long> chats = new HashMap<>();

    public User() {

    }

    public User(String email) {
        // Mandatory field
        this.email = email;
    }


}
