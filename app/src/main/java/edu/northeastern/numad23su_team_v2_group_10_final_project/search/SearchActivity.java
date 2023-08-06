package edu.northeastern.numad23su_team_v2_group_10_final_project.search;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;

import edu.northeastern.numad23su_team_v2_group_10_final_project.R;
import edu.northeastern.numad23su_team_v2_group_10_final_project.public_fragments.FSAdapter;
import edu.northeastern.numad23su_team_v2_group_10_final_project.public_fragments.TempPostFragment;

public class SearchActivity extends AppCompatActivity {
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
        if (extras != null && extras.containsKey("query")) {
            query = extras.getString("query");
        }
        viewModel.selectItem(query);
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        ViewPager2 viewpager = findViewById(R.id.view_pager);
        FSAdapter vpAdapter = new FSAdapter(this);
        vpAdapter.addFragment(TempPostFragment.newInstance(query, 0, false));
        vpAdapter.addFragment(TempPostFragment.newInstance(query, 2, false));
        vpAdapter.addFragment(TempPostFragment.newInstance(query, 1, false));
        vpAdapter.addFragment(TempPostFragment.newInstance(query, 3, false));
        viewpager.setAdapter(vpAdapter);
        ArrayList<String> fragmentTitle = new ArrayList<>();
        fragmentTitle.add("products");
        fragmentTitle.add("services");
        fragmentTitle.add("products in need");
        fragmentTitle.add("services in need");
        new TabLayoutMediator(tabLayout, viewpager,
                (tab, position) -> tab.setText(fragmentTitle.get(position))
        ).attach();

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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_top, menu);

        searchItem = menu.findItem(R.id.search);

        searchView = (SearchView) searchItem.getActionView();
        searchView.setIconifiedByDefault(false);
        searchItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
                searchItem.expandActionView();
                return true;
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                viewModel.selectItem(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        searchView.setQuery(query, true);
        searchItem.expandActionView();
        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(@NonNull MenuItem menuItem) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(@NonNull MenuItem menuItem) {
                SearchActivity.this.finish();
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }
}