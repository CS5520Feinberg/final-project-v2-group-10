package edu.northeastern.numad23su_team_v2_group_10_final_project.model;

import com.google.firebase.Timestamp;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ServerValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@IgnoreExtraProperties
public class Post {
    public Long type; // ["OfferProduct", "NeedProduct","OfferService", "NeedService"]
    public String userId;
    public List<String> images = new ArrayList<>();
    public List<String> smallImages = new ArrayList<>();
    public String title;
    public String text;
    public Double price;
    public Map<String, Long> replies = new HashMap<>();
    public Timestamp timestamp;
    public Boolean isActive;
    public String postId;
    public Long imgCnt;

    public Post() {

    }

    public Post(Long type, String userId, String title, String text, Double price, String postId, Long cnt) {
        this.type = type;
        this.userId = userId;
        this.title = title;
        this.text = text;
        this.price = price;
        this.isActive = true;
        this.postId = postId;
        this.imgCnt = cnt;
    }

    public Map<String,Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("type", type);
        map.put("userId", userId);
        map.put("title", title);
        map.put("text", text);
        map.put("price", price);
        map.put("isActive", isActive);
        map.put("timestamp", ServerValue.TIMESTAMP);
        map.put("postId", postId);
        map.put("imgCnt", imgCnt);
        if (images.size() > 0) {
            map.put("images", images);
        }
        if (smallImages.size() > 0) {
            map.put("smallImages", images);
        }
        if (replies.size() > 0) {
            map.put("replies", replies);
        }
        return map;
    }
}
