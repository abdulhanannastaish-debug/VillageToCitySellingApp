package com.example.villagetocityreseilingapp.model;

public class Seller {

    private String uid;
    private String sellerName;
    private String email;
    private String phone;
    private String location;
    private String cnic;
    private String cnicFrontImage;
    private String cnicBackImage;
    private boolean isVerified;

    public Seller() {
        // Empty constructor (Firebase ke liye zaroori)
    }

    public Seller(String uid, String sellerName, String email,
                  String phone, String location, String cnic,
                  String cnicFrontImage, String cnicBackImage,
                  boolean isVerified) {

        this.uid = uid;
        this.sellerName = sellerName;
        this.email = email;
        this.phone = phone;
        this.location = location;
        this.cnic = cnic;
        this.cnicFrontImage = cnicFrontImage;
        this.cnicBackImage = cnicBackImage;
        this.isVerified = isVerified;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCnic() {
        return cnic;
    }

    public void setCnic(String cnic) {
        this.cnic = cnic;
    }

    public String getCnicFrontImage() {
        return cnicFrontImage;
    }

    public void setCnicFrontImage(String cnicFrontImage) {
        this.cnicFrontImage = cnicFrontImage;
    }

    public String getCnicBackImage() {
        return cnicBackImage;
    }

    public void setCnicBackImage(String cnicBackImage) {
        this.cnicBackImage = cnicBackImage;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }
}
