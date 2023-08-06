package edu.northeastern.numad23su_team_v2_group_10_final_project.post.display_post;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import edu.northeastern.numad23su_team_v2_group_10_final_project.R;
import edu.northeastern.numad23su_team_v2_group_10_final_project.post.create_post.ImageClickListener;

public class ImageHolderPager extends RecyclerView.ViewHolder {
    public ImageView imageView;
    public CheckBox checkBox;

    public ImageHolderPager(View itemView, ImageClickListener listener) {
        super(itemView);
        this.imageView = itemView.findViewById(R.id.image_view);
        this.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onClick(getLayoutPosition());
            }
        });
        this.checkBox = itemView.findViewById(R.id.check_box);
    }
}