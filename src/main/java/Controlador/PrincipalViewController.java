
package Controlador;

import DAO.UserDAO;
import Model.SessionContext;
import Utils.SceneLoader;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;


public class PrincipalViewController implements Initializable {

    private final SessionContext sessionContext = SessionContext.getInstance();
    private final UserDAO userDAO = new UserDAO();

    @FXML
    private Label id_NombreUsuario;
    @FXML
    private Label id_Balance;
    @FXML
    private Button btn_Jugar;
    @FXML
    private Button btn_Usuario;
    @FXML
    private Button btn_Notificaciones;

    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        btn_Jugar.setOnAction(this::click_jugar);
        loadSessionData();
        refreshBalance();
    }    

    

    private void loadSessionData() {
        String nombre = sessionContext.getUsername()+ "!";
        id_NombreUsuario.setText(nombre != null && !nombre.isBlank() ? nombre : "Invitado");
    }

    public void refreshBalance() {
        if (sessionContext.getUserId() == null) {
            id_Balance.setText(formatBalance(0L));
            return;
        }
        long balance = userDAO.getBalanceByUserId(sessionContext.getUserId());
        sessionContext.setBalanceCent(balance);
        id_Balance.setText(formatBalance(balance));
    }

    private String formatBalance(long balanceCent) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));
        return format.format(balanceCent / 100.0);
    }

    @FXML
    private void click_jugar(ActionEvent event) {
        
        SceneLoader.switchScene(btn_Jugar, "/Vista/Slot View.fxml", "Slot", controller -> {
            if (controller instanceof SlotViewController slotController) {
                slotController.setSessionContext(sessionContext);
            }
        });
    }
    
}
