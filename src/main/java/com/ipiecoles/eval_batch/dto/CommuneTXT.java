package com.ipiecoles.eval_batch.dto;

public class CommuneTXT {
    private String codeInsee;
    private String codePostal;
    private Double latitude;
    private Double longitude;
    private String nom;

    public String getCodeInsee() {
        return codeInsee;
    }

    public void setCodeInsee(String codeInsee) {
        this.codeInsee = codeInsee;
    }

    public String getCodePostal() {
        return codePostal;
    }

    public void setCodePostal(String codePostal) {
        this.codePostal = codePostal;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    @Override
    public String toString() {
        return "CommuneTXT{" +
                "codeInsee='" + codeInsee + '\'' +
                ", codePostal='" + codePostal + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", nom='" + nom + '\'' +
                '}';
    }
}
