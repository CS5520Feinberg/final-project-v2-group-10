package edu.northeastern.numad23su_team_v2_group_10_final_project.post.create_post;

import static edu.northeastern.numad23su_team_v2_group_10_final_project.post.Utils.getPath;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ActionMenuView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.os.EnvironmentCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;


import com.google.android.material.bottomappbar.BottomAppBar;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import edu.northeastern.numad23su_team_v2_group_10_final_project.R;

public class UploadImageActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    BottomAppBar bottomAppBar;
    ActionMenuView actionMenuView;
    MenuItem editItem;
    MenuItem deleteItem;
    MenuItem cancelItem;
    MenuItem finishItem;
    MenuItem cameraItem;
    Button fab;
    Boolean isInEdit = false;

    ImageAdapter adapter;
    ArrayList<UploadImage> list = new ArrayList<>();

    private Uri mCameraUri;
    private String mCameraImagePath;
    private boolean isAndroidQ = Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q;

    private final static int PERMISSION_STORAGE = 99;
    private static final int PERMISSION_CAMERA_REQUEST_CODE = 98;
    private final static int IMAGE_QUEST = 100;
    private final static int CAMERA_REQUEST_CODE = 101;
    final static int COL = 2;
    final static String PHOTOS = "PHOTOS";
    final static String IN_EDIT = "IN_EDIT";
    int width;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_image);
        if (ContextCompat.checkSelfPermission(UploadImageActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(UploadImageActivity.this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_STORAGE);
        }
        getSupportActionBar().setTitle("select photos");
        fab = findViewById(R.id.addButton);
        recyclerView = findViewById(R.id.recycler_view);
        bottomAppBar = findViewById(R.id.bottomAppBar);
        actionMenuView = findViewById(R.id.additional_menu);
        this.getMenuInflater().inflate(R.menu.upload_image_left, actionMenuView.getMenu());
        editItem = bottomAppBar.getMenu().findItem(R.id.edit);
        deleteItem = actionMenuView.getMenu().findItem(R.id.delete);
        cancelItem = bottomAppBar.getMenu().findItem(R.id.cancel);
        cameraItem = actionMenuView.getMenu().findItem(R.id.camera);
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey(PHOTOS)) {
            list.clear();
            list.addAll(extras.getParcelableArrayList(PHOTOS));
        }
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(PHOTOS)) {
                list.clear();
                list.addAll(savedInstanceState.getParcelableArrayList(PHOTOS));
            }
            if (savedInstanceState.containsKey(IN_EDIT)) {
                isInEdit = savedInstanceState.getBoolean(IN_EDIT);
            }
        }
        for (int i = 0; i < list.size(); i++) {
            list.get(i).width = width;
        }
        switchMode();
        adapter = new ImageAdapter(this, list);
        ImageClickListener clickListener = new ImageClickListener() {
            @Override
            public void onClick(int position) {
                list.get(position).isSelected = !list.get(position).isSelected;
            }
        };
        adapter.setListener(clickListener);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, COL, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapter);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });
        editItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
                isInEdit = true;
                switchMode();
                for (UploadImage uploadImage : list) {
                    uploadImage.isInEditMode = true;
                }
                adapter.notifyItemRangeChanged(0, list.size());
                return true;
            }
        });
        cancelItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
                isInEdit = false;
                for (UploadImage uploadImage : list) {
                    uploadImage.isInEditMode = false;
                }
                adapter.notifyItemRangeChanged(0, list.size());
                switchMode();
                return true;
            }
        });
        deleteItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
                for (int i = list.size() - 1; i >= 0; i--) {
                    if (list.get(i).isSelected) {
                        list.remove(i);
                        adapter.notifyItemRemoved(i);
                    }
                }

                return true;
            }
        });

        cameraItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
                checkPermissionAndCamera();
                return true;
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.upload_image_top, menu);
        finishItem = menu.findItem(R.id.finish);
        finishItem.setVisible(!isInEdit);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // for top menu
        if (item.getItemId() == R.id.finish) {
            this.onBackPressed();
        }
        return true;
    }

    private void switchMode() {
        if (isInEdit) {
            fab.setVisibility(View.GONE);
        } else {
            fab.setVisibility(View.VISIBLE);
        }
        editItem.setVisible(!isInEdit);
        cameraItem.setVisible(!isInEdit);
        deleteItem.setVisible(isInEdit);
        cancelItem.setVisible(isInEdit);
        if (finishItem != null) finishItem.setVisible(!isInEdit);
    }

    private void selectImage() {
        Intent intent;
        intent = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, IMAGE_QUEST);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_QUEST && data != null && data.getData() != null) {
            ViewGroup parent = (ViewGroup) recyclerView.getParent();
            width = parent.getWidth();
            int count = data.getClipData().getItemCount();
            for (int i = 0; i < count; i++) {
                Uri imageUri = data.getClipData().getItemAt(i).getUri();
                list.add(new UploadImage(imageUri.toString(), getPath(this, imageUri), width / 2));
            }
            adapter.notifyItemRangeInserted(list.size() - count, count);
        } else if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                ViewGroup parent = (ViewGroup) recyclerView.getParent();
                width = parent.getWidth();
                list.add(new UploadImage(mCameraUri.toString(), getPath(this, mCameraUri), width / 2));
            } else {
                Toast.makeText(this, "Cancel", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean(IN_EDIT, isInEdit);
        outState.putParcelableArrayList(PHOTOS, list);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        Intent dt = new Intent();
        for (UploadImage uploadImage : list) {
            uploadImage.isInEditMode = false;
        }
        dt.putParcelableArrayListExtra("PHOTOS", list);
        setResult(RESULT_OK, dt);
        this.finish();
    }

    // camera ref: https://juejin.cn/post/6844903944095793166
    private void checkPermissionAndCamera() {
        int hasCameraPermission = ContextCompat.checkSelfPermission(getApplication(),
                Manifest.permission.CAMERA);
        if (hasCameraPermission == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    PERMISSION_CAMERA_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Need camera permission.", Toast.LENGTH_LONG).show();
            }
        }
    }


    private void openCamera() {
        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File photoFile = null;
        Uri photoUri = null;
        if (isAndroidQ) {
            photoUri = createImageUri();
        } else {
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (photoFile != null) {
                mCameraImagePath = photoFile.getAbsolutePath();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    photoUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
                } else {
                    photoUri = Uri.fromFile(photoFile);
                }
            }
        }

        mCameraUri = photoUri;
        if (photoUri != null) {
            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            captureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            startActivityForResult(captureIntent, CAMERA_REQUEST_CODE);
        }

    }

    private Uri createImageUri() {
        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            return getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
        } else {
            return getContentResolver().insert(MediaStore.Images.Media.INTERNAL_CONTENT_URI, new ContentValues());
        }
    }

    private File createImageFile() throws IOException {
        String imageName = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (!storageDir.exists()) {
            storageDir.mkdir();
        }
        File tempFile = new File(storageDir, imageName);
        if (!Environment.MEDIA_MOUNTED.equals(EnvironmentCompat.getStorageState(tempFile))) {
            return null;
        }
        return tempFile;
    }
}