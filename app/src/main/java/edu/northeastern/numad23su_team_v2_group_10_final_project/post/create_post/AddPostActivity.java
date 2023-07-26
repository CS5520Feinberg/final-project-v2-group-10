package edu.northeastern.numad23su_team_v2_group_10_final_project.post.create_post;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;

import java.util.ArrayList;
import java.util.List;

import edu.northeastern.numad23su_team_v2_group_10_final_project.R;

public class AddPostActivity extends AppCompatActivity {
    Button btnAddImage;
    RecyclerView recyclerView;
    ArrayList<UploadImage> list = new ArrayList<>();
    ActivityResultLauncher<Intent> queryLauncher;
    ImageAdapter adapter;
    int width;
    private final static int PERMISSION_STORAGE = 99;
    final int COL = 5;
    static final String PHOTOS = "PHOTOS";
    List<RadioButton> btnList = new ArrayList<>();
    int selectedType = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);
        if (ContextCompat.checkSelfPermission(AddPostActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(AddPostActivity.this,new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE} ,PERMISSION_STORAGE);
        }

        btnAddImage = findViewById(R.id.btn_add_image);
        recyclerView = findViewById(R.id.recycler_view);
        RadioButton btn_offer_product = findViewById(R.id.btn_offer_product);
        RadioButton btn_need_product = findViewById(R.id.btn_need_product);
        RadioButton btn_offer_service = findViewById(R.id.btn_offer_service);
        RadioButton btn_need_service = findViewById(R.id.btn_need_service);
        btnList.add(btn_offer_product);
        btnList.add(btn_need_product);
        btnList.add(btn_offer_service);
        btnList.add(btn_need_service);
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(PHOTOS)) {
                list = savedInstanceState.getParcelableArrayList(PHOTOS);
                for (int i = 0;i < list.size(); i++) {
                    list.get(i).width = width;
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
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelableArrayList(PHOTOS, list);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
    }
}