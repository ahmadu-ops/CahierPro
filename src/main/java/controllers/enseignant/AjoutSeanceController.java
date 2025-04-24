package controllers.enseignant;

import dao.CoursDAO;
import dao.SeanceDAO;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import models.Cours;
import models.Enseignant;
import models.Seance;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class AjoutSeanceController implements Initializable {

    @FXML private ComboBox<Cours> coursComboBox;
    @FXML private DatePicker dateSeancePicker;
    @FXML private Spinner<Integer> dureeSpinner; // Utiliser des entiers pour les heures
    @FXML private TextArea contenuTextArea;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Label dureeLabel; // Ajouter cette référence pour le label
    
    private Enseignant enseignant;
    private Cours coursPreselectionne;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Modifier le texte du label pour indiquer des heures
        if (dureeLabel != null) {
            dureeLabel.setText("Durée (en heures) *");
        }
        
        // Configuration du spinner pour des heures entières (entre 1 et 8 heures)
        SpinnerValueFactory<Integer> valueFactory = 
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 8, 2);  // Min, Max, Initial
        dureeSpinner.setValueFactory(valueFactory);
        
        // Configuration du DatePicker pour ne pas permettre de dates dans le passé
        dateSeancePicker.setValue(LocalDate.now());
        dateSeancePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });
        
        // Configuration du ComboBox de cours
        coursComboBox.setConverter(new StringConverter<Cours>() {
            @Override
            public String toString(Cours cours) {
                return cours == null ? "" : cours.getIntitule();
            }
            
            @Override
            public Cours fromString(String string) {
                return null;
            }
        });
    }
    
    public void setEnseignant(Enseignant enseignant) {
        this.enseignant = enseignant;
        loadCours();
    }
    
    public void setCoursPreselectionne(Cours cours) {
        this.coursPreselectionne = cours;
        if (coursComboBox != null && cours != null) {
            coursComboBox.setValue(cours);
        }
    }
    
    private void loadCours() {
        if (enseignant == null) return;
        
        try {
            List<Cours> cours = CoursDAO.findByEnseignant(enseignant.getId());
            coursComboBox.setItems(FXCollections.observableArrayList(cours));
            
            // Si un cours a été présélectionné, le sélectionner
            if (coursPreselectionne != null) {
                coursComboBox.setValue(coursPreselectionne);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des cours: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleSave(ActionEvent event) {
        // Vérification des champs obligatoires
        if (coursComboBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Champ requis", "Veuillez sélectionner un cours.");
            return;
        }
        
        if (dateSeancePicker.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Champ requis", "Veuillez sélectionner une date.");
            return;
        }
        
        if (contenuTextArea.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champ requis", "Veuillez saisir le contenu de la séance.");
            return;
        }
        
        try {
            // Conversion de la durée en minutes (car le modèle stocke en minutes)
            int dureeMinutes = dureeSpinner.getValue() * 60;
            
            Seance nouvelleSeance = new Seance(
                0, // L'ID sera généré par la base de données
                coursComboBox.getValue().getId(),
                contenuTextArea.getText().trim(),
                dateSeancePicker.getValue(),
                dureeMinutes,
                false // Par défaut, une nouvelle séance n'est pas validée
            );
            
            // Enregistrement dans la base de données
            boolean success = SeanceDAO.save(nouvelleSeance);
            
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "La séance a été enregistrée avec succès.");
                closeWindow();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'enregistrer la séance.");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'enregistrement de la séance: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleCancel(ActionEvent event) {
        // Demander confirmation si des données ont été saisies
        if (!contenuTextArea.getText().isEmpty() || coursComboBox.getValue() != null) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirmation");
            confirmation.setHeaderText("Annuler la saisie ?");
            confirmation.setContentText("Les données saisies seront perdues. Voulez-vous continuer ?");
            
            if (confirmation.showAndWait().get() == ButtonType.OK) {
                closeWindow();
            }
        } else {
            closeWindow();
        }
    }
    
    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
    
    private void showAlert(Alert.AlertType type, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(type == Alert.AlertType.INFORMATION ? "Succès" : "Attention");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}