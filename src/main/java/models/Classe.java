package models;

public class Classe {
    private int id;
    private String nom;
    private int responsableId; // Au lieu d'une référence directe à Responsable

    public Classe(int id, String nom, int responsableId) {
        this.id = id;
        this.nom = nom;
        this.responsableId = responsableId;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public int getResponsableId() {
        return responsableId;
    }

    public void setResponsableId(int responsableId) {
        this.responsableId = responsableId;
    }

    @Override
    public String toString() {
        return "Classe{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", responsableId=" + responsableId +
                '}';
    }
}