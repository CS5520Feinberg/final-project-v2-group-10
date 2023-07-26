package edu.northeastern.numad23su_team_v2_group_10_final_project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import edu.northeastern.numad23su_team_v2_group_10_final_project.profile.ProfileFragment;

public class LogInActivity extends AppCompatActivity {

    EditText loginEmail, loginPwd;
    Button loginBtn;
    FirebaseAuth mAuth;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            Intent intent = new Intent(this, ProfileFragment.class);
            startActivity(intent);
            finish();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        TextView redirectToSignup = findViewById(R.id.signupRedirectText);
        redirectToSignup.setOnClickListener(v -> {
            RegisterActivity();
        });

        mAuth = FirebaseAuth.getInstance();
        loginEmail = findViewById(R.id.loginEmail);
        loginPwd = findViewById(R.id.loginPwd);
        loginBtn = findViewById(R.id.loginbtn);

        loginBtn.setOnClickListener(v -> {
            String email, password;
            email = loginEmail.getText().toString();
            password = loginPwd.getText().toString();

            if (TextUtils.isEmpty(email)) {
                loginEmail.setError("Email is Required");
                return;
            }
            if (TextUtils.isEmpty(password)) {
                loginPwd.setError("Password is Required");
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (mAuth.getCurrentUser().isEmailVerified()) {
                                Toast.makeText(LogInActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, MainActivity.class));
                                finish();
                            } else {
                                Toast.makeText(LogInActivity.this, "Please verify your email address", Toast.LENGTH_SHORT).show();

                            }
                        } else {
                            Toast.makeText(LogInActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        });
    }

    private void RegisterActivity() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
        finish();
    }

}