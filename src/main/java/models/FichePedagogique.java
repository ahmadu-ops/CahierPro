package models;

import java.time.LocalDate;
import java.util.List;

public class FichePedagogique {
    private int id;
    private int coursId;
    private LocalDate dateGeneration;
    private double pourcentageRealisation;
    private String commentaire;
    private List<Seance> seances; // Les séances incluses dans la fiche

    public FichePedagogique(int id, int coursId, LocalDate dateGeneration, 
                           double pourcentageRealisation, String commentaire) {
        this.id = id;
        this.coursId = coursId;
        this.dateGeneration = dateGeneration;
        this.pourcentageRealisation = pourcentageRealisation;
        this.commentaire = commentaire;
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getCoursId() {
        return coursId;
    }

    public LocalDate getDateGeneration() {
        return dateGeneration;
    }

    public double getPourcentageRealisation() {
        return pourcentageRealisation;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public List<Seance> getSeances() {
        return seances;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setCoursId(int coursId) {
        this.coursId = coursId;
    }

    public void setDateGeneration(LocalDate dateGeneration) {
        this.dateGeneration = dateGeneration;
    }

    public void setPourcentageRealisation(double pourcentageRealisation) {
        this.pourcentageRealisation = pourcentageRealisation;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public void setSeances(List<Seance> seances) {
        this.seances = seances;
    }

    @Override
    public String toString() {
        return "Fiche pédagogique #" + id + " pour le cours #" + coursId + 
               " (générée le " + dateGeneration + ", réalisation: " + pourcentageRealisation + "%)";
    }
}