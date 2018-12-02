package com.spundev.capstone.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import com.spundev.capstone.model.Card;
import com.spundev.capstone.ui.adapter.LocalCardListAdapter;
import com.spundev.capstone.util.ThemeColorUtils;
import com.spundev.capstone.viewmodel.LocalCardListViewModel;

import com.spundev.capstone.R;
import com.spundev.capstone.widget.FavoritesWidget;

public class LocalCardListActivity extends AppCompatActivity implements LocalCardListAdapter.CardListAdapterHandler {

    private static final String CATEGORY_ID_EXTRA = "CATEGORY_ID_EXTRA";
    private static final String CATEGORY_NAME_EXTRA = "CATEGORY_NAME_EXTRA";
    private static final String CATEGORY_COLOR_EXTRA = "CATEGORY_COLOR_EXTRA";

    private int categoryId;

    private LocalCardListViewModel localCardListViewModel;
    private RecyclerView recyclerView;

    public static void start(Context context, int categoryId, String categoryName, @ColorInt int categoryColor) {
        Intent starter = new Intent(context, LocalCardListActivity.class);
        starter.putExtra(CATEGORY_ID_EXTRA, categoryId);
        starter.putExtra(CATEGORY_NAME_EXTRA, categoryName);
        starter.putExtra(CATEGORY_COLOR_EXTRA, categoryColor);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_list_local);

        // Intent extras
        Intent intent = getIntent();
        categoryId = intent.getIntExtra(CATEGORY_ID_EXTRA, 0);
        String categoryName = intent.getStringExtra(CATEGORY_NAME_EXTRA);
        int categoryColor = intent.getIntExtra(CATEGORY_COLOR_EXTRA, 0);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Toolbar back arrow and title
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setTitle(categoryName);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        ThemeColorUtils.changeActivityColors(this, toolbar, categoryColor);


        final TextView emptyTextView = findViewById(R.id.empty_textView);

        // Recycler view
        recyclerView = findViewById(R.id.local_category_cards_recyclerView);
        // Divider lines
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        // Adapter
        final LocalCardListAdapter adapter = new LocalCardListAdapter(this, recyclerView, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Get a new or existing ViewModel from the ViewModelProvider.
        LocalCardListViewModel.Factory factory = new LocalCardListViewModel.Factory(this.getApplication(), categoryId);
        localCardListViewModel = ViewModelProviders.of(this, factory).get(LocalCardListViewModel.class);

        // Add an observer on the LiveData returned by getAllCategories.
        // The onChanged() method fires when the observed data changes and the activity is
        // in the foreground.
        localCardListViewModel.getAllCardsFromCategory().observe(this, new Observer<List<Card>>() {

            @Override
            public void onChanged(@Nullable List<Card> cards) {
                adapter.setCards(cards);
                if (cards != null && !cards.isEmpty()) {
                    emptyTextView.setVisibility(View.INVISIBLE);
                } else {
                    emptyTextView.setVisibility(View.VISIBLE);
                }

            }
        });

        recyclerView.setAdapter(adapter);

        // add category fab button
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNewCardDialog();
            }
        });
    }


    @Override
    public void onCardPlayListener(Card card) {
        TTSActivity.start(this, card.getText());
    }

    @Override
    public void onCardFavoriteListener(Card card, boolean newValue) {
        card.setFavourite(newValue);
        localCardListViewModel.update(card);

        if (newValue) {
            Toast.makeText(this, R.string.added_to_favorites, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.removed_from_favorites, Toast.LENGTH_SHORT).show();
        }
        FavoritesWidget.updateFavoritesWidgets(this);
    }

    @Override
    public void onCardShareListener(Card card) {
        Toast.makeText(LocalCardListActivity.this, "Todo: Share", Toast.LENGTH_SHORT).show();
        // Share to firebase is not implemented because we need some moderation before we show the shared
        // card to the rest of the users and this needs to be implemented in the backend before (cloud functions)
    }

    @Override
    public void onCardEditListener(final Card card) {

        // EditText as dialog view
        final EditText editText = new EditText(this);
        editText.setText(card.getText());

        // Add margin to the EditText (https://stackoverflow.com/a/27776276)
        FrameLayout container = new FrameLayout(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = (int) getResources().getDimension(R.dimen.dialog_margin);
        params.leftMargin = (int) getResources().getDimension(R.dimen.dialog_margin);
        params.rightMargin = (int) getResources().getDimension(R.dimen.dialog_margin);
        editText.setLayoutParams(params);
        container.addView(editText);

        // Show add dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.edit_card_dialog));
        builder.setView(container);

        String positiveText = getString(R.string.edit_card_dialog_positive);
        builder.setPositiveButton(positiveText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // update button logic

                        String text = String.valueOf(editText.getText());
                        if (TextUtils.isEmpty(text)) {
                            Toast.makeText(LocalCardListActivity.this, R.string.error_empty_card_text, Toast.LENGTH_SHORT).show();
                        } else {
                            card.setText(text);
                            localCardListViewModel.update(card);

                            Snackbar.make(recyclerView, R.string.local_card_updated, Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });

        String negativeText = getString(android.R.string.cancel);
        builder.setNegativeButton(negativeText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // delete button logic
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        // display dialog
        dialog.show();

    }

    @Override
    public void onCardDeleteListener(final Card card) {

        // Show add dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.delete_card_dialog));
        builder.setMessage(card.getText());

        String positiveText = getString(R.string.delete_card_dialog_action);
        builder.setPositiveButton(positiveText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // update button logic
                        localCardListViewModel.delete(card);

                        Snackbar.make(recyclerView, R.string.local_card_deleted, Snackbar.LENGTH_SHORT).show();
                    }
                });

        String negativeText = getString(android.R.string.cancel);
        builder.setNegativeButton(negativeText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // delete button logic
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        // display dialog
        dialog.show();
    }

    private void showNewCardDialog() {

        // EditText as dialog view
        final EditText editText = new EditText(this);

        // Add margin to the EditText (https://stackoverflow.com/a/27776276)
        FrameLayout container = new FrameLayout(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = (int) getResources().getDimension(R.dimen.dialog_margin);
        params.leftMargin = (int) getResources().getDimension(R.dimen.dialog_margin);
        params.rightMargin = (int) getResources().getDimension(R.dimen.dialog_margin);
        editText.setLayoutParams(params);
        container.addView(editText);

        // Show add dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.new_card_dialog));
        builder.setView(container);

        String positiveText = getString(R.string.new_card_dialog_positive);
        builder.setPositiveButton(positiveText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // update button logic
                        String text = String.valueOf(editText.getText());

                        if (TextUtils.isEmpty(text)) {
                            Toast.makeText(LocalCardListActivity.this, R.string.error_empty_card_text, Toast.LENGTH_SHORT).show();
                        } else {
                            Card newCard = new Card(text);
                            newCard.setCategoryId(categoryId);
                            localCardListViewModel.insert(newCard);

                            Snackbar.make(recyclerView, R.string.local_card_created, Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });

        String negativeText = getString(android.R.string.cancel);
        builder.setNegativeButton(negativeText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // delete button logic
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        // display dialog
        dialog.show();
    }
}
