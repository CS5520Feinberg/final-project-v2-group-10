package edu.northeastern.numad23su_team_v2_group_10_final_project.post.display_post;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import android.view.MenuItem;


import edu.northeastern.numad23su_team_v2_group_10_final_project.R;
import edu.northeastern.numad23su_team_v2_group_10_final_project.product.ProductFragment;

public class DisplayUserPostsActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_user_posts);


        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment productFragment = new ProductFragment();
        Bundle args = new Bundle();
        args.putBoolean("showCurrentUserPostsOnly", true);
        productFragment.setArguments(args);
        fragmentTransaction.add(R.id.frameLayoutUserPosts, productFragment, "product");
        fragmentTransaction.show(productFragment).commit();

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


