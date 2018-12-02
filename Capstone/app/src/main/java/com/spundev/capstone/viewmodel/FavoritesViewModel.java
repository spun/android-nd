package com.spundev.capstone.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import java.util.List;

import com.spundev.capstone.model.Card;
import com.spundev.capstone.repository.LocalCardsRepository;

public class FavoritesViewModel extends AndroidViewModel {

    private final LocalCardsRepository repository;
    private final LiveData<List<Card>> categoryCards;

    public FavoritesViewModel(@NonNull Application application) {
        super(application);

        repository = new LocalCardsRepository(application);
        categoryCards = repository.getAllFavoriteCards();
    }

    public LiveData<List<Card>> getAllFavoriteCards() {
        return categoryCards;
    }

    public void insert(Card card) {
        repository.insert(card);
    }

    public void update(Card card) {
        repository.update(card);
    }

    public void delete(Card card) {
        repository.delete(card);
    }


}
