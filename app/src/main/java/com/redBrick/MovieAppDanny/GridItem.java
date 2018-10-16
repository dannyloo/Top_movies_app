package com.redBrick.MovieAppDanny;

import java.io.Serializable;

public class GridItem implements Serializable{
    private String image;
    private String trailer;
    private String title;
    private String fanRating;

    public GridItem() {
        super();
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTrailer(String trailer){ this.trailer = trailer; }

    public String getTrailer(){ return trailer; }

    public String getFanRating() {
        return fanRating;
    }

    public void setFanRating(String fanRating) {
        this.fanRating = fanRating;
    }
}
