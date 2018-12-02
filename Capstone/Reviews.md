# Feedback based changes v2

1) "You are still using Firestore dependency in your app."

- I have contacted support and I am waiting for an answer about this, the use of Firestore was one of my "Key Considerations" for the data persistance in stage 1. I choosed Firestore because some limitations of the Firebase RealtimeDB that didn't fit well for my needs around the "near me" feature and I don't want to develop my own backend for a feature that I think is an extra for the project. I don't know what else to do at the moment.

2) "Your widget is not displaying any items even when there are few items which are marked as favourite."

- Fixed. I left commented the <ListView />. Sorry about that.

3) "The buttons are not fully visible in landscape mode. (activity_my_account.xml)"

- Fixed with a parent scroll view.

# Feedback based changes v1

1) "You have some missing contentDescription attributes."

- Added all content descriptions missing

2) "You did not implement your widget."

- Added favorites widget

3) "You did not include the keystore file in your repo"

- Added keystore (root folder as keystore.jks) and a signingConfigs.

4) "Your installRelease task does not run successfully"

- Fixed.

# Capstone

## Description

Application created to help people with hearing impairments to communicate with other person using text or generated voice.

## Important information about this project

This project uses some features of Firebase like Firebase Auth or Firebase Firestore. The configuration file "google-services.json" is included (although I know it shouldn't be shared) because it doesn't exists an export option of the database and the structure of the firestore was to be exact in order to work.

This project also uses the Places API, and the key is declared in the android manifest xml. This key I think it shouldn't be necesary to be included and I apologise if is the case, but I want to make it easy to check and I think that the places api is not as known as the maps or others and I didn't want to be confusing.

In the folder "screeshots" I have included some screen captures of the Firebase console and the structure of the database that is being used. If it is necessary to offer access to firebase to review the project, I think it is not difficult to add users.

## Planned features (Stage 1)

- Organization of text cards in categories with colors (shops, hospital, work)

- Show as text or voice (text to speech) what the person with hearing or speaking impairment wants to communicate (the text of the card).

- Receive the response of the person in front using voice (speech to text) or written text.

- See and rate cards recommended by the community and save them to your device, so you can use them later without a connection. (**1)

- See cards about topics related to your location (health related if you are in a hospital, etc). (**2)

- Choose the output language so you can communicate with people from other countries. (**3)

**1: This feature uses the firebase firestore database (with firebaseui) and the project includes the required "google-services.json". This file shouldn't be shared, but I can't export the firebase database and I don't know a better way to share it.

**2: This feature also uses the firestore database and it requires that the cards are assigned manually to a place. In this example I only have attached a couple of cards to the "Eiffel Tower" as an example.

**3: I started using retrofit to fetch the translations but then I noticed that the "Translation api" is a paid service and I stopped developing it. There is some code related to this feature in Utils and in the sst activity.

## Other features

- Use of architecture components (with room)

- User account activity (firebase auth). This was intended to limit the community card votes to one per card per user, but the logic should be implemented in the backend, maybe using firebase cloud functions. **Important**: I had to add a sha1 key of my developer keystore in order to work with firebase auth. I don't know if that key is the same for every installation of the sdk and it may not work if they are different.

- Ability to share a text from another app to this one and show it in the TTS activity (intent filter).

- Basic crash reporting (only production build).

- App Launch shortcuts (favorites and near me).

- Search using a search provider (https://youtu.be/9OWmnYPX1uc) and custom recommendations.

- Places api

- ...

## Sources

- The expand card animation is based in the code of the project Plaid (https://github.com/nickbutcher/plaid)

- Other uses of code from sources like stackoverflow or youtube are mentioned directly in the code.