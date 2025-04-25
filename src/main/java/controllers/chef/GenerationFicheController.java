package controllers.chef;

import dao.ClasseDAO;
import dao.CoursDAO;
import dao.FichePedagogiqueDAO;
import dao.UtilisateurDAO;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.Classe;
import models.Cours;
import models.FichePedagogique;
import models.Utilisateur;
import views.utils.PDFGenerator;

import java.io.File;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class GenerationFicheController implements Initializable {

    @FXML private ComboBox<Classe> classeComboBox;
    @FXML private ComboBox<Cours> coursComboBox;
    @FXML private TextArea commentaireTextArea;
    @FXML private Label pourcentageLabel;
    @FXML private Label enseignantLabel;
    @FXML private Button cancelButton;
    @FXML private Button generateButton;

    private DecimalFormat df = new DecimalFormat("#.##");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Charger les classes
        loadClasses();
        
        // Ajouter un listener sur la sélection de classe
        classeComboBox.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    loadCoursByClasse(newValue.getId());
                } else {
                    coursComboBox.getItems().clear();
                }
            });
        
        // Ajouter un listener sur la sélection de cours
        coursComboBox.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    updateCoursInfo(newValue);
                } else {
                    pourcentageLabel.setText("Pourcentage de réalisation : --");
                    enseignantLabel.setText("Enseignant : --");
                }
            });
    }
    
    private void loadClasses() {
        try {
            List<Classe> classes = ClasseDAO.findAll();
            classeComboBox.setItems(FXCollections.observableArrayList(classes));
            
            // Configurer l'affichage des classes
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
    
    private void loadCoursByClasse(int classeId) {
        try {
            List<Cours> cours = CoursDAO.findByClasse(classeId);
            coursComboBox.setItems(FXCollections.observableArrayList(cours));
            
            // Configurer l'affichage des cours
            coursComboBox.setConverter(new javafx.util.StringConverter<Cours>() {
                @Override
                public String toString(Cours cours) {
                    return cours == null ? "" : cours.getIntitule();
                }
                
                @Override
                public Cours fromString(String string) {
                    return null;
                }
            });
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les cours", e.getMessage());
        }
    }
    
    private void updateCoursInfo(Cours cours) {
        try {
            // Mettre à jour le pourcentage de réalisation
            double pourcentage = FichePedagogiqueDAO.calculerPourcentageRealisation(cours.getId());
            pourcentageLabel.setText("Pourcentage de réalisation : " + df.format(pourcentage) + " %");
            
            // Mettre à jour l'enseignant
            Utilisateur enseignant = UtilisateurDAO.findById(cours.getEnseignantId());
            if (enseignant != null) {
                enseignantLabel.setText("Enseignant : " + enseignant.getPrenom() + " " + enseignant.getNom());
            } else {
                enseignantLabel.setText("Enseignant : Non assigné");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la mise à jour des informations du cours: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleGenerate(ActionEvent event) {
        // Vérifier les sélections
        if (classeComboBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection requise", null, "Veuillez sélectionner une classe.");
            return;
        }
        
        if (coursComboBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection requise", null, "Veuillez sélectionner un cours.");
            return;
        }
        
        try {
            Cours selectedCours = coursComboBox.getValue();
            String commentaire = commentaireTextArea.getText();
            
            // Générer la fiche pédagogique
            FichePedagogique fiche = FichePedagogiqueDAO.genererFiche(selectedCours.getId(), commentaire);
            
            if (fiche != null) {
                // Demander à l'utilisateur où sauvegarder le fichier PDF
                exportFiche(fiche);
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", null, "Impossible de générer la fiche pédagogique.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", null, "Erreur lors de la génération de la fiche : " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void exportFiche(FichePedagogique fiche) {
        try {
            // Créer un FileChooser pour permettre à l'utilisateur de choisir où sauvegarder le PDF
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer la fiche pédagogique");
            
            // Proposer un nom de fichier par défaut
            String defaultFileName = "fiche_" + fiche.getId() + "_" + 
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf";
            fileChooser.setInitialFileName(defaultFileName);
            
            // Définir les filtres pour n'afficher que les fichiers PDF
            FileChooser.ExtensionFilter extFilter = 
                new FileChooser.ExtensionFilter("Documents PDF (*.pdf)", "*.pdf");
            fileChooser.getExtensionFilters().add(extFilter);
            
            // Afficher la fenêtre de dialogue pour choisir l'emplacement et le nom du fichier
            File file = fileChooser.showSaveDialog(cancelButton.getScene().getWindow());
            
            if (file != null) {
                String filePath = file.getAbsolutePath();
                
                // Générer le PDF à l'emplacement choisi
                boolean success = PDFGenerator.genererPDF(fiche.getId(), filePath);
                
                if (success) {
                    // Le PDF est déjà ouvert automatiquement par la méthode genererPDF
                    
                    // Afficher un message de confirmation
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Succès");
                    alert.setHeaderText("La fiche a été exportée avec succès");
                    alert.setContentText("Le document a été généré et ouvert dans votre lecteur PDF.");
                    alert.showAndWait();
                    
                    // Fermer la fenêtre
                    closeWindow();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", null, "Impossible d'exporter la fiche en PDF.");
                }
            }
            // Si l'utilisateur annule, ne rien faire
            
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", null, "Erreur lors de l'export de la fiche : " + e.getMessage());
            e.printStackTrace();
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