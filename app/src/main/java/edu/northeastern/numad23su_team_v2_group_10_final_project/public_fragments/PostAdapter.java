package edu.northeastern.numad23su_team_v2_group_10_final_project.public_fragments;


import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import edu.northeastern.numad23su_team_v2_group_10_final_project.R;
import edu.northeastern.numad23su_team_v2_group_10_final_project.model.Post;


public class PostAdapter extends RecyclerView.Adapter<PostHolder>{
    List<Post> list;
    Context context;
    PostClickListener listener;
    String[] postTypes = {"offerProductPosts", "needProductPosts","offerServicePosts", "needServicePosts"};


    public void setListener(PostClickListener listener) {
        this.listener = listener;
    }

    public PostAdapter(Context context, List<Post> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public PostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =  LayoutInflater.from(parent.getContext()).inflate(R.layout.card_post_in_list_layout, parent, false);
        return new PostHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull PostHolder holder, int position) {
        Post post = list.get(position);
        holder.title.setText(post.title);

        holder.date.setText(new SimpleDateFormat("yyyy-MM-dd").format(post.timestamp.toDate()));
        holder.text.setText(post.text);
        if (post.price > 0.0) holder.price.setText("$" + String.format("%.2f", post.price));
        // get user name and avatar
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child(post.userId).child("name");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    holder.userName.setText(snapshot.getValue().toString());
                } else {
                    holder.userName.setText("anonymous");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        StorageReference storageRef1 = FirebaseStorage.getInstance().getReference().child("images")
                .child("avatar").child(post.userId).child("000.jpg");
        storageRef1.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(context)
                        .load(uri)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)         //ALL or NONE as your requirement
                        .thumbnail(Glide.with(context).load(R.drawable.ic_image_loading))
                        .error(R.drawable.ic_grey_person_24)
                        .into(holder.avatar);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Handle any errors
                Glide.with(context)
                        .load(R.drawable.ic_grey_person_24)
                        .fitCenter()
                        .into(holder.avatar);
            }
        });
        if (post.imgCnt == null || post.imgCnt == 0) {
            holder.postImage.setImageResource(R.drawable.ic_image_default);
        } else {
            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("images")
                    .child(postTypes[post.type.intValue()]).child(post.postId).child("small").child("000.jpg");
            storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(context)
                            .load(uri)
                            .centerCrop()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)         //ALL or NONE as your requirement
                            .thumbnail(Glide.with(context).load(R.drawable.ic_image_loading))
                            .error(R.drawable.ic_image_default)
                            .into(holder.postImage);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                    Glide.with(context)
                            .load(R.drawable.ic_image_default)
                            .fitCenter()
                            .into(holder.postImage);
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    /* Within the RecyclerView.Adapter class */

    // Clean all elements of the recycler
    public void clear() {
        list.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Post> list) {
        this.list.addAll(list);
        notifyDataSetChanged();
    }
}
