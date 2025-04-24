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

public class CahierTexteController implements Initializable {

    @FXML private ComboBox<Cours> coursComboBox;
    @FXML private TableView<Seance> seancesTableView;
    @FXML private TableColumn<Seance, String> dateColumn;
    @FXML private TableColumn<Seance, String> coursColumn;
    @FXML private TableColumn<Seance, String> enseignantColumn;
    @FXML private TableColumn<Seance, String> dureeColumn;  // Modifié pour utiliser String au lieu de Number
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
            int heures = data.getValue().getDuree() / 60;  // Division entière pour avoir un nombre entier d'heures
            return new SimpleStringProperty(heures + " h");
        });
        
        contenuColumn.setCellValueFactory(data -> {
            String contenu = data.getValue().getContenu();
            // Limiter la longueur du contenu affiché
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
                } else {
                    if (estValidee) {
                        setText("Validée");
                        setStyle("-fx-text-fill: #0a6d4c; -fx-font-weight: bold;");
                    } else {
                        setText("En attente");
                        setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");
                    }
                }
            }
        });
        
        // Configuration de la colonne d'actions
        actionsColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue()));
        actionsColumn.setCellFactory(param -> new TableCell<Seance, Seance>() {
            private final Button detailsButton = new Button("Détails");
            private Button validerButton = null;
            
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
                
                HBox buttonBox = new HBox(5);
                buttonBox.getChildren().add(detailsButton);
                
                // Ajouter le bouton de validation uniquement pour les séances non validées
                if (!seance.isEstValidee()) {
                    validerButton = new Button("Valider");
                    validerButton.getStyleClass().add("button-primary");
                    validerButton.setPrefWidth(80);
                    validerButton.setOnAction(event -> validerSeance(seance));
                    buttonBox.getChildren().add(validerButton);
                }
                
                setGraphic(buttonBox);
            }
        });
        
        // Ajouter un listener sur la combobox pour filtrer les séances
        coursComboBox.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    filtrerSeances(newValue.getId());
                } else {
                    loadAllSeances();
                }
            });
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
            
            // Configuration de l'affichage des cours
            coursComboBox.setConverter(new javafx.util.StringConverter<Cours>() {
                @Override
                public String toString(Cours cours) {
                    return cours == null ? "Tous les cours" : cours.getIntitule();
                }
                
                @Override
                public Cours fromString(String string) {
                    return null;
                }
            });
            
            // Ajouter l'option "Tous les cours"
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
            // Récupérer tous les cours de la classe
            List<Cours> cours = CoursDAO.findByClasse(classe.getId());
            
            // Récupérer toutes les séances pour ces cours (validées et non validées)
            List<Seance> toutesSeances = new ArrayList<>();
            for (Cours c : cours) {
                List<Seance> seancesCours = SeanceDAO.findByCours(c.getId());
                toutesSeances.addAll(seancesCours);
            }
            
            // Trier les séances par date (les plus récentes d'abord)
            toutesSeances.sort((s1, s2) -> s2.getDateSeance().compareTo(s1.getDateSeance()));
            
            seancesTableView.setItems(FXCollections.observableArrayList(toutesSeances));
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des séances: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void filtrerSeances(int coursId) {
        try {
            // Récupérer toutes les séances du cours sélectionné (validées et non validées)
            List<Seance> seancesCours = SeanceDAO.findByCours(coursId);
            
            // Trier les séances par date (les plus récentes d'abord)
            seancesCours.sort((s1, s2) -> s2.getDateSeance().compareTo(s1.getDateSeance()));
            
            seancesTableView.setItems(FXCollections.observableArrayList(seancesCours));
        } catch (Exception e) {
            System.err.println("Erreur lors du filtrage des séances: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void afficherDetails(Seance seance) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/responsable/DetailSeance.fxml"));
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
            // Demander confirmation
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Validation de séance");
            confirmation.setHeaderText("Valider la séance ?");
            confirmation.setContentText("Êtes-vous sûr de vouloir valider cette séance ?");
            
            Optional<ButtonType> result = confirmation.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                
                // Afficher le détail de la séance avec option de validation
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/responsable/DetailSeance.fxml"));
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
                
                // Rafraîchir la liste après fermeture
                stage.setOnHidden(e -> {
                    if (coursComboBox.getValue() != null) {
                        filtrerSeances(coursComboBox.getValue().getId());
                    } else {
                        loadAllSeances();
                    }
                });
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la validation de la séance: " + e.getMessage());
            e.printStackTrace();
        }
    }
}