package com.spundev.capstone.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.spundev.capstone.R;


public class LocalCategoryActionsBottomSheet extends BottomSheetDialogFragment {

    public static final String MODAL_TITLE_EXTRA = "MODAL_TITLE_EXTRA";

    private Button editButton;
    private Button deleteButton;

    public interface OnCategoryActionsModalListener {
        void onEditCategory();

        void onDeleteCategory();
    }

    private OnCategoryActionsModalListener onCategoryActionsModalListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.bottom_sheets_category_actions, container, false);

        // Set the modal title
        String value = getArguments().getString(MODAL_TITLE_EXTRA);
        TextView titleTextView = view.findViewById(R.id.bottom_sheet_title);
        titleTextView.setText(value);

        // Modal actions
        editButton = view.findViewById(R.id.bottom_sheet_edit_button);
        deleteButton = view.findViewById(R.id.bottom_sheet_delete_button);

        initListeners();

        return view;
    }

    private void initListeners() {
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onCategoryActionsModalListener != null) {
                    onCategoryActionsModalListener.onEditCategory();
                }
                getDialog().dismiss();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onCategoryActionsModalListener != null) {
                    onCategoryActionsModalListener.onDeleteCategory();
                }
                getDialog().dismiss();
            }
        });
    }


    public void setNewCategoryDialogListener(OnCategoryActionsModalListener listener) {
        onCategoryActionsModalListener = listener;
    }
}