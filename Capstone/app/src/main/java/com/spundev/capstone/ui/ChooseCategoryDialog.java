package com.spundev.capstone.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import com.spundev.capstone.model.Category;
import com.spundev.capstone.ui.adapter.LocalCategoryListAdapter;
import com.spundev.capstone.viewmodel.ChooseCategoryViewModel;

import com.spundev.capstone.R;

// Choose category to save community card
public class ChooseCategoryDialog extends DialogFragment implements LocalCategoryListAdapter.CategoryListAdapterHandler {

    // widgets
    private Button cancelButton;

    public interface OnChooseCategoryDialogListener {
        void onCategorySelected(Category category);
    }

    private OnChooseCategoryDialogListener onChooseCategoryDialogListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        View view = inflater.inflate(R.layout.dialog_choose_category, container, false);

        cancelButton = view.findViewById(R.id.cancel_choose_category_button);


        final TextView emptyTextView = view.findViewById(R.id.empty_textView);

        // Recycler view
        RecyclerView recyclerView = view.findViewById(R.id.choose_category_recyclerView);
        // Adapter
        final LocalCategoryListAdapter adapter = new LocalCategoryListAdapter(getActivity(), this);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));

        // Get a new or existing ViewModel from the ViewModelProvider.
        ChooseCategoryViewModel chooseCategoryViewModel = ViewModelProviders.of(this).get(ChooseCategoryViewModel.class);

        // Add an observer on the LiveData returned by getAllCategories.
        // The onChanged() method fires when the observed data changes and the activity is
        // in the foreground.
        chooseCategoryViewModel.getAllCategories().observe(this, new Observer<List<Category>>() {

            @Override
            public void onChanged(@Nullable List<Category> categories) {
                // Update the cached copy of the categories in the adapter.
                adapter.setCategories(categories);
                if (categories != null && !categories.isEmpty()) {
                    emptyTextView.setVisibility(View.INVISIBLE);
                } else {
                    emptyTextView.setVisibility(View.VISIBLE);
                }
            }
        });

        recyclerView.setAdapter(adapter);

        initListeners();

        return view;
    }

    private void initListeners() {
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    public void setChooseCategoryDialogListener(OnChooseCategoryDialogListener listener) {
        onChooseCategoryDialogListener = listener;
    }

    @Override
    public void onCategorySelected(Category category) {
        if (onChooseCategoryDialogListener != null) {
            onChooseCategoryDialogListener.onCategorySelected(category);
            getDialog().dismiss();
        }
    }

    @Override
    public void onCategoryLongPress(Category category) {

    }
}
