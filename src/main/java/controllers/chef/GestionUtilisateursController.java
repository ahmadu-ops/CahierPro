// src/main/java/controllers/chef/GestionUtilisateursController.java
package controllers.chef;

import dao.ClasseDAO;
import dao.UtilisateurDAO;
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
import models.Responsable;
import models.Utilisateur;

import java.io.IOException;
import java.net.URL;
import java.util.List;
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
                    roleAffiche = "Responsable";
                    break;
            }
            return new SimpleStringProperty(roleAffiche);
        });

        classeColumn.setVisible(false);
        classeColumn.setCellValueFactory(data -> {
            if (data.getValue() instanceof Responsable) {
                Responsable resp = (Responsable) data.getValue();
                Classe classe = ClasseDAO.findById(resp.getClasseId());
                return new SimpleStringProperty(classe != null ? classe.getNom() : "Non assignée");
            }
            return new SimpleStringProperty("-");
        });

        // Configurer le combobox de filtre
        roleComboBox.setItems(FXCollections.observableArrayList(
            "Tous les utilisateurs",
            "Enseignants",
            "Responsables de classe"
        ));
        roleComboBox.getSelectionModel().selectFirst();
        roleComboBox.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    filtrerUtilisateurs(newValue);
                    classeColumn.setVisible("Responsables de classe".equals(newValue));
                }
            });

        // Charger tous les utilisateurs au démarrage (sauf les chefs de département)
        loadAllUtilisateurs();

        // NOUVEAU : binding largeur + resize policy
        Region parent = (Region) utilisateursTableView.getParent();
        utilisateursTableView.prefWidthProperty().bind(parent.widthProperty());
        utilisateursTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    private void loadAllUtilisateurs() {
        try {
            List<Utilisateur> utilisateurs = UtilisateurDAO.findAll().stream()
                .filter(u -> !"chef_departement".equals(u.getRole()))
                .collect(Collectors.toList());
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
                    utilisateurs = UtilisateurDAO.findAll().stream()
                        .filter(u -> !"chef_departement".equals(u.getRole()))
                        .collect(Collectors.toList());
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
}