package controllers.chef;

import dao.ClasseDAO;
import dao.UtilisateurDAO;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
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
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class GestionUtilisateursController implements Initializable {

    @FXML private ComboBox<String> roleComboBox;
    @FXML private Button ajouterUtilisateurButton;
    @FXML private TableView<Utilisateur> utilisateursTableView;
    @FXML private TableColumn<Utilisateur, String> nomPrenomColumn;
    @FXML private TableColumn<Utilisateur, String> emailColumn;
    @FXML private TableColumn<Utilisateur, String> roleColumn;
    @FXML private TableColumn<Utilisateur, String> classeColumn;
    @FXML private TableColumn<Utilisateur, Utilisateur> actionsColumn;

    private Utilisateur utilisateur;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Configurer les cellules de la table
        nomPrenomColumn.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getPrenom() + " " + data.getValue().getNom()));
        
        emailColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getEmail()));
        
        roleColumn.setCellValueFactory(data -> {
            String role = data.getValue().getRole();
            String roleAffiche = "Inconnu";
            
            switch (role) {
                case "chef_departement":
                    roleAffiche = "Chef de Département";
                    break;
                case "enseignant":
                    roleAffiche = "Enseignant";
                    break;
                case "responsable_classe":
                    roleAffiche = "Responsable de Classe";
                    break;
            }
            
            return new SimpleStringProperty(roleAffiche);
        });
        
        classeColumn.setCellValueFactory(data -> {
            if (data.getValue() instanceof Responsable) {
                Responsable resp = (Responsable) data.getValue();
                Classe classe = ClasseDAO.findById(resp.getClasseId());
                return new SimpleStringProperty(classe != null ? classe.getNom() : "Non assignée");
            }
            return new SimpleStringProperty("-");
        });
        
        // Configurer la colonne d'actions
        actionsColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue()));
        actionsColumn.setCellFactory(param -> new TableCell<Utilisateur, Utilisateur>() {
            private final Button editButton = new Button("Modifier");
            private final Button deleteButton = new Button("Supprimer");
            
            @Override
            protected void updateItem(Utilisateur utilisateur, boolean empty) {
                super.updateItem(utilisateur, empty);
                
                if (utilisateur == null) {
                    setGraphic(null);
                    return;
                }
                
                // Empêcher la modification/suppression du chef de département connecté
                boolean isCurrentChef = utilisateur.getRole().equals("chef_departement");
                
                // Configurer les boutons
                editButton.getStyleClass().add("button-secondary");
                editButton.setOnAction(event -> modifierUtilisateur(utilisateur));
                editButton.setDisable(isCurrentChef);
                
                deleteButton.getStyleClass().add("button-danger");
                deleteButton.setOnAction(event -> supprimerUtilisateur(utilisateur));
                deleteButton.setDisable(isCurrentChef);
                
                // Créer un conteneur horizontal pour les boutons
                HBox hbox = new HBox(5);
                hbox.getChildren().addAll(editButton, deleteButton);
                setGraphic(hbox);
            }
        });
        
        // Configurer le combobox de filtre
        roleComboBox.setItems(FXCollections.observableArrayList(
            "Tous les utilisateurs", 
            "Enseignants", 
            "Responsables de classe"
        ));
        
        roleComboBox.getSelectionModel().selectFirst();
        
        // Ajouter un listener sur le combobox de filtre
        roleComboBox.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    filtrerUtilisateurs(newValue);
                }
            });
        
        // Charger tous les utilisateurs au démarrage
        loadAllUtilisateurs();
    }
    
    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }
    
    private void loadAllUtilisateurs() {
        try {
            List<Utilisateur> utilisateurs = UtilisateurDAO.findAll();
            utilisateursTableView.setItems(FXCollections.observableArrayList(utilisateurs));
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des utilisateurs: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void filtrerUtilisateurs(String filtre) {
        try {
            List<Utilisateur> utilisateurs;
            
            switch (filtre) {
                case "Enseignants":
                    utilisateurs = UtilisateurDAO.findByRole("enseignant");
                    break;
                case "Responsables de classe":
                    utilisateurs = UtilisateurDAO.findByRole("responsable_classe");
                    break;
                default:
                    utilisateurs = UtilisateurDAO.findAll();
                    break;
            }
            
            utilisateursTableView.setItems(FXCollections.observableArrayList(utilisateurs));
        } catch (Exception e) {
            System.err.println("Erreur lors du filtrage des utilisateurs: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleAjouterUtilisateur(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/chef/AjoutUtilisateur.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Ajouter un utilisateur");
            Scene scene = new Scene(root);
            scene.getStylesheets().add("/css/common.css");
            scene.getStylesheets().add("/css/chef.css");
            stage.setScene(scene);
            stage.show();
            
            // Recharger les utilisateurs après la fermeture de la fenêtre
            stage.setOnHidden(e -> {
                String filtre = roleComboBox.getValue();
                if (filtre != null) {
                    filtrerUtilisateurs(filtre);
                } else {
                    loadAllUtilisateurs();
                }
            });
            
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la vue Ajout Utilisateur: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void modifierUtilisateur(Utilisateur utilisateur) {
        // Cette fonctionnalité pourrait être implémentée plus tard
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText("La modification d'utilisateur n'est pas encore implémentée.");
        alert.showAndWait();
    }
    
    private void supprimerUtilisateur(Utilisateur utilisateur) {
        // Demander confirmation
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer l'utilisateur");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer l'utilisateur " + 
                                   utilisateur.getPrenom() + " " + utilisateur.getNom() + " ?");
        
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = UtilisateurDAO.delete(utilisateur.getId());
            
            if (success) {
                // Recharger la liste des utilisateurs
                String filtre = roleComboBox.getValue();
                if (filtre != null) {
                    filtrerUtilisateurs(filtre);
                } else {
                    loadAllUtilisateurs();
                }
                
                // Afficher un message de succès
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText(null);
                alert.setContentText("L'utilisateur a été supprimé avec succès.");
                alert.showAndWait();
            } else {
                // Afficher un message d'erreur
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText(null);
                alert.setContentText("Impossible de supprimer l'utilisateur.");
                alert.showAndWait();
            }
        }
    }
}