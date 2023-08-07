package edu.northeastern.numad23su_team_v2_group_10_final_project.post.display_post;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.northeastern.numad23su_team_v2_group_10_final_project.MainActivity;
import edu.northeastern.numad23su_team_v2_group_10_final_project.R;
import edu.northeastern.numad23su_team_v2_group_10_final_project.model.Reply;

public class ReplyOuterAdapter extends RecyclerView.Adapter<ReplyHolder> {
    List<Reply> list;
    Context context;
    ReplyViewModel replyViewModel;
    int mark;

    public ReplyOuterAdapter(Context context, List<Reply> list, ReplyViewModel replyViewModel, int mark) {
        this.context = context;
        this.list = list;
        this.replyViewModel = replyViewModel;
        this.mark = mark;
    }


    @NonNull
    @Override
    public ReplyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (this.mark == -1) {
            view =  LayoutInflater.from(parent.getContext()).inflate(R.layout.card_reply_layout, parent, false);
        } else  {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_reply_sub_layout, parent, false);
        }

        return new ReplyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReplyHolder holder, int position) {
        Reply reply = list.get(position);
        // get user name and avatar
        final String[] userName = new String[1];
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child(reply.userId).child("name");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    userName[0] = snapshot.getValue().toString();
                    holder.userName.setText(userName[0]);
                } else {
                    userName[0] = "anonymous";
                    holder.userName.setText("anonymous");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        StorageReference storageRef1 = FirebaseStorage.getInstance().getReference().child("images")
                .child("avatar").child(reply.userId).child("000.jpg");
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
        holder.date.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(reply.timestamp.toDate()));
        //content
        final String[] content = {""};
        if (reply.replyToUserId != null && reply.replyToUserId.length() > 0) {
            DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference().child("users").child(reply.replyToUserId).child("name");
            ref2.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        content[0] = "Reply to " + snapshot.getValue().toString() + ": ";
                        String contentStr = content[0] + reply.text;
                        holder.content.setText(contentStr);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else {
            holder.content.setText(reply.text);
        }
        // handle click avatar
        holder.avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, MainActivity.class);
                i.putExtra("USER", reply.userId);
                context.startActivity(i);
            }
        });
        // handle click reply
        holder.actionReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (reply.replyRootId != null && reply.replyRootId.length() > 0) {
                    replyViewModel.setReplyRootId(reply.replyRootId);
                    replyViewModel.setReplyToUserId(reply.userId);
                    replyViewModel.setReplyToName(userName[0]);
                } else {
                    replyViewModel.setReplyToUserId(reply.userId);
                    replyViewModel.setReplyRootId(reply.replyId);
                    replyViewModel.setReplyToName(userName[0]);
                }
                replyViewModel.setIndex(holder.getAdapterPosition());
                replyViewModel.setTrigger(!replyViewModel.getTrigger().getValue());
            }
        });
        // sub replies
        // replyList is assembled in fetchReplies()
        if (reply.replyList != null) {
            ReplyOuterAdapter sub = new ReplyOuterAdapter(context, reply.replyList, replyViewModel, position);
            holder.recyclerView.setLayoutManager(new WrapContentLinearLayoutManager(context,LinearLayoutManager.VERTICAL,false));
            holder.recyclerView.setAdapter(sub);
        } else {
            holder.recyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
