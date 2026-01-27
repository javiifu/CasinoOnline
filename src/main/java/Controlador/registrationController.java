/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package Controlador;

import DAO.UserDAO;
import Model.UserRegistrationData;
import Utils.SceneManager;
import java.net.URL;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

/**
 * FXML Controller class
 *
 * @author javib
 */
public class registrationController implements Initializable {

    private final UserDAO userDAO = new UserDAO();

    @FXML
    private AnchorPane btn;
    @FXML
    private Button btnRegistrar;
    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtNif;
    @FXML
    private TextField txtEmail;
    @FXML
    private TextField txtApellidos;
    @FXML
    private DatePicker dateNacimiento;
    @FXML
    private TextField txtTelefono;
    @FXML
    private TextField txtNombreCalle;
    @FXML
    private TextField txtNumCalle;
    @FXML
    private TextField txtPiso;
    @FXML
    private TextField txtCp;
    @FXML
    private ComboBox<String> cmbPais;
    @FXML
    private TextField txtCiudad;
    @FXML
    private TextField txtProvincia;
    @FXML
    private CheckBox chkCondiciones;
    @FXML
    private CheckBox chkLOPD;
    @FXML
    private Button btnVolver;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private Label lbl_Obligacion;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cmbPais.getItems().setAll(List.of("ES", "PT", "FR"));
        lbl_Obligacion.setVisible(false);
    }

    @FXML
    private void click(ActionEvent event) {
        lbl_Obligacion.setVisible(false);
        ValidationResult validation = validarCampos();
        if (!validation.isValid()) {
            lbl_Obligacion.setText(validation.message());
            lbl_Obligacion.setVisible(true);
            return;
        }

        char[] password = txtPassword.getText().toCharArray();
        try {
            if (userDAO.existeEmail(txtEmail.getText().trim())) {
                showAlert(Alert.AlertType.WARNING, "Email ya registrado", "Ya existe un usuario con ese email.");
                return;
            }
            Optional<UUID> userId = userDAO.registrarUsuario(buildRegistrationData(), password);
            if (userId.isPresent()) {
                SceneManager.switchScene(btnRegistrar, "/Vista/PrincipalView.fxml", "Principal");
            } else {
                showAlert(Alert.AlertType.ERROR, "Registro fallido", "No se pudo completar el registro.");
            }
        } finally {
            Arrays.fill(password, '\0');
            txtPassword.clear();
        }
    }

    @FXML
    private void clickVolver(ActionEvent event) {
        SceneManager.switchScene(btnVolver, "/Vista/LoginView.fxml", "Login");
    }

    private ValidationResult validarCampos() {
        if (isBlank(txtNombre)
                || isBlank(txtApellidos)
                || isBlank(txtNif)
                || isBlank(txtEmail)
                || dateNacimiento.getValue() == null
                || isBlank(txtTelefono)
                || isBlank(txtNombreCalle)
                || isBlank(txtNumCalle)
                || isBlank(txtPiso)
                || isBlank(txtCp)
                || cmbPais.getValue() == null
                || isBlank(txtCiudad)
                || isBlank(txtProvincia)
                || txtPassword.getText() == null
                || txtPassword.getText().isBlank()
                || !chkCondiciones.isSelected()
                || !chkLOPD.isSelected()) {
            return ValidationResult.invalid("Completa todos los campos obligatorios y acepta las condiciones.");
        }

        if (!txtEmail.getText().contains("@")) {
            return ValidationResult.invalid("El email no tiene un formato válido.");
        }

        LocalDate nacimiento = dateNacimiento.getValue();
        if (nacimiento != null && nacimiento.isAfter(LocalDate.now().minusYears(18))) {
            return ValidationResult.invalid("Debes ser mayor de edad para registrarte.");
        }

        return ValidationResult.valid();
    }

    private boolean isBlank(TextField field) {
        return field.getText() == null || field.getText().trim().isEmpty();
    }

    private UserRegistrationData buildRegistrationData() {
        String direccion = String.format("%s %s, %s",
                txtNombreCalle.getText().trim(),
                txtNumCalle.getText().trim(),
                txtPiso.getText().trim());

        return new UserRegistrationData(
                txtNombre.getText().trim(),
                txtApellidos.getText().trim(),
                dateNacimiento.getValue(),
                txtNif.getText().trim(),
                txtEmail.getText().trim(),
                txtTelefono.getText().trim(),
                direccion,
                txtCp.getText().trim(),
                cmbPais.getValue().toString(),
                txtCiudad.getText().trim(),
                txtProvincia.getText().trim()
        );
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
