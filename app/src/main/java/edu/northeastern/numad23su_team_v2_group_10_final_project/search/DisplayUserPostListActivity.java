package edu.northeastern.numad23su_team_v2_group_10_final_project.search;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;

import edu.northeastern.numad23su_team_v2_group_10_final_project.R;
import edu.northeastern.numad23su_team_v2_group_10_final_project.public_fragments.FSAdapter;
import edu.northeastern.numad23su_team_v2_group_10_final_project.public_fragments.TempPostFragment;

public class DisplayUserPostListActivity extends AppCompatActivity {
    private ItemViewModel viewModel;
    MenuItem searchItem;
    String query;
    SearchView searchView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ItemViewModel.class);
        setContentView(R.layout.activity_search);
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey("USER")) {
            query = extras.getString("USER");
        }
        viewModel.selectItem(query);
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        ViewPager2 viewpager = findViewById(R.id.view_pager);
        FSAdapter vpAdapter = new FSAdapter(this);
        vpAdapter.addFragment(TempPostFragment.newInstance(query, 0));
        vpAdapter.addFragment(TempPostFragment.newInstance(query, 2));
        vpAdapter.addFragment(TempPostFragment.newInstance(query, 1));
        vpAdapter.addFragment(TempPostFragment.newInstance(query, 3));
        viewpager.setAdapter(vpAdapter);
        ArrayList<String> fragmentTitle = new ArrayList<>();
        fragmentTitle.add("products");
        fragmentTitle.add("services");
        fragmentTitle.add("products in need");
        fragmentTitle.add("services in need");
        new TabLayoutMediator(tabLayout, viewpager,
                (tab, position) -> tab.setText(fragmentTitle.get(position))
        ).attach();


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewpager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
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