package com.example.mechanic_pc.uzhgorodschools.data;

public class SchoolData {
    private String name;
    private String url;
    private String date;
    private String notificationTitle;
    private String telephoneNumber;
    private String email;

    public SchoolData(){
    }

    public SchoolData(String name, String url){
        this.name = name;
        this.url = url;
    }

    public SchoolData(String name, String url, String date){
        this(name, url);
        this.date = date;
    }

    public SchoolData(String name, String url, String telephoneNumber, String email) {
        this(name, url);
        this.telephoneNumber = telephoneNumber;
        this.email = email;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getNotificationTitle() {
        return notificationTitle;
    }

    public void setNotificationTitle(String notificationTitle) {
        this.notificationTitle = notificationTitle;
    }

    public String getTelephoneNumber() {
        return telephoneNumber;
    }

    public void setTelephoneNumber(String telephoneNumber) {
        this.telephoneNumber = telephoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
