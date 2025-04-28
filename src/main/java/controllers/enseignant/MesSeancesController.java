// src/main/java/controllers/enseignant/MesSeancesController.java
package controllers.enseignant;

import dao.ClasseDAO;
import dao.CoursDAO;
import dao.SeanceDAO;
import dao.UtilisateurDAO;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import models.Cours;
import models.Enseignant;
import models.Seance;
import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MesSeancesController implements Initializable {

    @FXML private Button ajouterSeanceButton;
    @FXML private TableView<Seance> seancesTableView;
    @FXML private TableColumn<Seance, String> dateColumn;
    @FXML private TableColumn<Seance, String> coursColumn;
    @FXML private TableColumn<Seance, String> classeColumn;
    @FXML private TableColumn<Seance, String> dureeColumn;  // Modifié pour utiliser String
    @FXML private TableColumn<Seance, String> contenuColumn;
    @FXML private TableColumn<Seance, Boolean> statutColumn;
    
    private Enseignant enseignant;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Configuration des colonnes
        dateColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getDateSeance().format(dateFormatter)));
        
        coursColumn.setCellValueFactory(data -> {
            Cours cours = CoursDAO.findById(data.getValue().getCoursId());
            return new SimpleStringProperty(cours != null ? cours.getIntitule() : "N/A");
        });
        
        classeColumn.setCellValueFactory(data -> {
            Cours cours = CoursDAO.findById(data.getValue().getCoursId());
            if (cours != null) {
                var cl = ClasseDAO.findByCoursId(cours.getId());
                return new SimpleStringProperty(cl != null ? cl.getNom() : "N/A");
            }
            return new SimpleStringProperty("N/A");
        });
        
        // Modifier l'affichage pour montrer la durée en heures
        dureeColumn.setCellValueFactory(data -> {
            int heures = data.getValue().getDuree() / 60;
            return new SimpleStringProperty(heures + " h");
        });
        
        contenuColumn.setCellValueFactory(data -> {
            String contenu = data.getValue().getContenu();
            if (contenu != null && contenu.length() > 50) {
                contenu = contenu.substring(0, 47) + "...";
            }
            return new SimpleStringProperty(contenu);
        });
        
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
    
    public void setEnseignant(Enseignant enseignant) {
        this.enseignant = enseignant;
        loadSeances();
    }
    
    private void loadSeances() {
        if (enseignant == null) return;
        
        try {
            List<Cours> cours = CoursDAO.findByEnseignant(enseignant.getId());
            List<Seance> toutesSeances = new ArrayList<>();
            for (Cours c : cours) {
                toutesSeances.addAll(SeanceDAO.findByCours(c.getId()));
            }
            toutesSeances.sort((s1, s2) -> 
                s2.getDateSeance().compareTo(s1.getDateSeance()));
            seancesTableView.setItems(FXCollections.observableArrayList(toutesSeances));
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
            
            AjoutSeanceController controller = loader.getController();
            controller.setEnseignant(enseignant);
            
            Stage stage = new Stage();
            stage.setTitle("Ajouter une séance");
            Scene scene = new Scene(root);
            scene.getStylesheets().add("/css/common.css");
            scene.getStylesheets().add("/css/enseignant.css");
            stage.setScene(scene);
            stage.show();
            
            stage.setOnHidden(e -> loadSeances());
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la vue d'ajout de séance: "
                + e.getMessage());
            e.printStackTrace();
        }
    }
}