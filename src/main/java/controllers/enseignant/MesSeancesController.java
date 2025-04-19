package controllers.enseignant;

import dao.ClasseDAO;
import dao.CoursDAO;
import dao.SeanceDAO;
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
import javafx.stage.Stage;
import models.Classe;
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
    @FXML private TableColumn<Seance, Number> dureeColumn;
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
                Classe classe = ClasseDAO.findByCoursId(cours.getId());
                return new SimpleStringProperty(classe != null ? classe.getNom() : "N/A");
            }
            return new SimpleStringProperty("N/A");
        });
        
        dureeColumn.setCellValueFactory(data -> 
            new SimpleIntegerProperty(data.getValue().getDuree()));
        
        contenuColumn.setCellValueFactory(data -> {
            String contenu = data.getValue().getContenu();
            // Limiter la longueur du contenu affiché
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
    }
    
    public void setEnseignant(Enseignant enseignant) {
        this.enseignant = enseignant;
        loadSeances();
    }
    
    private void loadSeances() {
        if (enseignant == null) return;
        
        try {
            // Récupérer tous les cours de l'enseignant
            List<Cours> cours = CoursDAO.findByEnseignant(enseignant.getId());
            
            // Récupérer toutes les séances pour ces cours
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
    
    @FXML
    private void handleAjouterSeance(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/enseignant/AjoutSeance.fxml"));
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
            
            // Rafraîchir les données après fermeture
            stage.setOnHidden(e -> loadSeances());
            
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la vue d'ajout de séance: " + e.getMessage());
            e.printStackTrace();
        }
    }
}