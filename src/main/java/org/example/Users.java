package org.example;

public class Users {
    public String updatedAt;
    public String createdAt;
    public String uuid;
    public String identityProviderId;
    public String name;
    public boolean active;
    public String email;
    public int type;
    public String typeDescription;
    public String authenticationSource;
    public Embedded _embedded = new Embedded();
    public Links _links = new Links();
}
