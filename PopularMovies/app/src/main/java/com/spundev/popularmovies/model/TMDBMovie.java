package com.spundev.popularmovies.model;

import android.databinding.BaseObservable;
import android.os.Parcel;
import android.os.Parcelable;

public class TMDBMovie extends BaseObservable implements Parcelable {

    private final int id;
    private final String title;
    private final String releaseDate;
    private final String posterUrl;
    private final String voteAverage;
    private final String overview;

    public TMDBMovie(int id, String title, String releaseDate, String posterUrl, String voteAverage, String overview) {
        this.id = id;
        this.title = title;
        this.releaseDate = releaseDate;
        this.posterUrl = posterUrl;
        this.voteAverage = voteAverage;
        this.overview = overview;
    }

    private TMDBMovie(Parcel in) {
        id = in.readInt();
        title = in.readString();
        releaseDate = in.readString();
        posterUrl = in.readString();
        voteAverage = in.readString();
        overview = in.readString();
    }

    public static final Creator<TMDBMovie> CREATOR = new Creator<TMDBMovie>() {
        @Override
        public TMDBMovie createFromParcel(Parcel in) {
            return new TMDBMovie(in);
        }

        @Override
        public TMDBMovie[] newArray(int size) {
            return new TMDBMovie[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(title);
        parcel.writeString(releaseDate);
        parcel.writeString(posterUrl);
        parcel.writeString(voteAverage);
        parcel.writeString(overview);
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public String getVoteAverage() {
        return voteAverage;
    }

    public String getOverview() {
        return overview;
    }
}
