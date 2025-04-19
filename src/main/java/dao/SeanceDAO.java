package dao;

import models.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SeanceDAO {
    
    /**
     * Crée une nouvelle séance
     */
    public static boolean save(Seance seance) {
        String sql = "INSERT INTO seance (id_cours, contenu, date_seance, duree, estValidee) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, seance.getCoursId());
            pstmt.setString(2, seance.getContenu());
            pstmt.setDate(3, Date.valueOf(seance.getDateSeance()));
            pstmt.setInt(4, seance.getDuree());
            pstmt.setBoolean(5, seance.isEstValidee());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        seance.setId(rs.getInt(1));
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
     * Récupère une séance par son ID
     */
    public static Seance findById(int id) {
        String sql = "SELECT * FROM seance WHERE id_seance = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return extraireSeance(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Récupère toutes les séances pour un cours
     */
    public static List<Seance> findByCours(int coursId) {
        List<Seance> seances = new ArrayList<>();
        String sql = "SELECT * FROM seance WHERE id_cours = ? ORDER BY date_seance";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, coursId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                seances.add(extraireSeance(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return seances;
    }
    
    /**
     * Récupère les séances pour une période donnée
     */
    public static List<Seance> trouverParPeriode(int coursId, LocalDate debut, LocalDate fin) {
        List<Seance> seances = new ArrayList<>();
        String sql = "SELECT * FROM seance WHERE id_cours = ? AND date_seance BETWEEN ? AND ? ORDER BY date_seance";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, coursId);
            pstmt.setDate(2, Date.valueOf(debut));
            pstmt.setDate(3, Date.valueOf(fin));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                seances.add(extraireSeance(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return seances;
    }
    
    /**
     * Met à jour une séance
     */
    public static boolean update(Seance seance) {
        String sql = "UPDATE seance SET id_cours = ?, contenu = ?, date_seance = ?, duree = ?, estValidee = ? WHERE id_seance = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, seance.getCoursId());
            pstmt.setString(2, seance.getContenu());
            pstmt.setDate(3, Date.valueOf(seance.getDateSeance()));
            pstmt.setInt(4, seance.getDuree());
            pstmt.setBoolean(5, seance.isEstValidee());
            pstmt.setInt(6, seance.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Valide une séance
     */
    public static boolean valider(int id) {
        String sql = "UPDATE seance SET estValidee = TRUE WHERE id_seance = ?";
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
     * Supprime une séance
     */
    public static boolean delete(int id) {
        String sql = "DELETE FROM seance WHERE id_seance = ?";
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
     * Méthode utilitaire pour extraire une séance d'un ResultSet
     */
    private static Seance extraireSeance(ResultSet rs) throws SQLException {
        return new Seance(
            rs.getInt("id_seance"),
            rs.getInt("id_cours"),
            rs.getString("contenu"),
            rs.getDate("date_seance").toLocalDate(),
            rs.getInt("duree"),
            rs.getBoolean("estValidee")
        );
    }
}