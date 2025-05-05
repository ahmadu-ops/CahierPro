package controllers.chef;

import dao.CoursDAO;
import dao.FichePedagogiqueDAO;
import dao.UtilisateurDAO;
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
import models.*;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ChefDashboardController implements Initializable {

    @FXML private Label pageTitle;
    @FXML private Label userNameLabel;
    @FXML private Label usersCountLabel;
    @FXML private Label coursesCountLabel;
    @FXML private Label fichesCountLabel;
    @FXML private BorderPane contentArea;
    @FXML private StackPane dashboardView;
    
    @FXML private Button dashboardBtn;
    @FXML private Button usersBtn;
    @FXML private Button coursesBtn;
    @FXML private Button fichesBtn;
    @FXML private Button logoutBtn;
    
    private Utilisateur utilisateur;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Cette méthode est appelée après que tous les éléments FXML sont chargés
        // Initialiser avec les données par défaut
        loadDashboardData();
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
        // Charger les statistiques pour le tableau de bord
        try {
            // Nombre d'utilisateurs (enseignants et responsables)
            int enseignantsCount = UtilisateurDAO.findByRole("enseignant").size();
            int responsablesCount = UtilisateurDAO.findByRole("responsable_classe").size();
            usersCountLabel.setText(String.valueOf(enseignantsCount + responsablesCount));
            
            // Nombre de cours
            int coursCount = CoursDAO.findAll().size();
            coursesCountLabel.setText(String.valueOf(coursCount));
            
            // Nombre de fiches pédagogiques
            List<FichePedagogique> fiches = FichePedagogiqueDAO.findAll();
            fichesCountLabel.setText(String.valueOf(fiches.size()));
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
    private void handleUsersAction(ActionEvent event) {
        activateButton(usersBtn);
        pageTitle.setText("Gestion des Utilisateurs");
        
        loadView("/fxml/chef/GestionUtilisateurs.fxml");
    }
    
    @FXML
    private void handleCoursesAction(ActionEvent event) {
        activateButton(coursesBtn);
        pageTitle.setText("Gestion des Cours");
        loadView("/fxml/chef/GestionCours.fxml");
    }
    
    @FXML
    private void handleFichesAction(ActionEvent event) {
        activateButton(fichesBtn);
        pageTitle.setText("Fiches Pédagogiques");
        loadView("/fxml/chef/GestionFiches.fxml");
    }
    
    @FXML
    private void handleNewUserAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/chef/AjoutUtilisateur.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Ajouter un utilisateur");
            Scene scene = new Scene(root);
            scene.getStylesheets().add("/css/common.css");
            scene.getStylesheets().add("/css/chef.css");
            stage.setScene(scene);
            stage.show();
            
            // Rafraîchir les données après la fermeture
            stage.setOnHidden(e -> loadDashboardData());
            
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la vue Ajout Utilisateur: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleNewCourseAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/chef/AjoutCours.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Ajouter un cours");
            Scene scene = new Scene(root);
            scene.getStylesheets().add("/css/common.css");
            scene.getStylesheets().add("/css/chef.css");
            stage.setScene(scene);
            stage.show();
            
            // Rafraîchir les données après la fermeture
            stage.setOnHidden(e -> loadDashboardData());
            
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la vue Ajout Cours: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleGenerateFicheAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/chef/GenerationFiche.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Générer une Fiche Pédagogique");
            Scene scene = new Scene(root);
            scene.getStylesheets().add("/css/common.css");
            scene.getStylesheets().add("/css/chef.css");
            stage.setScene(scene);
            stage.show();
            
            // Rafraîchir les données après la fermeture
            stage.setOnHidden(e -> loadDashboardData());
            
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la vue Génération Fiche: " + e.getMessage());
            e.printStackTrace();
        }
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
        usersBtn.getStyleClass().remove("menu-button-active");
        coursesBtn.getStyleClass().remove("menu-button-active");
        fichesBtn.getStyleClass().remove("menu-button-active");
        
        dashboardBtn.getStyleClass().add("menu-button");
        usersBtn.getStyleClass().add("menu-button");
        coursesBtn.getStyleClass().add("menu-button");
        fichesBtn.getStyleClass().add("menu-button");
        
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
                if (controller != null && controller.getClass().getMethod("setUtilisateur", Utilisateur.class) != null) {
                    controller.getClass().getMethod("setUtilisateur", Utilisateur.class)
                            .invoke(controller, utilisateur);
                }
            } catch (Exception e) {
                // Pas toutes les vues auront cette méthode, donc on ignore cette exception
                System.out.println("Le contrôleur " + controller.getClass().getName() + " n'a pas de méthode setUtilisateur");
            }
            
            contentArea.setCenter(view);
            
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la vue " + fxmlFile + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}