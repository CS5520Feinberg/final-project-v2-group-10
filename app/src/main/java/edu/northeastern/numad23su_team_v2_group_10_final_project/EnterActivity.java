package edu.northeastern.numad23su_team_v2_group_10_final_project;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

import edu.northeastern.numad23su_team_v2_group_10_final_project.post.display_post.DisplayPostActivity;

public class EnterActivity extends AppCompatActivity {

    Button btn_register;
    Button btn_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter);
        btn_register = findViewById(R.id.btn_register);
        btn_login = findViewById(R.id.btn_login);

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(EnterActivity.this, LogInActivity.class);
                startActivity(i);
            }
        });

        btn_register.setOnClickListener(V -> {
            Intent i = new Intent(EnterActivity.this, RegisterActivity.class);
            startActivity(i);
        });
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey("postId")) {
            Intent i = new Intent(EnterActivity.this, MainActivity.class);
            String postType = extras.getString("postType");
            String postId = extras.getString("postId");
            String pos = extras.getString("pos");
            i.putExtra("postType", postType);
            i.putExtra("postId", postId);
            i.putExtra("pos", pos);
            startActivity(i);
        } else if (extras != null && extras.containsKey("campus")) {
            Intent i = new Intent(EnterActivity.this, MainActivity.class);
            String name = extras.getString("name");
            String userId = extras.getString("userId");
            String campus = extras.getString("campus");
            String email = extras.getString("email");
            i.putExtra("name", name);
            i.putExtra("userId", userId);
            i.putExtra("campus", campus);
            i.putExtra("email", email);
            startActivity(i);
        }
    }

//    private void showCustomActionBar() {
//        ActionBar actionBar = getSupportActionBar();
//        actionBar.setDisplayShowCustomEnabled(true);
//        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        View v = inflater.inflate(R.layout.neulogo_image, null);
//        actionBar.setCustomView(v);
//    }
}