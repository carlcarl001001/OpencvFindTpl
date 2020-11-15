package com.demo.carl.opencvhelloworld;

import android.graphics.RectF;

public class Result {
    public RectF box;
    public double score;

    public Result(RectF box, double score) {
        this.box = box;
        this.score = score;
    }
}
