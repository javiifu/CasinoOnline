
package Controlador;

import DAO.UserDAO;
import Utils.SceneManager;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

public class LoginController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(LoginController.class.getName());
    private final UserDAO userDAO = new UserDAO();

    @FXML
    private Button btn_login;
    @FXML
    private Hyperlink link_registrarse;
    @FXML
    private TextField txt_usuario;
    @FXML
    private PasswordField txt_password;
    @FXML
    private Label lbl_Error;
    @FXML
    private ImageView id_Carrusel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Inicialización adicional si se requiere.
        lbl_Error.setVisible(false);
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        ValidationResult validation = validarCampos();
        String usuario = txt_usuario.getText() == null ? "" : txt_usuario.getText().trim();
        String passwordRaw = txt_password.getText();
        char[] password = passwordRaw == null ? new char[0] : passwordRaw.toCharArray();

        if(!validation.isValid()){
            lbl_Error.setText(validation.message());
            lbl_Error.setVisible(true);
            return;
        }

        Task<Boolean> loginTask = new Task<>() {
            @Override
            protected Boolean call() {
                try {
                    return userDAO.validarCredenciales(usuario, password);
                } finally {
                    clearPassword(password);
                }
            }
        };

        btn_login.disableProperty().bind(loginTask.runningProperty());
        link_registrarse.disableProperty().bind(loginTask.runningProperty());

        loginTask.setOnSucceeded(e -> {
            boolean ok = loginTask.getValue();
            txt_password.clear();
            if (ok) {
                SceneManager.switchScene(btn_login, "/Vista/PrincipalView.fxml", "Principal");
            } else {
                lbl_Error.setText(validation.message());
                lbl_Error.setVisible(true);
            }
        });

        loginTask.setOnFailed(e -> {
            txt_password.clear();
            Throwable ex = loginTask.getException();
            LOGGER.log(Level.SEVERE, "Error al validar credenciales", ex);
            lbl_Error.setText("No fue posible validar el inicio de sesión. Pruebe más tarde.");
            lbl_Error.setVisible(true);
        });

        Thread thread = new Thread(loginTask, "login-task");
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void handleRegistro(ActionEvent event) {
        SceneManager.switchScene(link_registrarse, "/Vista/registrationView.fxml", "Registro");
    }

    private void clearPassword(char[] password) {
        Arrays.fill(password, '\0');
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private ValidationResult validarCampos() {
        if (isBlank(txt_usuario)|| isBlank(txt_password)) {
            return ValidationResult.invalid("Debe rellenar los campos de usuario y contraseña");
        }
        
        return ValidationResult.valid();
    }
    
    private boolean isBlank(TextField field) {
        return field.getText() == null || field.getText().trim().isEmpty();
    }
    
    private record ValidationResult(boolean isValid, String message) {
        static ValidationResult valid() {
            return new ValidationResult(true, "");
        }

        static ValidationResult invalid(String message) {
            return new ValidationResult(false, message);
        }
    }
}
