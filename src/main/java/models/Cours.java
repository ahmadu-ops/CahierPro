package models;

public class Cours {
    private int id;
    private String nom;
    private int volumeHoraire;
    private int enseignantId; // Ajout pour correspondre à la base de données

    public Cours(int id, String nom, int volumeHoraire, int enseignantId) {
        this.id = id;
        this.nom = nom;
        this.volumeHoraire = volumeHoraire;
        this.enseignantId = enseignantId;
    }

        // Getters et Setters
        public int getId() {
            return id;
        }
    
        public void setId(int id) {
            this.id = id;
        }
    
        public String getIntitule() {
            return nom;
        }
    
        public void setNom(String nom) {
            this.nom = nom;
        }
    
        public int getVolumeHoraire() {
            return volumeHoraire;
        }
    
        public void setVolumeHoraire(int volumeHoraire) {
            this.volumeHoraire = volumeHoraire;
        }

    public int getEnseignantId() {
        return enseignantId;
    }

    public void setEnseignantId(int enseignantId) {
        this.enseignantId = enseignantId;
    }

    @Override
    public String toString() {
        return "Cours{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", volumeHoraire=" + volumeHoraire +
                ", enseignantId=" + enseignantId +
                '}';
    }
}