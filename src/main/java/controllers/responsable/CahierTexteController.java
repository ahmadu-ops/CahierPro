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
import javafx.scene.layout.Region;  // <-- ajouté pour le binding
import javafx.stage.Stage;
import models.*;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class CahierTexteController implements Initializable {

    @FXML private ComboBox<Cours> coursComboBox;
    @FXML private TableView<Seance> seancesTableView;
    @FXML private TableColumn<Seance, String> dateColumn;
    @FXML private TableColumn<Seance, String> coursColumn;
    @FXML private TableColumn<Seance, String> enseignantColumn;
    @FXML private TableColumn<Seance, String> dureeColumn;
    @FXML private TableColumn<Seance, String> contenuColumn;
    @FXML private TableColumn<Seance, Boolean> statutColumn;
    @FXML private TableColumn<Seance, Seance> actionsColumn;
    
    private Responsable responsable;
    private Classe classe;
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
        
        enseignantColumn.setCellValueFactory(data -> {
            Cours cours = CoursDAO.findById(data.getValue().getCoursId());
            if (cours != null) {
                Utilisateur enseignant = UtilisateurDAO.findById(cours.getEnseignantId());
                return new SimpleStringProperty(enseignant != null ? 
                        enseignant.getPrenom() + " " + enseignant.getNom() : "N/A");
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
        
        // Configuration de la colonne de statut
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
        
        // Configuration de la colonne d'actions
        actionsColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue()));
        actionsColumn.setCellFactory(param -> new TableCell<Seance, Seance>() {
            private final Button detailsButton = new Button("Détails");
            @Override
            protected void updateItem(Seance seance, boolean empty) {
                super.updateItem(seance, empty);
                if (seance == null || empty) {
                    setGraphic(null);
                    return;
                }
                detailsButton.getStyleClass().add("button-secondary");
                detailsButton.setOnAction(event -> afficherDetails(seance));
                
                setGraphic(detailsButton);
            }
        });
        
        // Listener sur la combobox pour filtrer
        coursComboBox.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldV, newV) -> {
                if (newV != null) filtrerSeances(newV.getId());
                else loadAllSeances();
            });

        // --- AJOUTÉ : binding largeur + resize policy ---
        Region parent = (Region) seancesTableView.getParent();
        seancesTableView.prefWidthProperty().bind(parent.widthProperty());
        seancesTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
    
    public void setResponsable(Responsable responsable) {
        this.responsable = responsable;
    }
    
    public void setClasse(Classe classe) {
        this.classe = classe;
        if (classe != null) {
            loadCoursComboBox();
            loadAllSeances();
        }
    }
    
    private void loadCoursComboBox() {
        try {
            List<Cours> cours = CoursDAO.findByClasse(classe.getId());
            coursComboBox.setItems(FXCollections.observableArrayList(cours));
            coursComboBox.setConverter(new javafx.util.StringConverter<Cours>() {
                @Override
                public String toString(Cours c) {
                    return c == null ? "Tous les cours" : c.getIntitule();
                }
                @Override
                public Cours fromString(String s) { return null; }
            });
            coursComboBox.getItems().add(0, null);
            coursComboBox.getSelectionModel().selectFirst();
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des cours: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadAllSeances() {
        if (classe == null) return;
        try {
            List<Seance> toutes = new ArrayList<>();
            for (Cours c : CoursDAO.findByClasse(classe.getId())) {
                toutes.addAll(SeanceDAO.findByCours(c.getId()));
            }
            toutes.sort((s1, s2) -> s2.getDateSeance().compareTo(s1.getDateSeance()));
            seancesTableView.setItems(FXCollections.observableArrayList(toutes));
        } catch (Exception e) {
            System.err.println("Erreur chargement séances: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void filtrerSeances(int coursId) {
        try {
            var liste = SeanceDAO.findByCours(coursId);
            liste.sort((s1, s2) -> s2.getDateSeance().compareTo(s1.getDateSeance()));
            seancesTableView.setItems(FXCollections.observableArrayList(liste));
        } catch (Exception e) {
            System.err.println("Erreur filtrage séances: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void afficherDetails(Seance seance) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass()
                .getResource("/fxml/responsable/DetailSeance.fxml"));
            Parent root = loader.load();
            DetailSeanceController ctrl = loader.getController();
            ctrl.setSeance(seance);
            ctrl.setMode("detail");
            Stage st = new Stage();
            st.setTitle("Détails de la séance");
            st.setScene(new Scene(root));
            st.getScene().getStylesheets().addAll("/css/common.css","/css/responsable.css");
            st.show();
        } catch (IOException e) {
            System.err.println("Erreur détails séance: " + e.getMessage());
            e.printStackTrace();
        }
    }    
}
