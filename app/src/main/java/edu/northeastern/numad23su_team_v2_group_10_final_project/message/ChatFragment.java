package edu.northeastern.numad23su_team_v2_group_10_final_project.message;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import edu.northeastern.numad23su_team_v2_group_10_final_project.R;
import edu.northeastern.numad23su_team_v2_group_10_final_project.model.ChatMessage;

public class ChatFragment extends Fragment {

    public ChatFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_message_children_chat, container, false);
        return view;
    }
}