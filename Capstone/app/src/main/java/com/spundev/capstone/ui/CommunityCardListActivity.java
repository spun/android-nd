package com.spundev.capstone.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.SnapshotParser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import com.spundev.capstone.model.Card;
import com.spundev.capstone.model.Category;
import com.spundev.capstone.model.firestore.CardFirestore;
import com.spundev.capstone.repository.LocalCardsRepository;
import com.spundev.capstone.ui.adapter.CommunityCardListAdapter;

import com.spundev.capstone.R;

public class CommunityCardListActivity extends AppCompatActivity implements CommunityCardListAdapter.CardListAdapterHandler {

    private static final String CATEGORY_ID_EXTRA = "CATEGORY_ID_EXTRA";
    private static final String CATEGORY_NAME_EXTRA = "CATEGORY_NAME_EXTRA";

    private String categoryId;
    // Firestore cards collection
    @SuppressWarnings("SpellCheckingInspection")
    private final CollectionReference cardsCollRef = FirebaseFirestore.getInstance().collection("cards");

    private RecyclerView recyclerView;
    private TextView emptyView;

    public static void start(Context context, String categoryId, String categoryName) {
        Intent starter = new Intent(context, CommunityCardListActivity.class);
        starter.putExtra(CATEGORY_ID_EXTRA, categoryId);
        starter.putExtra(CATEGORY_NAME_EXTRA, categoryName);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_list_community);

        // Intent extras
        Intent intent = getIntent();
        categoryId = intent.getStringExtra(CATEGORY_ID_EXTRA);
        String categoryName = intent.getStringExtra(CATEGORY_NAME_EXTRA);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Toolbar back arrow and title
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setTitle(categoryName);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        emptyView = findViewById(R.id.empty_textView);
        // Recycler view
        recyclerView = findViewById(R.id.community_category_cards_recyclerView);
        // Divider lines
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        // LinearLayoutManager
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Empty list view
        emptyView = findViewById(R.id.empty_textView);

        getCardsList();
    }


    private void getCardsList() {

        // Set up firebase query
        //noinspection SpellCheckingInspection
        final Query query = cardsCollRef.whereEqualTo("categories." + categoryId, true).orderBy("score", Query.Direction.DESCENDING);

        // Configure recycler adapter options
        FirestoreRecyclerOptions<CardFirestore> options = new FirestoreRecyclerOptions.Builder<CardFirestore>()
                .setQuery(query, new SnapshotParser<CardFirestore>() {
                    @NonNull
                    @Override
                    public CardFirestore parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                        CardFirestore c = snapshot.toObject(CardFirestore.class);
                        c.setId(snapshot.getId());
                        return c;
                    }
                })
                .setLifecycleOwner(this)
                .build();

        // Create our FirestoreRecyclerAdapter object
        FirestoreRecyclerAdapter adapter = new CommunityCardListAdapter(options, this, recyclerView, emptyView, this) {
            @Override
            public void onDataChanged() {
                if(getItemCount() == 0) {
                    emptyView.setVisibility(View.VISIBLE);
                } else {
                    emptyView.setVisibility(View.GONE);
                }
            }
        };
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onCardPlayListener(CardFirestore card) {
        TTSActivity.start(this, card.getText());
    }

    @Override
    public void onCardSaveListener(final CardFirestore card) {

        ChooseCategoryDialog dialog = new ChooseCategoryDialog();
        dialog.setChooseCategoryDialogListener(new ChooseCategoryDialog.OnChooseCategoryDialogListener() {
            @Override
            public void onCategorySelected(Category category) {
                Card c = new Card(card.getText());
                c.setCategoryId(category.getId());

                LocalCardsRepository repository = new LocalCardsRepository(getApplication());
                repository.insert(c);

                Snackbar.make(recyclerView, R.string.community_card_saved, Snackbar.LENGTH_SHORT).show();
            }
        });
        dialog.show(getSupportFragmentManager(), "Choose category");


    }
}
