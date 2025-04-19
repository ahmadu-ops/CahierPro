package controllers.responsable;

import dao.ClasseDAO;
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
import models.Classe;
import models.Cours;
import models.Responsable;
import models.Seance;
import models.Utilisateur;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ResponsableDashboardController implements Initializable {

    @FXML private Label pageTitle;
    @FXML private Label userNameLabel;
    @FXML private Label classeLabel;
    @FXML private Label coursCountLabel;
    @FXML private Label seancesAttentesLabel;
    @FXML private BorderPane contentArea;
    @FXML private StackPane dashboardView;
    
    @FXML private Button dashboardBtn;
    @FXML private Button cahierTexteBtn;
    @FXML private Button seancesValiderBtn;
    @FXML private Button logoutBtn;
    
    private Responsable responsable;
    private Classe classe;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Cette méthode est appelée après que tous les éléments FXML sont chargés
    }
    
    public void setUtilisateur(Utilisateur utilisateur) {
        if (utilisateur instanceof Responsable) {
            this.responsable = (Responsable) utilisateur;
            
            // Mettre à jour le nom de l'utilisateur dans l'interface
            userNameLabel.setText(responsable.getPrenom() + " " + responsable.getNom());
            
            // Charger les informations de la classe dont l'utilisateur est responsable
            loadClasseInfo();
            
            // Charger les données du tableau de bord
            loadDashboardData();
        }
    }
    
    private void loadClasseInfo() {
        try {
            if (responsable == null) return;
            
            // Récupérer la classe dont l'utilisateur est responsable
            classe = ClasseDAO.findById(responsable.getClasseId());
            
            if (classe != null) {
                classeLabel.setText(classe.getNom());
            } else {
                classeLabel.setText("Non assignée");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des informations de la classe: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadDashboardData() {
        try {
            if (classe == null) return;
            
            // Nombre de cours associés à la classe
            List<Cours> cours = CoursDAO.findByClasse(classe.getId());
            coursCountLabel.setText(String.valueOf(cours.size()));
            
            // Nombre de séances en attente de validation
            int seancesEnAttente = 0;
            
            for (Cours c : cours) {
                List<Seance> seances = SeanceDAO.findByCours(c.getId());
                
                // Compter les séances non validées
                seancesEnAttente += seances.stream()
                    .filter(s -> !s.isEstValidee())
                    .count();
            }
            
            seancesAttentesLabel.setText(String.valueOf(seancesEnAttente));
            
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
    private void handleCahierTexteAction(ActionEvent event) {
        activateButton(cahierTexteBtn);
        pageTitle.setText("Cahier de Texte");
        loadView("/fxml/responsable/CahierTexte.fxml");
    }
    
    @FXML
    private void handleSeancesValiderAction(ActionEvent event) {
        activateButton(seancesValiderBtn);
        pageTitle.setText("Séances à Valider");
        loadView("/fxml/responsable/SeancesValider.fxml");
    }
    
    @FXML
    private void handleConsulterCahierTexteAction(ActionEvent event) {
        // Redirige vers la section "Cahier de Texte" du menu
        activateButton(cahierTexteBtn);
        pageTitle.setText("Cahier de Texte");
        loadView("/fxml/responsable/CahierTexte.fxml");
    }
    
    @FXML
    private void handleValiderSeancesAction(ActionEvent event) {
        // Redirige vers la section "Séances à Valider" du menu
        activateButton(seancesValiderBtn);
        pageTitle.setText("Séances à Valider");
        loadView("/fxml/responsable/SeancesValider.fxml");
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
        cahierTexteBtn.getStyleClass().remove("menu-button-active");
        seancesValiderBtn.getStyleClass().remove("menu-button-active");
        
        dashboardBtn.getStyleClass().add("menu-button");
        cahierTexteBtn.getStyleClass().add("menu-button");
        seancesValiderBtn.getStyleClass().add("menu-button");
        
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
            
            // Passer le responsable au contrôleur si nécessaire
            Object controller = loader.getController();
            try {
                if (controller != null && controller.getClass().getMethod("setResponsable", Responsable.class) != null) {
                    controller.getClass().getMethod("setResponsable", Responsable.class)
                            .invoke(controller, responsable);
                }
            } catch (Exception e) {
                // Pas toutes les vues auront cette méthode, donc on ignore cette exception
                System.out.println("Le contrôleur n'a pas de méthode setResponsable");
            }
            
            try {
                if (controller != null && controller.getClass().getMethod("setClasse", Classe.class) != null) {
                    controller.getClass().getMethod("setClasse", Classe.class)
                            .invoke(controller, classe);
                }
            } catch (Exception e) {
                // Pas toutes les vues auront cette méthode, donc on ignore cette exception
                System.out.println("Le contrôleur n'a pas de méthode setClasse");
            }
            
            contentArea.setCenter(view);
            
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la vue " + fxmlFile + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}