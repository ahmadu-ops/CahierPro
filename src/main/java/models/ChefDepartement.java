package models;

public class ChefDepartement extends Utilisateur {

    public ChefDepartement(int id, String nom, String prenom, String email, String motDePasse) {
        super(id, nom, prenom, email, motDePasse, "chef_departement");
    }

    @Override
    public String toString() {
        return "Chef de Département: " + super.toString();
    }
}