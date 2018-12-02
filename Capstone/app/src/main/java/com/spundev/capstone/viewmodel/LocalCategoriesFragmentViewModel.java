package com.spundev.capstone.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import java.util.List;

import com.spundev.capstone.model.Category;
import com.spundev.capstone.repository.LocalCategoriesRepository;

public class LocalCategoriesFragmentViewModel extends AndroidViewModel {

    private final LocalCategoriesRepository repository;
    private final LiveData<List<Category>> allCategories;

    public LocalCategoriesFragmentViewModel(@NonNull Application application) {
        super(application);

        repository = new LocalCategoriesRepository(application);
        allCategories = repository.getAllCategories();
    }

    public LiveData<List<Category>> getAllCategories() {
        return allCategories;
    }

    public void insert(Category category) {
        repository.insert(category);
    }

    public void delete(Category category) {
        repository.delete(category);
    }

    public void update(Category category) {
        repository.update(category);
    }

}