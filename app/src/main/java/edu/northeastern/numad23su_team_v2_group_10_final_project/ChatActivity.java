package edu.northeastern.numad23su_team_v2_group_10_final_project;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;

import java.sql.Array;
import java.util.Arrays;

import edu.northeastern.numad23su_team_v2_group_10_final_project.message.ChatAdapter;
import edu.northeastern.numad23su_team_v2_group_10_final_project.message.UsersAdapter;
import edu.northeastern.numad23su_team_v2_group_10_final_project.model.ChatMessage;
import edu.northeastern.numad23su_team_v2_group_10_final_project.model.ChatRoom;
import edu.northeastern.numad23su_team_v2_group_10_final_project.model.User;
import edu.northeastern.numad23su_team_v2_group_10_final_project.util.AndroidUtil;
import edu.northeastern.numad23su_team_v2_group_10_final_project.util.FirebaseUtil;

public class ChatActivity extends AppCompatActivity {

    User otherUser;
    String chatroomId;
    ChatRoom chatRoom;
    TextView otherUsername;
    EditText messageInput;
    ImageButton sendMessageBtn;
    ImageButton backBtn;
    Toolbar toolbar;
    RecyclerView recyclerView;
    ChatAdapter chatAdapter;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_message_children_chat);

        //get the otherUser
        otherUser = AndroidUtil.getUserModelFromIntent(getIntent());
        chatroomId = FirebaseUtil.getChatroomId(FirebaseUtil.currentUserId(), otherUser.getUserId());

        otherUsername = findViewById(R.id.display_username);
        otherUsername.setText(otherUser.getName());
        backBtn = findViewById(R.id.backButton);
        messageInput = findViewById(R.id.chat_message_input);
        sendMessageBtn = findViewById(R.id.sendButton);
        recyclerView = findViewById(R.id.chat_recycler_view);
        //toolbar = findViewById(R.id.chat_toolbar);

        backBtn.setOnClickListener(v -> {
            onBackPressed();
        });

        sendMessageBtn.setOnClickListener(v -> {
            String message = messageInput.getText().toString().trim();
            if (message.isEmpty()) {return;}
            sendMessageToUser(message);
        });
        getOrCreateChatRoom();
        setupChatRecyclerView();
    }

    private void setupChatRecyclerView() {
        Query query = FirebaseUtil.getChatroomMessageReference(chatroomId)
                .orderBy("timestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<ChatMessage> options = new FirestoreRecyclerOptions.Builder<ChatMessage>()
                .setQuery(query,ChatMessage.class)
                .build();

        chatAdapter = new ChatAdapter(options,getApplicationContext());
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
        FirebaseUtil.getChatroomReference(chatroomId).set(chatRoom);

        ChatMessage chatMessage = new ChatMessage(message, FirebaseUtil.currentUserId(), Timestamp.now());
        FirebaseUtil.getChatroomMessageReference(chatroomId).add(chatMessage).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                messageInput.setText("");
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
}
