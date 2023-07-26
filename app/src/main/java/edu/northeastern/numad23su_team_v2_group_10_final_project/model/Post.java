package edu.northeastern.numad23su_team_v2_group_10_final_project.model;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@IgnoreExtraProperties
public class Post {
    public Long type; // ["OfferProduct", "NeedProduct","OfferService", "NeedService"]
    public String userId;
    public List<String> images = new ArrayList<>();
    public String title;
    public String text;
    public Double price;
    public Map<String, Long> replies = new HashMap<>();
    public Long timeStamp;
    public Boolean isActive;

    public Post() {

    }

    public Post(Long type, String userId, String title, String text, Double price) {
        this.type = type;
        this.userId = userId;
        this.title = title;
        this.text = text;
        this.price = price;
        this.isActive = true;
    }

    //TODO: implement toMap() to add timestamp
}
