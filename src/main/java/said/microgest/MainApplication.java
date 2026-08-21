package said.microgest;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import said.microgest.config.HibernateUtil;

public class MainApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {


        var em = HibernateUtil.getEntityManager();
        if (em != null) {
            System.out.println("✅ Connexion à PostgreSQL réussie !");
            em.close();
        }

        Parent root = FXMLLoader.load(getClass().getResource("/views/login-view.fxml"));

        Scene scene = new Scene(root, 500, 400);


        String css = getClass().getResource("/css/application.css").toExternalForm();
        System.out.println("CSS Path: " + css);


        stage.setTitle("MicroGest - Connexion");
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}