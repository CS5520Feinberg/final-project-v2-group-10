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
    TextView userId;
    TextView text;
    TextView price;
    ImageView postImage;


    public PostHolder(@NonNull View itemView, PostClickListener listener) {
        super(itemView);
        this.listener = listener;
        this.title = itemView.findViewById(R.id.title_text);
        this.userId = itemView.findViewById(R.id.user_id_text);
        this.text = itemView.findViewById(R.id.content_text);
        this.price = itemView.findViewById(R.id.price);
        this.postImage = itemView.findViewById(R.id.post_image);
    }
}
