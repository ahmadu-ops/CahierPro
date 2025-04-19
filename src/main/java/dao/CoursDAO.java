package dao;

import models.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CoursDAO {

    /**
     * Récupère tous les cours
     */
    public static List<Cours> findAll() {
        List<Cours> cours = new ArrayList<>();
        String sql = "SELECT * FROM cours";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                cours.add(extraireCours(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cours;
    }

    /**
     * Récupère un cours par son ID
     */
    public static Cours findById(int id) {
        String sql = "SELECT * FROM cours WHERE id_cours = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return extraireCours(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Récupère tous les cours d'un enseignant
     */
    public static List<Cours> findByEnseignant(int enseignantId) {
        List<Cours> cours = new ArrayList<>();
        String sql = "SELECT * FROM cours WHERE id_enseignant = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, enseignantId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                cours.add(extraireCours(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cours;
    }

        /**
     * Récupère tous les cours associés à une classe
     */
    public static List<Cours> findByClasse(int classeId) {
        List<Cours> cours = new ArrayList<>();
        String sql = "SELECT c.* FROM cours c " +
                    "JOIN classe_cours cc ON c.id_cours = cc.id_cours " +
                    "WHERE cc.id_classe = ?";
        
        try (Connection connection = DatabaseConnection.getConnection();
            PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, classeId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                cours.add(extraireCours(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cours;
    }

    /**
     * Ajoute un nouveau cours
     */
    public static boolean save(Cours cours) {
        String sql = "INSERT INTO cours (nom_cours, volume_horaire, id_enseignant) VALUES (?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, cours.getIntitule());
            pstmt.setInt(2, cours.getVolumeHoraire());
            pstmt.setInt(3, cours.getEnseignantId());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        cours.setId(rs.getInt(1));
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
     * Met à jour un cours existant
     */
    public static boolean update(Cours cours) {
        String sql = "UPDATE cours SET nom_cours = ?, volume_horaire = ?, id_enseignant = ? WHERE id_cours = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, cours.getIntitule());
            pstmt.setInt(2, cours.getVolumeHoraire());
            pstmt.setInt(3, cours.getEnseignantId());
            pstmt.setInt(4, cours.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Supprime un cours
     */
    public static boolean delete(int id) {
        String sql = "DELETE FROM cours WHERE id_cours = ?";
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
     * Méthode utilitaire pour extraire un cours à partir d'un ResultSet
     */
    private static Cours extraireCours(ResultSet rs) throws SQLException {
        return new Cours(
            rs.getInt("id_cours"),
            rs.getString("nom_cours"),
            rs.getInt("volume_horaire"),
            rs.getInt("id_enseignant")
        );
    }
}