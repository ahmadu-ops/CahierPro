package dao;

import models.*;
import java.time.LocalDate;
import java.util.List;

public class TestDAO {

    public static void main(String[] args) {
        // Tester la connexion à la base de données
        testConnexion();
        
        // Créer des données de test
        creerDonneesDeTest();
        
        // Tester les DAO
        testUtilisateurDAO();
        testClasseDAO();
        testCoursDAO();
        testSeanceDAO();
        testFichePedagogiqueDAO();
        
        // Tester la génération de PDF
        testGenerationPDF();
    }
    
    private static void testConnexion() {
        System.out.println("=== Test de connexion à la base de données ===");
        try {
            DatabaseConnection.getConnection();
            System.out.println("Connexion réussie !");
        } catch (Exception e) {
            System.err.println("Erreur de connexion : " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }
    
    private static void creerDonneesDeTest() {
        System.out.println("=== Création des données de test ===");
        
        // Créer des utilisateurs si nécessaire
        if (UtilisateurDAO.findAll().isEmpty()) {
            System.out.println("Création des utilisateurs...");
            
            // Chef de département
            ChefDepartement chef = new ChefDepartement(0, "Dupont", "Jean", "chef@example.com", "password");
            UtilisateurDAO.save(chef);
            
            // Enseignants
            Enseignant ens1 = new Enseignant(0, "Martin", "Sophie", "sophie@example.com", "password");
            Enseignant ens2 = new Enseignant(0, "Leroy", "Paul", "paul@example.com", "password");
            UtilisateurDAO.save(ens1);
            UtilisateurDAO.save(ens2);
            
            // Responsables de classe
            Responsable resp1 = new Responsable(0, "Petit", "Marie", "marie@example.com", "password", 0);
            Responsable resp2 = new Responsable(0, "Bernard", "Thomas", "thomas@example.com", "password", 0);
            UtilisateurDAO.save(resp1);
            UtilisateurDAO.save(resp2);
            
            System.out.println("Utilisateurs créés avec succès.");
        }
        
        // Récupérer les IDs des utilisateurs
        List<Utilisateur> enseignants = UtilisateurDAO.findByRole("enseignant");
        List<Utilisateur> responsables = UtilisateurDAO.findByRole("responsable_classe");
        
        if (enseignants.isEmpty() || responsables.isEmpty()) {
            System.err.println("Erreur : Aucun enseignant ou responsable trouvé.");
            return;
        }
        
        // Créer des classes si nécessaire
        if (ClasseDAO.findAll().isEmpty()) {
            System.out.println("Création des classes...");
            
            Classe classe1 = new Classe(0, "Licence Informatique 3", ((Responsable) responsables.get(0)).getId());
            Classe classe2 = new Classe(0, "Master Informatique 1", ((Responsable) responsables.get(1)).getId());
            ClasseDAO.save(classe1);
            ClasseDAO.save(classe2);
            
            // Mettre à jour les responsables avec les IDs de classe
            ((Responsable) responsables.get(0)).setClasseId(classe1.getId());
            ((Responsable) responsables.get(1)).setClasseId(classe2.getId());
            UtilisateurDAO.update(responsables.get(0));
            UtilisateurDAO.update(responsables.get(1));
            
            System.out.println("Classes créées avec succès.");
        }
        
        // Créer des cours si nécessaire
        if (CoursDAO.findAll().isEmpty()) {
            System.out.println("Création des cours...");
            
            Cours cours1 = new Cours(0, "Programmation Java", 60, enseignants.get(0).getId());
            Cours cours2 = new Cours(0, "Bases de données", 45, enseignants.get(1).getId());
            Cours cours3 = new Cours(0, "Algorithmes avancés", 30, enseignants.get(0).getId());
            CoursDAO.save(cours1);
            CoursDAO.save(cours2);
            CoursDAO.save(cours3);
            
            // Associer les cours aux classes
            List<Classe> classes = ClasseDAO.findAll();
            if (!classes.isEmpty()) {
                ClasseDAO.associerCours(classes.get(0).getId(), cours1.getId());
                ClasseDAO.associerCours(classes.get(0).getId(), cours2.getId());
                ClasseDAO.associerCours(classes.get(1).getId(), cours3.getId());
            }
            
            System.out.println("Cours créés avec succès.");
        }
        
        // Créer des séances si nécessaire
        List<Cours> cours = CoursDAO.findAll();
        if (!cours.isEmpty() && SeanceDAO.findByCours(cours.get(0).getId()).isEmpty()) {
            System.out.println("Création des séances...");
            
            // Séances pour le premier cours
            Seance seance1 = new Seance(0, cours.get(0).getId(), "Introduction à Java", LocalDate.now().minusDays(15), 120, true);
            Seance seance2 = new Seance(0, cours.get(0).getId(), "Programmation orientée objet", LocalDate.now().minusDays(8), 120, true);
            Seance seance3 = new Seance(0, cours.get(0).getId(), "Collections et génériques", LocalDate.now().minusDays(1), 120, false);
            SeanceDAO.save(seance1);
            SeanceDAO.save(seance2);
            SeanceDAO.save(seance3);
            
            // Séances pour le deuxième cours
            Seance seance4 = new Seance(0, cours.get(1).getId(), "Modèle relationnel", LocalDate.now().minusDays(14), 90, true);
            Seance seance5 = new Seance(0, cours.get(1).getId(), "SQL avancé", LocalDate.now().minusDays(7), 90, false);
            SeanceDAO.save(seance4);
            SeanceDAO.save(seance5);
            
            System.out.println("Séances créées avec succès.");
        }
        
        System.out.println();
    }
    
    private static void testUtilisateurDAO() {
        System.out.println("=== Test de UtilisateurDAO ===");
        
        // Tester la récupération de tous les utilisateurs
        List<Utilisateur> utilisateurs = UtilisateurDAO.findAll();
        System.out.println("Nombre d'utilisateurs : " + utilisateurs.size());
        
        // Tester la connexion
        if (!utilisateurs.isEmpty()) {
            Utilisateur u = utilisateurs.get(0);
            Utilisateur logged = UtilisateurDAO.verifierConnexion(u.getEmail(), u.getMotDePasse());
            System.out.println("Connexion avec " + u.getEmail() + " : " + (logged != null ? "réussie" : "échouée"));
        }
        
        System.out.println();
    }
    
    private static void testClasseDAO() {
        System.out.println("=== Test de ClasseDAO ===");
        
        // Tester la récupération de toutes les classes
        List<Classe> classes = ClasseDAO.findAll();
        System.out.println("Nombre de classes : " + classes.size());
        
        // Afficher les détails des classes
        for (Classe classe : classes) {
            System.out.println("Classe : " + classe.getNom() + ", Responsable ID : " + classe.getResponsableId());
            
            // Tester la récupération des cours associés
            List<Cours> cours = CoursDAO.findByClasse(classe.getId());
            System.out.println("  - Nombre de cours associés : " + cours.size());
            for (Cours c : cours) {
                System.out.println("    * " + c.getIntitule());
            }
        }
        
        System.out.println();
    }
    
    private static void testCoursDAO() {
        System.out.println("=== Test de CoursDAO ===");
        
        // Tester la récupération de tous les cours
        List<Cours> cours = CoursDAO.findAll();
        System.out.println("Nombre de cours : " + cours.size());
        
        // Tester la récupération des cours par enseignant
        List<Utilisateur> enseignants = UtilisateurDAO.findByRole("enseignant");
        if (!enseignants.isEmpty()) {
            int enseignantId = enseignants.get(0).getId();
            List<Cours> coursEnseignant = CoursDAO.findByEnseignant(enseignantId);
            System.out.println("Nombre de cours pour l'enseignant #" + enseignantId + " : " + coursEnseignant.size());
        }
        
        System.out.println();
    }
    
    private static void testSeanceDAO() {
        System.out.println("=== Test de SeanceDAO ===");
        
        // Tester la récupération des séances par cours
        List<Cours> cours = CoursDAO.findAll();
        if (!cours.isEmpty()) {
            int coursId = cours.get(0).getId();
            List<Seance> seances = SeanceDAO.findByCours(coursId);
            System.out.println("Nombre de séances pour le cours #" + coursId + " : " + seances.size());
            
            // Afficher les détails des séances
            for (Seance seance : seances) {
                System.out.println("Séance du " + seance.getDateSeance() + " : " + seance.getContenu() + 
                                  " (validée : " + seance.isEstValidee() + ")");
            }
            
            // Tester la validation d'une séance
            if (!seances.isEmpty() && !seances.get(seances.size() - 1).isEstValidee()) {
                int seanceId = seances.get(seances.size() - 1).getId();
                boolean valide = SeanceDAO.valider(seanceId);
                System.out.println("Validation de la séance #" + seanceId + " : " + (valide ? "réussie" : "échouée"));
            }
        }
        
        System.out.println();
    }
    
    private static void testFichePedagogiqueDAO() {
        System.out.println("=== Test de FichePedagogiqueDAO ===");
        
        // Tester le calcul du pourcentage de réalisation
        List<Cours> cours = CoursDAO.findAll();
        if (!cours.isEmpty()) {
            int coursId = cours.get(0).getId();
            double pourcentage = FichePedagogiqueDAO.calculerPourcentageRealisation(coursId);
            System.out.println("Pourcentage de réalisation du cours #" + coursId + " : " + pourcentage + "%");
            
            // Tester la génération d'une fiche
            FichePedagogique fiche = FichePedagogiqueDAO.genererFiche(coursId, "Test de génération de fiche");
            if (fiche != null) {
                System.out.println("Fiche générée avec succès. ID : " + fiche.getId());
                
                // Tester la récupération d'une fiche
                FichePedagogique ficheLue = FichePedagogiqueDAO.findById(fiche.getId());
                if (ficheLue != null) {
                    System.out.println("Fiche lue : " + ficheLue);
                    System.out.println("Nombre de séances : " + (ficheLue.getSeances() != null ? ficheLue.getSeances().size() : 0));
                }
                
                // Tester la récupération des infos complètes
                FichePedagogiqueDAO.FicheInfosComplete infos = FichePedagogiqueDAO.getInfosComplete(fiche.getId());
                if (infos != null) {
                    System.out.println("Infos complètes récupérées :");
                    System.out.println("  - Cours : " + infos.getCours().getIntitule());
                    System.out.println("  - Enseignant : " + infos.getEnseignant().getPrenom() + " " + infos.getEnseignant().getNom());
                    System.out.println("  - Nombre de séances : " + infos.getSeances().size());
                }
            } else {
                System.err.println("Erreur lors de la génération de la fiche.");
            }
        }
        
        System.out.println();
    }
    
    private static void testGenerationPDF() {
        System.out.println("=== Test de génération de PDF ===");
        
        // Récupérer une fiche existante
        List<Cours> cours = CoursDAO.findAll();
        if (!cours.isEmpty()) {
            int coursId = cours.get(0).getId();
            List<FichePedagogique> fiches = FichePedagogiqueDAO.findByCours(coursId);
            
            if (fiches.isEmpty()) {
                System.out.println("Aucune fiche existante, génération d'une nouvelle fiche...");
                FichePedagogique fiche = FichePedagogiqueDAO.genererFiche(coursId, "Test de génération de PDF");
                if (fiche != null) {
                    fiches.add(fiche);
                }
            }
            
            if (!fiches.isEmpty()) {
                int ficheId = fiches.get(0).getId();
                String outputPath = "fiche_" + ficheId + ".pdf";
                
                System.out.println("Tentative de génération du PDF pour la fiche #" + ficheId + " dans " + outputPath);
                try {
                    boolean success = views.utils.PDFGenerator.genererPDF(ficheId, outputPath);
                    System.out.println("Génération du PDF : " + (success ? "réussie" : "échouée"));
                } catch (Exception e) {
                    System.err.println("Erreur lors de la génération du PDF : " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.err.println("Aucune fiche disponible pour générer un PDF.");
            }
        }
        
        System.out.println();
    }
}