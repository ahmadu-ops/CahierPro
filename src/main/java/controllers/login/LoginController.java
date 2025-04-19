package controllers.login;

import dao.UtilisateurDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.ChefDepartement;
import models.Enseignant;
import models.Responsable;
import models.Utilisateur;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private Button loginButton;

    @FXML
    void handleLogin(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();

        // Vérifier si les champs sont vides
        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Veuillez remplir tous les champs");
            return;
        }

        // Vérifier les identifiants
        Utilisateur utilisateur = UtilisateurDAO.verifierConnexion(email, password);

        if (utilisateur == null) {
            errorLabel.setText("Email ou mot de passe incorrect");
            return;
        }

        try {
            // Déterminer la vue à afficher selon le rôle
            String fxmlFile;
            String windowTitle;
            String cssFile;

            if (utilisateur instanceof ChefDepartement) {
                fxmlFile = "/fxml/chef/ChefDashboard.fxml";
                windowTitle = "Tableau de bord - Chef de Département";
                cssFile = "/css/chef.css";
            } else if (utilisateur instanceof Enseignant) {
                fxmlFile = "/fxml/enseignant/EnseignantDashboard.fxml";
                windowTitle = "Tableau de bord - Enseignant";
                cssFile = "/css/enseignant.css";
            } else if (utilisateur instanceof Responsable) {
                fxmlFile = "/fxml/responsable/ResponsableDashboard.fxml";
                windowTitle = "Tableau de bord - Responsable de Classe";
                cssFile = "/css/responsable.css";
            } else {
                errorLabel.setText("Type d'utilisateur non reconnu");
                return;
            }

            // Charger la vue correspondante
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            
            // Récupérer le contrôleur et passer l'utilisateur connecté
            Object controller = loader.getController();
            
            // Utilisation de reflection pour appeler setUtilisateur sur n'importe quel contrôleur
            try {
                controller.getClass().getMethod("setUtilisateur", Utilisateur.class)
                          .invoke(controller, utilisateur);
            } catch (Exception e) {
                System.err.println("Erreur lors du passage de l'utilisateur au contrôleur: " + e.getMessage());
            }

            // Créer une nouvelle scène
            Scene scene = new Scene(root);
            scene.getStylesheets().add("/css/common.css");
            scene.getStylesheets().add(cssFile);
            
            // Récupérer la fenêtre actuelle et y charger la nouvelle scène
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setTitle(windowTitle);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            errorLabel.setText("Erreur lors du chargement de la vue: " + e.getMessage());
            e.printStackTrace();
        }
    }
}