package edu.northeastern.numad23su_team_v2_group_10_final_project.public_fragments;

import static edu.northeastern.numad23su_team_v2_group_10_final_project.post.Utils.getPostTypes;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import edu.northeastern.numad23su_team_v2_group_10_final_project.MainActivity;
import edu.northeastern.numad23su_team_v2_group_10_final_project.R;
import edu.northeastern.numad23su_team_v2_group_10_final_project.UserViewModel;
import edu.northeastern.numad23su_team_v2_group_10_final_project.model.Post;
import edu.northeastern.numad23su_team_v2_group_10_final_project.post.display_post.DisplayPostActivity;
import edu.northeastern.numad23su_team_v2_group_10_final_project.post.SearchUtils;
import edu.northeastern.numad23su_team_v2_group_10_final_project.search.ItemViewModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TempPostFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TempPostFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String className;

    String[] postTypes = getPostTypes();
    private UserViewModel userViewModel;

    private ItemViewModel viewModel;
    TextView textView;
    RecyclerView recyclerView;
    List<Post> list;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFireStoreRef;
    private DatabaseReference mDbRef;
    private StorageReference mStorageRef;
    int limit = 5;
    PostAdapter adapter;
    private DocumentSnapshot lastVisible;
    private boolean isScrolling = false;
    private boolean isLastItemReached = false;
    final Handler handler = new Handler(Looper.getMainLooper());

    // query: the query string for list posts
    // post type: the list post type
    // if query == "": would display all records
    private String query;
    private Integer postType;

    private SwipeRefreshLayout swipeContainer;

    public TempPostFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TempPostFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TempPostFragment newInstance(String param1, Integer param2) {
        TempPostFragment fragment = new TempPostFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putInt(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            query = getArguments().getString(ARG_PARAM1);
            postType = getArguments().getInt(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_post, container, false);
        return view;
    }

    // binding is here
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ItemViewModel.class);
        textView = view.findViewById(R.id.textView);
        recyclerView = view.findViewById(R.id.recycler_view);
        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        mAuth = FirebaseAuth.getInstance();
        mDbRef =  FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mFireStoreRef = FirebaseFirestore.getInstance();
        className = getActivity().getClass().getSimpleName();

        list = new ArrayList<>();
        adapter = new PostAdapter(getContext(),list);
        adapter.setListener(new PostClickListener() {
            @Override
            public void onClick(int position) {
                Intent i = new Intent(getActivity(), DisplayPostActivity.class);
                i.putExtra("postType", postTypes[list.get(position).type.intValue()]);
                i.putExtra("postId", list.get(position).postId);
                startActivity(i);
            }

            @Override
            public void onAvatarClick(int position) {
                String userId = list.get(position).userId;
                if (className.equals("SearchActivity") || className.equals("DisplayUserPostListActivity")) {
                    //Start main activity
                    Intent i = new Intent(getActivity(), MainActivity.class);
                    i.putExtra("USER", userId);
                    startActivity(i);
                } else if (className.equals("MainActivity")) {
                    userViewModel.setUser(userId);
                    userViewModel.setLastTab(getParentFragment().getClass().getSimpleName());
                    MainActivity main = (MainActivity)getActivity();
                    main.switchToUserTab();
                }
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        fetchData(true);
        // Add pull recycler view to refresh function
        // Lookup the swipe container view

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchData(true);
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    isScrolling = true;
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager linearLayoutManager = ((LinearLayoutManager) recyclerView.getLayoutManager());
                int firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();
                int visibleItemCount = linearLayoutManager.getChildCount();
                int totalItemCount = linearLayoutManager.getItemCount();

                if (isScrolling && (firstVisibleItemPosition + visibleItemCount == totalItemCount) && !isLastItemReached) {
                    isScrolling = false;
                    fetchData(false);
                }
            }
        };
        recyclerView.addOnScrollListener(onScrollListener);
        viewModel.getSelectedItem().observe(getViewLifecycleOwner(), item -> {
            if (!query.equals(item)) {
                query = item;
                fetchData(true);
            }
        });
    }

    private void fetchData(boolean clear) {
        swipeContainer.setRefreshing(true);
        Query q;
        CollectionReference ref = mFireStoreRef.collection("posts").document(postTypes[postType]).collection("posts") ;
        if (clear) {
            if (query.length() == 0) { // from main
                q = ref.whereEqualTo("isActive", true).orderBy("timestamp", Query.Direction.DESCENDING).limit(limit);
            } else {
                limit = Integer.MAX_VALUE; // cancel limit for search and user post list
                if (className.equals("SearchActivity")) {
                    q = ref.where(Filter.and(SearchUtils.generateFilterArr(query)));
                } else {
                    q = ref.whereEqualTo("userId", query);
                }

            }
        } else {
            if (query.length() == 0) { // from main
                q = ref.whereEqualTo("isActive", true).orderBy("timestamp", Query.Direction.DESCENDING);
                if (lastVisible != null) q = q.startAfter(lastVisible).limit(limit);
                else q = q.limit(limit);
            } else {
                limit = Integer.MAX_VALUE; // cancel limit for search and user post list
                if (className.equals("SearchActivity")) {
                    q = ref.where(Filter.and(SearchUtils.generateFilterArr(query)));
                } else {
                    q = ref.whereEqualTo("userId", query);
                }
                if (lastVisible != null) q = q.startAfter(lastVisible);
            }
        }

        // pagination ref: https://stackoverflow.com/questions/50741958/how-to-paginate-firestore-with-android
        q.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (clear) {
                        list.clear();
                    }
                    for (DocumentSnapshot document : task.getResult()) {
                        Post post = document.toObject(Post.class);
                        list.add(post);
                    }
                    adapter.notifyDataSetChanged();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            swipeContainer.setRefreshing(false);
                        }
                    }, 500);

                    if (task.getResult().size() > 0) lastVisible = task.getResult().getDocuments().get(task.getResult().size() - 1);
                    if (task.getResult().size() < limit) {
                        isLastItemReached = true;
                    } else {
                        isLastItemReached = false;
                    }

                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchData(true);
    }
}