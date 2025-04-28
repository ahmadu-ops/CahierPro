// src/main/java/controllers/enseignant/MesCoursController.java
package controllers.enseignant;

import dao.ClasseDAO;
import dao.CoursDAO;
import dao.FichePedagogiqueDAO;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import models.Cours;
import models.Enseignant;

import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.List;
import java.util.ResourceBundle;

public class MesCoursController implements Initializable {
    
    @FXML private TableView<Cours> coursTableView;
    @FXML private TableColumn<Cours, String> nomCoursColumn;
    @FXML private TableColumn<Cours, String> classeColumn;
    @FXML private TableColumn<Cours, Number> volumeHoraireColumn;
    @FXML private TableColumn<Cours, Number> realisationColumn;
    @FXML private TableColumn<Cours, Cours> actionsColumn;
    
    private Enseignant enseignant;
    private DecimalFormat df = new DecimalFormat("#.##");
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Configuration des colonnes
        nomCoursColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getIntitule()));
        
        classeColumn.setCellValueFactory(data -> {
            var classe = ClasseDAO.findByCoursId(data.getValue().getId());
            return new SimpleStringProperty(classe != null ? classe.getNom() : "N/A");
        });
        
        volumeHoraireColumn.setCellValueFactory(data -> 
            new SimpleIntegerProperty(data.getValue().getVolumeHoraire()));
        
        realisationColumn.setCellValueFactory(data -> {
            double pourcentage = FichePedagogiqueDAO
                .calculerPourcentageRealisation(data.getValue().getId());
            return new SimpleDoubleProperty(pourcentage);
        });
        
        // Formatter la colonne de pourcentage
        realisationColumn.setCellFactory(column -> new TableCell<Cours, Number>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(df.format(item.doubleValue()) + " %");
                }
            }
        });
        
        // Configuration de la colonne d'actions
        actionsColumn.setCellValueFactory(param -> 
            new SimpleObjectProperty<>(param.getValue()));
        actionsColumn.setCellFactory(param -> new TableCell<Cours, Cours>() {
            private final Button voirSeancesButton = new Button("Voir séances");
            private final Button ajouterSeanceButton = new Button("+ Séance");
            
            @Override
            protected void updateItem(Cours cours, boolean empty) {
                super.updateItem(cours, empty);
                
                if (cours == null) {
                    setGraphic(null);
                    return;
                }
                
                // Configuration des boutons
                voirSeancesButton.getStyleClass().add("button-secondary");
                voirSeancesButton.setOnAction(event -> voirSeances(cours));
                
                ajouterSeanceButton.getStyleClass().add("button-primary");
                ajouterSeanceButton.setOnAction(event -> ajouterSeance(cours));
                
                // Création d'un conteneur pour les boutons
                HBox hbox = new HBox(5, voirSeancesButton, ajouterSeanceButton);
                setGraphic(hbox);
            }
        });

        // --- AJOUTÉ : binding largeur + resize policy ---
        Region parent = (Region) coursTableView.getParent();
        coursTableView.prefWidthProperty().bind(parent.widthProperty());
        coursTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
    
    public void setEnseignant(Enseignant enseignant) {
        this.enseignant = enseignant;
        loadCours();
    }
    
    private void loadCours() {
        if (enseignant == null) return;
        
        try {
            List<Cours> cours = CoursDAO.findByEnseignant(enseignant.getId());
            coursTableView.setItems(FXCollections.observableArrayList(cours));
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des cours: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void voirSeances(Cours cours) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass()
                .getResource("/fxml/enseignant/SeancesCours.fxml"));
            Parent root = loader.load();
            
            SeancesCoursController controller = loader.getController();
            controller.setCours(cours);
            controller.setEnseignant(enseignant);
            
            Stage stage = new Stage();
            stage.setTitle("Séances du cours: " + cours.getIntitule());
            Scene scene = new Scene(root);
            scene.getStylesheets().add("/css/common.css");
            scene.getStylesheets().add("/css/enseignant.css");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la vue des séances: "
                + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void ajouterSeance(Cours cours) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass()
                .getResource("/fxml/enseignant/AjoutSeance.fxml"));
            Parent root = loader.load();
            
            AjoutSeanceController controller = loader.getController();
            controller.setEnseignant(enseignant);
            controller.setCoursPreselectionne(cours);
            
            Stage stage = new Stage();
            stage.setTitle("Ajouter une séance");
            Scene scene = new Scene(root);
            scene.getStylesheets().add("/css/common.css");
            scene.getStylesheets().add("/css/enseignant.css");
            stage.setScene(scene);
            stage.show();
            
            // Rafraîchir les données après fermeture
            stage.setOnHidden(e -> loadCours());
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la vue d'ajout de séance: "
                + e.getMessage());
            e.printStackTrace();
        }
    }
}