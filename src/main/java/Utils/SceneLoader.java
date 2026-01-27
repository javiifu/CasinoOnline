package Utils;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneLoader {

    private static final Logger LOGGER = Logger.getLogger(SceneLoader.class.getName());

    private SceneLoader() {
    }

    public static <T> T switchScene(Node source, String fxmlPath, String title, Consumer<T> controllerConsumer) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneLoader.class.getResource(fxmlPath));
            Parent root = loader.load();
            T controller = loader.getController();
            if (controllerConsumer != null && controller != null) {
                controllerConsumer.accept(controller);
            }
            Stage stage = (Stage) source.getScene().getWindow();
            stage.setScene(new Scene(root));
            if (title != null) {
                stage.setTitle(title);
            }
            stage.show();
            return controller;
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "No se pudo cargar la vista: " + fxmlPath, ex);
            throw new RuntimeException("Error al cambiar de vista", ex);
        }
    }
}
