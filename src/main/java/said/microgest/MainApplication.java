package said.microgest;

import javafx.application.Application;
import javafx.stage.Stage;
import jakarta.persistence.EntityManager;
import said.microgest.config.HibernateUtil;

public class MainApplication extends Application {

    @Override
    public void start(Stage stage) {

        EntityManager em = HibernateUtil.getEntityManager();

        if (em != null) {
            System.out.println("✅ Connexion à PostgreSQL réussie !");
            em.close();
        }

        stage.setTitle("MicroGest");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}