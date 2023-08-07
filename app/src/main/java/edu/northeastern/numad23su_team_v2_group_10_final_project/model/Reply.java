package edu.northeastern.numad23su_team_v2_group_10_final_project.model;

import com.google.firebase.Timestamp;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@IgnoreExtraProperties
public class Reply {
    // <post document> -> replies -> [replyId] -> replies -> [replyId]
    public String userId;
    public String text;
    public com.google.firebase.Timestamp timestamp;
    public String postId;
    public String replyRootId;
    public String replyToUserId;
    public String replyId;
    public String userName;
    public Map<String, Object> replies;
    public List<Reply> replyList;

    public Reply() {

    }

    public Reply(String replyId, String postId, String userId, String text) {
        this.userId = userId;
        this.text = text;
        this.postId = postId;
        this.replyId = replyId;
        // userName will be retrieved from realtime database
    }

    public Map<String, Object> toMap() {
        HashMap<String,Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("text", text);
        map.put("postId", postId);
        map.put("timestamp", FieldValue.serverTimestamp());
        if (replyRootId != null) map.put("replyRootId", replyRootId);
        if (replyToUserId != null) map.put("replyToUserId", replyToUserId);
        if (replyId != null) map.put("replyId", replyId);
        return map;
    }
}
