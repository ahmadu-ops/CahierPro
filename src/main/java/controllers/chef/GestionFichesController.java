// src/main/java/controllers/chef/GestionFichesController.java
package controllers.chef;

import dao.ClasseDAO;
import dao.CoursDAO;
import dao.FichePedagogiqueDAO;
import dao.UtilisateurDAO;
import javafx.beans.property.SimpleDoubleProperty;
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
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.Classe;
import models.Cours;
import models.FichePedagogique;
import models.Utilisateur;
import views.utils.PDFGenerator;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class GestionFichesController implements Initializable {

    @FXML private Button genererFicheButton;
    @FXML private TableView<FichePedagogique> fichesTableView;
    @FXML private TableColumn<FichePedagogique, String> dateColumn;
    @FXML private TableColumn<FichePedagogique, String> coursColumn;
    @FXML private TableColumn<FichePedagogique, String> enseignantColumn;
    @FXML private TableColumn<FichePedagogique, String> classeColumn;
    @FXML private TableColumn<FichePedagogique, Number> pourcentageColumn;
    @FXML private TableColumn<FichePedagogique, FichePedagogique> actionsColumn;

    private Utilisateur utilisateur;
    private final DecimalFormat df = new DecimalFormat("#.##");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Configurer les cellules de la table
        dateColumn.setCellValueFactory(data ->
            new SimpleStringProperty(
                data.getValue().getDateGeneration().format(dateFormatter)
            )
        );

        coursColumn.setCellValueFactory(data -> {
            Cours c = CoursDAO.findById(data.getValue().getCoursId());
            return new SimpleStringProperty(c != null ? c.getIntitule() : "N/A");
        });

        enseignantColumn.setCellValueFactory(data -> {
            Cours c = CoursDAO.findById(data.getValue().getCoursId());
            if (c != null) {
                Utilisateur ens = UtilisateurDAO.findById(c.getEnseignantId());
                return new SimpleStringProperty(
                    ens != null
                        ? ens.getPrenom() + " " + ens.getNom()
                        : "N/A"
                );
            }
            return new SimpleStringProperty("N/A");
        });

        classeColumn.setCellValueFactory(data -> {
            Cours c = CoursDAO.findById(data.getValue().getCoursId());
            if (c != null) {
                Classe cl = ClasseDAO.findByCoursId(c.getId());
                return new SimpleStringProperty(cl != null ? cl.getNom() : "N/A");
            }
            return new SimpleStringProperty("N/A");
        });

        pourcentageColumn.setCellValueFactory(data ->
            new SimpleDoubleProperty(data.getValue().getPourcentageRealisation())
        );
        pourcentageColumn.setCellFactory(column -> new TableCell<FichePedagogique, Number>() {
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

        actionsColumn.setCellValueFactory(param ->
            new SimpleObjectProperty<>(param.getValue())
        );
        actionsColumn.setCellFactory(param -> new TableCell<FichePedagogique, FichePedagogique>() {
            private final Button viewButton = new Button("Voir");
            private final Button exportButton = new Button("Exporter");

            @Override
            protected void updateItem(FichePedagogique fiche, boolean empty) {
                super.updateItem(fiche, empty);
                if (fiche == null || empty) {
                    setGraphic(null);
                    return;
                }
                viewButton.getStyleClass().add("button-secondary");
                viewButton.setOnAction(event -> viewFiche(fiche));

                exportButton.getStyleClass().add("button-primary");
                exportButton.setOnAction(event -> exportFiche(fiche));

                HBox hbox = new HBox(5, viewButton, exportButton);
                setGraphic(hbox);
            }
        });

        // Charger les fiches
        loadFiches();

        // NOUVEAU : binding largeur + resize policy
        Region parent = (Region) fichesTableView.getParent();
        fichesTableView.prefWidthProperty().bind(parent.widthProperty());
        fichesTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    private void loadFiches() {
        try {
            List<FichePedagogique> fiches = FichePedagogiqueDAO.findAll();
            fichesTableView.setItems(FXCollections.observableArrayList(fiches));
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des fiches: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGenererFiche(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/chef/GenerationFiche.fxml")
            );
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Générer une Fiche Pédagogique");
            Scene scene = new Scene(root);
            scene.getStylesheets().add("/css/common.css");
            scene.getStylesheets().add("/css/chef.css");
            stage.setScene(scene);
            stage.show();

            stage.setOnHidden(e -> loadFiches());
        } catch (IOException e) {
            System.err.println(
                "Erreur lors du chargement de la vue Génération Fiche: " + e.getMessage()
            );
            e.printStackTrace();
        }
    }

    private void viewFiche(FichePedagogique fiche) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/chef/DetailFiche.fxml")
            );
            Parent root = loader.load();
            controllers.chef.DetailFicheController controller = loader.getController();
            controller.setFicheId(fiche.getId());

            Stage stage = new Stage();
            stage.setTitle("Détail de la fiche pédagogique");
            Scene scene = new Scene(root);
            scene.getStylesheets().add("/css/common.css");
            scene.getStylesheets().add("/css/chef.css");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println(
                "Erreur lors du chargement de la vue détail fiche: " + e.getMessage()
            );
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                      "Impossible d'afficher les détails de la fiche.");
        }
    }

    private void exportFiche(FichePedagogique fiche) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer la fiche pédagogique");
            String defaultName = "fiche_"
                + fiche.getId() + "_"
                + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + ".pdf";
            fileChooser.setInitialFileName(defaultName);
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Documents PDF (*.pdf)", "*.pdf")
            );

            File file = fileChooser.showSaveDialog(fichesTableView.getScene().getWindow());
            if (file != null) {
                boolean success = PDFGenerator.genererPDF(fiche.getId(), file.getAbsolutePath());
                if (!success) {
                    showAlert(Alert.AlertType.ERROR, "Erreur",
                              "Impossible d'exporter la fiche en PDF.");
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'export de la fiche: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                      "Impossible d'exporter la fiche en PDF: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
