**Resubmission changes**

> [X] App properly specifies elevations for app bars, FABs, and other elements specified in the Material Design specification.

Files

- `activity_article_list.xml`
- `fragment_article_detail.xml`
- `share_fab_button.xml`
- `dimens.xml`

> Others (Suggestion)
- Remove unused file `SelectionBuilder`
- Extract class `MyPagerAdapter` into a separate file.


--- Original readme ---

# README

## 1. Changes based on feedback we received

> Kagure says: “The color scheme is really sad and I shouldn't feel sad.”
- New theme colors using the orange from the logo as accent.

> Jay says: “Is the text supposed to be so wonky and unreadable? It is not accessible to those of us without perfect vision."
- The text of the detail activity is now black instead of gray.
- Standard font and text sizes.

> Lyla says: “This app is starting to shape up but it feels a bit off in quite a few places. I can't put finger on it but it feels odd.”
- Fixed the size of the images so they fill the width of the cards and the detail activity.
- Reduced the height of the main list toolbar.
- Support (collapsible) toolbar in the detail activity.

## 2. Other changes

### 2.1. Performance

With the original project, I noticed the detail activity was taking too long to launch and, after doing some testing, I realized that it was because of the long text of the articles. To reduce the time and memory of loading this text, I decided to use a recyclerview to show only the portions of the text needed and load the rest when necessary.

In order to implement this, I added a new table of paragraphs to the database and changed the service "UpdaterService" to split the text of the article in paragraphs and then add each one of them as a new row in the database.

Once this was done, the loading of the detail activity improved noticeably, but now the update service was taking longer than before because it needed to change large number of rows of the database each time.

To avoid this, the update service now generates a hash for each article (md5 is enough for our purpose) and saves it into the database. Each time we launch the update service, we generate the hash of the received article and check if we already have it in the database so we can skip it.

The usage of this RecyclerView to show the text also has affected the implementation of the layouts. For example, to show the header I had tried to use a NestedScrollView to show the header where we have the author and the date above the text:

```
NestedScrollView
├── LinearLayout  // Header
└── RecyclerView  // Text
```

But by doing this we lose all the performance improvements (Source: <https://stackoverflow.com/a/44581288>). To avoid this problem, the header is now a new type of "ViewHolder" within the RecyclerView adapter at the position 0.

After this changes, the detailed activity is now faster to launch and the flicker effect due to the deletion and recreation of the articles in the database is also gone.

### 2.2. Animations

- Added "windowEnterTransition" to the Detail acticity.
- Shared element between the "ArticleListActivity" and the detail fragment inside the ViewPager.
- Fade in effect for each image of the list of articles using "ViewPropertyAnimator".
- Use of "layoutAnimation" in the RecyclerView inside the "ArticleListActivity".
- Animated vector drawable for the share fab button.

### 2.3. Use of Support library

- Replace ActionBarActivity with AppCompatActivity.
- Theme changes (AppCompat).
- Support implementations of LoaderManager, Loader, Fragments, etc.
- Design library (fab, CoordinatorLayout, etc).
