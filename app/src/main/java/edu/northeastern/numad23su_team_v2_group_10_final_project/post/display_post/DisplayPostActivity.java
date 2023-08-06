package edu.northeastern.numad23su_team_v2_group_10_final_project.post.display_post;

import static edu.northeastern.numad23su_team_v2_group_10_final_project.post.SearchUtils.generateKey;
import static edu.northeastern.numad23su_team_v2_group_10_final_project.post.Utils.getPostTypes;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.stfalcon.imageviewer.StfalconImageViewer;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import de.hdodenhof.circleimageview.CircleImageView;
import edu.northeastern.numad23su_team_v2_group_10_final_project.MainActivity;
import edu.northeastern.numad23su_team_v2_group_10_final_project.R;
import edu.northeastern.numad23su_team_v2_group_10_final_project.model.Post;
import edu.northeastern.numad23su_team_v2_group_10_final_project.model.Reply;
import edu.northeastern.numad23su_team_v2_group_10_final_project.post.create_post.AddPostActivity;
import edu.northeastern.numad23su_team_v2_group_10_final_project.post.create_post.ImageClickListener;
import edu.northeastern.numad23su_team_v2_group_10_final_project.post.create_post.UploadImage;

public class DisplayPostActivity extends AppCompatActivity {
    private static int EDIT = 100;

    String[] postTypes = getPostTypes();
    ReplyViewModel replyViewModel;


    String postType;
    String postId;

    ViewPager2 pager2;
    SwipeRefreshLayout swipeContainer;
    CircleImageView avatar;
    TextView userName;
    TextView date;
    TextView title;
    TextView price;
    TextView text;
    Button btn_chat;
    Button btn_deactivate;
    Button btn_edit;
    EditText comment;
    ImageButton btn_send;
    RecyclerView recyclerView;
    TextView tv_reply_to;

    int width = 0;
    Post post;
    List<UploadImage> list = new ArrayList<>();
    List<Reply> replies = new ArrayList<>();
    AtomicInteger cnt = new AtomicInteger(0);
    boolean isScrolling = false;

    ImageAdapterPager adapter;
    ReplyOuterAdapter replyOuterAdapter;

    private FirebaseFirestore mFireStoreRef;
    private DocumentReference documentReference;
    private DatabaseReference mDbRef;
    private StorageReference mStorageRef;
    private FirebaseAuth mAuth;
    ActivityResultLauncher<Intent> queryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_post);
        Bundle bundle = getIntent().getExtras();
        if (bundle == null || !bundle.containsKey("postType") || !bundle.containsKey("postId")) {
            finish();
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mAuth = FirebaseAuth.getInstance();
        mDbRef = FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mFireStoreRef = FirebaseFirestore.getInstance();
        postType = bundle.getString("postType");
        postId = bundle.getString("postId");

        documentReference = mFireStoreRef.collection("posts").document(postType).collection("posts")
                .document(postId);

        replyViewModel = new ViewModelProvider(this).get(ReplyViewModel.class);
        replyViewModel.setReplyRootId("");
        replyViewModel.setReplyToUserId("");
        replyViewModel.setTrigger(true);

        btn_chat = findViewById(R.id.btn_chat);
        btn_deactivate = findViewById(R.id.btn_deactivate);
        btn_edit = findViewById(R.id.btn_edit);
        comment = findViewById(R.id.comment_input);
        tv_reply_to = findViewById(R.id.tv_reply_to);
        btn_send = findViewById(R.id.btn_send);
        recyclerView = findViewById(R.id.recycler_view);
        avatar = findViewById(R.id.avatar_image);
        userName = findViewById(R.id.user_name_text);
        date = findViewById(R.id.date_text);
        title = findViewById(R.id.title_text);
        price = findViewById(R.id.price_text);
        text = findViewById(R.id.content_text);
        swipeContainer = findViewById(R.id.swipeContainer);
        pager2 = findViewById(R.id.view_pager);
        adapter = new ImageAdapterPager(getApplicationContext(), list);
        pager2.setAdapter(adapter);


        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchData();
                fetchReplies();
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        StfalconImageViewer.Builder<UploadImage> builder = new StfalconImageViewer.Builder<>
                (this, list, (imageView, item) -> {
                    String imageUri = item.imageUri;
                    String path = item.imagePath;
                    int width = item.width;
                    Bitmap bitmap = BitmapFactory.decodeFile(path);
                    if (path.length() > 0) {
                        imageView.setImageBitmap(bitmap);
                    } else {
                        imageView.setImageURI(Uri.parse(imageUri));
                    }
                });

        ImageClickListener listener = new ImageClickListener() {
            @Override
            public void onClick(int position) {
                builder.withHiddenStatusBar(false)
                        .allowZooming(true)
                        .allowSwipeToDismiss(true)
                        .withImageChangeListener((pos) -> {
                            //some codes
                        })
                        .withBackgroundColorResource(R.color.black)
                        .show();
            }
        };
        adapter.setListener(listener);

        replyOuterAdapter = new ReplyOuterAdapter(this, replies, replyViewModel);
        recyclerView.setLayoutManager(new WrapContentLinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        //recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(replyOuterAdapter);
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    isScrolling = true;
                    replyViewModel.setReplyRootId("");
                    tv_reply_to.setVisibility(View.GONE);
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (isScrolling) {
                    isScrolling = false;
                    replyViewModel.setReplyRootId("");
                    tv_reply_to.setVisibility(View.GONE);
                }
            }
        });

        replyViewModel.getTrigger().observe(this, trigger -> {
            if (replyViewModel.getReplyToName().getValue() != null) {
                tv_reply_to.setVisibility(View.VISIBLE);
                tv_reply_to.setText("replying to " + replyViewModel.getReplyToName().getValue() + ":");
            }
            comment.setText("");
        });

        swipeContainer.setRefreshing(true);
        fetchData();
        fetchReplies();

        queryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        fetchData();
                    }
                });

        avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(DisplayPostActivity.this, MainActivity.class);
                i.putExtra("USER", post.userId);
                startActivity(i);
            }
        });

        btn_deactivate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                post.isActive = !post.isActive;
                if (post.isActive) {
                    post.title = post.title.substring(10);
                } else {
                    post.title = "(inactive)" + post.title;
                }
                Map<String, Object> map = post.toMap();
                map.put("timestamp", FieldValue.serverTimestamp());
                mFireStoreRef.collection("posts").document(postType).collection("posts")
                        .document(postId).set(map).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(DisplayPostActivity.this, "Update failed.", Toast.LENGTH_LONG).show();
                            }
                        }).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                fetchData();
                            }
                        });
            }
        });

        btn_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(DisplayPostActivity.this, AddPostActivity.class);
                i.putExtra("type", post.type);
                i.putExtra("postId", postId);
                i.putExtra("title", post.title);
                i.putExtra("text", post.text);
                i.putExtra("price", post.price);
                ArrayList<UploadImage> arr = new ArrayList<>(list);
                i.putParcelableArrayListExtra("images", arr);
                queryLauncher.launch(i);
            }
        });

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // <post document> -> replies -> [replyRootId] -> replies -> [replyId]
                String replyRootId = replyViewModel.getReplyRootId().getValue();
                if (replyRootId.length() == 0) {
                    // a direct reply
                    String replyId = generateKey();
                    String text = comment.getText().toString();
                    String userId = mAuth.getUid();
                    Reply reply = new Reply(replyId, postId, userId, text);

                    documentReference.collection("replies").document(replyId).set(reply.toMap()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            replyViewModel.setReplyToUserId("");
                            replyViewModel.setReplyRootId("");
                            replyViewModel.setReplyToName("");
                            comment.setText("");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(DisplayPostActivity.this, "Reply failed.", Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    String replyId = generateKey();
                    String text = comment.getText().toString();
                    String userId = mAuth.getUid();
                    Reply reply = new Reply(replyId, postId, userId, text);
                    reply.replyRootId = replyRootId;
                    reply.replyToUserId = replyViewModel.getReplyToUserId().getValue();

                    documentReference.collection("replies").document(replyRootId).collection("replies").document(replyId).set(reply.toMap())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    replyViewModel.setReplyToUserId("");
                                    replyViewModel.setReplyRootId("");
                                    replyViewModel.setReplyToName("");
                                    comment.setText("");
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(DisplayPostActivity.this, "Reply failed.", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });

        pager2.post(new Runnable() {
            @Override
            public void run() {
                width = pager2.getMeasuredWidth();
                for (UploadImage image : list) {
                    image.width = width;
                }
                pager2.getLayoutParams().height = width;
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    private void fetchReplies() {
        CollectionReference ref = documentReference.collection("replies");
        Query q = ref.orderBy("timestamp", Query.Direction.DESCENDING);
        q.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    replies.clear();
                }
                int index = -1;
                for (DocumentSnapshot document : task.getResult()) {
                    index++;
                    Reply reply = document.toObject(Reply.class);
                    replies.add(reply);
                    replyOuterAdapter.notifyDataSetChanged();
                    int finalIndex = index;
                    documentReference.collection("replies").document(reply.replyId).collection("replies")
                                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (!task.isSuccessful()) return;
                                    replies.get(finalIndex).replyList = new ArrayList<>();
                                    for (DocumentSnapshot document: task.getResult()) {
                                        Reply reply = document.toObject(Reply.class);
                                        replies.get(finalIndex).replyList.add(reply);
                                    }
                                    Collections.sort(replies.get(finalIndex).replyList, (a, b) -> {
                                        return b.timestamp.compareTo(a.timestamp);
                                    });
                                    replyOuterAdapter.notifyItemChanged(finalIndex);
                                }
                            });

                }
            }
        });
    }

    private void fetchData() {
        cnt.set(0);
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                post = documentSnapshot.toObject(Post.class);
                if (!post.isActive) {
                    btn_deactivate.setText("ACTIVATE");
                }
                if (!mAuth.getUid().equals(post.userId)) {
                    btn_deactivate.setVisibility(View.GONE);
                    btn_edit.setVisibility(View.GONE);
                }
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child(post.userId).child("name");
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getValue() != null) {
                            userName.setText(snapshot.getValue().toString());
                        } else {
                            userName.setText("anonymous");
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
                        Glide.with(getApplicationContext())
                                .load(uri)
                                .centerCrop()
                                .diskCacheStrategy(DiskCacheStrategy.ALL)         //ALL or NONE as your requirement
                                .thumbnail(Glide.with(getApplicationContext()).load(R.drawable.ic_image_loading))
                                .error(R.drawable.ic_grey_person_24)
                                .into(avatar);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle any errors
                        Glide.with(getApplicationContext())
                                .load(R.drawable.ic_grey_person_24)
                                .fitCenter()
                                .into(avatar);
                    }
                });
                if (post.timestamp != null) date.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(post.timestamp.toDate()));
                title.setText(post.title);
                if (post.price > 0.0) {
                    String str = "price: $";
                    if (post.type == 1 || post.type == 3) {
                        str = "payment: $";
                    }
                    price.setText(str + String.format("%.2f", post.price));
                } else {
                    price.setVisibility(View.GONE);
                }
                text.setText(post.text);
                if (post != null && post.imgCnt != null && post.imgCnt > 0) {
                    // fetch images
                    list.clear();
                    for (int i = 0; i < post.imgCnt; i++) {
                        Resources resources = getApplicationContext().getResources();
                        int resourceId = R.drawable.ic_image_loading;
                        Uri uri = new Uri.Builder()
                                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                                .authority(resources.getResourcePackageName(resourceId))
                                .appendPath(resources.getResourceTypeName(resourceId))
                                .appendPath(resources.getResourceEntryName(resourceId))
                                .build();
                        list.add(new UploadImage(uri.toString(), "", width));
                        String prefix = String.format("%03d", i);
                        String picName = prefix + ".jpg";
                        StorageReference ref2 = mStorageRef.child("images").child(postType).child(postId).child("original")
                                .child(picName);
                        int finalI = i;
                        File outputDir = getApplicationContext().getCacheDir();
                        try {
                            File outputFile = File.createTempFile(prefix, ".jpg", outputDir);
                            ref2.getFile(outputFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                    list.get(finalI).imagePath = outputFile.getAbsolutePath();
                                    int t = cnt.getAndIncrement();
                                    if (t == post.imgCnt - 1) swipeContainer.setRefreshing(false);
                                    adapter.notifyDataSetChanged();
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    adapter.notifyDataSetChanged();

                } else {
                    list.clear();
                    swipeContainer.setRefreshing(false);
                    pager2.setVisibility(View.GONE);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(DisplayPostActivity.this, "Fetch post failed.", Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT && resultCode == RESULT_OK) {
            fetchData();
        }
    }

}