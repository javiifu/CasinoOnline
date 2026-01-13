
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
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Inicialización adicional si se requiere.
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String usuario = txt_usuario.getText() == null ? "" : txt_usuario.getText().trim();
        String passwordRaw = txt_password.getText();
        char[] password = passwordRaw == null ? new char[0] : passwordRaw.toCharArray();

        if (usuario.isBlank() || password.length == 0) {
            showAlert(Alert.AlertType.WARNING, "Datos incompletos", "Ingrese usuario y contraseña.");
            clearPassword(password);
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
                showAlert(Alert.AlertType.ERROR, "Credenciales inválidas", "Usuario o contraseña incorrectos.");
            }
        });

        loginTask.setOnFailed(e -> {
            txt_password.clear();
            Throwable ex = loginTask.getException();
            LOGGER.log(Level.SEVERE, "Error al validar credenciales", ex);
            showAlert(Alert.AlertType.ERROR, "Error", "No fue posible validar el acceso. Intente nuevamente.");
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
}
