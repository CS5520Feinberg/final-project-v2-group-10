package edu.northeastern.numad23su_team_v2_group_10_final_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;

import android.os.Build;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import edu.northeastern.numad23su_team_v2_group_10_final_project.databinding.ActivityMainBinding;
import edu.northeastern.numad23su_team_v2_group_10_final_project.message.MessageFragment;
import edu.northeastern.numad23su_team_v2_group_10_final_project.product.ProductFragment;
import edu.northeastern.numad23su_team_v2_group_10_final_project.profile.ProfileFragment;
import edu.northeastern.numad23su_team_v2_group_10_final_project.search.SearchActivity;
import edu.northeastern.numad23su_team_v2_group_10_final_project.service.ServiceFragment;

public class MainActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1252;
    private UserViewModel userViewModel;
    ActivityMainBinding binding;
    MenuItem searchItem;
    ProductFragment productFragment;
    ServiceFragment serviceFragment;
    MessageFragment messageFragment;
    ProfileFragment profileFragment;
    String[] tags = {"product", "service", "message", "profile"};
    Boolean showSearch = true;
    boolean jumpFromPost = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FinalProjectApplication myApplication = (FinalProjectApplication) getApplicationContext();
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        createNotificationChannel();
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w("Main", "Fetching FCM registration token failed", task.getException());
                            return;
                        }
                        // Get new FCM registration token
                        String token = task.getResult();
                        FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getUid()).update("token", token);
                    }
                });

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        userViewModel.setUser(FirebaseAuth.getInstance().getUid());


        System.out.println(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION));
        // Check for location permission
        System.out.println(PackageManager.PERMISSION_GRANTED);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request location permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            fetchLastKnownLocation();
        }

        binding.bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                jumpFromPost = false;
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
                    userViewModel.setUser(FirebaseAuth.getInstance().getUid());
                    invalidateOptionsMenu();
                    replaceFragment("profile");
                }
                return true;
            }
        });
        // TODO: Add condition for intent extra

        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey("USER")) {
            // highlighting without clicking
            String userId = extras.getString("USER");
            userViewModel.setUser(userId);
            MenuItem menuItem = binding.bottomNavigationView.getMenu().findItem(R.id.user);
            menuItem.setChecked(true);
            invalidateOptionsMenu();
            replaceFragment("profile");
        } else {
            if (savedInstanceState == null || !savedInstanceState.containsKey("SEL")) {
                View view = binding.bottomNavigationView.findViewById(R.id.product);
                view.performClick();
            }
        }
    }

    @Override
    protected void onNewIntent (Intent intent) {
        // return to main activity and check if it is a user profile callback
        super.onNewIntent(intent);
        Bundle extras = intent.getExtras();
        if (extras != null && extras.containsKey("USER")) {
            // highlighting without clicking
            String userId = extras.getString("USER");
            userViewModel.setUser(userId);
            MenuItem menuItem = binding.bottomNavigationView.getMenu().findItem(R.id.user);
            menuItem.setChecked(true);
            invalidateOptionsMenu();
            replaceFragment("profile");
        }
    }

    public void switchToUserTab() {
        MenuItem menuItem = binding.bottomNavigationView.getMenu().findItem(R.id.user);
        menuItem.setChecked(true);
        invalidateOptionsMenu();
        replaceFragment("profile");
        jumpFromPost = true;
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

    @Override
    public void onBackPressed() {
        if (jumpFromPost) {
            String lastTab = userViewModel.getLastTab().getValue();
            if (lastTab.equals("ProductFragment")) {
                View view = binding.bottomNavigationView.findViewById(R.id.product);
                view.performClick();
            } else if (lastTab.equals("ServiceFragment")) {
                View view = binding.bottomNavigationView.findViewById(R.id.service);
                view.performClick();
            }
        } else {
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLastKnownLocation();
            }
        }
    }


    private void fetchLastKnownLocation() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            // Use Geocoder to get the nearest city
                            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                            List<Address> addresses = null;
                            try {
                                addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                if (addresses != null && !addresses.isEmpty()) {
                                    String city = addresses.get(0).getLocality();  // Get the city name
                                    saveCityToDatabase(city);  // Save the city to the database
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
        }
    }


    private void saveCityToDatabase(String city) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userRef = database.getReference("users").child(userId);
        userRef.child("lastSeenCity").setValue(city);
    }

    public void createNotificationChannel() {
        // This must be called early because it must be called before a notification is sent.
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(getString(R.string.channel_id), name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}