package com.spundev.capstone.util;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;

import com.spundev.capstone.model.firestore.CardFirestore;

public class CommunityUtils {

    public static void addScore(CardFirestore card) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final DocumentReference sfDocRef = db.collection("cards").document(card.getId());
        card.setScore(card.getScore() + 1);
        sfDocRef.set(card, SetOptions.merge());
    }

    public static void subScore(CardFirestore card) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final DocumentReference sfDocRef = db.collection("cards").document(card.getId());
        card.setScore(card.getScore() - 1);
        sfDocRef.set(card, SetOptions.merge());
    }


    public static ArrayList<String> getRanking(String idCard) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final ArrayList<String> cards = new ArrayList<>();
        final Query sfDocRef = db.collection("cards").orderBy("score", Query.Direction.DESCENDING).limit(5);
        sfDocRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot qSnap = task.getResult();
                    if (!qSnap.isEmpty()) {
                        for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                            cards.add(doc.getId());
                        }
                    } else {
                        Log.d("Query Data", "Data is not valid");
                    }
                }
            }
        });

        return cards;
    }
}
