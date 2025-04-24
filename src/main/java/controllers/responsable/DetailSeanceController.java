package controllers.responsable;

import dao.ClasseDAO;
import dao.CoursDAO;
import dao.SeanceDAO;
import dao.UtilisateurDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import models.Classe;
import models.Cours;
import models.Seance;
import models.Utilisateur;

import java.net.URL;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class DetailSeanceController implements Initializable {

    @FXML private Label coursLabel;
    @FXML private Label enseignantLabel;
    @FXML private Label classeLabel;
    @FXML private Label dateLabel;
    @FXML private Label dureeLabel;
    @FXML private Label statutLabel;
    @FXML private TextArea contenuTextArea;
    @FXML private HBox buttonContainer;
    @FXML private Button closeButton;
    @FXML private Button validerButton;
    
    private Seance seance;
    private String mode = "detail"; // "detail" ou "validation"
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Par défaut, le bouton Valider n'est pas visible en mode "detail"
        validerButton.setVisible(false);
        validerButton.setManaged(false);
        
        // Ajuster l'espace pour que le bouton Fermer soit centré
        HBox.setHgrow(closeButton, Priority.ALWAYS);
        closeButton.setMaxWidth(Double.MAX_VALUE);
    }
    
    public void setSeance(Seance seance) {
        this.seance = seance;
        if (seance != null) {
            loadSeanceDetails();
        }
    }
    
    public void setMode(String mode) {
        this.mode = mode;
        
        if ("validation".equals(mode)) {
            // En mode validation, le bouton Valider est visible
            validerButton.setVisible(true);
            validerButton.setManaged(true);
            
            // Réinitialiser les contraintes pour avoir les deux boutons côte à côte
            HBox.setHgrow(closeButton, Priority.NEVER);
            closeButton.setMaxWidth(-1); // Utiliser -1 au lieu de Use.COMPUTED
        }
    }
    
    private void loadSeanceDetails() {
        try {
            // Récupérer les informations associées à la séance
            Cours cours = CoursDAO.findById(seance.getCoursId());
            
            if (cours != null) {
                coursLabel.setText(cours.getIntitule());
                
                // Récupérer l'enseignant
                Utilisateur enseignant = UtilisateurDAO.findById(cours.getEnseignantId());
                if (enseignant != null) {
                    enseignantLabel.setText(enseignant.getPrenom() + " " + enseignant.getNom());
                } else {
                    enseignantLabel.setText("Non assigné");
                }
                
                // Récupérer la classe
                Classe classe = ClasseDAO.findByCoursId(cours.getId());
                if (classe != null) {
                    classeLabel.setText(classe.getNom());
                } else {
                    classeLabel.setText("Non assignée");
                }
            } else {
                coursLabel.setText("Cours inconnu");
            }
            
            // Afficher les détails de la séance
            dateLabel.setText(seance.getDateSeance().format(dateFormatter));
            // Convertir la durée de minutes en heures
            int heures = seance.getDuree() / 60;  // Division entière
            dureeLabel.setText(heures + " heures");
            statutLabel.setText(seance.isEstValidee() ? "Validée" : "En attente");
            contenuTextArea.setText(seance.getContenu());
            
            // Appliquer un style au statut
            if (seance.isEstValidee()) {
                statutLabel.setStyle("-fx-text-fill: #0a6d4c; -fx-font-weight: bold;");
            } else {
                statutLabel.setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");
            }
            
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des détails de la séance: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleValider(ActionEvent event) {
        try {
            boolean success = SeanceDAO.valider(seance.getId());
            
            if (success) {
                // Mettre à jour l'affichage
                seance.setEstValidee(true);
                statutLabel.setText("Validée");
                statutLabel.setStyle("-fx-text-fill: #0a6d4c; -fx-font-weight: bold;");
                
                // Masquer le bouton Valider
                validerButton.setVisible(false);
                validerButton.setManaged(false);
                
                // Adapter le conteneur de boutons
                HBox.setHgrow(closeButton, Priority.ALWAYS);
                closeButton.setMaxWidth(Double.MAX_VALUE);
                
                // Afficher un message de confirmation
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Séance validée");
                alert.setHeaderText(null);
                alert.setContentText("La séance a été validée avec succès.");
                alert.showAndWait();
                
                // Fermer la fenêtre après validation
                Stage stage = (Stage) closeButton.getScene().getWindow();
                stage.close();
            } else {
                // Afficher un message d'erreur
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText(null);
                alert.setContentText("Impossible de valider la séance.");
                alert.showAndWait();
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la validation de la séance: " + e.getMessage());
            e.printStackTrace();
            
            // Afficher un message d'erreur
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Une erreur est survenue lors de la validation: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    @FXML
    private void handleClose(ActionEvent event) {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}