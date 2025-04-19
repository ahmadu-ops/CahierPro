package models;

public class Responsable extends Utilisateur {
    private int classeId;

    public Responsable(int id, String nom, String prenom, String email, String motDePasse, int classeId) {
        super(id, nom, prenom, email, motDePasse, "responsable_classe");
        this.classeId = classeId;
    }

    public int getClasseId() {
        return classeId;
    }

    public void setClasseId(int classeId) {
        this.classeId = classeId;
    }

    @Override
    public String toString() {
        return "Responsable{" +
                "classeId=" + classeId +
                ", nom=" + getNom() +
                ", prenom=" + getPrenom() +
                '}';
    }
}