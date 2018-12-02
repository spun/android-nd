package com.spundev.capstone.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.transition.AutoTransition;
import android.support.transition.Transition;
import android.support.transition.TransitionManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.spundev.capstone.R;
import com.spundev.capstone.model.Card;
import com.spundev.capstone.util.CardAnimator;

import java.util.List;

import static com.spundev.capstone.util.AnimUtils.getFastOutSlowInInterpolator;

public class LocalCardListAdapter extends RecyclerView.Adapter<LocalCardListAdapter.CardViewHolder> {

    // Click handlers
    private final Context context;
    private final RecyclerView recyclerView;
    private final CardListAdapterHandler adapterClickHandler;

    // Expand collapse animation
    private static final int EXPAND = 0x1;
    private static final int COLLAPSE = 0x2;
    private static final int ACTIONS = 0x3;
    private int expandedCardPosition = RecyclerView.NO_POSITION;
    private final Transition expandCollapse;
    private final CardAnimator cardAnimator;

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

    public interface CardListAdapterHandler {
        void onCardPlayListener(Card card);

        void onCardFavoriteListener(Card card, boolean newValue);

        void onCardShareListener(Card card);

        void onCardEditListener(Card card);

        void onCardDeleteListener(Card card);
    }

    private final LayoutInflater layoutInflater;
    private List<Card> cardsList; // Cached copy of categories

    public LocalCardListAdapter(Context context, RecyclerView rv, CardListAdapterHandler ch) {
        this.layoutInflater = LayoutInflater.from(context);
        this.context = context;
        this.recyclerView = rv;
        this.adapterClickHandler = ch;
        setHasStableIds(true);

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
        View itemView = layoutInflater.inflate(R.layout.list_item_card_local, parent, false);
        return new CardViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final CardViewHolder holder, final int position) {
        Card current = cardsList.get(position);
        holder.textTextView.setText(current.getText());
        holder.setCollapsed();

        if (current.isFavourite()) {
            holder.cardNotFavoriteButton.setVisibility(View.GONE);
            holder.cardFavoriteButton.setVisibility(View.VISIBLE);
        } else {
            holder.cardNotFavoriteButton.setVisibility(View.VISIBLE);
            holder.cardFavoriteButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position, @NonNull List<Object> partialChangePayloads) {

        if ((partialChangePayloads.contains(EXPAND))) {
            holder.setExpanded();
        } else if (partialChangePayloads.contains(COLLAPSE)) {
            holder.setCollapsed();
        } else if (partialChangePayloads.contains(ACTIONS)) {
            holder.setViewActions();
        } else {
            onBindViewHolder(holder, position);
        }
    }

    public void setCards(List<Card> cards) {
        cardsList = cards;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return cardsList.get(position).getId();
    }

    @Override
    public int getItemCount() {
        if (cardsList != null)
            return cardsList.size();
        else return 0;
    }

    class CardViewHolder extends RecyclerView.ViewHolder {

        private final TextView textTextView;
        private final ImageView cardOpenImageView;
        private final ImageView cardCloseImageView;
        private final View actionsButtonBar;
        private final View moreActionsButtonBar;
        private final ImageView cardActionsButton;
        private final ImageView cardCloseActionsButton;
        private final Button cardPlayButton;
        // Extra buttons
        private final ImageView cardFavoriteButton;
        private final ImageView cardNotFavoriteButton;
        private final ImageView cardShareButton;
        private final ImageView cardEditButton;
        private final ImageView cardDeleteButton;


        CardViewHolder(final View itemView) {
            super(itemView);
            textTextView = itemView.findViewById(R.id.card_text_textView);
            cardOpenImageView = itemView.findViewById(R.id.card_open_imageView);
            cardCloseImageView = itemView.findViewById(R.id.card_close_imageView);
            // Action bars
            actionsButtonBar = itemView.findViewById(R.id.actions_button_bar);
            moreActionsButtonBar = itemView.findViewById(R.id.more_actions_button_bar);
            cardActionsButton = itemView.findViewById(R.id.card_actions_button);
            cardCloseActionsButton = itemView.findViewById(R.id.card_close_actions_button);
            cardPlayButton = itemView.findViewById(R.id.card_play_button);
            // Extra buttons
            cardFavoriteButton = itemView.findViewById(R.id.card_favorite_button);
            cardNotFavoriteButton = itemView.findViewById(R.id.card_not_favorite_button);
            cardShareButton = itemView.findViewById(R.id.card_share_button);
            cardEditButton = itemView.findViewById(R.id.card_edit_button);
            cardDeleteButton = itemView.findViewById(R.id.card_delete_button);

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

            // Show extra actions
            cardActionsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position == RecyclerView.NO_POSITION) return;

                    TransitionManager.beginDelayedTransition(recyclerView, new AutoTransition().setDuration(100));
                    cardAnimator.setAnimateMoves(false);

                    notifyItemChanged(position, ACTIONS);
                }
            });

            // Close extra actions
            cardCloseActionsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position == RecyclerView.NO_POSITION) return;

                    TransitionManager.beginDelayedTransition(recyclerView, new AutoTransition().setDuration(100));
                    cardAnimator.setAnimateMoves(false);

                    notifyItemChanged(position, EXPAND);
                }
            });

            // Play card
            cardPlayButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (adapterClickHandler != null) {
                        int adapterPosition = getAdapterPosition();
                        if (adapterPosition != RecyclerView.NO_POSITION) {
                            Card selectedCard = cardsList.get(adapterPosition);
                            adapterClickHandler.onCardPlayListener(selectedCard);
                        }
                    }
                }
            });

            // Add card to favorites
            cardFavoriteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (adapterClickHandler != null) {
                        int adapterPosition = getAdapterPosition();
                        if (adapterPosition != RecyclerView.NO_POSITION) {
                            Card selectedCard = cardsList.get(adapterPosition);
                            adapterClickHandler.onCardFavoriteListener(selectedCard, false);
                        }
                    }
                }
            });

            // Add card to favorites
            cardNotFavoriteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (adapterClickHandler != null) {
                        int adapterPosition = getAdapterPosition();
                        if (adapterPosition != RecyclerView.NO_POSITION) {
                            Card selectedCard = cardsList.get(adapterPosition);
                            adapterClickHandler.onCardFavoriteListener(selectedCard, true);
                        }
                    }
                }
            });

            // Share card with the community
            cardShareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (adapterClickHandler != null) {
                        int adapterPosition = getAdapterPosition();
                        if (adapterPosition != RecyclerView.NO_POSITION) {
                            Card selectedCard = cardsList.get(adapterPosition);
                            adapterClickHandler.onCardShareListener(selectedCard);
                        }
                    }
                }
            });

            // Edit card content
            cardEditButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (adapterClickHandler != null) {
                        int adapterPosition = getAdapterPosition();
                        if (adapterPosition != RecyclerView.NO_POSITION) {
                            Card selectedCard = cardsList.get(adapterPosition);
                            adapterClickHandler.onCardEditListener(selectedCard);
                        }
                    }
                }
            });

            // Delete card
            cardDeleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (adapterClickHandler != null) {
                        int adapterPosition = getAdapterPosition();
                        if (adapterPosition != RecyclerView.NO_POSITION) {
                            Card selectedCard = cardsList.get(adapterPosition);
                            adapterClickHandler.onCardDeleteListener(selectedCard);
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
            moreActionsButtonBar.setVisibility(View.GONE);
            itemView.setActivated(false);
        }

        void setExpanded() {
            textTextView.setTextAppearance(context, android.support.v7.appcompat.R.style.TextAppearance_AppCompat_Subhead);
            cardOpenImageView.setVisibility(View.GONE);
            cardCloseImageView.setVisibility(View.VISIBLE);
            actionsButtonBar.setVisibility(View.VISIBLE);
            moreActionsButtonBar.setVisibility(View.GONE);
            itemView.setActivated(true);
            itemView.setOnClickListener(null);

        }

        void setViewActions() {
            textTextView.setTextAppearance(context, android.support.v7.appcompat.R.style.TextAppearance_AppCompat_Subhead);
            cardOpenImageView.setVisibility(View.GONE);
            cardCloseImageView.setVisibility(View.VISIBLE);
            actionsButtonBar.setVisibility(View.GONE);
            moreActionsButtonBar.setVisibility(View.VISIBLE);
            itemView.setActivated(true);
        }
    }
}