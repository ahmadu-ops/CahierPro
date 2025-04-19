package controllers.chef;

import dao.ClasseDAO;
import dao.CoursDAO;
import dao.UtilisateurDAO;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Classe;
import models.Cours;
import models.Enseignant;
import models.Utilisateur;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AjoutCoursController implements Initializable {

    @FXML private TextField intituleField;
    @FXML private TextField volumeHoraireField;
    @FXML private ComboBox<Enseignant> enseignantComboBox;
    @FXML private ComboBox<Classe> classeComboBox;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Charger les enseignants
        loadEnseignants();
        
        // Charger les classes
        loadClasses();
        
        // Valider que le volume horaire est un nombre
        volumeHoraireField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                volumeHoraireField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }
    
    private void loadEnseignants() {
        try {
            List<Utilisateur> utilisateurs = UtilisateurDAO.findByRole("enseignant");
            List<Enseignant> enseignants = utilisateurs.stream()
                .filter(u -> u instanceof Enseignant)
                .map(u -> (Enseignant) u)
                .collect(Collectors.toList());
            
            enseignantComboBox.setItems(FXCollections.observableArrayList(enseignants));
            
            // Définir le convertisseur pour afficher le nom de l'enseignant
            enseignantComboBox.setConverter(new javafx.util.StringConverter<Enseignant>() {
                @Override
                public String toString(Enseignant enseignant) {
                    return enseignant == null ? "" : enseignant.getPrenom() + " " + enseignant.getNom();
                }
                
                @Override
                public Enseignant fromString(String string) {
                    return null;
                }
            });
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les enseignants", e.getMessage());
        }
    }
    
    private void loadClasses() {
        try {
            List<Classe> classes = ClasseDAO.findAll();
            
            classeComboBox.setItems(FXCollections.observableArrayList(classes));
            
            // Définir le convertisseur pour afficher le nom de la classe
            classeComboBox.setConverter(new javafx.util.StringConverter<Classe>() {
                @Override
                public String toString(Classe classe) {
                    return classe == null ? "" : classe.getNom();
                }
                
                @Override
                public Classe fromString(String string) {
                    return null;
                }
            });
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les classes", e.getMessage());
        }
    }
    
    @FXML
    private void handleSave(ActionEvent event) {
        // Valider les entrées
        if (intituleField.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champs manquants", "Intitulé requis", 
                    "Veuillez saisir un intitulé pour le cours.");
            return;
        }
        
        if (volumeHoraireField.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champs manquants", "Volume horaire requis", 
                    "Veuillez saisir un volume horaire pour le cours.");
            return;
        }
        
        Enseignant enseignant = enseignantComboBox.getValue();
        if (enseignant == null) {
            showAlert(Alert.AlertType.WARNING, "Champs manquants", "Enseignant requis", 
                    "Veuillez sélectionner un enseignant pour le cours.");
            return;
        }
        
        Classe classe = classeComboBox.getValue();
        if (classe == null) {
            showAlert(Alert.AlertType.WARNING, "Champs manquants", "Classe requise", 
                    "Veuillez sélectionner une classe pour le cours.");
            return;
        }
        
        try {
            // Créer le cours
            Cours cours = new Cours(
                0, // ID sera généré par la base de données
                intituleField.getText(),
                Integer.parseInt(volumeHoraireField.getText()),
                enseignant.getId()
            );
            
            boolean success = CoursDAO.save(cours);
            
            if (success) {
                // Associer à la classe
                if (cours.getId() > 0) {
                    ClasseDAO.associerCours(classe.getId(), cours.getId());
                }
                
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Cours ajouté", 
                        "Le cours a été ajouté avec succès.");
                
                closeWindow();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de l'ajout", 
                        "Impossible d'ajouter le cours.");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Volume horaire invalide", 
                    "Le volume horaire doit être un nombre entier.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de l'ajout", e.getMessage());
        }
    }
    
    @FXML
    private void handleCancel(ActionEvent event) {
        closeWindow();
    }
    
    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
    
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}