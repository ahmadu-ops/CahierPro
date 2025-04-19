package controllers.responsable;

import dao.ClasseDAO;
import dao.CoursDAO;
import dao.SeanceDAO;
import dao.UtilisateurDAO;
import javafx.beans.property.SimpleIntegerProperty;
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
    @FXML private TableColumn<Seance, Number> dureeColumn1;
    @FXML private TableColumn<Seance, String> contenuColumn1;
    @FXML private TableColumn<Seance, Seance> actionsColumn1;
    
    // Éléments pour les séances validées
    @FXML private ComboBox<Cours> coursValideesComboBox;
    @FXML private TableView<Seance> seancesValideesTableView;
    @FXML private TableColumn<Seance, String> dateColumn2;
    @FXML private TableColumn<Seance, String> coursColumn2;
    @FXML private TableColumn<Seance, String> enseignantColumn2;
    @FXML private TableColumn<Seance, Number> dureeColumn2;
    @FXML private TableColumn<Seance, String> contenuColumn2;
    @FXML private TableColumn<Seance, Seance> actionsColumn2;
    
    private Responsable responsable;
    private Classe classe;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Configuration des colonnes pour les séances non validées
        configurerColonnesNonValidees();
        
        // Configuration des colonnes pour les séances validées
        configurerColonnesValidees();
        
        // Ajouter les listeners pour les combobox
        configurerListenersCombobox();
    }
    
    private void configurerColonnesNonValidees() {
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
                return new SimpleStringProperty(enseignant != null ? 
                        enseignant.getPrenom() + " " + enseignant.getNom() : "N/A");
            }
            return new SimpleStringProperty("N/A");
        });
        
        dureeColumn1.setCellValueFactory(data -> 
            new SimpleIntegerProperty(data.getValue().getDuree()));
        
        contenuColumn1.setCellValueFactory(data -> {
            String contenu = data.getValue().getContenu();
            // Limiter la longueur du contenu affiché
            if (contenu != null && contenu.length() > 50) {
                contenu = contenu.substring(0, 47) + "...";
            }
            return new SimpleStringProperty(contenu);
        });
        
        actionsColumn1.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue()));
        actionsColumn1.setCellFactory(param -> new TableCell<Seance, Seance>() {
            private final Button detailsButton = new Button("Détails");
            private final Button validerButton = new Button("Valider");
            
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
                
                validerButton.getStyleClass().add("button-primary");
                validerButton.setPrefWidth(80);
                validerButton.setOnAction(event -> validerSeance(seance));
                
                HBox buttonBox = new HBox(5);
                buttonBox.getChildren().addAll(detailsButton, validerButton);
                setGraphic(buttonBox);
            }
        });
    }
    
    private void configurerColonnesValidees() {
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
                return new SimpleStringProperty(enseignant != null ? 
                        enseignant.getPrenom() + " " + enseignant.getNom() : "N/A");
            }
            return new SimpleStringProperty("N/A");
        });
        
        dureeColumn2.setCellValueFactory(data -> 
            new SimpleIntegerProperty(data.getValue().getDuree()));
        
        contenuColumn2.setCellValueFactory(data -> {
            String contenu = data.getValue().getContenu();
            // Limiter la longueur du contenu affiché
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
    }
    
    private void configurerListenersCombobox() {
        // Pour les séances non validées
        coursNonValideesComboBox.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    filtrerSeancesNonValidees(newValue.getId());
                } else {
                    loadAllSeancesNonValidees();
                }
            });
        
        // Pour les séances validées
        coursValideesComboBox.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    filtrerSeancesValidees(newValue.getId());
                } else {
                    loadAllSeancesValidees();
                }
            });
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
            
            // Pour les séances non validées
            coursNonValideesComboBox.setItems(FXCollections.observableArrayList(cours));
            
            // Pour les séances validées
            coursValideesComboBox.setItems(FXCollections.observableArrayList(cours));
            
            // Configuration de l'affichage des cours
            javafx.util.StringConverter<Cours> converter = new javafx.util.StringConverter<Cours>() {
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
            
            // Ajouter l'option "Tous les cours"
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
            // Récupérer tous les cours de la classe
            List<Cours> cours = CoursDAO.findByClasse(classe.getId());
            
            // Récupérer toutes les séances non validées pour ces cours
            List<Seance> seancesNonValidees = new ArrayList<>();
            for (Cours c : cours) {
                List<Seance> seancesCours = SeanceDAO.findByCours(c.getId());
                
                // Filtrer pour ne garder que les séances non validées
                List<Seance> nonValidees = seancesCours.stream()
                    .filter(s -> !s.isEstValidee())
                    .collect(Collectors.toList());
                
                seancesNonValidees.addAll(nonValidees);
            }
            
            // Trier les séances par date (les plus récentes d'abord)
            seancesNonValidees.sort((s1, s2) -> s2.getDateSeance().compareTo(s1.getDateSeance()));
            
            seancesNonValideesTableView.setItems(FXCollections.observableArrayList(seancesNonValidees));
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des séances non validées: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadAllSeancesValidees() {
        if (classe == null) return;
        
        try {
            // Récupérer tous les cours de la classe
            List<Cours> cours = CoursDAO.findByClasse(classe.getId());
            
            // Récupérer toutes les séances validées pour ces cours
            List<Seance> seancesValidees = new ArrayList<>();
            for (Cours c : cours) {
                List<Seance> seancesCours = SeanceDAO.findByCours(c.getId());
                
                // Filtrer pour ne garder que les séances validées
                List<Seance> validees = seancesCours.stream()
                    .filter(Seance::isEstValidee)
                    .collect(Collectors.toList());
                
                seancesValidees.addAll(validees);
            }
            
            // Trier les séances par date (les plus récentes d'abord)
            seancesValidees.sort((s1, s2) -> s2.getDateSeance().compareTo(s1.getDateSeance()));
            
            seancesValideesTableView.setItems(FXCollections.observableArrayList(seancesValidees));
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des séances validées: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void filtrerSeancesNonValidees(int coursId) {
        try {
            List<Seance> seancesCours = SeanceDAO.findByCours(coursId);
            
            // Filtrer pour ne garder que les séances non validées
            List<Seance> seancesNonValidees = seancesCours.stream()
                .filter(s -> !s.isEstValidee())
                .collect(Collectors.toList());
            
            // Trier les séances par date (les plus récentes d'abord)
            seancesNonValidees.sort((s1, s2) -> s2.getDateSeance().compareTo(s1.getDateSeance()));
            
            seancesNonValideesTableView.setItems(FXCollections.observableArrayList(seancesNonValidees));
        } catch (Exception e) {
            System.err.println("Erreur lors du filtrage des séances non validées: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void filtrerSeancesValidees(int coursId) {
        try {
            List<Seance> seancesCours = SeanceDAO.findByCours(coursId);
            
            // Filtrer pour ne garder que les séances validées
            List<Seance> seancesValidees = seancesCours.stream()
                .filter(Seance::isEstValidee)
                .collect(Collectors.toList());
            
            // Trier les séances par date (les plus récentes d'abord)
            seancesValidees.sort((s1, s2) -> s2.getDateSeance().compareTo(s1.getDateSeance()));
            
            seancesValideesTableView.setItems(FXCollections.observableArrayList(seancesValidees));
        } catch (Exception e) {
            System.err.println("Erreur lors du filtrage des séances validées: " + e.getMessage());
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
                    // Mettre à jour les deux tableaux
                    Cours coursNonValidees = coursNonValideesComboBox.getValue();
                    Cours coursValidees = coursValideesComboBox.getValue();
                    
                    if (coursNonValidees != null) {
                        filtrerSeancesNonValidees(coursNonValidees.getId());
                    } else {
                        loadAllSeancesNonValidees();
                    }
                    
                    if (coursValidees != null) {
                        filtrerSeancesValidees(coursValidees.getId());
                    } else {
                        loadAllSeancesValidees();
                    }
                });
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la validation de la séance: " + e.getMessage());
            e.printStackTrace();
        }
    }
}