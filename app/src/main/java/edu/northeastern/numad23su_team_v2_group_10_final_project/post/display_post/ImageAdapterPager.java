package edu.northeastern.numad23su_team_v2_group_10_final_project.post.display_post;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.PhoneAuthOptions;

import java.util.List;

import edu.northeastern.numad23su_team_v2_group_10_final_project.R;
import edu.northeastern.numad23su_team_v2_group_10_final_project.post.Utils;
import edu.northeastern.numad23su_team_v2_group_10_final_project.post.create_post.ImageClickListener;
import edu.northeastern.numad23su_team_v2_group_10_final_project.post.create_post.UploadImage;

public class ImageAdapterPager extends RecyclerView.Adapter<ImageHolderPager> {
    List<UploadImage> list;
    Context context;
    ImageClickListener listener;

    public ImageAdapterPager(Context context, List<UploadImage> list) {
        this.context = context;
        this.list = list;
    }

    public void setListener(ImageClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ImageHolderPager onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =  LayoutInflater.from(parent.getContext()).inflate(R.layout.card_image_layout2, parent, false);
        return new ImageHolderPager(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageHolderPager holder, int position) {
        UploadImage item = list.get(position);
        String imageUri = item.imageUri;
        String path = item.imagePath;
        Bitmap bitmap = item.bitmap;
        int width = item.width;
        if (item.isInEditMode) {
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.checkBox.setChecked(item.isSelected);
        } else {
            holder.checkBox.setVisibility(View.GONE);
        }
        holder.imageView.getLayoutParams().height = width;
        if (bitmap == null) bitmap = BitmapFactory.decodeFile(path);
        Bitmap cropImage = Utils.centerSquareScaleBitmap(bitmap, width);
        if (path.length() > 0 || item.bitmap != null) {
            holder.imageView.setImageBitmap(cropImage);
        } else {
            holder.imageView.setImageURI(Uri.parse(imageUri));
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
