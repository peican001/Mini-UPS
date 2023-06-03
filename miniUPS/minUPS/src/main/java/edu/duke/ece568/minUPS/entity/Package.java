package edu.duke.ece568.minUPS.entity;

import javax.persistence.*;

@Entity
@Table
public class Package {
    @Id
    private Long packageID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "truckID")
    private Truck truck;
    private String upsID;

    public Integer getWarehouseID() {
        return warehouseID;
    }

    public void setWarehouseID(Integer warehouseID) {
        this.warehouseID = warehouseID;
    }

    @Column(nullable = false)
    private Integer warehouseID;
    @Column(nullable = false)
    private String details;
    @Column(nullable = false)
    private Integer destinationX;
    @Column(nullable = false)
    private Integer destinationY;
    @Column(nullable = false)
    private String status;

    public Truck getTruck() {
        return truck;
    }

    public void setTruck(Truck truck) {
        this.truck = truck;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public enum Status {
        CREATED("create"),
        ROUTING("truck in route package"),
        WAITING("truck waiting for package"),
        DELIVERING("delivering"),
        DELIVERED("delivered");

        private String text;

        Status(String str) {
            this.text = str;
        }

        public String getText() {
            return text;
        }

        public static Package.Status fromString(String text) {
            for (Package.Status s : Package.Status.values()) {
                if (s.text.equalsIgnoreCase(text)) {
                    return s;
                }
            }
            throw new IllegalArgumentException("No such enum " + text);
        }
    }

    public String getUpsID() {
        return upsID;
    }

    public void setUpsID(String upsID) {
        this.upsID = upsID;
    }

    public Integer getDestinationX() {
        return destinationX;
    }

    public void setDestinationX(Integer destinationX) {
        this.destinationX = destinationX;
    }

    public Integer getDestinationY() {
        return destinationY;
    }

    public void setDestinationY(Integer destinationY) {
        this.destinationY = destinationY;
    }

    public Long getPackageID() {
        return packageID;
    }

    public void setPackageID(Long pid) {
        this.packageID = pid;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String description) {
        this.details = description;
    }

}

