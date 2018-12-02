package com.spundev.capstone.ui;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.spundev.capstone.R;
import com.spundev.capstone.model.firestore.CategoryFirestore;
import com.spundev.capstone.ui.adapter.CommunityCategoryListAdapter;

public class CommunityCategoriesFragment extends Fragment {

    private TextView emptyView;
    private RecyclerView categoriesRecyclerView;

    // Firestore collection
    @SuppressWarnings("SpellCheckingInspection")
    private final CollectionReference categoriesCollRef = FirebaseFirestore.getInstance().collection("categories");
    @SuppressWarnings("SpellCheckingInspection")
    private static final String NAME_KEY = "name";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_community_categories_tab, container, false);

        // Empty view
        emptyView = view.findViewById(R.id.empty_textView);
        // Recycler view
        categoriesRecyclerView = view.findViewById(R.id.community_categories_recyclerView);
        // LinearLayoutManager
        categoriesRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));

        getCategoriesList();

        return view;
    }


    private void getCategoriesList() {

        // Set up firebase query
        final Query query = categoriesCollRef.orderBy(NAME_KEY);

        // Configure recycler adapter options
        FirestoreRecyclerOptions<CategoryFirestore> options = new FirestoreRecyclerOptions.Builder<CategoryFirestore>()
                .setQuery(query, CategoryFirestore.class)
                .setLifecycleOwner(this)
                .build();

        // Create our FirestoreRecyclerAdapter object
        FirestoreRecyclerAdapter adapter = new CommunityCategoryListAdapter(options, new CommunityCategoryListAdapter.CommunityCategoryListAdapterOnClickHandler() {
            @Override
            public void onCategorySelected(CategoryFirestore category) {
                CommunityCardListActivity.start(getActivity(), category.getId(), category.getName());
            }
        }) {
            @Override
            public void onDataChanged() {
                if (getItemCount() == 0) {
                    emptyView.setVisibility(View.VISIBLE);
                } else {
                    emptyView.setVisibility(View.GONE);
                }
            }
        };
        categoriesRecyclerView.setAdapter(adapter);
    }
}