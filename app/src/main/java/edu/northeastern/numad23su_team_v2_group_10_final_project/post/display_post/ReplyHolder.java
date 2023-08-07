package edu.northeastern.numad23su_team_v2_group_10_final_project.post.display_post;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import edu.northeastern.numad23su_team_v2_group_10_final_project.R;

public class ReplyHolder extends RecyclerView.ViewHolder {
    ImageView avatar;
    TextView userName;
    TextView date;
    TextView content;
    TextView actionReply;
    RecyclerView recyclerView;

    public ReplyHolder(@NonNull View itemView) {
        super(itemView);
        avatar = itemView.findViewById(R.id.avatar_image);
        userName = itemView.findViewById(R.id.user_name_text);
        date = itemView.findViewById(R.id.date_text);
        content = itemView.findViewById(R.id.content_text);
        actionReply = itemView.findViewById(R.id.action_reply);
        recyclerView = itemView.findViewById(R.id.recycler_view);
    }
}
