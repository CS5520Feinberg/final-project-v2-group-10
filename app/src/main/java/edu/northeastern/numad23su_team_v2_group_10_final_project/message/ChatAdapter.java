package edu.northeastern.numad23su_team_v2_group_10_final_project.message;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import edu.northeastern.numad23su_team_v2_group_10_final_project.R;
import edu.northeastern.numad23su_team_v2_group_10_final_project.model.ChatMessage;
import edu.northeastern.numad23su_team_v2_group_10_final_project.util.FirebaseUtil;

public class ChatAdapter extends FirestoreRecyclerAdapter<ChatMessage, ChatAdapter.ChatViewHolder> {
    Context context;

    public ChatAdapter(@NonNull FirestoreRecyclerOptions<ChatMessage> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull ChatAdapter.ChatViewHolder holder, int position, @NonNull ChatMessage model) {
        if (model.getSenderId().equals(FirebaseUtil.currentUserId())){
            holder.leftChatLayout.setVisibility(View.GONE);
            holder.rightChatLayout.setVisibility(View.VISIBLE);
            holder.rightChatTextview.setText(model.getMessage());
            holder.rightChatTextSentTime.setText(FirebaseUtil.timestampToStringTime(model.getTimestamp()));
        } else {
            holder.leftChatLayout.setVisibility(View.VISIBLE);
            holder.rightChatLayout.setVisibility(View.GONE);
            holder.leftChatTextview.setText(model.getMessage());
            holder.leftChatTextSentTime.setText(FirebaseUtil.timestampToStringTime(model.getTimestamp()));
        }
    }

    @NonNull
    @Override
    public ChatAdapter.ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_chat_items, parent, false);
        return new ChatAdapter.ChatViewHolder(view);
    }

    class ChatViewHolder extends RecyclerView.ViewHolder{

        LinearLayout leftChatLayout, rightChatLayout;
        TextView leftChatTextview, rightChatTextview, leftChatTextSentTime, rightChatTextSentTime;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            leftChatLayout = itemView.findViewById(R.id.left_chat_layout);
            rightChatLayout = itemView.findViewById(R.id.right_chat_layout);
            leftChatTextview = itemView.findViewById(R.id.left_chat_textview);
            rightChatTextview = itemView.findViewById(R.id.right_chat_textview);
            leftChatTextSentTime = itemView.findViewById(R.id.left_chat_sent_time);
            rightChatTextSentTime = itemView.findViewById(R.id.right_chat_sent_time);
        }
    }
}
