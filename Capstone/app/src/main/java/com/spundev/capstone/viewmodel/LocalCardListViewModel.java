package com.spundev.capstone.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import java.util.List;

import com.spundev.capstone.model.Card;
import com.spundev.capstone.repository.LocalCardsRepository;

public class LocalCardListViewModel extends AndroidViewModel {

    private final LocalCardsRepository repository;
    private final LiveData<List<Card>> categoryCards;

    LocalCardListViewModel(@NonNull Application application, final int categoryId) {
        super(application);

        repository = new LocalCardsRepository(application);
        categoryCards = repository.getAllCardsFromCategory(categoryId);
    }

    public LiveData<List<Card>> getAllCardsFromCategory() {
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


    /**
     * A creator is used to inject the category ID into the ViewModel
     */
    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        @NonNull
        private final Application mApplication;
        private final int mCategoryId;

        public Factory(@NonNull Application application, int productId) {
            mApplication = application;
            mCategoryId = productId;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            //noinspection unchecked
            return (T) new LocalCardListViewModel(mApplication, mCategoryId);
        }
    }
}
