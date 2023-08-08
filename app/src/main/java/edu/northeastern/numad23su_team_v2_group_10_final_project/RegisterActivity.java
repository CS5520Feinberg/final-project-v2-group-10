package edu.northeastern.numad23su_team_v2_group_10_final_project;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.dhaval2404.imagepicker.ImagePicker;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.regex.Pattern;

import edu.northeastern.numad23su_team_v2_group_10_final_project.util.FirebaseUtil;
import edu.northeastern.numad23su_team_v2_group_10_final_project.model.User;

public class RegisterActivity extends AppCompatActivity {

    EditText signupUsername, signupEmail, signupPwd, signupConfirmPwd;

    String[] campus = {"Arlington", "Boston", "Charlotte", "Miami", "Oakland", "Portland", "San Francisco", "Silicon Valley", "Seattle"};

    ImageView imageView;
    AutoCompleteTextView autoCompleteTextView;

    ArrayAdapter<String> adapterCampus;
    Button signupBtn;

    FloatingActionButton floatingActionButton;
    FirebaseAuth mAuth;

    private DatabaseReference mDatabase;

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
        floatingActionButton = findViewById(R.id.floatingActionButton);

        String defaultImageUri = "https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_640.png";
        Picasso.get().load(defaultImageUri).into(imageView);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        floatingActionButton.setOnClickListener(v -> {
            ImagePicker.Companion.with(this)
                    .crop(10,10)
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
            if (TextUtils.isEmpty(email) && !validEmail(email)) {
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

            User newUser = new User("", username, email, selectCampus);
            registerUser(email, password, newUser, username);
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

    private void registerUser(String email, String password, User newUser, String username) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (task.isSuccessful()) {
                        updateUserProfile(username);
                        newUser.userId = user.getUid();
                        sendEmailVerification(user);
                        String userid = user.getUid();
                        mDatabase.child("users").child(userid).setValue(newUser);
                        FirebaseUtil.currentUserDetails().set(newUser);
                        uploadUserImage(userid);
                        LoginActivity();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Failed to register user!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendEmailVerification(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(task -> Toast.makeText(RegisterActivity.this, "Please check your email for verifying", Toast.LENGTH_SHORT).show());
    }

    private void uploadUserImage(String userid) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        String path = "images/avatar/" + userid + "/000.jpg";
        StorageReference avatarRef = storageRef.child(path);

        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = avatarRef.putBytes(data);
        uploadTask.addOnFailureListener(exception -> {
            Toast.makeText(RegisterActivity.this, "Failed to upload Image!", Toast.LENGTH_SHORT).show();
        }).addOnSuccessListener(taskSnapshot -> {
            Toast.makeText(RegisterActivity.this, "Upload Image Successfully!", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateUserProfile(String username) {
        FirebaseUser user = mAuth.getCurrentUser();
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(username).build();
        user.updateProfile(profileUpdates).addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()) {
                Toast.makeText(RegisterActivity.this, "User Profile Updated", Toast.LENGTH_SHORT).show();
            }
        });
    }
}