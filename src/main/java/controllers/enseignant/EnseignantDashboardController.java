package controllers.enseignant;

import dao.CoursDAO;
import dao.SeanceDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import models.Cours;
import models.Enseignant;
import models.Seance;
import models.Utilisateur;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class EnseignantDashboardController implements Initializable {

    @FXML private Label pageTitle;
    @FXML private Label userNameLabel;
    @FXML private Label coursCountLabel;
    @FXML private Label seancesCountLabel;
    @FXML private Label validatedSeancesLabel;
    @FXML private Label savedSeancesLabel;
    @FXML private BorderPane contentArea;
    @FXML private StackPane dashboardView;
    
    @FXML private Button dashboardBtn;
    @FXML private Button coursBtn;
    @FXML private Button seancesBtn;
    @FXML private Button logoutBtn;
    
    private Utilisateur utilisateur;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Cette méthode est appelée après que tous les éléments FXML sont chargés
    }
    
    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
        
        // Mettre à jour le nom de l'utilisateur dans l'interface
        if (utilisateur != null) {
            userNameLabel.setText(utilisateur.getPrenom() + " " + utilisateur.getNom());
        }
        
        // Charger les données du tableau de bord
        loadDashboardData();
    }
    
    private void loadDashboardData() {
        try {
            if (utilisateur == null) return;
            
            // Nombre de cours assignés à l'enseignant
            List<Cours> cours = CoursDAO.findByEnseignant(utilisateur.getId());
            coursCountLabel.setText(String.valueOf(cours.size()));
            
            // Nombre total de séances et séances validées
            int totalSeances = 0;
            int validatedSeances = 0;
            
            for (Cours c : cours) {
                List<Seance> seances = SeanceDAO.findByCours(c.getId());
                totalSeances += seances.size();
                
                // Compter les séances validées
                validatedSeances += seances.stream()
                    .filter(Seance::isEstValidee)
                    .count();
            }
            
            // Mettre à jour le nombre de séances total
            seancesCountLabel.setText(String.valueOf(totalSeances));
            
            // Afficher le nombre de séances validées en gras
            validatedSeancesLabel.setText(String.valueOf(validatedSeances));
            
            // Afficher "sur X séances" en dessous
            savedSeancesLabel.setText("sur " + totalSeances + " séances");
            
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des données du dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleDashboardAction(ActionEvent event) {
        activateButton(dashboardBtn);
        pageTitle.setText("Tableau de Bord");
        showDashboardView();
    }
    
    @FXML
    private void handleCoursAction(ActionEvent event) {
        activateButton(coursBtn);
        pageTitle.setText("Mes Cours");
        loadView("/fxml/enseignant/MesCours.fxml");
    }
    
    @FXML
    private void handleSeancesAction(ActionEvent event) {
        activateButton(seancesBtn);
        pageTitle.setText("Mes Séances");
        loadView("/fxml/enseignant/MesSeances.fxml");
    }
    
    @FXML
    private void handleAddSeanceAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/enseignant/AjoutSeance.fxml"));
            Parent root = loader.load();
            
            AjoutSeanceController controller = loader.getController();
            controller.setEnseignant((Enseignant) utilisateur);
            
            Stage stage = new Stage();
            stage.setTitle("Ajouter une séance");
            Scene scene = new Scene(root);
            scene.getStylesheets().add("/css/common.css");
            scene.getStylesheets().add("/css/enseignant.css");
            stage.setScene(scene);
            stage.show();
            
            // Rafraîchir les données après la fermeture
            stage.setOnHidden(e -> loadDashboardData());
            
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la vue Ajout Séance: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleViewCoursAction(ActionEvent event) {
        activateButton(coursBtn);
        pageTitle.setText("Mes Cours");
        loadView("/fxml/enseignant/MesCours.fxml");
    }
    
    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            // Charger la vue de connexion
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login/LoginView.fxml"));
            Parent root = loader.load();
            
            // Créer une nouvelle scène avec la vue de connexion
            Scene scene = new Scene(root);
            scene.getStylesheets().add("/css/common.css");
            scene.getStylesheets().add("/css/login.css");
            
            // Récupérer la fenêtre actuelle et y charger la nouvelle scène
            Stage stage = (Stage) logoutBtn.getScene().getWindow();
            stage.setTitle("Connexion");
            stage.setScene(scene);
            stage.show();
            
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la vue de connexion: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void activateButton(Button button) {
        // Désactiver tous les boutons
        dashboardBtn.getStyleClass().remove("menu-button-active");
        coursBtn.getStyleClass().remove("menu-button-active");
        seancesBtn.getStyleClass().remove("menu-button-active");
        
        dashboardBtn.getStyleClass().add("menu-button");
        coursBtn.getStyleClass().add("menu-button");
        seancesBtn.getStyleClass().add("menu-button");
        
        // Activer le bouton cliqué
        button.getStyleClass().remove("menu-button");
        button.getStyleClass().add("menu-button-active");
    }
    
    private void showDashboardView() {
        contentArea.setCenter(dashboardView);
        loadDashboardData(); // Actualiser les données
    }
    
    private void loadView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent view = loader.load();
            
            // Passer l'utilisateur au contrôleur si nécessaire
            Object controller = loader.getController();
            try {
                if (controller != null && controller.getClass().getMethod("setEnseignant", Enseignant.class) != null) {
                    controller.getClass().getMethod("setEnseignant", Enseignant.class)
                            .invoke(controller, utilisateur);
                }
            } catch (Exception e) {
                // Pas toutes les vues auront cette méthode, donc on ignore cette exception
                System.out.println("Le contrôleur n'a pas de méthode setEnseignant");
            }
            
            contentArea.setCenter(view);
            
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la vue " + fxmlFile + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}