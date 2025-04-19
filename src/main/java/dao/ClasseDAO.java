package dao;

import models.Classe;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClasseDAO {

    /**
     * Récupère toutes les classes
     */
    public static List<Classe> findAll() {
        List<Classe> classes = new ArrayList<>();
        String sql = "SELECT * FROM classe";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                classes.add(extraireClasse(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return classes;
    }

    /**
     * Récupère une classe par son ID
     */
    public static Classe findById(int id) {
        String sql = "SELECT * FROM classe WHERE id_classe = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return extraireClasse(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Récupère une classe par l'ID du responsable
     */
    public static Classe findByResponsable(int responsableId) {
        String sql = "SELECT * FROM classe WHERE id_responsable = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, responsableId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return extraireClasse(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Récupère une classe associée à un cours
     */
    public static Classe findByCoursId(int coursId) {
        String sql = "SELECT c.* FROM classe c " +
                    "JOIN classe_cours cc ON c.id_classe = cc.id_classe " +
                    "WHERE cc.id_cours = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, coursId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return extraireClasse(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Ajoute une nouvelle classe
     */
    public static boolean save(Classe classe) {
        String sql = "INSERT INTO classe (nom_classe, id_responsable) VALUES (?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, classe.getNom());
            pstmt.setInt(2, classe.getResponsableId());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        classe.setId(rs.getInt(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Met à jour une classe existante
     */
    public static boolean update(Classe classe) {
        String sql = "UPDATE classe SET nom_classe = ?, id_responsable = ? WHERE id_classe = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, classe.getNom());
            pstmt.setInt(2, classe.getResponsableId());
            pstmt.setInt(3, classe.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Supprime une classe
     */
    public static boolean delete(int id) {
        String sql = "DELETE FROM classe WHERE id_classe = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Associe un cours à une classe
     */
    public static boolean associerCours(int classeId, int coursId) {
        String sql = "INSERT INTO classe_cours (id_classe, id_cours) VALUES (?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, classeId);
            pstmt.setInt(2, coursId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Dissocie un cours d'une classe
     */
    public static boolean dissocierCours(int classeId, int coursId) {
        String sql = "DELETE FROM classe_cours WHERE id_classe = ? AND id_cours = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, classeId);
            pstmt.setInt(2, coursId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Méthode utilitaire pour extraire une classe à partir d'un ResultSet
     */
    private static Classe extraireClasse(ResultSet rs) throws SQLException {
        return new Classe(
            rs.getInt("id_classe"),
            rs.getString("nom_classe"),
            rs.getInt("id_responsable")
        );
    }
}