package edu.northeastern.numad23su_team_v2_group_10_final_project.message;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

import edu.northeastern.numad23su_team_v2_group_10_final_project.ChatActivity;
import edu.northeastern.numad23su_team_v2_group_10_final_project.R;
import edu.northeastern.numad23su_team_v2_group_10_final_project.model.User;
import edu.northeastern.numad23su_team_v2_group_10_final_project.util.AndroidUtil;
import edu.northeastern.numad23su_team_v2_group_10_final_project.util.FirebaseUtil;

public class UsersAdapter extends FirestoreRecyclerAdapter<User, UsersAdapter.UsersViewHolder> {

    Context context;

    public UsersAdapter(@NonNull FirestoreRecyclerOptions<User> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull UsersViewHolder holder, int position, @NonNull User model) {
        holder.username.setText(model.getName());
        if (model.getCampus() != null) holder.campus.setText(model.getCampus());
        else holder.campus.setText("Boston");
        if (model.getUserId().equals(FirebaseUtil.currentUserId())) {
            holder.username.setText(model.getName() + " (Me)");
        }
        holder.setupUserProfileImage(model.getUserId());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatActivity.class);
                AndroidUtil.passUserModelAsIntent(intent, model);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
    }

    @NonNull
    @Override
    public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.fragment_message_children_users_items, parent, false);
        return new UsersViewHolder(view);
    }

    class UsersViewHolder extends RecyclerView.ViewHolder {
        TextView username;
        TextView campus;
        ImageView userImage;

        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.user_name_text);
            campus = itemView.findViewById(R.id.campus);
            userImage = itemView.findViewById(R.id.userImage);
        }

        public void setupUserProfileImage(String userID) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            String path = "images/avatar/" + userID + "/000.jpg";
            StorageReference storageRef = storage.getReference(path);
            try {
                File localfile = File.createTempFile("tempfile", ".jpg");
                storageRef.getFile(localfile)
                        .addOnSuccessListener(taskSnapshot -> {
                            Bitmap bitmap = BitmapFactory.decodeFile(localfile.getAbsolutePath());
                            userImage.setImageBitmap(bitmap);
                        }).addOnFailureListener(e -> Log.d(TAG, "Failed to load user profile image."));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
