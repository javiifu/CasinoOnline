/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package Controlador;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

/**
 * FXML Controller class
 *
 * @author javib
 */
public class registrationController implements Initializable {

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
    private ComboBox<?> cmbPais;
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
    private Label lbl_Obligacion;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    


    @FXML
    private void clickVolver(ActionEvent event) {
    }

    @FXML
    private void clickRegistro(ActionEvent event) {
    }
    
}
