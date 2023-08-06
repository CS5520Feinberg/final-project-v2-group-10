package edu.northeastern.numad23su_team_v2_group_10_final_project.post.display_post;


import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;


import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import edu.northeastern.numad23su_team_v2_group_10_final_project.R;
import edu.northeastern.numad23su_team_v2_group_10_final_project.databinding.ActivityDisplayUserPostsBinding;
import edu.northeastern.numad23su_team_v2_group_10_final_project.message.MessageFragment;
import edu.northeastern.numad23su_team_v2_group_10_final_project.model.User;
import edu.northeastern.numad23su_team_v2_group_10_final_project.product.ProductFragment;
import edu.northeastern.numad23su_team_v2_group_10_final_project.profile.ProfileFragment;
import edu.northeastern.numad23su_team_v2_group_10_final_project.service.ServiceFragment;

public class DisplayUserPostsActivity extends AppCompatActivity {

    private TextView userPostsTitle;

    ActivityDisplayUserPostsBinding binding;
    String[] tags = {"product", "service"};

    ProductFragment productFragment;
    ServiceFragment serviceFragment;
    MessageFragment messageFragment;
    ProfileFragment profileFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_user_posts);
        binding = ActivityDisplayUserPostsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

       binding.bottomNavigationViewUserPosts.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.product) {
                    invalidateOptionsMenu();
                    replaceFragment("product");
                } else if (id == R.id.service) {
                    invalidateOptionsMenu();
                    replaceFragment("service");
                }
                return true;
            }
        });

        userPostsTitle = findViewById(R.id.user_posts_title);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();

            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
            DatabaseReference userRef = mDatabase.child("users").child(userId);
            userRef.addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        String capitalizedName = user.getName().substring(0, 1).toUpperCase() + user.getName().substring(1);
                        Log.d(TAG, "User name: " + user.getName());
                        Log.d(TAG, String.format("%s's Posts", capitalizedName));
                        userPostsTitle.setText(String.format("%s's Posts", capitalizedName));


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

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment productFragment = new ProductFragment();
        Bundle args = new Bundle();
        args.putBoolean("showCurrentUserPostsOnly", true);
        productFragment.setArguments(args);
        fragmentTransaction.add(R.id.frameLayoutUserPosts, productFragment, "product");
        fragmentTransaction.show(productFragment).commit();

    }


    private Fragment checkFragment(String tag, FragmentManager fragmentManager, FragmentTransaction fragmentTransaction) {
        Fragment fragment = fragmentManager.findFragmentByTag(tag);
        if (fragment == null) {
            if (tag.equals("product")) {
                fragment = productFragment = new ProductFragment();
                Bundle args = new Bundle();
                args.putBoolean("showCurrentUserPostsOnly", true);
                fragment.setArguments(args);
                fragmentTransaction.add(R.id.frameLayoutUserPosts, fragment, "product");

            } else if (tag.equals("service")) {
                fragment = serviceFragment = new ServiceFragment();
                Bundle args = new Bundle();
                args.putBoolean("showCurrentUserPostsOnly", true);
                fragment.setArguments(args);
                fragmentTransaction.add(R.id.frameLayoutUserPosts, serviceFragment, "service");
            }
        }
        return fragment;
    }
    private void replaceFragment(String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment fragment = checkFragment(tag, fragmentManager, fragmentTransaction);
        hideAllFragments(fragmentManager, fragmentTransaction);
        fragmentTransaction.show(fragment).commit();
    }

    private void hideAllFragments(FragmentManager fragmentManager, FragmentTransaction transaction) {
        for (String tag: tags) {
            Fragment fragment = fragmentManager.findFragmentByTag(tag);
            if (fragment != null) {
                transaction.hide(fragment);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt("SEL", binding.bottomNavigationViewUserPosts.getSelectedItemId());
        super.onSaveInstanceState(outState);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }


}


