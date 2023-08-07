package edu.northeastern.numad23su_team_v2_group_10_final_project.profile;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import edu.northeastern.numad23su_team_v2_group_10_final_project.LogInActivity;
import edu.northeastern.numad23su_team_v2_group_10_final_project.R;
import edu.northeastern.numad23su_team_v2_group_10_final_project.UserViewModel;
import edu.northeastern.numad23su_team_v2_group_10_final_project.model.User;
import edu.northeastern.numad23su_team_v2_group_10_final_project.search.DisplayUserPostListActivity;
import edu.northeastern.numad23su_team_v2_group_10_final_project.search.ItemViewModel;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    private UserViewModel userViewModel;
    private TextView userIdView;
    private Button accountSettingButton;

    private TextView userName;
    private TextView userEmail;
    private String userId;
    private Button userPosts;

    private FirebaseAuth mAuth;
    private ImageView userImage;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private FloatingActionButton floatingActionButton;
    Activity logout;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        userIdView = view.findViewById(R.id.user_id);
        userViewModel = new ViewModelProvider(getActivity()).get(UserViewModel.class);
        userViewModel.getUser().observe(getViewLifecycleOwner(), userId -> {
            // update UI here
            this.userId = userId;
        });
        logout = getActivity();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
            DatabaseReference userRef = mDatabase.child("users").child(userId);
            userRef.addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User dbuser = snapshot.getValue(User.class);
                    if (dbuser != null) {
                        Log.d(TAG, "User name: " + dbuser.getName());
                        userName = view.findViewById(R.id.userName);
                        userEmail = view.findViewById(R.id.userEmail);
                        userName.setText(dbuser.getName());
                        userEmail.setText(dbuser.getEmail());

                    } else {
                        Log.w(TAG, "No user data found.");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.w(TAG, "Failed to read value.", error.toException());
                }
            });
        } else {
            Log.d(TAG, "No current user.");
        }



        userPosts = view.findViewById(R.id.userPosts);

        userPosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), DisplayUserPostListActivity.class);
                i.putExtra("USER", userId);
                startActivity(i);
            }
        });

        userImage = view.findViewById(R.id.userImage);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        String path = "images/avatar/" + user.getUid() + "/000.jpg";
        StorageReference storageRef = storage.getReference(path);
        try {
            File localfile = File.createTempFile("tempfile", ".jpg");
            storageRef.getFile(localfile)
                    .addOnSuccessListener(taskSnapshot -> {
                        Bitmap bitmap = BitmapFactory.decodeFile(localfile.getAbsolutePath());
                        userImage.setImageBitmap(bitmap);
                    }).addOnFailureListener(e -> Toast.makeText(logout, e.getMessage(), Toast.LENGTH_SHORT).show());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        floatingActionButton = view.findViewById(R.id.floatingActionButton);

        floatingActionButton.setOnClickListener(v -> {
            ImagePicker.with(this)
                    .crop()//Crop image(Optional), Check Customization for more option
                    .compress(1024)			//Final image size will be less than 1 MB(Optional)
                    .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                    .start();
        });


        accountSettingButton = view.findViewById(R.id.accountSetting);
        accountSettingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AccountSettingsActivity.class);
                startActivity(intent);
            }
        });

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // The request code should match with the one used in the ImagePicker start() method
        if (requestCode == ImagePicker.REQUEST_CODE && resultCode == RESULT_OK && data != null) {

            // Get the Uri of the selected image
            Uri uri = data.getData();

            // Load the image into the ImageView
            Glide.with(this)
                    .asBitmap()
                    .load(uri)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            userImage.setImageBitmap(resource);
                            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                            uploadUserImage(firebaseUser.getUid(), resource);

                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                        }
                    });



        }
    }

    public void onStart() {
        super.onStart();
        Button logoutBtn = logout.findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(logout, LogInActivity.class);
            startActivity(intent);
        });
    }

    private void uploadUserImage(String userid, Bitmap bitmap) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        String path = "images/avatar/" + userid + "/000.jpg";
        StorageReference avatarRef = storageRef.child(path);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = avatarRef.putBytes(data);
        uploadTask.addOnFailureListener(exception -> {
            Toast.makeText(getContext(), exception.getMessage(), Toast.LENGTH_SHORT).show();
        }).addOnSuccessListener(taskSnapshot -> {
            Toast.makeText(getContext(), "Upload Image Successfully!", Toast.LENGTH_SHORT).show();
        });
    }

}