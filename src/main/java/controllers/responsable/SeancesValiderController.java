// src/main/java/controllers/responsable/SeancesValiderController.java
package controllers.responsable;

import dao.ClasseDAO;
import dao.CoursDAO;
import dao.SeanceDAO;
import dao.UtilisateurDAO;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import models.*;

import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class SeancesValiderController implements Initializable {

    // Éléments pour les séances non validées
    @FXML private ComboBox<Cours> coursNonValideesComboBox;
    @FXML private TableView<Seance> seancesNonValideesTableView;
    @FXML private TableColumn<Seance, String> dateColumn1;
    @FXML private TableColumn<Seance, String> coursColumn1;
    @FXML private TableColumn<Seance, String> enseignantColumn1;
    @FXML private TableColumn<Seance, String> dureeColumn1;
    @FXML private TableColumn<Seance, String> contenuColumn1;
    @FXML private TableColumn<Seance, Seance> actionsColumn1;
    
    // Éléments pour les séances validées
    @FXML private ComboBox<Cours> coursValideesComboBox;
    @FXML private TableView<Seance> seancesValideesTableView;
    @FXML private TableColumn<Seance, String> dateColumn2;
    @FXML private TableColumn<Seance, String> coursColumn2;
    @FXML private TableColumn<Seance, String> enseignantColumn2;
    @FXML private TableColumn<Seance, String> dureeColumn2;
    @FXML private TableColumn<Seance, String> contenuColumn2;
    @FXML private TableColumn<Seance, Seance> actionsColumn2;
    
    private Responsable responsable;
    private Classe classe;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Configuration des colonnes pour les séances non validées
        dateColumn1.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getDateSeance().format(dateFormatter)));
        
        coursColumn1.setCellValueFactory(data -> {
            Cours cours = CoursDAO.findById(data.getValue().getCoursId());
            return new SimpleStringProperty(cours != null ? cours.getIntitule() : "N/A");
        });
        
        enseignantColumn1.setCellValueFactory(data -> {
            Cours cours = CoursDAO.findById(data.getValue().getCoursId());
            if (cours != null) {
                Utilisateur enseignant = UtilisateurDAO.findById(cours.getEnseignantId());
                return new SimpleStringProperty(enseignant != null
                    ? enseignant.getPrenom() + " " + enseignant.getNom()
                    : "N/A");
            }
            return new SimpleStringProperty("N/A");
        });
        
        dureeColumn1.setCellValueFactory(data -> {
            int heures = data.getValue().getDuree() / 60;
            return new SimpleStringProperty(heures + " h");
        });
        
        contenuColumn1.setCellValueFactory(data -> {
            String contenu = data.getValue().getContenu();
            if (contenu != null && contenu.length() > 50) {
                contenu = contenu.substring(0, 47) + "...";
            }
            return new SimpleStringProperty(contenu);
        });
        
        actionsColumn1.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue()));
        actionsColumn1.setCellFactory(param -> new TableCell<Seance, Seance>() {
            private final Button validerButton = new Button("Valider");
            
            @Override
            protected void updateItem(Seance seance, boolean empty) {
                super.updateItem(seance, empty);
                if (seance == null || empty) {
                    setGraphic(null);
                    return;
                }
                
                validerButton.getStyleClass().add("button-primary");
                validerButton.setPrefWidth(80);
                validerButton.setOnAction(event -> validerSeance(seance));
                
                setGraphic(validerButton);
            }
        });
        
        // Configuration des colonnes pour les séances validées
        dateColumn2.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getDateSeance().format(dateFormatter)));
        
        coursColumn2.setCellValueFactory(data -> {
            Cours cours = CoursDAO.findById(data.getValue().getCoursId());
            return new SimpleStringProperty(cours != null ? cours.getIntitule() : "N/A");
        });
        
        enseignantColumn2.setCellValueFactory(data -> {
            Cours cours = CoursDAO.findById(data.getValue().getCoursId());
            if (cours != null) {
                Utilisateur enseignant = UtilisateurDAO.findById(cours.getEnseignantId());
                return new SimpleStringProperty(enseignant != null
                    ? enseignant.getPrenom() + " " + enseignant.getNom()
                    : "N/A");
            }
            return new SimpleStringProperty("N/A");
        });
        
        dureeColumn2.setCellValueFactory(data -> {
            int heures = data.getValue().getDuree() / 60;
            return new SimpleStringProperty(heures + " h");
        });
        
        contenuColumn2.setCellValueFactory(data -> {
            String contenu = data.getValue().getContenu();
            if (contenu != null && contenu.length() > 50) {
                contenu = contenu.substring(0, 47) + "...";
            }
            return new SimpleStringProperty(contenu);
        });
        
        actionsColumn2.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue()));
        actionsColumn2.setCellFactory(param -> new TableCell<Seance, Seance>() {
            private final Button detailsButton = new Button("Détails");
            
            @Override
            protected void updateItem(Seance seance, boolean empty) {
                super.updateItem(seance, empty);
                if (seance == null || empty) {
                    setGraphic(null);
                    return;
                }
                detailsButton.getStyleClass().add("button-secondary");
                detailsButton.setPrefWidth(80);
                detailsButton.setOnAction(event -> afficherDetails(seance));
                setGraphic(detailsButton);
            }
        });
        
        // Ajouter les listeners pour les combobox
        coursNonValideesComboBox.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    filtrerSeancesNonValidees(newValue.getId());
                } else {
                    loadAllSeancesNonValidees();
                }
            });
        
        coursValideesComboBox.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    filtrerSeancesValidees(newValue.getId());
                } else {
                    loadAllSeancesValidees();
                }
            });

        // --- binding largeur + resize policy pour les deux TableView ---
        Region parent1 = (Region) seancesNonValideesTableView.getParent();
        seancesNonValideesTableView.prefWidthProperty().bind(parent1.widthProperty());
        seancesNonValideesTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Region parent2 = (Region) seancesValideesTableView.getParent();
        seancesValideesTableView.prefWidthProperty().bind(parent2.widthProperty());
        seancesValideesTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
    
    public void setResponsable(Responsable responsable) {
        this.responsable = responsable;
    }
    
    public void setClasse(Classe classe) {
        this.classe = classe;
        if (classe != null) {
            loadCoursComboBoxes();
            loadAllSeancesNonValidees();
            loadAllSeancesValidees();
        }
    }
    
    private void loadCoursComboBoxes() {
        try {
            List<Cours> cours = CoursDAO.findByClasse(classe.getId());
            coursNonValideesComboBox.setItems(FXCollections.observableArrayList(cours));
            coursValideesComboBox.setItems(FXCollections.observableArrayList(cours));
            
            javafx.util.StringConverter<Cours> converter = new javafx.util.StringConverter<>() {
                @Override
                public String toString(Cours cours) {
                    return cours == null ? "Tous les cours" : cours.getIntitule();
                }
                @Override
                public Cours fromString(String string) {
                    return null;
                }
            };
            coursNonValideesComboBox.setConverter(converter);
            coursValideesComboBox.setConverter(converter);
            coursNonValideesComboBox.getItems().add(0, null);
            coursValideesComboBox.getItems().add(0, null);
            coursNonValideesComboBox.getSelectionModel().selectFirst();
            coursValideesComboBox.getSelectionModel().selectFirst();
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des cours: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadAllSeancesNonValidees() {
        if (classe == null) return;
        try {
            List<Cours> cours = CoursDAO.findByClasse(classe.getId());
            List<Seance> nonValidees = new ArrayList<>();
            for (Cours c : cours) {
                nonValidees.addAll(
                    SeanceDAO.findByCours(c.getId()).stream()
                      .filter(s -> !s.isEstValidee())
                      .collect(Collectors.toList())
                );
            }
            nonValidees.sort((s1, s2) -> s2.getDateSeance().compareTo(s1.getDateSeance()));
            seancesNonValideesTableView.setItems(FXCollections.observableArrayList(nonValidees));
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des séances non validées: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadAllSeancesValidees() {
        if (classe == null) return;
        try {
            List<Cours> cours = CoursDAO.findByClasse(classe.getId());
            List<Seance> validees = new ArrayList<>();
            for (Cours c : cours) {
                validees.addAll(
                    SeanceDAO.findByCours(c.getId()).stream()
                      .filter(Seance::isEstValidee)
                      .collect(Collectors.toList())
                );
            }
            validees.sort((s1, s2) -> s2.getDateSeance().compareTo(s1.getDateSeance()));
            seancesValideesTableView.setItems(FXCollections.observableArrayList(validees));
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des séances validées: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void filtrerSeancesNonValidees(int coursId) {
        try {
            List<Seance> nonValidees = SeanceDAO.findByCours(coursId).stream()
                .filter(s -> !s.isEstValidee())
                .sorted((s1, s2) -> s2.getDateSeance().compareTo(s1.getDateSeance()))
                .collect(Collectors.toList());
            seancesNonValideesTableView.setItems(FXCollections.observableArrayList(nonValidees));
        } catch (Exception e) {
            System.err.println("Erreur lors du filtrage des séances non validées: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void filtrerSeancesValidees(int coursId) {
        try {
            List<Seance> validees = SeanceDAO.findByCours(coursId).stream()
                .filter(Seance::isEstValidee)
                .sorted((s1, s2) -> s2.getDateSeance().compareTo(s1.getDateSeance()))
                .collect(Collectors.toList());
            seancesValideesTableView.setItems(FXCollections.observableArrayList(validees));
        } catch (Exception e) {
            System.err.println("Erreur lors du filtrage des séances validées: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void afficherDetails(Seance seance) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass()
                .getResource("/fxml/responsable/DetailSeance.fxml"));
            Parent root = loader.load();
            DetailSeanceController controller = loader.getController();
            controller.setSeance(seance);
            controller.setMode("detail");
            Stage stage = new Stage();
            stage.setTitle("Détails de la séance");
            Scene scene = new Scene(root);
            scene.getStylesheets().add("/css/common.css");
            scene.getStylesheets().add("/css/responsable.css");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur lors de l'affichage des détails de la séance: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void validerSeance(Seance seance) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass()
                .getResource("/fxml/responsable/DetailSeance.fxml"));
            Parent root = loader.load();
            DetailSeanceController controller = loader.getController();
            controller.setSeance(seance);
            controller.setMode("validation");
            Stage stage = new Stage();
            stage.setTitle("Validation de la séance");
            Scene scene = new Scene(root);
            scene.getStylesheets().add("/css/common.css");
            scene.getStylesheets().add("/css/responsable.css");
            stage.setScene(scene);
            stage.show();
            stage.setOnHidden(e -> {
                if (coursNonValideesComboBox.getValue() != null) {
                    filtrerSeancesNonValidees(coursNonValideesComboBox.getValue().getId());
                } else {
                    loadAllSeancesNonValidees();
                }
                if (coursValideesComboBox.getValue() != null) {
                    filtrerSeancesValidees(coursValideesComboBox.getValue().getId());
                } else {
                    loadAllSeancesValidees();
                }
            });
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la validation de la séance: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
