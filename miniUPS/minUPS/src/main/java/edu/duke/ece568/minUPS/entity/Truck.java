package edu.duke.ece568.minUPS.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table
public class Truck {
    @Id
    private Integer truckID;
    @Column(nullable = false)
    private String status;
    @Column(nullable = false)
    private Integer posX;
    @Column(nullable = false)
    private Integer posY;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getPosX() {
        return posX;
    }

    public void setPosX(Integer posX) {
        this.posX = posX;
    }

    public Integer getPosY() {
        return posY;
    }

    public void setPosY(Integer posY) {
        this.posY = posY;
    }

    public Integer getTruckID() {
        return truckID;
    }

    public void setTruckID(Integer id) {
        this.truckID = id;
    }

    public enum Status {
        IDLE("idle"),
        TRAVELING("traveling"),
        ARRIVE("arrive warehouse"),
        LOADING("loading"),
        DELIVERING("delivering");

        private String text;

        Status(String str) {
            this.text = str;
        }

        public String getText() {
            return text;
        }

        public static Status fromString(String text) {
            for (Status s : Status.values()) {
                if (s.text.equalsIgnoreCase(text)) {
                    return s;
                }
            }
            throw new IllegalArgumentException("No such enum " + text);
        }
    }
}
