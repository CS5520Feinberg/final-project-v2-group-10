package edu.northeastern.numad23su_team_v2_group_10_final_project.message;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;

import edu.northeastern.numad23su_team_v2_group_10_final_project.R;
import edu.northeastern.numad23su_team_v2_group_10_final_project.public_fragments.FSAdapter;


public class MessageFragment extends Fragment {

    public MessageFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_message, container, false);
        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        ViewPager2 viewpager = view.findViewById(R.id.view_pager);

        FSAdapter vpAdapter = new FSAdapter(this);

        vpAdapter.addFragment(new HistoryFragment());
        vpAdapter.addFragment(new UsersFragment());
        viewpager.setAdapter(vpAdapter);

        ArrayList<String> fragmentTitle = new ArrayList<>();
        fragmentTitle.add("History");
        fragmentTitle.add("Search Users");

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
        return view;
    }
}