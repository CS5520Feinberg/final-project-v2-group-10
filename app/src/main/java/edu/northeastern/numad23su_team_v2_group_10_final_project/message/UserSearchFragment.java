package edu.northeastern.numad23su_team_v2_group_10_final_project.message;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;

import edu.northeastern.numad23su_team_v2_group_10_final_project.R;
import edu.northeastern.numad23su_team_v2_group_10_final_project.model.User;
import edu.northeastern.numad23su_team_v2_group_10_final_project.util.FirebaseUtil;

public class UserSearchFragment extends Fragment {
    private static final String TAG = "Campus Help";
    EditText searchInput;
    ImageButton searchButton;

    RecyclerView recyclerView;
    UserSearchAdapter userSearchAdapter;
    public UserSearchFragment(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_message_users_search, container, false);

        searchInput = view.findViewById(R.id.search_username_input);
        searchButton = view.findViewById(R.id.search_user_btn);
        recyclerView = view.findViewById(R.id.search_user_recycler_view);

        searchButton.setOnClickListener(v -> {
            String searchTerm = searchInput.getText().toString();
            if (searchTerm.isEmpty()) {
                searchInput.setError("Invalid Username");
                return;
            }
            setupSearchRecyclerView(searchTerm);
        });
        return view;
    }

    private void setupSearchRecyclerView(String searchTerm){

        Query query = FirebaseUtil.allUserCollectionReference()
                .whereGreaterThanOrEqualTo("name",searchTerm)
                .whereLessThanOrEqualTo("name",searchTerm+'\uf8ff');

        FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(query,User.class)
                .build();

        Log.d(TAG, "Number of items in Firestore query result: " + options.getSnapshots().size());

        userSearchAdapter = new UserSearchAdapter(options,requireContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(userSearchAdapter);
        userSearchAdapter.startListening();
    }


    @Override
    public void onStart() {
        super.onStart();
        //userSearchAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        //if (userSearchAdapter!=null){
            //userSearchAdapter.stopListening();
        //}
    }

    @Override
    public void onResume() {
        searchInput.setText("");
        super.onResume();
        //if (userSearchAdapter!=null){
            //userSearchAdapter.startListening();
        //}
    }
}