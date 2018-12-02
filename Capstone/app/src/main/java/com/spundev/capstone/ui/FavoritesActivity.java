package com.spundev.capstone.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.spundev.capstone.R;
import com.spundev.capstone.model.Card;
import com.spundev.capstone.ui.adapter.LocalCardListAdapter;
import com.spundev.capstone.util.ThemeColorUtils;
import com.spundev.capstone.viewmodel.FavoritesViewModel;
import com.spundev.capstone.widget.FavoritesWidget;

import java.util.List;

public class FavoritesActivity extends AppCompatActivity implements LocalCardListAdapter.CardListAdapterHandler {

    public static void start(Context context) {
        Intent starter = new Intent(context, FavoritesActivity.class);
        context.startActivity(starter);
    }

    private FavoritesViewModel favoritesViewModel;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Toolbar back arrow
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Change toolbar and status bar colors
        int toolbarColor = Color.parseColor("#9c27b0");
        ThemeColorUtils.changeActivityColors(this, toolbar, toolbarColor);

        final TextView emptyTextView = findViewById(R.id.empty_textView);


        // Recycler view
        recyclerView = findViewById(R.id.favorites_cards_recyclerView);
        // Divider lines
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        // Adapter
        final LocalCardListAdapter adapter = new LocalCardListAdapter(this, recyclerView, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Get a new or existing ViewModel from the ViewModelProvider.
        favoritesViewModel = ViewModelProviders.of(this).get(FavoritesViewModel.class);

        // Add an observer on the LiveData returned by getAllFavoriteCards.
        // The onChanged() method fires when the observed data changes and the activity is
        // in the foreground.
        favoritesViewModel.getAllFavoriteCards().observe(this, new Observer<List<Card>>() {

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
    }

    @Override
    public void onCardPlayListener(Card card) {
        TTSActivity.start(this, card.getText());
    }

    @Override
    public void onCardFavoriteListener(Card card, boolean newValue) {
        card.setFavourite(newValue);
        favoritesViewModel.update(card);

        if (newValue) {
            Toast.makeText(this, R.string.added_to_favorites, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.removed_from_favorites, Toast.LENGTH_SHORT).show();
        }

        FavoritesWidget.updateFavoritesWidgets(this);
    }

    @Override
    public void onCardShareListener(Card card) {
        Toast.makeText(FavoritesActivity.this, "Todo: Share", Toast.LENGTH_SHORT).show();
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
        builder.setTitle(getString(R.string.edit_text_title));
        builder.setView(container);

        String positiveText = getString(R.string.edit_card_dialog_positive);
        builder.setPositiveButton(positiveText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // update button logic
                        String text = String.valueOf(editText.getText());
                        card.setText(text);
                        favoritesViewModel.update(card);

                        Snackbar.make(recyclerView, R.string.local_card_updated, Snackbar.LENGTH_SHORT).show();
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
        builder.setTitle(R.string.delete_card_dialog);
        builder.setMessage(card.getText());

        String positiveText = getString(R.string.delete_card_dialog_action);
        builder.setPositiveButton(positiveText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // update button logic
                        favoritesViewModel.delete(card);

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
}
