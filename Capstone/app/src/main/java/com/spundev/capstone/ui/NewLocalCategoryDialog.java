package com.spundev.capstone.ui;

import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;

import com.spundev.capstone.ui.adapter.CategoryColorsAdapter;

import com.spundev.capstone.R;


public class NewLocalCategoryDialog extends DialogFragment {

    public interface OnNewCategoryDialogListener {
        void onCreateCategory(String categoryName, @ColorInt int categoryColor);
    }

    private OnNewCategoryDialogListener onNewCategoryDialogListener;
    private CategoryColorsAdapter colorsRecyclerViewAdapter;

    // widgets
    private TextInputLayout categoryNameEditTextInputLayout;
    private EditText categoryNameEditText;
    private Button cancelButton;
    private Button createButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        View view = inflater.inflate(R.layout.dialog_new_category, container, false);

        categoryNameEditTextInputLayout = view.findViewById(R.id.category_name_textInputLayout);
        categoryNameEditText = view.findViewById(R.id.category_name_editText);
        cancelButton = view.findViewById(R.id.cancel_create_category_button);
        createButton = view.findViewById(R.id.create_category_button);

        // Recycler view
        RecyclerView colorsRecyclerView = view.findViewById(R.id.category_colors_recyclerView);
        // Adapter
        colorsRecyclerViewAdapter = new CategoryColorsAdapter(getActivity());
        colorsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));

        colorsRecyclerView.setAdapter(colorsRecyclerViewAdapter);

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

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String categoryNameInput = categoryNameEditText.getText().toString();
                if (!TextUtils.isEmpty(categoryNameInput)) {
                    if (onNewCategoryDialogListener != null) {
                        int selectedColor = colorsRecyclerViewAdapter.getSelectedColor();
                        onNewCategoryDialogListener.onCreateCategory(categoryNameInput, selectedColor);
                        getDialog().dismiss();
                    }
                } else {
                    categoryNameEditTextInputLayout.setError("Name is required");
                }
            }
        });

    }

    public void setNewCategoryDialogListener(OnNewCategoryDialogListener listener) {
        onNewCategoryDialogListener = listener;
    }


    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    }
}
