package controllers.chef;

import dao.ClasseDAO;
import dao.CoursDAO;
import dao.FichePedagogiqueDAO;
import dao.UtilisateurDAO;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.Classe;
import models.Cours;
import models.FichePedagogique;
import models.Seance;
import models.Utilisateur;
import views.utils.PDFGenerator;

import java.io.File;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class DetailFicheController implements Initializable {

    @FXML private Label coursLabel;
    @FXML private Label enseignantLabel;
    @FXML private Label classeLabel;
    @FXML private Label dateGenerationLabel;
    @FXML private Label pourcentageLabel;
    @FXML private Label volumeHoraireLabel;
    
    @FXML private TableView<Seance> seancesTableView;
    @FXML private TableColumn<Seance, String> dateSeanceColumn;
    @FXML private TableColumn<Seance, Number> dureeColumn;
    @FXML private TableColumn<Seance, String> contenuColumn;
    @FXML private TableColumn<Seance, Boolean> valideeColumn;
    
    @FXML private TextArea commentaireTextArea;
    @FXML private Button exportPdfButton;
    @FXML private Button closeButton;
    
    private int ficheId;
    private FichePedagogiqueDAO.FicheInfosComplete infos;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private DecimalFormat df = new DecimalFormat("#.##");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Configuration des colonnes du tableau des séances
        dateSeanceColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getDateSeance().format(dateFormatter)));
        
        dureeColumn.setCellValueFactory(data -> 
            new SimpleIntegerProperty(data.getValue().getDuree()));
        
        contenuColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getContenu()));
        
        valideeColumn.setCellValueFactory(data -> 
            new SimpleBooleanProperty(data.getValue().isEstValidee()));
        
        valideeColumn.setCellFactory(column -> new CheckBoxTableCell<>());
    }
    
    public void setFicheId(int ficheId) {
        this.ficheId = ficheId;
        loadFicheDetails();
    }
    
    private void loadFicheDetails() {
        try {
            infos = FichePedagogiqueDAO.getInfosComplete(ficheId);
            if (infos == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les détails de la fiche.");
                return;
            }
            
            FichePedagogique fiche = infos.getFiche();
            Cours cours = infos.getCours();
            Utilisateur enseignant = infos.getEnseignant();
            Classe classe = ClasseDAO.findByCoursId(cours.getId());
            
            // Remplir les informations de base
            coursLabel.setText(cours.getIntitule());
            enseignantLabel.setText(enseignant.getPrenom() + " " + enseignant.getNom());
            classeLabel.setText(classe != null ? classe.getNom() : "Non définie");
            dateGenerationLabel.setText(fiche.getDateGeneration().format(dateFormatter));
            pourcentageLabel.setText(df.format(fiche.getPourcentageRealisation()) + " %");
            volumeHoraireLabel.setText(cours.getVolumeHoraire() + " h");
            
            // Remplir le commentaire
            commentaireTextArea.setText(fiche.getCommentaire());
            
            // Remplir le tableau des séances
            seancesTableView.setItems(FXCollections.observableArrayList(infos.getSeances()));
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des détails de la fiche: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les détails de la fiche: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleExportPdf(ActionEvent event) {
        try {
            // Créer un FileChooser pour permettre à l'utilisateur de choisir où sauvegarder le PDF
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer la fiche pédagogique");
            
            // Proposer un nom de fichier par défaut
            String defaultFileName = "fiche_" + ficheId + "_" + 
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf";
            fileChooser.setInitialFileName(defaultFileName);
            
            // Définir les filtres pour n'afficher que les fichiers PDF
            FileChooser.ExtensionFilter extFilter = 
                new FileChooser.ExtensionFilter("Documents PDF (*.pdf)", "*.pdf");
            fileChooser.getExtensionFilters().add(extFilter);
            
            // Afficher la fenêtre de dialogue pour choisir l'emplacement et le nom du fichier
            File file = fileChooser.showSaveDialog(exportPdfButton.getScene().getWindow());
            
            if (file != null) {
                String filePath = file.getAbsolutePath();
                
                // Générer le PDF à l'emplacement choisi
                boolean success = PDFGenerator.genererPDF(ficheId, filePath);
                
                if (success) {
                    // Le PDF est déjà ouvert automatiquement par la méthode genererPDF
                    showAlert(Alert.AlertType.INFORMATION, "Succès", 
                        "La fiche a été exportée avec succès et ouverte dans votre lecteur PDF.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'exporter la fiche en PDF.");
                }
            }
            // Si l'utilisateur annule, ne rien faire
            
        } catch (Exception e) {
            System.err.println("Erreur lors de l'export de la fiche: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'exporter la fiche en PDF: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleClose(ActionEvent event) {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}