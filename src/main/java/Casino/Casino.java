/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package Casino;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 *
 * @author javib
 */
public class Casino extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {
        
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Casino.class.getResource("/Vista/LoginView.fxml"));
        Pane ventana = (Pane)loader.load();
        Scene escena = new Scene(ventana);
        
        stage.setScene(escena);
        stage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
