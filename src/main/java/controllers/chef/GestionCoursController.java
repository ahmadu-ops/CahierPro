// src/main/java/controllers/chef/GestionCoursController.java
package controllers.chef;

import dao.ClasseDAO;
import dao.CoursDAO;
import dao.FichePedagogiqueDAO;
import dao.UtilisateurDAO;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import models.Classe;
import models.Cours;
import models.Utilisateur;

import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.List;
import java.util.ResourceBundle;

public class GestionCoursController implements Initializable {

    @FXML private ComboBox<Classe> classeComboBox;
    @FXML private Button ajouterCoursButton;
    @FXML private TableView<Cours> coursTableView;
    @FXML private TableColumn<Cours, String> nomCoursColumn;
    @FXML private TableColumn<Cours, String> enseignantColumn;
    @FXML private TableColumn<Cours, String> classeColumn;
    @FXML private TableColumn<Cours, Number> volumeHoraireColumn;
    @FXML private TableColumn<Cours, Number> realisationColumn;

    private Utilisateur utilisateur;
    private DecimalFormat df = new DecimalFormat("#.##");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Configurer les cellules de la table
        nomCoursColumn.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getIntitule()));
        enseignantColumn.setCellValueFactory(data -> {
            Utilisateur ens = UtilisateurDAO.findById(data.getValue().getEnseignantId());
            return new SimpleStringProperty(ens != null
                ? ens.getPrenom() + " " + ens.getNom()
                : "N/A");
        });
        classeColumn.setCellValueFactory(data -> {
            Classe cl = ClasseDAO.findByCoursId(data.getValue().getId());
            return new SimpleStringProperty(cl != null ? cl.getNom() : "N/A");
        });
        volumeHoraireColumn.setCellValueFactory(data ->
            new SimpleIntegerProperty(data.getValue().getVolumeHoraire()));
        realisationColumn.setCellValueFactory(data -> {
            double pct = FichePedagogiqueDAO.calculerPourcentageRealisation(data.getValue().getId());
            return new SimpleDoubleProperty(pct);
        });
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

        // Charger les classes
        loadClasses();
        classeComboBox.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    loadCoursByClasse(newValue.getId());
                } else {
                    loadAllCours();
                }
            });

        // Charger tous les cours au démarrage
        loadAllCours();

        // NOUVEAU : binding largeur + resize policy
        Region parent = (Region) coursTableView.getParent();
        coursTableView.prefWidthProperty().bind(parent.widthProperty());
        coursTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    private void loadClasses() {
        try {
            List<Classe> classes = ClasseDAO.findAll();
            classeComboBox.setItems(FXCollections.observableArrayList(classes));

            classeComboBox.setConverter(new javafx.util.StringConverter<Classe>() {
                @Override
                public String toString(Classe classe) {
                    return classe == null ? "Toutes les classes" : classe.getNom();
                }
                @Override
                public Classe fromString(String string) {
                    return null;
                }
            });

            classeComboBox.getItems().add(0, null);
            classeComboBox.getSelectionModel().selectFirst();
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des classes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadAllCours() {
        try {
            List<Cours> cours = CoursDAO.findAll();
            coursTableView.setItems(FXCollections.observableArrayList(cours));
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des cours: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadCoursByClasse(int classeId) {
        try {
            List<Cours> cours = CoursDAO.findByClasse(classeId);
            coursTableView.setItems(FXCollections.observableArrayList(cours));
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des cours par classe: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAjouterCours(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/chef/AjoutCours.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Ajouter un cours");
            Scene scene = new Scene(root);
            scene.getStylesheets().add("/css/common.css");
            scene.getStylesheets().add("/css/chef.css");
            stage.setScene(scene);
            stage.show();

            stage.setOnHidden(e -> {
                if (classeComboBox.getValue() == null) {
                    loadAllCours();
                } else {
                    loadCoursByClasse(classeComboBox.getValue().getId());
                }
            });

        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la vue Ajout Cours: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
