package com.spundev.capstone.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.SnapshotParser;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.spundev.capstone.R;
import com.spundev.capstone.model.Card;
import com.spundev.capstone.model.Category;
import com.spundev.capstone.model.firestore.CardFirestore;
import com.spundev.capstone.repository.LocalCardsRepository;
import com.spundev.capstone.ui.adapter.CommunityCardListAdapter;
import com.spundev.capstone.util.ThemeColorUtils;

public class NearMeCardsActivity extends AppCompatActivity implements CommunityCardListAdapter.CardListAdapterHandler {

    private static final String TAG = "NearMeCardsActivity";
    private static final int PLACE_PICKER_REQUEST = 1;

    private static final int ERROR_DIALOG_REQUEST = 9001;

    private static final String SELECTED_PLACE_ID = "SELECTED_PLACE_ID";
    private static final String SELECTED_PLACE_NAME = "SELECTED_PLACE_NAME";
    private static final String SELECTED_PLACE_ADDRESS = "SELECTED_PLACE_ADDRESS";

    private String selectedPlaceId;
    private String selectedPlaceName;
    private String selectedPlaceAddress;

    private RecyclerView recyclerView;
    private TextView emptyView;
    private TextView locationPlaceName;
    private TextView locationPlaceAddress;

    private final CollectionReference cardsCollRef = FirebaseFirestore.getInstance().collection("cards");

    public static void start(Context context) {
        Intent starter = new Intent(context, NearMeCardsActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_near_me_cards);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Toolbar back arrow
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Change toolbar and status bar colors
        int toolbarColor = Color.parseColor("#ff5722");
        ThemeColorUtils.changeActivityColors(this, toolbar, toolbarColor);

        locationPlaceName = findViewById(R.id.location_name_textView);
        locationPlaceAddress = findViewById(R.id.location_address_imageView);
        ImageView changeLocationImageView = findViewById(R.id.change_location_imageView);

        changeLocationImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchPlacePicker();
            }
        });

        // Recycler view
        recyclerView = findViewById(R.id.near_me_cards_recyclerView);
        // Divider lines
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        // LinearLayoutManager
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Empty list view
        emptyView = findViewById(R.id.empty_textView);

        if (savedInstanceState == null) {
            launchPlacePicker();
        }
    }

    private boolean isPlayServicesOK() {
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (available == ConnectionResult.SUCCESS) {
            // everything is fine
            Log.d(TAG, "isPlayServicesOK: Google Play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            Log.d(TAG, "isPlayServicesOK: An error occurred but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, R.string.play_services_error, Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private void launchPlacePicker() {
        if (isPlayServicesOK()) {
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            try {
                startActivityForResult(builder.build(NearMeCardsActivity.this), PLACE_PICKER_REQUEST);
            } catch (GooglePlayServicesRepairableException e) {
                Log.e(TAG, "GooglePlayServicesRepairableException: " + e.getMessage());
            } catch (GooglePlayServicesNotAvailableException e) {
                Log.e(TAG, "GooglePlayServicesNotAvailableException: " + e.getMessage());
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                selectedPlaceName = place.getName().toString();
                locationPlaceName.setText(selectedPlaceName);
                selectedPlaceAddress = place.getAddress().toString();
                locationPlaceAddress.setText(selectedPlaceAddress);
                selectedPlaceId = place.getId();
                populateRecyclerView(selectedPlaceId);
            } else {
                if (selectedPlaceId == null) {
                    finish();
                }
            }
        }
    }

    // Populate results
    private void populateRecyclerView(String placeId) {

        // Set up firebase query
        //noinspection SpellCheckingInspection
        final Query query = cardsCollRef.whereEqualTo("places." + placeId, true);
        Log.d(TAG, "onActivityResult: selectedPlaceId " + selectedPlaceId );

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
        final FirestoreRecyclerAdapter adapter = new CommunityCardListAdapter(options, this, recyclerView, emptyView, this) {
            @Override
            public void onDataChanged() {
                if (getItemCount() == 0) {
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


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SELECTED_PLACE_ID, selectedPlaceId);
        outState.putString(SELECTED_PLACE_NAME, selectedPlaceName);
        outState.putString(SELECTED_PLACE_ADDRESS, selectedPlaceAddress);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        selectedPlaceId = savedInstanceState.getString(SELECTED_PLACE_ID);
        selectedPlaceName = savedInstanceState.getString(SELECTED_PLACE_NAME);
        selectedPlaceAddress = savedInstanceState.getString(SELECTED_PLACE_ADDRESS);
        populateRecyclerView(selectedPlaceId);
        locationPlaceName.setText(selectedPlaceName);
        locationPlaceAddress.setText(selectedPlaceAddress);
    }
}
