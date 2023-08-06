package edu.northeastern.numad23su_team_v2_group_10_final_project.post.create_post;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import edu.northeastern.numad23su_team_v2_group_10_final_project.R;

public class ImageHolder extends RecyclerView.ViewHolder {
    public ImageView imageView;
    public CheckBox checkBox;

    public ImageHolder(View itemView, ImageClickListener listener) {
        super(itemView);
        this.imageView = itemView.findViewById(R.id.image_view);
        this.checkBox = itemView.findViewById(R.id.check_box);
        this.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onClick(getLayoutPosition());
            }
        });
    }
}
