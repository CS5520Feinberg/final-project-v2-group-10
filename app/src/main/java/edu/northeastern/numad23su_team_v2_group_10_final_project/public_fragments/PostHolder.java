package edu.northeastern.numad23su_team_v2_group_10_final_project.public_fragments;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import edu.northeastern.numad23su_team_v2_group_10_final_project.R;

public class PostHolder extends RecyclerView.ViewHolder{
    PostClickListener listener;
    TextView title;
    ImageView avatar;
    TextView userName;
    TextView date;
    TextView text;
    TextView price;
    ImageView postImage;


    public PostHolder(@NonNull View itemView, PostClickListener listener) {
        super(itemView);
        this.listener = listener;
        this.title = itemView.findViewById(R.id.title_text);
        this.avatar = itemView.findViewById(R.id.avatar_image);
        this.userName = itemView.findViewById(R.id.user_name_text);
        this.date = itemView.findViewById(R.id.date_text);
        this.text = itemView.findViewById(R.id.content_text);
        this.price = itemView.findViewById(R.id.price);
        this.postImage = itemView.findViewById(R.id.post_image);
        postImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onClick(getLayoutPosition());
            }
        });
        avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onAvatarClick(getLayoutPosition());
            }
        });
    }
}
