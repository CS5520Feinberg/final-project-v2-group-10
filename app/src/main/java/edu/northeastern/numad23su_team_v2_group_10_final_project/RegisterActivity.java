package edu.northeastern.numad23su_team_v2_group_10_final_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    EditText signupUsername, signupEmail, signupPwd, signupConfirmPwd;
    Button signupBtn;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        TextView redirectToLogin = findViewById(R.id.loginRedirectText);
        redirectToLogin.setOnClickListener(v -> {
            LoginActivity();
        });

        mAuth = FirebaseAuth.getInstance();

        signupUsername = findViewById(R.id.signupUsername);
        signupEmail = findViewById(R.id.signupEmail);
        signupPwd = findViewById(R.id.signupPwd);
        signupConfirmPwd = findViewById(R.id.signupConfirmPwd);
        signupBtn = findViewById(R.id.signupBtn);

        signupBtn.setOnClickListener(v -> {
            String username, email, password, confirmPwd;
            username = signupUsername.getText().toString();
            email = signupEmail.getText().toString();
            password = signupPwd.getText().toString();
            confirmPwd = signupConfirmPwd.getText().toString();

            if (TextUtils.isEmpty(username)) {
                signupUsername.setError("Username is Required");
                return;
            }
            if (TextUtils.isEmpty(email) || !validEmail(email)) {
                signupEmail.setError("Valid NEU Email is Required");
                return;
            }
            if (TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPwd)) {
                signupPwd.setError("Password is Required");
                return;
            }
            if (!password.equals(confirmPwd)) {
                signupConfirmPwd.setError("Password Do not Match");
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            mAuth.getCurrentUser().sendEmailVerification()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(RegisterActivity.this, "Register Successfully, please check your email for verifying", Toast.LENGTH_SHORT).show();

                                            } else {
                                                Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }


    private void LoginActivity() {
        Intent intent = new Intent(this, LogInActivity.class);
        startActivity(intent);
        finish();
    }

    private boolean validEmail(String emailAddress) {
        String regexPattern = "^(.+)@northeastern.edu";
        Pattern pattern = Pattern.compile(regexPattern);
        return pattern.matcher(emailAddress).matches();
    }

}