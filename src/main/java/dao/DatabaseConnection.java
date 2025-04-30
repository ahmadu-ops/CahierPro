package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/gestion_cahier_texte"; // Remplace "nom_de_ta_base" par le nom de ta base de données
    private static final String USER = "root"; // Remplace par ton nom d'utilisateur MySQL
    private static final String PASSWORD = "   "; // Remplace par ton mot de passe MySQL, si nécessaire

    // Méthode pour obtenir la connexion
    public static Connection getConnection() throws SQLException {
        try {
            // Charger le driver JDBC
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("JDBC Driver non trouvé", e);
        }
    }

    // Méthode pour tester la connexion
    public static void main(String[] args) {
        try (Connection connection = getConnection()) {
            // Exemple d'utilisation de la connexion : exécuter une requête simple
            Statement stmt = connection.createStatement();
            stmt.executeQuery("SELECT 1"); // Requête simple pour vérifier la connexion
            System.out.println("Connexion réussie !");
        } catch (SQLException e) {
            System.out.println("Erreur de connexion : " + e.getMessage());
        }
    }
}
