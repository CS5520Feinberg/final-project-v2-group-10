package edu.northeastern.numad23su_team_v2_group_10_final_project.post.create_post;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.List;

import edu.northeastern.numad23su_team_v2_group_10_final_project.R;
import edu.northeastern.numad23su_team_v2_group_10_final_project.post.Utils;

public class ImageAdapter extends RecyclerView.Adapter<ImageHolder> {
    List<UploadImage> list;
    Context context;
    ImageClickListener listener;

    public ImageAdapter(Context context, List<UploadImage> list) {
        this.context = context;
        this.list = list;
    }

    public void setListener(ImageClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ImageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =  LayoutInflater.from(parent.getContext()).inflate(R.layout.card_image_layout, parent, false);
        return new ImageHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageHolder holder, int position) {
        UploadImage item = list.get(position);
        String imageUri = item.imageUri;
        String path = item.imagePath;
        int width = item.width;
        if (item.isInEditMode) {
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.checkBox.setChecked(item.isSelected);
        } else {
            holder.checkBox.setVisibility(View.GONE);
        }
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        Bitmap cropImage = Utils.centerSquareScaleBitmap(bitmap, width);
        holder.imageView.setImageBitmap(cropImage);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
