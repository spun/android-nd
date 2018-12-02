# README

## API key
The key should be included in the file "gradle.properties" with the format:  
`MyMovieDbApiKey = "..."`

## Layouts structure
Three main activities:

### Main activity 
Popular, top rated and favorites lists.

```
activity_main.xml
├── @+id/toolbar
└── content_main.xml
    ├── @+id/main_container  (popular, top rated and favorites fragments)
    ├── @+id/bottom_navigation_bar
    └── @+id/detail_container (** only if sw > 600dp, two pane **)
```

### Detail activity 
Popular, top rated and favorites lists.

```
activity_detail.xml
├── @+id/toolbar
└── @+id/detail_container
    └── fragment_detail.xml (also with a sw600dp variant)
        └── movie_data.xml  (all the movie details)
```

We separate the content of the details in its own xml (`movie_data.xml`) to simplify the content in `fragment_detail.xml` and `fragment_detail.xml[sw600dp]`.

### Settings activity 
Theme chooser. Content of the settings in `xml/pref_general.xml`.

```
activity_settings.xml
├── @+id/toolbar
└── @+id/settings_fragment
```

## 3. Some extras

### - Data binding
Use of the Data Binding library.

### - Launcher App Shortcuts (nougat +)
On nougat or more recent versions, you can hold the app icon to reveal specific actions that let the users quickly start common or recommended tasks. In this case, we have a shortcut for each list of movies (popular, top rated, favorites).

### - Bottom navigation bar
It uses a bottom navigation bar to choose between popular, top rated or favorites.

### - Dark theme (from settings)
It includes a settings activity in which you can change between a dark or a light theme.

### - Share first video
You can share the url of the first video in the detail view. If the movie doesn't have videos, the share button will not appear.

### - Expandable comments
By default we only show four lines per comment/review. To see the full content you can click on them to expand or collapse comments individually.
