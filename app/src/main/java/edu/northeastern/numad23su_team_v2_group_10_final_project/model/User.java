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


}
