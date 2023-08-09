package edu.northeastern.numad23su_team_v2_group_10_final_project.message;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;

import edu.northeastern.numad23su_team_v2_group_10_final_project.R;
import edu.northeastern.numad23su_team_v2_group_10_final_project.model.ChatRoom;
import edu.northeastern.numad23su_team_v2_group_10_final_project.util.FirebaseUtil;

public class HistoryFragment extends Fragment {

    RecyclerView recyclerView;
    HistoryAdapter historyAdapter;

    public HistoryFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_message_chat_history, container, false);
        recyclerView = view.findViewById(R.id.history_recycler_view);
        setupHistoryRecyclerView();

        return view;
    }

    private void setupHistoryRecyclerView(){

        Query query = FirebaseUtil.allChatroomCollectionReference()
                .whereArrayContains("userIds",FirebaseUtil.currentUserId())
                .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<ChatRoom> options = new FirestoreRecyclerOptions.Builder<ChatRoom>()
                .setQuery(query,ChatRoom.class)
                .build();

        Log.d(TAG, "Number of items in Firestore query result: " + options.getSnapshots().size());

        historyAdapter = new HistoryAdapter(options,requireContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(historyAdapter);
        historyAdapter.startListening();
    }

    @Override
    public void onStart() {
        super.onStart();
        if(historyAdapter!=null)
            historyAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(historyAdapter!=null)
            historyAdapter.stopListening();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(historyAdapter!=null)
            historyAdapter.notifyDataSetChanged();
    }
}