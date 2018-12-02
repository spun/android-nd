package com.spundev.capstone.ui;


import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.spundev.capstone.R;
import com.spundev.capstone.model.Category;
import com.spundev.capstone.ui.adapter.LocalCategoryListAdapter;
import com.spundev.capstone.util.PreferencesUtils;
import com.spundev.capstone.viewmodel.LocalCategoriesFragmentViewModel;

import java.util.List;

public class LocalCategoriesFragment extends Fragment implements LocalCategoryListAdapter.CategoryListAdapterHandler {

    private LocalCategoriesFragmentViewModel localCategoriesFragmentViewModel;

    // Top fixed categories
    private View favoriteCategoryView;
    private View nearMeCategoryView;
    private View newCategoryView;

    private RecyclerView recyclerView;
    private Button quickCardButton;
    private Button listenFastButton;

    @Override
    public void onStart() {
        super.onStart();
        // Check if near me is disabled from settings
        if (!PreferencesUtils.getPreferenceNearMe(getActivity())) {
            nearMeCategoryView.setVisibility(View.GONE);
        } else {
            nearMeCategoryView.setVisibility(View.VISIBLE);
        }
    }

    private View view;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_local_categories_tab, container, false);

        // Top fixed categories
        favoriteCategoryView = view.findViewById(R.id.favorite_category_view);
        nearMeCategoryView = view.findViewById(R.id.near_me_category_view);
        newCategoryView = view.findViewById(R.id.new_category_view);

        // Bottom action buttons
        quickCardButton = view.findViewById(R.id.quick_card_button);
        listenFastButton = view.findViewById(R.id.listen_fast_button);

        // Empty text view
        final TextView emptyTextView = view.findViewById(R.id.empty_textView);

        // Local categories recycler view
        recyclerView = view.findViewById(R.id.local_categories_recyclerView);
        // Adapter
        final LocalCategoryListAdapter adapter = new LocalCategoryListAdapter(getActivity(), this);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));

        // Get a new or existing ViewModel from the ViewModelProvider.
        localCategoriesFragmentViewModel = ViewModelProviders.of(this).get(LocalCategoriesFragmentViewModel.class);

        // Add an observer on the LiveData returned by getAllCategories.
        // The onChanged() method fires when the observed data changes and the activity is in the foreground.
        localCategoriesFragmentViewModel.getAllCategories().observe(this, new Observer<List<Category>>() {

            @Override
            public void onChanged(@Nullable List<Category> categories) {
                // Update the cached copy of the categories in the adapter.
                adapter.setCategories(categories);
                // Show the empty message if we don't have categories
                if (categories != null && !categories.isEmpty()) {
                    emptyTextView.setVisibility(View.INVISIBLE);
                } else {
                    emptyTextView.setVisibility(View.VISIBLE);
                }
            }
        });
        recyclerView.setAdapter(adapter);

        // Set listeners
        initListeners();

        return view;
    }

    private void initListeners() {
        // Favorites button
        favoriteCategoryView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Launch favorites activity
                FavoritesActivity.start(getActivity());
            }
        });
        // Near me button
        nearMeCategoryView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Launch "near me" activity
                NearMeCardsActivity.start(getActivity());
            }
        });
        // New category button
        newCategoryView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNewCategoryClick();
            }
        });

        // Quick card button click listener
        quickCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Launch the TTS activity without text
                TTSActivity.start(getActivity(), "");
            }
        });

        // Start listening button click listener
        listenFastButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Launch the STT activity without text
                STTActivity.start(getActivity());
            }
        });
    }

    // Show the new category dialog
    private void onNewCategoryClick() {
        NewLocalCategoryDialog dialog = new NewLocalCategoryDialog();
        dialog.setNewCategoryDialogListener(new NewLocalCategoryDialog.OnNewCategoryDialogListener() {
            @Override
            public void onCreateCategory(String categoryName, int categoryColor) {
                Category category = new Category(categoryName, categoryColor);
                localCategoriesFragmentViewModel.insert(category);

                Snackbar.make(recyclerView, R.string.local_category_created, Snackbar.LENGTH_SHORT).show();
            }
        });
        // Show dialog
        dialog.show(getFragmentManager(), "Create category");
    }


    // From the recycler view adapter. When a category from the list is selected, launch the details activity
    @Override
    public void onCategorySelected(Category category) {
        // Launch the category details activity
        LocalCardListActivity.start(getActivity(), category.getId(), category.getName(), category.getColor());
    }

    // From the recycler view adapter. When a category is long pressed, show the options modal
    @Override
    public void onCategoryLongPress(final Category category) {

        Bundle args = new Bundle();
        args.putString(LocalCategoryActionsBottomSheet.MODAL_TITLE_EXTRA, category.getName());

        LocalCategoryActionsBottomSheet modalBottomSheet = new LocalCategoryActionsBottomSheet();
        modalBottomSheet.setArguments(args);

        // The modal has two possible actions (edit and delete).
        modalBottomSheet.setNewCategoryDialogListener(new LocalCategoryActionsBottomSheet.OnCategoryActionsModalListener() {
            @Override
            public void onEditCategory() {
                showEditCategoryDialog(category);
            }

            @Override
            public void onDeleteCategory() {
                localCategoriesFragmentViewModel.delete(category);
                Snackbar.make(recyclerView, R.string.local_category_deleted, Snackbar.LENGTH_SHORT).show();
            }
        });
        modalBottomSheet.show(getFragmentManager(), "bottom sheet");
    }

    // Show the "edit category" dialog
    private void showEditCategoryDialog(final Category category) {
        // EditText as dialog view
        final EditText editText = new EditText(getActivity());
        editText.setText(category.getName());

        // Add margin to the EditText (https://stackoverflow.com/a/27776276)
        FrameLayout container = new FrameLayout(getActivity());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = (int) getResources().getDimension(R.dimen.dialog_margin);
        params.leftMargin = (int) getResources().getDimension(R.dimen.dialog_margin);
        params.rightMargin = (int) getResources().getDimension(R.dimen.dialog_margin);
        editText.setLayoutParams(params);
        container.addView(editText);

        // Create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.edit_category_dialog_title);
        builder.setView(container);

        String positiveText = getString(R.string.edit_card_dialog_positive);
        builder.setPositiveButton(positiveText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // update button logic
                        String text = String.valueOf(editText.getText());
                        category.setName(text);
                        localCategoriesFragmentViewModel.update(category);
                        Snackbar.make(recyclerView, R.string.local_category_updated, Snackbar.LENGTH_SHORT).show();
                    }
                });

        // display dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
