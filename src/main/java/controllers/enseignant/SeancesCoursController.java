// src/main/java/controllers/enseignant/SeancesCoursController.java
package controllers.enseignant;

import dao.ClasseDAO;
import dao.FichePedagogiqueDAO;
import dao.SeanceDAO;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Region;            // <— ajouté pour le binding
import javafx.stage.Stage;
import models.Classe;
import models.Cours;
import models.Enseignant;
import models.Seance;

import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class SeancesCoursController implements Initializable {
    
    @FXML private Label titreCoursLabel;
    @FXML private Label classeLabel;
    @FXML private Label volumeHoraireLabel;
    @FXML private Label pourcentageLabel;
    
    @FXML private Button ajouterSeanceButton;
    @FXML private TableView<Seance> seancesTableView;
    @FXML private TableColumn<Seance, String> dateColumn;
    @FXML private TableColumn<Seance, String> dureeColumn;
    @FXML private TableColumn<Seance, String> contenuColumn;
    @FXML private TableColumn<Seance, Boolean> statutColumn;
    @FXML private Button closeButton;
    
    private Cours cours;
    private Enseignant enseignant;
    private final DecimalFormat df = new DecimalFormat("#.##");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Configuration des colonnes
        dateColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getDateSeance().format(dateFormatter)));
        
        dureeColumn.setCellValueFactory(data -> {
            int heures = data.getValue().getDuree() / 60;
            return new SimpleStringProperty(heures + " h");
        });
        
        contenuColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getContenu()));
        
        statutColumn.setCellValueFactory(data -> 
            new SimpleBooleanProperty(data.getValue().isEstValidee()));
        statutColumn.setCellFactory(column -> new TableCell<Seance, Boolean>() {
            @Override
            protected void updateItem(Boolean estValidee, boolean empty) {
                super.updateItem(estValidee, empty);
                if (empty || estValidee == null) {
                    setText(null);
                    setStyle("");
                } else if (estValidee) {
                    setText("Validée");
                    setStyle("-fx-text-fill: #0a6d4c; -fx-font-weight: bold;");
                } else {
                    setText("En attente");
                    setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");
                }
            }
        });

        // --- AJOUTÉ : binding largeur + resize policy ---
        Region parent = (Region) seancesTableView.getParent();
        seancesTableView.prefWidthProperty().bind(parent.widthProperty());
        seancesTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
    
    public void setCours(Cours cours) {
        this.cours = cours;
        if (cours != null) {
            loadCoursDetails();
            loadSeances();
        }
    }
    
    public void setEnseignant(Enseignant enseignant) {
        this.enseignant = enseignant;
    }
    
    private void loadCoursDetails() {
        titreCoursLabel.setText("Séances du cours: " + cours.getIntitule());
        volumeHoraireLabel.setText(cours.getVolumeHoraire() + " heures");
        
        Classe classe = ClasseDAO.findByCoursId(cours.getId());
        classeLabel.setText(classe != null ? classe.getNom() : "Non assignée");
        
        double pct = FichePedagogiqueDAO.calculerPourcentageRealisation(cours.getId());
        pourcentageLabel.setText(df.format(pct) + " %");
    }
    
    private void loadSeances() {
        try {
            List<Seance> seances = SeanceDAO.findByCours(cours.getId());
            seances.sort((s1, s2) -> s2.getDateSeance().compareTo(s1.getDateSeance()));
            seancesTableView.setItems(FXCollections.observableArrayList(seances));
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des séances: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleAjouterSeance(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass()
                .getResource("/fxml/enseignant/AjoutSeance.fxml"));
            Parent root = loader.load();
            AjoutSeanceController ctrl = loader.getController();
            ctrl.setEnseignant(enseignant);
            ctrl.setCoursPreselectionne(cours);
            
            Stage stage = new Stage();
            stage.setTitle("Ajouter une séance");
            Scene scene = new Scene(root);
            scene.getStylesheets().addAll("/css/common.css","/css/enseignant.css");
            stage.setScene(scene);
            stage.show();
            stage.setOnHidden(e -> {
                loadCoursDetails();
                loadSeances();
            });
        } catch (IOException e) {
            System.err.println("Erreur ajout séance: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleClose(ActionEvent event) {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}
