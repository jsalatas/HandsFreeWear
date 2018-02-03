package gr.ictpro.jsalatas.gestures.model;

import java.util.List;

public class Point {

    private Long time;

    private Integer xa;

    private Integer ya;

    private Integer za;

    private Integer classification;

    public Point(long time, Integer xa, Integer ya, Integer za, Integer classification) {
        this.time = time;
        this.xa = xa;
        this.ya = ya;
        this.za = za;
        this.classification = classification;
    }

//    public Integer getId() {
//        return id;
//    }

    public Long getTime() {
        return time;
    }

    public Integer getXa() {
        return xa;
    }

    public Integer getYa() {
        return ya;
    }

    public Integer getZa() {
        return za;
    }


    public Integer getClassification() {
        return classification;
    }

    public void setClassification(Integer classification) {
        this.classification = classification;
    }

    @Override
    public String toString() {
        return "Point{" +
//                "" + id +
                " " + time +
                " " + xa +
                " " + ya +
                " " + za +
                ": " + classification +
                '}';
    }
}
