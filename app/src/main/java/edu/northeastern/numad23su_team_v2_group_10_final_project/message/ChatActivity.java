package edu.northeastern.numad23su_team_v2_group_10_final_project.message;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import edu.northeastern.numad23su_team_v2_group_10_final_project.R;
import edu.northeastern.numad23su_team_v2_group_10_final_project.message.ChatAdapter;
import edu.northeastern.numad23su_team_v2_group_10_final_project.model.ChatMessage;
import edu.northeastern.numad23su_team_v2_group_10_final_project.model.ChatRoom;
import edu.northeastern.numad23su_team_v2_group_10_final_project.model.User;
import edu.northeastern.numad23su_team_v2_group_10_final_project.util.AndroidUtil;
import edu.northeastern.numad23su_team_v2_group_10_final_project.util.FirebaseUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {

    User otherUser;
    String chatroomId;
    ChatRoom chatRoom;
    TextView otherUsername;
    EditText messageInput;
    ImageButton sendMessageBtn;
    ImageButton backBtn;
    ImageView otherUserImage;
    Toolbar toolbar;
    RecyclerView recyclerView;
    ChatAdapter chatAdapter;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //get the otherUser
        otherUser = AndroidUtil.getUserModelFromIntent(getIntent());
        chatroomId = FirebaseUtil.getChatroomId(FirebaseUtil.currentUserId(), otherUser.getUserId());

        otherUsername = findViewById(R.id.display_username);
        if (otherUser.getUserId().equals(FirebaseUtil.currentUserId())) {
            otherUsername.setText(otherUser.getName() + " (Me)");
        } else {
            otherUsername.setText(otherUser.getName());
        }

        backBtn = findViewById(R.id.backButton);
        messageInput = findViewById(R.id.chat_message_input);
        sendMessageBtn = findViewById(R.id.sendButton);
        recyclerView = findViewById(R.id.chat_recycler_view);
        //toolbar = findViewById(R.id.chat_toolbar);

        otherUserImage = findViewById(R.id.userImageView);
        setupUserProfileImage(otherUser.getUserId());

        backBtn.setOnClickListener(v -> {
            onBackPressed();
        });

        sendMessageBtn.setOnClickListener(v -> {
            String message = messageInput.getText().toString().trim();
            if (message.isEmpty()) {
                return;
            }
            sendMessageToUser(message);
        });
        getOrCreateChatRoom();
        setupChatRecyclerView();
    }

    private void setupUserProfileImage(String userID) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        String path = "images/avatar/" + userID + "/000.jpg";
        StorageReference storageRef = storage.getReference(path);
        try {
            File localfile = File.createTempFile("tempfile", ".jpg");
            storageRef.getFile(localfile)
                    .addOnSuccessListener(taskSnapshot -> {
                        Bitmap bitmap = BitmapFactory.decodeFile(localfile.getAbsolutePath());
                        otherUserImage.setImageBitmap(bitmap);
                    }).addOnFailureListener(e -> Log.d(TAG, "Failed to load user profile image."));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void setupChatRecyclerView() {
        Query query = FirebaseUtil.getChatroomMessageReference(chatroomId)
                .orderBy("timestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<ChatMessage> options = new FirestoreRecyclerOptions.Builder<ChatMessage>()
                .setQuery(query, ChatMessage.class)
                .build();

        chatAdapter = new ChatAdapter(options, getApplicationContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setReverseLayout(true);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(chatAdapter);
        chatAdapter.startListening();
        chatAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                recyclerView.smoothScrollToPosition(0);
            }
        });
    }

    private void sendMessageToUser(String message) {

        chatRoom.setLastMessageTimestamp(Timestamp.now());
        chatRoom.setLastMessageSenderId(FirebaseUtil.currentUserId());
        chatRoom.setLastMessage(message);
        FirebaseUtil.getChatroomReference(chatroomId).set(chatRoom);

        ChatMessage chatMessage = new ChatMessage(message, FirebaseUtil.currentUserId(), Timestamp.now());
        FirebaseUtil.getChatroomMessageReference(chatroomId).add(chatMessage).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                messageInput.setText("");
                sendNotificationToUser(message);
            }
        });
    }

    void sendNotificationToUser(String message) {

        FirebaseUtil.currentUserDetails().get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                User currentUser = task.getResult().toObject(User.class);
                try {
                    JSONObject jsonObject = new JSONObject();

                    JSONObject notificationObj = new JSONObject();
                    notificationObj.put("title", currentUser.getName());
                    notificationObj.put("body", message);

                    JSONObject dataObj = new JSONObject();
                    dataObj.put("userId", currentUser.getUserId());

                    jsonObject.put("notification", notificationObj);
                    jsonObject.put("data", dataObj);

                    DocumentReference documentReference = FirebaseUtil.allUserCollectionReference().document(otherUser.getUserId());
                    documentReference.get().addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String token = documentSnapshot.getString("token");
                            try {
                                jsonObject.put("to", token);
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                            if (token != null) {
                                Log.d(TAG, "Other user's token: " + token);
                            } else {
                                Log.d(TAG, "Token field doesn't exist in the document.");
                            }
                        } else {
                            Log.d(TAG, "No token found.");
                        }
                    }).addOnFailureListener(e -> {
                        System.out.println("Error retrieving document: " + e.getMessage());
                    });
                    callApi(jsonObject);

                } catch (Exception e) {

                }

            }
        });

    }


    private void getOrCreateChatRoom() {
        FirebaseUtil.getChatroomReference(chatroomId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                chatRoom = task.getResult().toObject(ChatRoom.class);
                if (chatRoom == null) {
                    chatRoom = new ChatRoom(chatroomId,
                            Arrays.asList(FirebaseUtil.currentUserId(), otherUser.getUserId()),
                            Timestamp.now(),
                            ""
                    );
                    FirebaseUtil.getChatroomReference(chatroomId).set(chatRoom);
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (chatRoom.getLastMessage() == null) {
            FirebaseUtil.getChatroomReference(chatroomId).delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Log.d(TAG, "No message sent, chat deleted successfully.");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "Error deleting document");
                        }
                    });
        }
    }

    void callApi(JSONObject jsonObject) {
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient();
        String url = "https://fcm.googleapis.com/fcm/send";
        RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Authorization", "Bearer YOUR-KEY")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

            }
        });
    }
}
