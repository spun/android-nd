# Corrections

## 1. App uses RecyclerView and can handle recipe steps that include videos or images.
I didn't understand if I should put the thumbnail in the recylerView steps or in the step details fragment, so I've put it in both (list_item_step.xml and fragment_step_detail.xml) using databinding and a custom BindingAdapter that uses glide.

## 2. If the video is paused and the screen is rotated, the video should remain paused
Fixed. New "mPlayWhenReady" variable saved "onSaveInstanceState".

## 3. It is required that you release Exoplayer in onPause or onStop
Fixed following the exoplayer demo code(https://github.com/google/ExoPlayer/blob/release-v2/demos/main/src/main/java/com/google/android/exoplayer2/demo/PlayerActivity.java#L186)


-------------------------------


# README

## 1. Layouts structure
Three main activities and a widget.

### 1.1. Main activity 
Recipes list.

```
activity_main.xml
├── @+id/toolbar
└── content_main.xml
    ├── @+id/recipes_recyclerView
    └── @+id/empty_recipes_view
```

The items inflated in the recycler view are different between phones and tablets.


### 1.2. RecipeDetailActivity
Steps and ingredients lists (tabs).

```
activity_recipe_detail.xml (with a sw320dp and a sw600dp variant)*
├── @+id/toolbar
└── @+id/my_view_pager
    └── fragment_recipe_steps.xml
        └── @+id/steps_recyclerView
        └── @+id/empty_steps_textView
    └── fragment_recipe_ingredients
        └── @+id/ingredients_recyclerView
        └── @+id/empty_ingredients_textView
```

*On devices with < sw320dp, we remove the collapsing toolbar.


### 1.3. StepDetailActivity
Contains a fragment with all the data about the step. This fragment will be
inflated inside `activity_recipe_detail.xml` on sw600dp devices (two pane).

```
activity_settings.xml
└── fragment_step_detail.xml
    └── @+id/main_media_frame (video)
    └── NestedScrollView    
        └── @+id/step_detail_short_description
        └── @+id/step_detail_description        
    └── @+id/stepsProgressBar (shows the recipe progress)
    └── @+id/goBackButton (previous step)
    └── @+id/goNextButton (next step)
```

### 1.4. Shopping List Widget
Widget with a list of ingredients and checkbox to mark the availability of each one.

```
shopping_list_widget.xml
└── @+id/widget_title_row
    └── @+id/widget_title_text
    └── @+id/widget_settings
└── @+id/shopping_list
└── @+id/shopping_list_empty_textView
```


## 2. Use of libraries
- firebase-jobdispatcher
- retrofit2 + gson + retrofit2:converter-gson
- glide
- support (appcompat, support, design, cardview)


## 3. Espresso tests
Each app activity has some tests where we check thing like the activity title, the content or the correct launch of other activities.


## 4. Some extras

### 4.1. Data binding
Use of the Data Binding library.

### 4.2. Offline capabilities 
Once we fetch the recipes, we save them on a local database an we use a provider to access the data.

### 4.3. Job to periodically sync the recipes
MainActivity creates a sync job using `firebase-jobdispatcher` that asks for new recipes every seven days or if we currently don't have recipes (`sync\RecipesSyncUtils.java`).

### 4.4. Recipe search
Menu item "Search" in MainActivity to filter by recipe name.

### 4.5. Ingredients shopping list
The app and widget ingredients lists have checkbox so the user can keep track of the ingredients that he or she have or need to buy the next time you go shopping. The communication between the app and checkbox is capable to "sync" both lists.

### 4.6. Best practices in media playback
Aside from the requirements, the "step video" is able to request audio focus (to stop other audio sources) and respond to focus changes (it will pause if we receive a call during a video). It uses MediaSession to receive playback commands and it will also listen to `ACTION_AUDIO_BECOMING_NOISY` to pause when we disconnect headphones from the device.
