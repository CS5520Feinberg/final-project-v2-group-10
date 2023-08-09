package edu.northeastern.numad23su_team_v2_group_10_final_project;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

public class EnterActivity extends AppCompatActivity {

    Button btn_register;
    Button btn_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter);
        showCustomActionBar();
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
    }

    private void showCustomActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.neulogo_image, null);
        actionBar.setCustomView(v);
    }
}