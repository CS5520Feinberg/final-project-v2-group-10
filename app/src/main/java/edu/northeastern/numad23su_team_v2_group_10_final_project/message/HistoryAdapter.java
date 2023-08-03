package edu.northeastern.numad23su_team_v2_group_10_final_project.message;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import edu.northeastern.numad23su_team_v2_group_10_final_project.ChatActivity;
import edu.northeastern.numad23su_team_v2_group_10_final_project.R;
import edu.northeastern.numad23su_team_v2_group_10_final_project.model.ChatRoom;
import edu.northeastern.numad23su_team_v2_group_10_final_project.model.User;
import edu.northeastern.numad23su_team_v2_group_10_final_project.util.AndroidUtil;
import edu.northeastern.numad23su_team_v2_group_10_final_project.util.FirebaseUtil;

public class HistoryAdapter extends FirestoreRecyclerAdapter<ChatRoom, HistoryAdapter.ChatRoomsViewHolder> {

    Context context;

    public HistoryAdapter(@NonNull FirestoreRecyclerOptions<ChatRoom> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull ChatRoomsViewHolder holder, int position, @NonNull ChatRoom model) {
        FirebaseUtil.getOtherUserFromChatroom(model.getUserIds())
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean lastMessageSentByMe = model.getLastMessageSenderId().equals(FirebaseUtil.currentUserId());

                        User otherUser = task.getResult().toObject(User.class);
                        holder.username.setText(otherUser.getName());

                        if (lastMessageSentByMe) {
                            holder.lastReceived.setText("You: " + model.getLastMessage());
                        } else {
                            holder.lastReceived.setText(model.getLastMessage());
                        }
                        holder.lastReceivedTime.setText(FirebaseUtil.timestampToStringTime(model.getLastMessageTimestamp()));
                        holder.lasReceivedDate.setText(FirebaseUtil.timestampToStringDate(model.getLastMessageTimestamp()));

                        holder.itemView.setOnClickListener( v -> {
                            Intent intent = new Intent(context, ChatActivity.class);
                            AndroidUtil.passUserModelAsIntent(intent, otherUser);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                        });
                    }
                });
    }

    @NonNull
    @Override
    public ChatRoomsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.fragment_message_children_chat_history_items, parent, false);
        return new ChatRoomsViewHolder(view);
    }

    class ChatRoomsViewHolder extends RecyclerView.ViewHolder {
        TextView username;
        TextView lastReceivedTime;
        TextView lasReceivedDate;
        TextView lastReceived;

        public ChatRoomsViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.user_name_text);
            lastReceived = itemView.findViewById(R.id.last_msg_received);
            lastReceivedTime = itemView.findViewById(R.id.last_msg_received_time);
            lasReceivedDate = itemView.findViewById(R.id.last_msg_received_date);
        }
    }
}
