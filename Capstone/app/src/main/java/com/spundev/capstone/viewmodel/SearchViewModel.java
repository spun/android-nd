package com.spundev.capstone.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.spundev.capstone.model.Card;
import com.spundev.capstone.repository.LocalCardsRepository;

import java.util.List;

public class SearchViewModel extends AndroidViewModel {

    private final LocalCardsRepository repository;
    private LiveData<List<Card>> categoryCards;

    public SearchViewModel(@NonNull Application application) {
        super(application);

        repository = new LocalCardsRepository(application);
        categoryCards = null;
    }

    public LiveData<List<Card>> getAllCardsFromQuery(String searchQuery) {
        categoryCards = repository.getAllCardsFromQuery(searchQuery);
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
