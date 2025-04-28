// src/main/java/controllers/chef/DetailFicheController.java
package controllers.chef;

import dao.ClasseDAO;
import dao.CoursDAO;
import dao.FichePedagogiqueDAO;
import dao.UtilisateurDAO;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.Region;            // <— ajouté pour le binding
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import models.*;
import views.utils.PDFGenerator;

import java.io.File;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class DetailFicheController implements Initializable {

    @FXML private Label coursLabel;
    @FXML private Label enseignantLabel;
    @FXML private Label classeLabel;
    @FXML private Label dateGenerationLabel;
    @FXML private Label pourcentageLabel;
    @FXML private Label volumeHoraireLabel;
    
    @FXML private TableView<Seance> seancesTableView;
    @FXML private TableColumn<Seance, String> dateSeanceColumn;
    @FXML private TableColumn<Seance, Number> dureeColumn;
    @FXML private TableColumn<Seance, String> contenuColumn;
    @FXML private TableColumn<Seance, Boolean> valideeColumn;
    
    @FXML private TextArea commentaireTextArea;
    @FXML private Button exportPdfButton;
    @FXML private Button closeButton;
    
    private int ficheId;
    private FichePedagogiqueDAO.FicheInfosComplete infos;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DecimalFormat df = new DecimalFormat("#.##");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dateSeanceColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getDateSeance().format(dateFormatter)));
        
        dureeColumn.setCellValueFactory(data -> 
            new SimpleIntegerProperty(data.getValue().getDuree()));
        
        contenuColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getContenu()));
        
        valideeColumn.setCellValueFactory(data -> 
            new SimpleBooleanProperty(data.getValue().isEstValidee()));
        valideeColumn.setCellFactory(column -> new CheckBoxTableCell<>());

        // --- AJOUTÉ : binding largeur + resize policy ---
        Region parent = (Region) seancesTableView.getParent();
        seancesTableView.prefWidthProperty().bind(parent.widthProperty());
        seancesTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
    
    public void setFicheId(int ficheId) {
        this.ficheId = ficheId;
        loadFicheDetails();
    }
    
    private void loadFicheDetails() {
        try {
            infos = FichePedagogiqueDAO.getInfosComplete(ficheId);
            if (infos == null) throw new RuntimeException("Infos null");
            
            FichePedagogique f = infos.getFiche();
            Cours c = infos.getCours();
            Utilisateur ens = infos.getEnseignant();
            Classe cl = ClasseDAO.findByCoursId(c.getId());
            
            coursLabel.setText(c.getIntitule());
            enseignantLabel.setText(ens.getPrenom()+" "+ens.getNom());
            classeLabel.setText(cl!=null?cl.getNom():"Non définie");
            dateGenerationLabel.setText(f.getDateGeneration().format(dateFormatter));
            pourcentageLabel.setText(df.format(f.getPourcentageRealisation())+" %");
            volumeHoraireLabel.setText(c.getVolumeHoraire()+" h");
            commentaireTextArea.setText(f.getCommentaire());
            
            seancesTableView.setItems(FXCollections.observableArrayList(infos.getSeances()));
        } catch (Exception e) {
            System.err.println("Erreur chargement fiche: "+e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR,"Erreur",
                      "Impossible de charger les détails: "+e.getMessage());
        }
    }
    
    @FXML
    private void handleExportPdf(ActionEvent event) {
        try {
            FileChooser fc = new FileChooser();
            fc.setTitle("Enregistrer PDF");
            String def = "fiche_"+ficheId+"_"+LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))+".pdf";
            fc.setInitialFileName(def);
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF","*.pdf"));
            File file = fc.showSaveDialog(exportPdfButton.getScene().getWindow());
            if (file!=null && !PDFGenerator.genererPDF(ficheId,file.getAbsolutePath())) {
                showAlert(Alert.AlertType.ERROR,"Erreur","Impossible d'exporter en PDF.");
            }
        } catch (Exception e) {
            System.err.println("Erreur export PDF: "+e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR,"Erreur","Export PDF échoué: "+e.getMessage());
        }
    }
    
    @FXML
    private void handleClose(ActionEvent event) {
        ((Stage)closeButton.getScene().getWindow()).close();
    }
    
    private void showAlert(Alert.AlertType t,String title,String content) {
        Alert a = new Alert(t);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(content);
        a.showAndWait();
    }
}
