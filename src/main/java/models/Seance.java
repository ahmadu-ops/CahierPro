package models;

import java.time.LocalDate;

public class Seance {
    private int id;
    private int coursId;
    private String contenu;
    private LocalDate dateSeance;
    private int duree;
    private boolean estValidee;

    public Seance(int id, int coursId, String contenu, LocalDate dateSeance, int duree, boolean estValidee) {
        this.id = id;
        this.coursId = coursId;
        this.contenu = contenu;
        this.dateSeance = dateSeance;
        this.duree = duree;
        this.estValidee = estValidee;
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getCoursId() {
        return coursId;
    }

    public String getContenu() {
        return contenu;
    }

    public LocalDate getDateSeance() {
        return dateSeance;
    }

    public int getDuree() {
        return duree;
    }

    public boolean isEstValidee() {
        return estValidee;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setCoursId(int coursId) {
        this.coursId = coursId;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public void setDateSeance(LocalDate dateSeance) {
        this.dateSeance = dateSeance;
    }

    public void setDuree(int duree) {
        this.duree = duree;
    }

    public void setEstValidee(boolean estValidee) {
        this.estValidee = estValidee;
    }

    @Override
    public String toString() {
        return "Séance du " + dateSeance + " durée: " + duree + "min, validée: " + (estValidee ? "Oui" : "Non");
    }
}