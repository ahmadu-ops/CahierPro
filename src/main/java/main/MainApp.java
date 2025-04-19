package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Chargement de la vue de connexion
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login/LoginView.fxml"));
        Parent root = loader.load();
        
        // Configuration de la scène avec les nouveaux fichiers CSS
        Scene scene = new Scene(root);
        scene.getStylesheets().add("/css/common.css");
        scene.getStylesheets().add("/css/login.css");
        
        // Configuration de la fenêtre principale
        primaryStage.setTitle("Gestion du Cahier de Texte - Connexion");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}