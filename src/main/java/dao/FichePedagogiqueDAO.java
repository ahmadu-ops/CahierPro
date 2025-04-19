package dao;

import models.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FichePedagogiqueDAO {
    
    /**
     * Crée une nouvelle fiche pédagogique
     */
    public static boolean save(FichePedagogique fiche) {
        String sql = "INSERT INTO fiche_pedagogique (id_cours, date_generation, pourcentage_realisation, commentaire) VALUES (?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, fiche.getCoursId());
            pstmt.setDate(2, Date.valueOf(fiche.getDateGeneration()));
            pstmt.setDouble(3, fiche.getPourcentageRealisation());
            pstmt.setString(4, fiche.getCommentaire());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        fiche.setId(rs.getInt(1));
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
     * Récupère une fiche pédagogique par son ID
     */
    public static FichePedagogique findById(int id) {
        String sql = "SELECT * FROM fiche_pedagogique WHERE id_fiche = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                FichePedagogique fiche = extraireFiche(rs);
                // Charger les séances associées à cette fiche
                chargerSeances(fiche);
                return fiche;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Récupère toutes les fiches pédagogiques pour un cours
     */
    public static List<FichePedagogique> findByCours(int coursId) {
        List<FichePedagogique> fiches = new ArrayList<>();
        String sql = "SELECT * FROM fiche_pedagogique WHERE id_cours = ? ORDER BY date_generation DESC";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, coursId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                FichePedagogique fiche = extraireFiche(rs);
                // Charger les séances associées à cette fiche
                chargerSeances(fiche);
                fiches.add(fiche);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return fiches;
    }

    /**
 * Récupère toutes les fiches pédagogiques
 */
    public static List<FichePedagogique> findAll() {
        List<FichePedagogique> fiches = new ArrayList<>();
        String sql = "SELECT * FROM fiche_pedagogique ORDER BY date_generation DESC";
        try (Connection connection = DatabaseConnection.getConnection();
            PreparedStatement pstmt = connection.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                FichePedagogique fiche = extraireFiche(rs);
                fiches.add(fiche);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return fiches;
    }
    
    /**
     * Met à jour une fiche pédagogique
     */
    public static boolean update(FichePedagogique fiche) {
        String sql = "UPDATE fiche_pedagogique SET id_cours = ?, date_generation = ?, pourcentage_realisation = ?, commentaire = ? WHERE id_fiche = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, fiche.getCoursId());
            pstmt.setDate(2, Date.valueOf(fiche.getDateGeneration()));
            pstmt.setDouble(3, fiche.getPourcentageRealisation());
            pstmt.setString(4, fiche.getCommentaire());
            pstmt.setInt(5, fiche.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Supprime une fiche pédagogique
     */
    public static boolean delete(int id) {
        String sql = "DELETE FROM fiche_pedagogique WHERE id_fiche = ?";
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
     * Calcule le pourcentage de réalisation d'un cours
     */
    /**
 * Calcule le pourcentage de réalisation d'un cours
 */
    public static double calculerPourcentageRealisation(int coursId) {
        // Récupérer le volume horaire total du cours
        int volumeHoraireTotal = 0;
        String sqlCours = "SELECT volume_horaire FROM cours WHERE id_cours = ?";
        try (Connection connection = DatabaseConnection.getConnection();
            PreparedStatement pstmt = connection.prepareStatement(sqlCours)) {
            pstmt.setInt(1, coursId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                volumeHoraireTotal = rs.getInt("volume_horaire");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return 0.0;
        }
        
        // Si le cours n'a pas de volume horaire, retourner 0
        if (volumeHoraireTotal <= 0) {
            return 0.0;
        }
        
        // Récupérer la somme des durées des séances validées
        int totalMinutesValidees = 0;
        String sqlSeances = "SELECT SUM(duree) as total_minutes FROM seance WHERE id_cours = ? AND estValidee = TRUE";
        try (Connection connection = DatabaseConnection.getConnection();
            PreparedStatement pstmt = connection.prepareStatement(sqlSeances)) {
            pstmt.setInt(1, coursId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getObject("total_minutes") != null) {
                totalMinutesValidees = rs.getInt("total_minutes");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return 0.0;
        }
        
        // Convertir les minutes en heures (puisque le volume horaire est en heures)
        double totalHeuresValidees = totalMinutesValidees / 60.0;
        
        // Calculer le pourcentage (ne pas dépasser 100%)
        double pourcentage = (totalHeuresValidees / volumeHoraireTotal) * 100;
        return Math.min(pourcentage, 100.0); // S'assurer que le pourcentage ne dépasse pas 100%
    }
    
    /**
     * Génère une fiche pédagogique pour un cours
     */
    public static FichePedagogique genererFiche(int coursId, String commentaire) {
        // Calculer le pourcentage de réalisation
        double pourcentage = calculerPourcentageRealisation(coursId);
        
        // Créer une nouvelle fiche
        FichePedagogique fiche = new FichePedagogique(
            0, // ID sera généré par la base de données
            coursId,
            LocalDate.now(),
            pourcentage,
            commentaire
        );
        
        // Sauvegarder la fiche en base de données
        if (save(fiche)) {
            // Charger les séances pour cette fiche
            List<Seance> seances = SeanceDAO.findByCours(coursId);
            fiche.setSeances(seances);
            return fiche;
        }
        
        return null;
    }
    
    /**
     * Méthode utilitaire pour extraire une fiche d'un ResultSet
     */
    private static FichePedagogique extraireFiche(ResultSet rs) throws SQLException {
        return new FichePedagogique(
            rs.getInt("id_fiche"),
            rs.getInt("id_cours"),
            rs.getDate("date_generation").toLocalDate(),
            rs.getDouble("pourcentage_realisation"),
            rs.getString("commentaire")
        );
    }
    
    /**
     * Charge les séances associées à une fiche pédagogique
     */
    private static void chargerSeances(FichePedagogique fiche) {
        List<Seance> seances = SeanceDAO.findByCours(fiche.getCoursId());
        fiche.setSeances(seances);
    }
    
    /**
     * Récupère toutes les informations nécessaires pour générer un PDF de la fiche
     */
    public static FicheInfosComplete getInfosComplete(int ficheId) {
        FichePedagogique fiche = findById(ficheId);
        if (fiche == null) {
            return null;
        }
        
        // Récupérer les informations du cours
        Cours cours = CoursDAO.findById(fiche.getCoursId());
        if (cours == null) {
            return null;
        }
        
        // Récupérer les informations de l'enseignant
        Enseignant enseignant = (Enseignant) UtilisateurDAO.findById(cours.getEnseignantId());
        if (enseignant == null) {
            return null;
        }
        
        // Récupérer les séances du cours
        List<Seance> seances = fiche.getSeances();
        if (seances == null) {
            seances = SeanceDAO.findByCours(cours.getId());
        }
        
        // Créer un objet contenant toutes les informations
        return new FicheInfosComplete(fiche, cours, enseignant, seances);
    }
    
    /**
     * Classe interne pour regrouper toutes les informations nécessaires à la génération d'un PDF
     */
    public static class FicheInfosComplete {
        private FichePedagogique fiche;
        private Cours cours;
        private Enseignant enseignant;
        private List<Seance> seances;
        
        public FicheInfosComplete(FichePedagogique fiche, Cours cours, Enseignant enseignant, List<Seance> seances) {
            this.fiche = fiche;
            this.cours = cours;
            this.enseignant = enseignant;
            this.seances = seances;
        }
        
        public FichePedagogique getFiche() {
            return fiche;
        }
        
        public Cours getCours() {
            return cours;
        }
        
        public Enseignant getEnseignant() {
            return enseignant;
        }
        
        public List<Seance> getSeances() {
            return seances;
        }
    }
}