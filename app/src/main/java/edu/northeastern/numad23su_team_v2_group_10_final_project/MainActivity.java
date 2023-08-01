package edu.northeastern.numad23su_team_v2_group_10_final_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationBarView;

import edu.northeastern.numad23su_team_v2_group_10_final_project.databinding.ActivityMainBinding;
import edu.northeastern.numad23su_team_v2_group_10_final_project.message.MessageFragment;
import edu.northeastern.numad23su_team_v2_group_10_final_project.product.ProductFragment;
import edu.northeastern.numad23su_team_v2_group_10_final_project.profile.ProfileFragment;
import edu.northeastern.numad23su_team_v2_group_10_final_project.search.ItemViewModel;
import edu.northeastern.numad23su_team_v2_group_10_final_project.search.SearchActivity;
import edu.northeastern.numad23su_team_v2_group_10_final_project.service.ServiceFragment;

public class MainActivity extends AppCompatActivity {
    private ItemViewModel viewModel;
    ActivityMainBinding binding;
    MenuItem searchItem;
    ProductFragment productFragment;
    ServiceFragment serviceFragment;
    MessageFragment messageFragment;
    ProfileFragment profileFragment;
    String[] tags = {"product", "service", "message", "profile"};
    Boolean showSearch = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FinalProjectApplication myApplication = (FinalProjectApplication) getApplicationContext();
        viewModel = new ViewModelProvider(this).get(ItemViewModel.class);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.product) {
                    showSearch = true;
                    invalidateOptionsMenu();
                    replaceFragment("product");
                } else if (id == R.id.service) {
                    showSearch = true;
                    invalidateOptionsMenu();
                    replaceFragment("service");
                } else if (id == R.id.message) {
                    showSearch = false;
                    invalidateOptionsMenu();
                    replaceFragment("message");
                } else if (id == R.id.user) {
                    showSearch = false;
                    invalidateOptionsMenu();
                    replaceFragment("profile");
                }
                return true;
            }
        });
        // TODO: Add condition for intent extra
        if (savedInstanceState == null || !savedInstanceState.containsKey("SEL")) {
            View view = binding.bottomNavigationView.findViewById(R.id.product);
            view.performClick();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_top, menu);
        searchItem = menu.findItem(R.id.search);
        searchItem.setVisible(showSearch);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Search posts.");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if (s.trim().length() < 3) {
                    Toast.makeText(MainActivity.this, "Query length should >= 3", Toast.LENGTH_LONG).show();
                    return true;
                }
                Intent i = new Intent(MainActivity.this, SearchActivity.class);
                i.putExtra("query", s.trim());
                startActivity(i);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        return true;
    }

    private void replaceFragment(String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment fragment = checkFragment(tag, fragmentManager, fragmentTransaction);
        hideAllFragments(fragmentManager, fragmentTransaction);
        fragmentTransaction.show(fragment).commit();
    }

    private Fragment checkFragment(String tag, FragmentManager fragmentManager, FragmentTransaction fragmentTransaction) {
        Fragment fragment = fragmentManager.findFragmentByTag(tag);
        if (fragment == null) {
            if (tag.equals("product")) {
                fragment = productFragment = new ProductFragment();
                fragmentTransaction.add(R.id.frameLayout, productFragment, "product");
            } else if (tag.equals("service")) {
                fragment = serviceFragment = new ServiceFragment();
                fragmentTransaction.add(R.id.frameLayout, serviceFragment, "service");
            } else if (tag.equals("message")) {
                fragment = messageFragment = new MessageFragment();
                fragmentTransaction.add(R.id.frameLayout, messageFragment, "message");
            } else if (tag.equals("profile")) {
                fragment = profileFragment = new ProfileFragment();
                fragmentTransaction.add(R.id.frameLayout, profileFragment, "profile");
            }
        }
        return fragment;
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
        outState.putInt("SEL", binding.bottomNavigationView.getSelectedItemId());
        super.onSaveInstanceState(outState);
    }
}