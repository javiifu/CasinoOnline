
package Controlador;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.List;
import java.util.Arrays;
import javafx.util.Duration;
import javafx.animation.FadeTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;


public class LoginController implements Initializable {

    private ImageView sliderImageView;

    private List<Image> imagenes;
    private int indiceActual = 0;
    @FXML
    private Button btnLogin;
    @FXML
    private Hyperlink hyper_Regsitro;
    @FXML
    private ImageView id_Carrusel;
    @FXML
    private TextField txt_email;
    @FXML
    private PasswordField psw_con;

    public void initialize(URL url, ResourceBundle resourceBundle) {
    
    }

    private void cambiarImagen() {
        indiceActual = (indiceActual + 1) % imagenes.size();

        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), sliderImageView);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        fadeOut.setOnFinished(e -> {
            sliderImageView.setImage(imagenes.get(indiceActual));

            FadeTransition fadeIn = new FadeTransition(Duration.millis(500), sliderImageView);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });

        fadeOut.play();
    }
    
}
