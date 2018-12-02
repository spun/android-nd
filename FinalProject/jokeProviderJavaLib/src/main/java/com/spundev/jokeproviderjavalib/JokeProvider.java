package com.spundev.jokeproviderjavalib;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class JokeProvider {

    private static final List<String> badJokes = Arrays.asList(
            "What time did the man go to the dentist? Tooth hurt-y.",
            "I'm reading a book about anti-gravity. It's impossible to put down!",
            "Want to hear a joke about a piece of paper? Never mind... it's tearable.",
            "Me: 'Dad, make me a sandwich!' Dad: 'Poof, You're a sandwich!'",
            "Two peanuts were walking down the street. One was a salted.",
            "I used to have a job at a calendar factory but I got the sack because I took a couple of days off.",
            "Did you hear about the restaurant on the moon? Great food, no atmosphere.",
            "How many apples grow on a tree? All of them.",
            "The rotation of earth really makes my day."
    );


    public static String getJoke() {
        Random random = new Random();
        return badJokes.get(random.nextInt(badJokes.size()));
    }
}
