package edu.northeastern.numad23su_team_v2_group_10_final_project.post.display_post;

import static edu.northeastern.numad23su_team_v2_group_10_final_project.post.Utils.getPostTypes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import android.content.ContentResolver;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.stfalcon.imageviewer.StfalconImageViewer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import edu.northeastern.numad23su_team_v2_group_10_final_project.R;
import edu.northeastern.numad23su_team_v2_group_10_final_project.model.Post;
import edu.northeastern.numad23su_team_v2_group_10_final_project.post.create_post.ImageClickListener;
import edu.northeastern.numad23su_team_v2_group_10_final_project.post.create_post.UploadImage;

public class DisplayPostActivity extends AppCompatActivity {
    String[] postTypes = getPostTypes();
    String postType;
    String postId;

    ViewPager2 pager2;
    SwipeRefreshLayout swipeContainer;
    ImageView avatar;
    TextView userName;
    TextView date;
    TextView title;
    TextView text;

    int width = 0;
    Post post;
    List<UploadImage> list = new ArrayList<>();
    AtomicInteger cnt = new AtomicInteger(0);

    ImageAdapterPager adapter;

    private FirebaseFirestore mFireStoreRef;
    private DatabaseReference mDbRef;
    private StorageReference mStorageRef;
    private FirebaseAuth mAuth;


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
        mDbRef =  FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mFireStoreRef = FirebaseFirestore.getInstance();

        postType = bundle.getString("postType");
        postId = bundle.getString("postId");

        avatar = findViewById(R.id.avatar_image);
        userName = findViewById(R.id.user_name_text);
        date = findViewById(R.id.date_text);
        title = findViewById(R.id.title_text);
        text = findViewById(R.id.content_text);
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        pager2 = findViewById(R.id.view_pager);
        adapter = new ImageAdapterPager(getApplicationContext(), list);
        pager2.setAdapter(adapter);

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchData();
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
                    Bitmap bitmap = item.bitmap;
                    int width = item.width;
                    if (bitmap == null) bitmap = BitmapFactory.decodeFile(path);
                    if (path.length() > 0 || item.bitmap != null) {
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
        swipeContainer.setRefreshing(true);
        fetchData();

        pager2.post(new Runnable() {
            @Override
            public void run() {
                width = pager2.getMeasuredWidth();
                for (UploadImage image: list) {
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


    private void fetchData() {
        cnt.set(0);
        mFireStoreRef.collection("posts").document(postType).collection("posts")
                .document(postId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        post = documentSnapshot.toObject(Post.class);
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
                        date.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(post.timestamp.toDate()));
                        title.setText(post.title);
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
                                list.add(new UploadImage(uri.toString(),"", width));
                                String picName = String.format("%03d.jpg", i);
                                StorageReference ref2 = mStorageRef.child("images").child(postType).child(postId).child("original")
                                        .child(picName);
                                int finalI = i;
                                ref2.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Glide.with(DisplayPostActivity.this).asBitmap().load(uri).into(new CustomTarget<Bitmap>() {
                                            @Override
                                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                                list.get(finalI).bitmap = resource;
                                                int t = cnt.getAndIncrement();
                                                if (t == post.imgCnt - 1) swipeContainer.setRefreshing(false);
                                                adapter.notifyDataSetChanged();
                                            }

                                            @Override
                                            public void onLoadCleared(@Nullable Drawable placeholder) {

                                            }
                                        });
                                    }
                                });

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

}