package com.spundev.capstone.ui;

import android.app.SearchManager;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.spundev.capstone.R;
import com.spundev.capstone.model.Card;
import com.spundev.capstone.ui.adapter.LocalCardListAdapter;
import com.spundev.capstone.viewmodel.SearchViewModel;
import com.spundev.capstone.widget.FavoritesWidget;

import java.util.List;

public class SearchActivity extends AppCompatActivity implements LocalCardListAdapter.CardListAdapterHandler, Observer<List<Card>> {

    // View model
    private SearchViewModel searchViewModel;
    private LiveData<List<Card>> searchLiveData;

    private RecyclerView recyclerView;
    private TextView emptyTextView;
    private LocalCardListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Toolbar back arrow
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Empty text view
        emptyTextView = findViewById(R.id.empty_textView);

        // Recycler view
        recyclerView = findViewById(R.id.search_cards_recyclerView);
        // Divider lines
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        // Adapter
        adapter = new LocalCardListAdapter(this, recyclerView, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Get a new or existing ViewModel from the ViewModelProvider.
        searchViewModel = ViewModelProviders.of(this).get(SearchViewModel.class);

        // Add an observer on the LiveData returned by getAllCardsFromQuery.
        // The onChanged() method fires when the observed data changes and the activity is
        // in the foreground.
        searchLiveData = searchViewModel.getAllCardsFromQuery("w");
        searchLiveData.observe(this, this);

        recyclerView.setAdapter(adapter);

        handleIntent(getIntent());

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        String intentAction = intent.getAction();
        if (Intent.ACTION_SEARCH.equals(intentAction)) {
            updateQuery(intent.getStringExtra(SearchManager.QUERY));
        } else if (Intent.ACTION_VIEW.equals(intentAction)) {
            String intentSuggestionData = intent.getDataString();
            TTSActivity.start(this, intentSuggestionData);
            // The search activity only function is to launch the tts activity
            finish();
        }
    }


    private void updateQuery(String searchQuery) {
        // remove old observer
        searchLiveData.removeObserver(this);
        // create new with the new query
        searchLiveData = searchViewModel.getAllCardsFromQuery(searchQuery);
        searchLiveData.observe(this, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu.
        getMenuInflater().inflate(R.menu.menu_search, menu);

        // Search config
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        ComponentName componentName = new ComponentName(this, SearchActivity.class);
        // Search view
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName));
        return true;
    }

    @Override
    public void onCardPlayListener(Card card) {
        TTSActivity.start(this, card.getText());
    }

    @Override
    public void onCardFavoriteListener(Card card, boolean newValue) {
        card.setFavourite(newValue);
        searchViewModel.update(card);

        if (newValue) {
            Toast.makeText(this, R.string.added_to_favorites, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.removed_from_favorites, Toast.LENGTH_SHORT).show();
        }
        FavoritesWidget.updateFavoritesWidgets(this);
    }

    @Override
    public void onCardShareListener(Card card) {
        Toast.makeText(SearchActivity.this, "Todo: Share", Toast.LENGTH_SHORT).show();
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
                        searchViewModel.update(card);

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
                        searchViewModel.delete(card);

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

    // Change in results from observer
    @Override
    public void onChanged(@Nullable List<Card> cards) {

        adapter.setCards(cards);
        if (cards != null && !cards.isEmpty()) {
            emptyTextView.setVisibility(View.INVISIBLE);
        } else {
            emptyTextView.setVisibility(View.VISIBLE);
        }
    }
}
