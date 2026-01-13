package Utils;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneManager {

    private static final Logger LOGGER = Logger.getLogger(SceneManager.class.getName());

    private SceneManager() {
    }

    public static void switchScene(Node source, String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(SceneManager.class.getResource(fxmlPath));
            Stage stage = (Stage) source.getScene().getWindow();
            stage.setScene(new Scene(root));
            if (title != null) {
                stage.setTitle(title);
            }
            stage.show();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "No se pudo cargar la vista: " + fxmlPath, ex);
            throw new RuntimeException("Error al cambiar de vista", ex);
        }
    }
}
