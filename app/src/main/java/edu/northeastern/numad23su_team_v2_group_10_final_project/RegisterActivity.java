package edu.northeastern.numad23su_team_v2_group_10_final_project;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    EditText signupUsername, signupEmail, signupPwd, signupConfirmPwd;

    String[] campus = {"Arlington", "Boston", "Charlotte", "Miami", "Oakland", "Portland", "San Francisco", "Silicon Valley", "Seattle"};

    ImageView imageView;
    AutoCompleteTextView autoCompleteTextView;

    ArrayAdapter<String> adapterCampus;
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
        autoCompleteTextView = findViewById(R.id.selectCampus);
        adapterCampus = new ArrayAdapter<>(this, R.layout.list_campus, campus);
        autoCompleteTextView.setAdapter(adapterCampus);
        imageView = findViewById(R.id.imageView);



        autoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
            String campusItem = adapterCampus.getItem(position);
            Toast.makeText(RegisterActivity.this, "Item" + campusItem, Toast.LENGTH_SHORT).show();
        });

        imageView.setOnClickListener(v -> {
            ImagePicker.with(this)
                    .crop()	    			//Crop image(Optional), Check Customization for more option
                    .compress(1024)			//Final image size will be less than 1 MB(Optional)
                    .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                    .start();
        });


        signupBtn.setOnClickListener(v -> {
            String username, email, password, confirmPwd, selectCampus;
            username = signupUsername.getText().toString();
            email = signupEmail.getText().toString();
            password = signupPwd.getText().toString();
            confirmPwd = signupConfirmPwd.getText().toString();
            selectCampus = autoCompleteTextView.getText().toString();

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

            if (selectCampus.equals("")) {
                Toast.makeText(RegisterActivity.this, "Please choose your campus", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            mAuth.getCurrentUser().sendEmailVerification()
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            Toast.makeText(RegisterActivity.this, "Register Successfully, please check your email for verifying", Toast.LENGTH_SHORT).show();

                                        } else {
                                            Toast.makeText(RegisterActivity.this, task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri uri = data.getData();
        imageView.setImageURI(uri);
    }
}