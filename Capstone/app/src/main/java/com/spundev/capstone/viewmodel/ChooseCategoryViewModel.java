package com.spundev.capstone.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import java.util.List;

import com.spundev.capstone.model.Category;
import com.spundev.capstone.repository.LocalCategoriesRepository;

public class ChooseCategoryViewModel extends AndroidViewModel {

    private final LiveData<List<Category>> allCategories;

    public ChooseCategoryViewModel(@NonNull Application application) {
        super(application);

        LocalCategoriesRepository repository = new LocalCategoriesRepository(application);
        allCategories = repository.getAllCategories();
    }

    public LiveData<List<Category>> getAllCategories() {
        return allCategories;
    }
}
