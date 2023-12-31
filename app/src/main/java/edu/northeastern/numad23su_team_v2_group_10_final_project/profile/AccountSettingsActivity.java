package edu.northeastern.numad23su_team_v2_group_10_final_project.profile;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

import edu.northeastern.numad23su_team_v2_group_10_final_project.LogInActivity;
import edu.northeastern.numad23su_team_v2_group_10_final_project.R;
import edu.northeastern.numad23su_team_v2_group_10_final_project.model.User;
import edu.northeastern.numad23su_team_v2_group_10_final_project.util.FirebaseUtil;

public class AccountSettingsActivity extends AppCompatActivity {

    EditText updateUsername, updateEmail, updatePwd, updateConfirmPwd;

    String[] campus = {"Arlington", "Boston", "Charlotte", "Miami", "Oakland", "Portland", "San Francisco", "Silicon Valley", "Seattle"};

    ImageView avatar;
    AutoCompleteTextView autoCompleteTextView;

    ArrayAdapter<String> adapterCampus;
    Button updateProfleBtn;

    FloatingActionButton floatingActionButton;
    FirebaseAuth mAuth;

    private String originalEmail;

    private DatabaseReference mDatabase;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // The request code should match with the one used in the ImagePicker start() method
        if (requestCode == ImagePicker.REQUEST_CODE && resultCode == RESULT_OK && data != null) {

            // Get the Uri of the selected image
            Uri uri = data.getData();

            // Load the image into the ImageView
            Glide.with(this)
                    .load(uri)
                    .into(avatar);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);
//        showCustomActionBar();

        updateProfleBtn = findViewById(R.id.updateProfileBtn);
        updateUsername = findViewById(R.id.updateUsername);
        updateEmail = findViewById(R.id.updateEmail);
        updatePwd = findViewById(R.id.updatePwd);
        updateConfirmPwd = findViewById(R.id.updateConfirmPwd);
        autoCompleteTextView = findViewById(R.id.updateCampus);
        adapterCampus = new ArrayAdapter<>(this, R.layout.list_campus, campus);
        autoCompleteTextView.setAdapter(adapterCampus);
        avatar = findViewById(R.id.imageView);
        floatingActionButton = findViewById(R.id.floatingActionButton);

        floatingActionButton.setOnClickListener(v -> {
            ImagePicker.Companion.with(this)
                    .crop(10,10)//Crop image(Optional), Check Customization for more option
                    .compress(1024)            //Final image size will be less than 1 MB(Optional)
                    .maxResultSize(1080, 1080)    //Final image resolution will be less than 1080 x 1080(Optional)
                    .start();
        });


        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            originalEmail = firebaseUser.getEmail();
            String userId = firebaseUser.getUid();

            DocumentReference userFireRef = FirebaseUtil.currentUserDetails();

            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
            DatabaseReference userRef = mDatabase.child("users").child(userId);
            userRef.addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        Log.d(TAG, "User name (Realtime Database): " + user.getName());
                        Log.d(TAG, "User email (Realtime Database): " + user.getEmail());
                        Log.d(TAG, "User campus (Realtime Database): " + user.getCampus());
                        updateUsername.setText(user.getName());
                        updateEmail.setText(user.getEmail());
                        autoCompleteTextView.setText(user.getCampus(), false);
                    } else {
                        Log.w(TAG, "No user data found.");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.w(TAG, "Failed to read value.", error.toException());
                }
            });

            userFireRef.addSnapshotListener((documentSnapshot, e) -> {
                if (e != null) {
                    Log.w(TAG, "Error getting Firestore document.", e);
                    return;
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    User userFirestore = documentSnapshot.toObject(User.class);
                    if (userFirestore != null) {
                        Log.d(TAG, "User name (Firestore): " + userFirestore.getName());
                        Log.d(TAG, "User email (Firestore): " + userFirestore.getEmail());
                        Log.d(TAG, "User campus (Firestore): " + userFirestore.getCampus());
                    } else {
                        Log.w(TAG, "No user data found in Firestore.");
                    }
                }
            });
        } else {
            Log.d(TAG, "No current user.");
        }

        StorageReference storageRef1 = FirebaseStorage.getInstance().getReference().child("images")
                .child("avatar").child(firebaseUser.getUid()).child("000.jpg");
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
        updateProfleBtn.setOnClickListener(v -> {
            String username, email, password, confirmPwd, selectCampus;
            username = updateUsername.getText().toString();
            email = updateEmail.getText().toString();
            password = updatePwd.getText().toString();
            confirmPwd = updateConfirmPwd.getText().toString();
            selectCampus = autoCompleteTextView.getText().toString();

            if (TextUtils.isEmpty(username)) {
                updateUsername.setError("Username is Required");
                return;
            }
            if (TextUtils.isEmpty(email)) {
                updateEmail.setError("Valid NEU Email is Required");
                return;
            }
            if (!password.equals(confirmPwd)) {
                updateConfirmPwd.setError("Password Do not Match");
                return;
            }

            if (selectCampus.equals("")) {
                Toast.makeText(AccountSettingsActivity.this, "Please choose your campus", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            // Update username and selectCampus in Realtime Database
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
            HashMap<String, Object> updates = new HashMap<>();
            updates.put("name", username);
            updates.put("campus", selectCampus);
            updates.put("email", email);


            mDatabase.child("users").child(user.getUid()).updateChildren(updates)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "User updated.");
                            } else {
                                Log.w(TAG, "Update failed", task.getException());
                            }
                        }
                    });

            DocumentReference mFirestore = FirebaseUtil.currentUserDetails();
            User userFirestore = new User(FirebaseUtil.currentUserId(), username, email, selectCampus);
            mFirestore.set(userFirestore).addOnSuccessListener(aVoid -> Log.d(TAG, "User data updated in Firestore"))
                    .addOnFailureListener(e -> Log.e(TAG, "Error updating user data in Firestore", e));

            uploadUserImage(user.getUid());

            if (!originalEmail.equals(email)) {

                // Update email and password with Firebase Authentication
                user.updateEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "User email address updated.");
                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                    user.sendEmailVerification();
                                    if (!password.isEmpty()) {
                                        updatePassword(user, password);
                                    }

                                } else {
                                    Log.w(TAG, "Update failed", task.getException());
                                }
                            }
                        });
            } else {
                if (!password.isEmpty()) {
                    updatePassword(user, password);
                }
            }


        });


    }


    private void updatePassword(FirebaseUser user, String password) {

        user.updatePassword(password)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User password updated.");


                            // Sign out the user
                            FirebaseAuth.getInstance().signOut();


                            // Navigate user to the login screen
                            Intent intent = new Intent(AccountSettingsActivity.this, LogInActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(AccountSettingsActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            Log.w(TAG, "Update failed", task.getException());
                        }
                    }
                });

    }

    private void uploadUserImage(String userid) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        String path = "images/avatar/" + userid + "/000.jpg";
        StorageReference avatarRef = storageRef.child(path);

        Bitmap bitmap = ((BitmapDrawable) avatar.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = avatarRef.putBytes(data);
        uploadTask.addOnFailureListener(exception -> {
            Toast.makeText(AccountSettingsActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
        }).addOnSuccessListener(taskSnapshot -> {
            Toast.makeText(AccountSettingsActivity.this, "Upload Image Successfully!", Toast.LENGTH_SHORT).show();

        });
    }

//    private void showCustomActionBar() {
//        ActionBar actionBar = getSupportActionBar();
//        actionBar.setDisplayShowCustomEnabled(true);
//        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        View v = inflater.inflate(R.layout.neulogo_image, null);
//        actionBar.setCustomView(v);
//    }

}
