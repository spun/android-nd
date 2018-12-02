## 1. Requirements

### 1.1. Create a Java library

New Java library called `jokeProviderJavaLib`.


### 1.2. Create an Android Library

New Android Library called `jokesandroidlibrary`. It contains an Activity `JokeActivity` with a text view that shows the joke.


### 1.3. Setup GCE

New method called `tellJoke` implemented inside the class `MyEndpoint` of the backend. This method uses the Java Library `jokeProviderJavaLib` to get the joke.


### 1.4. Add Functional Tests

New test file:
`app\src\androidTest\java\com\udacity\gradle\builditbigger\EndpointsAsyncTaskTest.java`

In this test, we use the same AsyncTask that we use in our app that retrieves a joke from the backend and checks if the result is valid.


### 1.5. Add a Paid Flavor

Paid and free flavors added to the app graddle file. Each flavor implements the class `MainActivityFragment` and the `xml` for this fragment (`fragment_main.xml`).

The free flavor also includes a `string.xml` with the ad codes and an `AndroidManifest.xml` for the `google_play_services_version` meta-data tag. This files will be merged with the ones declared in the main folder.

---

## 2. Optional Tasks

For extra practice to make your project stand out, complete the following tasks.

### 2.1. Add Interstitial Ad

Added to the free version of the `MainActivityFragment`.


### 2.2. Add Loading Indicator

New ProgressBar widget added to both versions of the `MainActivityFragment` and `fragment_main.xml`.


### 2.3. Configure Test Task

New task declared inside the root `build.gradle` file.

> FinalProject > Tasks > custom tasks > runTestsWithBackend
