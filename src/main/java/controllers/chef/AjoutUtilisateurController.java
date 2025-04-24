package controllers.chef;

import dao.ClasseDAO;
import dao.UtilisateurDAO;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.*;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class AjoutUtilisateurController implements Initializable {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private PasswordField motDePasseField;
    @FXML private PasswordField confirmMotDePasseField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private HBox classeContainer;
    @FXML private ComboBox<Classe> classeComboBox;
    @FXML private TextField nouvelleClasseField;
    @FXML private VBox nouvelleClasseContainer;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Configurer les options du rôle - ENLEVER chef_departement des options
        roleComboBox.setItems(FXCollections.observableArrayList(
                "enseignant", "responsable_classe"));
        
        // Par défaut, sélectionner "enseignant"
        roleComboBox.setValue("enseignant");
        
        // Ajouter un écouteur pour afficher/masquer la sélection de classe
        roleComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if ("responsable_classe".equals(newVal)) {
                classeContainer.setVisible(true);
                classeContainer.setManaged(true);
                nouvelleClasseContainer.setVisible(true);
                nouvelleClasseContainer.setManaged(true);
            } else {
                classeContainer.setVisible(false);
                classeContainer.setManaged(false);
                nouvelleClasseContainer.setVisible(false);
                nouvelleClasseContainer.setManaged(false);
            }
        });
        
        // Initialiser la visibilité des containers
        classeContainer.setVisible(false);
        classeContainer.setManaged(false);
        nouvelleClasseContainer.setVisible(false);
        nouvelleClasseContainer.setManaged(false);
        
        // Charger les classes disponibles
        loadClasses();
    }
    
    private void loadClasses() {
        try {
            // Charger toutes les classes
            List<Classe> classes = ClasseDAO.findAll();
            classeComboBox.setItems(FXCollections.observableArrayList(classes));
            
            // Configurer l'affichage des classes
            classeComboBox.setConverter(new javafx.util.StringConverter<Classe>() {
                @Override
                public String toString(Classe classe) {
                    return classe == null ? "" : classe.getNom();
                }
                
                @Override
                public Classe fromString(String string) {
                    return null;
                }
            });
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les classes", e.getMessage());
        }
    }
    
    @FXML
    private void handleSave(ActionEvent event) {
        // Vérifier les champs obligatoires
        if (nomField.getText().isEmpty() || prenomField.getText().isEmpty() || 
            emailField.getText().isEmpty() || motDePasseField.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champs incomplets", "Veuillez remplir tous les champs obligatoires",
                    "Nom, prénom, email et mot de passe sont requis.");
            return;
        }
        
        // Vérifier le format de l'email
        if (!EMAIL_PATTERN.matcher(emailField.getText()).matches()) {
            showAlert(Alert.AlertType.WARNING, "Format email invalide", "L'email n'est pas valide",
                    "Veuillez saisir une adresse email valide.");
            return;
        }
        
        // Vérifier la correspondance des mots de passe
        if (!motDePasseField.getText().equals(confirmMotDePasseField.getText())) {
            showAlert(Alert.AlertType.WARNING, "Mots de passe différents", "Les mots de passe ne correspondent pas",
                    "Veuillez saisir des mots de passe identiques.");
            return;
        }
        
        // Si c'est un responsable, vérifier qu'une classe est sélectionnée ou qu'une nouvelle est fournie
        if ("responsable_classe".equals(roleComboBox.getValue())) {
            if (classeComboBox.getValue() == null && (nouvelleClasseField.getText() == null || nouvelleClasseField.getText().trim().isEmpty())) {
                showAlert(Alert.AlertType.WARNING, "Classe requise", "Information de classe manquante",
                        "Veuillez soit sélectionner une classe existante, soit fournir un nom pour une nouvelle classe.");
                return;
            }
        }
        
        try {
            // Créer l'utilisateur selon le rôle choisi
            String role = roleComboBox.getValue();
            String nom = nomField.getText();
            String prenom = prenomField.getText();
            String email = emailField.getText();
            String motDePasse = motDePasseField.getText();
            
            boolean success = false;
            
            switch (role) {
                case "enseignant":
                    Enseignant enseignant = new Enseignant(0, nom, prenom, email, motDePasse);
                    success = UtilisateurDAO.save(enseignant);
                    break;
                    
                case "responsable_classe":
                    Responsable responsable = new Responsable(0, nom, prenom, email, motDePasse, 0);
                    success = UtilisateurDAO.save(responsable);
                    
                    if (success) {
                        // Récupérer l'ID du responsable créé
                        List<Responsable> responsables = getResponsableByEmail(email);
                        if (!responsables.isEmpty()) {
                            Responsable savedResp = responsables.get(0);
                            Classe classe;
                            
                            // Soit utiliser une classe existante soit en créer une nouvelle
                            if (classeComboBox.getValue() != null) {
                                classe = classeComboBox.getValue();
                                classe.setResponsableId(savedResp.getId());
                                ClasseDAO.update(classe);
                            } else if (nouvelleClasseField.getText() != null && !nouvelleClasseField.getText().trim().isEmpty()) {
                                // Créer une nouvelle classe
                                classe = new Classe(0, nouvelleClasseField.getText().trim(), savedResp.getId());
                                if (ClasseDAO.save(classe)) {
                                    // Mettre à jour le responsable avec l'ID de la nouvelle classe
                                    savedResp.setClasseId(classe.getId());
                                    UtilisateurDAO.update(savedResp);
                                }
                            }
                        }
                    }
                    break;
            }
            
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Utilisateur créé",
                        "L'utilisateur a été créé avec succès.");
                closeWindow();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la création",
                        "Impossible de créer l'utilisateur.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la création", e.getMessage());
        }
    }
    
    private List<Responsable> getResponsableByEmail(String email) {
        try {
            List<Utilisateur> utilisateurs = UtilisateurDAO.findAll();
            return utilisateurs.stream()
                    .filter(u -> u instanceof Responsable && u.getEmail().equals(email))
                    .map(u -> (Responsable) u)
                    .collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return java.util.Collections.emptyList();
        }
    }
    
    @FXML
    private void handleCancel(ActionEvent event) {
        closeWindow();
    }
    
    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
    
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}