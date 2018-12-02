package com.spundev.capstone.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.transition.AutoTransition;
import android.support.transition.Transition;
import android.support.transition.TransitionManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.spundev.capstone.R;
import com.spundev.capstone.model.firestore.CardFirestore;
import com.spundev.capstone.util.CardAnimator;
import com.spundev.capstone.util.CommunityUtils;

import java.util.List;

import static com.spundev.capstone.util.AnimUtils.getFastOutSlowInInterpolator;

public class CommunityCardListAdapter extends FirestoreRecyclerAdapter<CardFirestore, CommunityCardListAdapter.CardViewHolder> {

    private final Context context;
    private final RecyclerView recyclerView;
    private final CommunityCardListAdapter.CardListAdapterHandler adapterClickHandler;

    // Expand collapse animation
    private static final int EXPAND = 0x1;
    private static final int COLLAPSE = 0x2;
    private int expandedCardPosition = RecyclerView.NO_POSITION;
    private final Transition expandCollapse;
    private final CardAnimator cardAnimator;

    public interface CardListAdapterHandler {
        void onCardPlayListener(CardFirestore card);

        void onCardSaveListener(CardFirestore card);
    }

    /**
     * We run a transition to expand/collapse comments. Scrolling the RecyclerView while this is
     * running causes issues, so we consume touch events while the transition runs.
     */
    private final View.OnTouchListener touchEater = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return true;
        }
    };

    public CommunityCardListAdapter(@NonNull FirestoreRecyclerOptions<CardFirestore> options, Context context, RecyclerView rv, View emptyListView, CommunityCardListAdapter.CardListAdapterHandler ch) {
        super(options);
        this.context = context;
        this.recyclerView = rv;
        this.adapterClickHandler = ch;

        cardAnimator = new CardAnimator();
        recyclerView.setItemAnimator(cardAnimator);

        expandCollapse = new AutoTransition();
        expandCollapse.setDuration(context.getResources().getInteger(R.integer.comment_expand_collapse_duration));
        expandCollapse.setInterpolator(getFastOutSlowInInterpolator());
        expandCollapse.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(@NonNull Transition transition) {
                recyclerView.setOnTouchListener(touchEater);
            }

            @Override
            public void onTransitionEnd(@NonNull Transition transition) {
                cardAnimator.setAnimateMoves(true);
                recyclerView.setOnTouchListener(null);
            }

            @Override
            public void onTransitionCancel(@NonNull Transition transition) {
            }

            @Override
            public void onTransitionPause(@NonNull Transition transition) {
            }

            @Override
            public void onTransitionResume(@NonNull Transition transition) {
            }
        });
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create a new instance of the ViewHolder, in this case we are using a custom
        // layout called R.layout.list_item_card_legacy for each item
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_card_community, parent, false);

        return new CardViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onBindViewHolder(@NonNull CardViewHolder holder, int position, @NonNull CardFirestore model) {
        holder.textTextView.setText(model.getText());
        if (model.getScore() > 0) {
            holder.cardScoreTextView.setText("+" + String.valueOf(model.getScore()));
            holder.cardScoreTextView.setTextColor(ContextCompat.getColor(context, R.color.positive_score));
        } else if (model.getScore() < 0) {
            holder.cardScoreTextView.setText("-" + String.valueOf(model.getScore()));
            holder.cardScoreTextView.setTextColor(ContextCompat.getColor(context, R.color.negative_score));
        } else {
            holder.cardScoreTextView.setText(String.valueOf(model.getScore()));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        CardFirestore current = getItem(position);
        onBindViewHolder(holder, position, current);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position, @NonNull List<Object> payloads) {
        if ((payloads.contains(EXPAND))) {
            holder.setExpanded();
        } else if (payloads.contains(COLLAPSE)) {
            holder.setCollapsed();
        } else {
            onBindViewHolder(holder, position);
        }
    }

    class CardViewHolder extends RecyclerView.ViewHolder {
        private final TextView textTextView;
        private final ImageView cardOpenImageView;
        private final ImageView cardCloseImageView;
        private final View actionsButtonBar;
        private final Button cardPlayButton;
        private final ImageView cardSaveButton;
        private final TextView cardScoreTextView;
        private final ImageView cardUpVoteButton;
        private final ImageView cardDownVoteButton;

        CardViewHolder(final View itemView) {
            super(itemView);
            textTextView = itemView.findViewById(R.id.card_text_textView);
            cardOpenImageView = itemView.findViewById(R.id.card_open_imageView);
            cardCloseImageView = itemView.findViewById(R.id.card_close_imageView);
            actionsButtonBar = itemView.findViewById(R.id.actions_button_bar);
            cardPlayButton = itemView.findViewById(R.id.card_play_button);
            cardSaveButton = itemView.findViewById(R.id.card_save_button);
            cardScoreTextView = itemView.findViewById(R.id.card_score_textView);
            cardUpVoteButton = itemView.findViewById(R.id.card_up_vote_button);
            cardDownVoteButton = itemView.findViewById(R.id.card_down_vote_button);

            initListeners();
        }

        private void initListeners() {

            // Expand/collapse when we tap the text
            View.OnClickListener openCloseClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position == RecyclerView.NO_POSITION) return;

                    TransitionManager.beginDelayedTransition(recyclerView, expandCollapse);
                    cardAnimator.setAnimateMoves(false);

                    // collapse any currently expanded items
                    if (RecyclerView.NO_POSITION != expandedCardPosition) {
                        notifyItemChanged(expandedCardPosition, COLLAPSE);
                    }

                    // expand this item (if it wasn't already)
                    if (expandedCardPosition != position) {
                        expandedCardPosition = position;
                        notifyItemChanged(position, EXPAND);

                        itemView.requestFocus();
                        recyclerView.scrollToPosition(position);

                    } else {
                        expandedCardPosition = RecyclerView.NO_POSITION;
                    }
                }
            };
            textTextView.setOnClickListener(openCloseClickListener);
            cardOpenImageView.setOnClickListener(openCloseClickListener);
            cardCloseImageView.setOnClickListener(openCloseClickListener);


            // Play card
            cardPlayButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (adapterClickHandler != null) {
                        int adapterPosition = getAdapterPosition();
                        if (adapterPosition != RecyclerView.NO_POSITION) {
                            CardFirestore selectedCard = getItem(adapterPosition);
                            adapterClickHandler.onCardPlayListener(selectedCard);
                        }
                    }
                }
            });

            // Save card
            cardSaveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (adapterClickHandler != null) {
                        int adapterPosition = getAdapterPosition();
                        if (adapterPosition != RecyclerView.NO_POSITION) {
                            CardFirestore selectedCard = getItem(adapterPosition);
                            adapterClickHandler.onCardSaveListener(selectedCard);
                        }
                    }
                }
            });

            // up vote card
            // Save card
            cardUpVoteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (adapterClickHandler != null) {
                        int adapterPosition = getAdapterPosition();
                        if (adapterPosition != RecyclerView.NO_POSITION) {
                            CardFirestore selectedCard = getItem(adapterPosition);
                            CommunityUtils.addScore(selectedCard);
                        }
                    }
                }
            });


            // down vote
            cardDownVoteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (adapterClickHandler != null) {
                        int adapterPosition = getAdapterPosition();
                        if (adapterPosition != RecyclerView.NO_POSITION) {
                            CardFirestore selectedCard = getItem(adapterPosition);
                            CommunityUtils.subScore(selectedCard);
                        }
                    }
                }
            });
        }

        void setCollapsed() {
            textTextView.setTextAppearance(context, android.support.v7.appcompat.R.style.TextAppearance_AppCompat_Body2);
            cardOpenImageView.setVisibility(View.VISIBLE);
            cardCloseImageView.setVisibility(View.GONE);
            actionsButtonBar.setVisibility(View.GONE);
            itemView.setActivated(false);
        }

        void setExpanded() {
            textTextView.setTextAppearance(context, android.support.v7.appcompat.R.style.TextAppearance_AppCompat_Subhead);
            cardOpenImageView.setVisibility(View.GONE);
            cardCloseImageView.setVisibility(View.VISIBLE);
            actionsButtonBar.setVisibility(View.VISIBLE);
            itemView.setActivated(true);
            itemView.setOnClickListener(null);

        }
    }
}
