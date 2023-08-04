package edu.northeastern.numad23su_team_v2_group_10_final_project.post.create_post;

import static edu.northeastern.numad23su_team_v2_group_10_final_project.post.SearchUtils.generateKey;
import static edu.northeastern.numad23su_team_v2_group_10_final_project.post.SearchUtils.triGram;
import static edu.northeastern.numad23su_team_v2_group_10_final_project.post.Utils.getPostTypes;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.core.CompositeFilter;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import edu.northeastern.numad23su_team_v2_group_10_final_project.FinalProjectApplication;
import edu.northeastern.numad23su_team_v2_group_10_final_project.R;
import edu.northeastern.numad23su_team_v2_group_10_final_project.model.Post;
import edu.northeastern.numad23su_team_v2_group_10_final_project.post.SearchUtils;

public class AddPostActivity extends AppCompatActivity implements ExitDialogFragment.NoticeDialogListener {
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFireStoreRef;
    private DatabaseReference mDbRef;
    private StorageReference mStorageRef;

    AtomicInteger cnt = new AtomicInteger(0);

    ExitDialogFragment exitDialogFragment = new ExitDialogFragment();
    Button btnAddImage;
    Button btnSubmit;
    RecyclerView recyclerView;
    List<RadioButton> btnList = new ArrayList<>();
    EditText title;
    EditText price;
    EditText content;
    ProgressBar progressBar;

    //["OfferProduct", "NeedProduct","OfferService", "NeedService"]
    String[] postTypes = getPostTypes();
    ArrayList<UploadImage> list = new ArrayList<>();
    ActivityResultLauncher<Intent> queryLauncher;
    ImageAdapter adapter;
    int width;
    private final static int PERMISSION_STORAGE = 99;
    final int COL = 5;
    static final String PHOTOS = "PHOTOS";
    static final String SEL_TYPE = "SEL_TYPE";


    int selPostType = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mAuth = FirebaseAuth.getInstance();
        mDbRef =  FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mFireStoreRef = FirebaseFirestore.getInstance();
        getSupportActionBar().setTitle("create post");
        FinalProjectApplication myApplication = (FinalProjectApplication) getApplicationContext();
        if (ContextCompat.checkSelfPermission(AddPostActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(AddPostActivity.this,new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE} ,PERMISSION_STORAGE);
        }

        btnAddImage = findViewById(R.id.btn_add_image);
        btnSubmit = findViewById(R.id.submit);
        recyclerView = findViewById(R.id.recycler_view);
        title = findViewById(R.id.edt_title);
        price = findViewById(R.id.edt_price);
        content = findViewById(R.id.edt_content);
        progressBar = findViewById(R.id.progressBar);
        RadioButton btn_offer_product = findViewById(R.id.btn_offer_product);
        RadioButton btn_need_product = findViewById(R.id.btn_need_product);
        RadioButton btn_offer_service = findViewById(R.id.btn_offer_service);
        RadioButton btn_need_service = findViewById(R.id.btn_need_service);
        btnList.add(btn_offer_product);
        btnList.add(btn_need_product);
        btnList.add(btn_offer_service);
        btnList.add(btn_need_service);
        if(progressBar.getVisibility() == View.VISIBLE) {
            progressBar.setVisibility(View.GONE);
        }

        for (int i = 0; i < btnList.size(); i++) {
            int finalI = i;
            btnList.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clickSelTypeButton(finalI);
                }
            });
        }
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(PHOTOS)) {
                list = savedInstanceState.getParcelableArrayList(PHOTOS);
                for (int i = 0;i < list.size(); i++) {
                    list.get(i).width = width;
                }
            }
            if (savedInstanceState.containsKey("cnt")) {
                cnt.set(savedInstanceState.getInt("cnt"));
            }
            if (savedInstanceState.containsKey(SEL_TYPE)) {
                   selPostType = savedInstanceState.getInt(SEL_TYPE);
            }
            final String stringRef = savedInstanceState.getString("reference");
            if (stringRef != null) {
                List<UploadTask> tasks = mStorageRef.getActiveUploadTasks();
                if (tasks.size() > 0) {
                    // Get the task monitoring the upload
                    progressBar.setVisibility(View.VISIBLE);
                    for (UploadTask task: tasks) {
                        // Add new listeners to the task using an Activity scope
                        task.addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot state) {
                                // Success!
                                // ...
                                int res = cnt.getAndIncrement();
                                if (res == list.size() * 2 - 1) {
                                    progressBar.setVisibility(View.GONE);
                                    finish();
                                }
                            }
                        });
                        task.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(AddPostActivity.this, "Upload image failed.", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            }
        }
        adapter = new ImageAdapter(this,list);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, COL, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapter);
        queryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.hasExtra("PHOTOS")) {
                            int prevSize = list.size();
                            list.clear();
                            adapter.notifyItemRangeRemoved(0, prevSize);
                            list.addAll(data.getParcelableArrayListExtra("PHOTOS"));
                            for (int i = 0;i < list.size(); i++) {
                                list.get(i).width = width;
                            }
                            adapter.notifyItemRangeInserted(0, list.size());
                        }

                    }
                });

        btnAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(AddPostActivity.this, UploadImageActivity.class);
                i.putParcelableArrayListExtra(PHOTOS, list);
                queryLauncher.launch(i);
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitPost();
            }
        });

        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                width = recyclerView.getMeasuredWidth() / COL;
                for (int i = 0; i < list.size(); i++) {
                    list.get(i).width = width;
                }
                adapter.notifyItemRangeChanged(0, list.size());
            }
        });
    }

    private void clickSelTypeButton(int index) {
        selPostType = index;
        for (int i = 0; i < btnList.size(); i++) {
            if (i != index) {
                btnList.get(i).setChecked(false);
            }
        }
    }

    private void submitPost() {
        // user, post type, title, content are mandatory
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || !user.isEmailVerified()) {
            Toast.makeText(this, "User is not logged in.", Toast.LENGTH_LONG).show();
            return;
        }
        String userId = user.getUid();
        String titleStr = title.getText().toString();
        String priceStr = price.getText().toString();
        String contentStr = content.getText().toString();
        if (selPostType == -1) {
            Toast.makeText(this, "Please select post type.", Toast.LENGTH_LONG).show();
            return;
        }
        if (titleStr.trim().length() == 0) {
            Toast.makeText(this, "Title cannot be empty.", Toast.LENGTH_LONG).show();
            return;
        }
        if (contentStr.trim().length() == 0) {
            Toast.makeText(this, "Content cannot be empty.", Toast.LENGTH_LONG).show();
            return;
        }
        Double priceVal = 0.0;
        if (priceStr.length() > 0) {
            try {
                priceVal = Double.parseDouble(priceStr);
            } catch (Exception e) {
                Toast.makeText(this, "Please input a valid price.", Toast.LENGTH_LONG).show();
                return;
            }
        }
        cnt.set(0);
        progressBar.setVisibility(View.VISIBLE);
        String key = generateKey();
        Post p = new Post(Long.valueOf(selPostType), userId, titleStr, contentStr, priceVal, key, Long.valueOf(list.size()));
        Map<String, Object> postMap = p.toMap();
        HashMap<String, Object> updates = new HashMap<>();
        updates.put("/posts/" + postTypes[selPostType] + "/" + key, postMap);
        updates.put("/users/" + userId + "/" + postTypes[selPostType] + "/" + key, ServerValue.TIMESTAMP);
        mDbRef.updateChildren(updates).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),
                        "Post failed." , Toast.LENGTH_LONG).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                // add data to FireStore (for full text search)
                // ref: https://levelup.gitconnected.com/firestore-full-text-search-at-no-extra-cost-ee148856685
                // NOTE: would generate a key related to timestamp
                Map<String, Object> data = triGram(titleStr + " " + contentStr);
                data.putAll(postMap);
                data.put("timestamp", FieldValue.serverTimestamp());
                mFireStoreRef.collection("posts").document(postTypes[selPostType]).collection("posts").document(key).set(data).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddPostActivity.this, "upload to FireStore failed.", Toast.LENGTH_LONG).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
//                        mFireStoreRef.collection("posts").document(postTypes[selPostType]).collection("posts").
//                                where(Filter.and(SearchUtils.generateFilterArr("test"))).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                                        if (task.isSuccessful()) {
//                                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                                Log.d("xx", document.getId() + " => " + document.getData());
//                                            }
//                                        } else {
//                                            Log.d("xx", "Error getting documents: ", task.getException());
//                                        }
//                                    }
//                                });
                    }
                });

                if (list.size() == 0) {
                    progressBar.setVisibility(View.GONE);
                    finish();
                }

                // upload images
                for (int i = 0; i < list.size(); i++) {
                    String name = String.format("%03d.jpg", i);
                    UploadImage item = list.get(i);
                    Bitmap bitmap = BitmapFactory.decodeFile(item.imagePath);
                    ByteArrayOutputStream baosOrig = new ByteArrayOutputStream();
                    ByteArrayOutputStream baosSmall = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baosOrig);
                    int origWidth = bitmap.getWidth();
                    double factor = 500.0 / origWidth;
                    int scaleHeight = (int)(bitmap.getHeight() * factor);
                    Bitmap scaledImg = Bitmap.createScaledBitmap(bitmap, 500, scaleHeight, true);
                    scaledImg.compress(Bitmap.CompressFormat.JPEG, 80, baosSmall);
                    byte[] data1 = baosOrig.toByteArray();
                    byte[] data2 = baosSmall.toByteArray();
                    StorageReference mountainImagesRef1 = mStorageRef.child("images/" + postTypes[selPostType] + "/" + key + "/original/" + name);
                    UploadTask uploadTask1 = mountainImagesRef1.putBytes(data1);
                    int finalI = i;
                    uploadTask1.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Toast.makeText(AddPostActivity.this, "Upload image " + (finalI + 1) + " failed.", Toast.LENGTH_LONG).show();
                            Log.d("xx", exception.toString());
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                            // ...
                            int res = cnt.getAndIncrement();
                            if (res == list.size() * 2 - 1) {
                                progressBar.setVisibility(View.GONE);
                                finish();
                            }
                        }
                    });
                    StorageReference mountainImagesRef2 = mStorageRef.child("images/" + postTypes[selPostType] + "/" + key + "/small/" + name);
                    UploadTask uploadTask2 = mountainImagesRef2.putBytes(data2);
                    uploadTask2.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Toast.makeText(AddPostActivity.this, "Upload image " + (finalI + 1) + " failed.", Toast.LENGTH_LONG).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                            // ...
                            int res = cnt.getAndIncrement();
                            if (res == list.size() * 2 - 1) {
                                progressBar.setVisibility(View.GONE);
                                finish();
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelableArrayList(PHOTOS, list);
        outState.putInt(SEL_TYPE, selPostType);
        outState.putInt("cnt", cnt.intValue());
        if (mStorageRef != null) {
            outState.putString("reference", mStorageRef.toString());
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        exitDialogFragment.show(getSupportFragmentManager(), "exit");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                exitDialogFragment.show(getSupportFragmentManager(), "exit");
                break;
        }
        return true;
    }


    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        finish();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        exitDialogFragment.dismiss();
    }
}