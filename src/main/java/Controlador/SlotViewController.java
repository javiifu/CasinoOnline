
package Controlador;

import Model.SessionContext;
import Utils.SceneLoader;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Rectangle;
import slot.engine.GameService;
import slot.engine.PayoutEvaluator;
import slot.engine.SpinEngine;
import slot.engine.SpinOutcome;
import slot.db.SpinLog;
import slot.db.SpinLogDao;
import slot.db.SpinLogUtil;
import slot.model.SlotConfig;
import slot.model.SlotConfigFactory;
import slot.rng.SplittableRandomSource;
import slot.ui.ReelCanvasView;
import java.time.Instant;


public class SlotViewController implements Initializable {

    @FXML
    private Button btnSpin;
    @FXML
    private Button btnSubirApuesta;
    @FXML
    private Button btnMenos;
    @FXML
    private Slider sldVolumen;
    @FXML
    private Button btnAuto;
    @FXML
    private Button btnVolver;
    @FXML
    private AnchorPane reelContainer;

    private SlotConfig config;
    private GameService gameService;
    private ReelCanvasView reelView;
    private boolean spinning;
    private SpinLogDao spinLogDao;
    private SessionContext sessionContext;
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        config = SlotConfigFactory.createDefault(1);
        gameService = new GameService(new SpinEngine(new SplittableRandomSource()), new PayoutEvaluator());
        spinLogDao = new SpinLogDao();

        reelView = new ReelCanvasView(config, reelContainer.getPrefWidth(), reelContainer.getPrefHeight());
        reelContainer.getChildren().add(reelView);

        Rectangle clip = new Rectangle(reelContainer.getPrefWidth(), reelContainer.getPrefHeight());
        reelContainer.setClip(clip);
        reelContainer.widthProperty().addListener((obs, oldVal, newVal) -> {
            reelView.setWidth(newVal.doubleValue());
            clip.setWidth(newVal.doubleValue());
        });
        reelContainer.heightProperty().addListener((obs, oldVal, newVal) -> {
            reelView.setHeight(newVal.doubleValue());
            clip.setHeight(newVal.doubleValue());
        });

        reelView.setOnSpinFinished(() -> {
            spinning = false;
            btnSpin.setDisable(false);
        });

        btnSpin.setOnAction(event -> spinOnce());
        btnVolver.setOnAction(this::handleVolver);
    }    

    public void setSessionContext(SessionContext sessionContext) {
        this.sessionContext = sessionContext;
    }

    private void handleVolver(ActionEvent event) {
        SceneLoader.switchScene(btnVolver, "/Vista/PrincipalView.fxml", "Principal", controller -> {
            if (controller instanceof PrincipalViewController principalController) {
                principalController.refreshBalance();
            }
        });
    }

    private void spinOnce() {
        if (spinning) {
            return;
        }
        spinning = true;
        btnSpin.setDisable(true);
        SpinOutcome outcome = gameService.spin(config);
        logSpin(outcome);
        reelView.startSpin(outcome.result());
    }

    private void logSpin(SpinOutcome outcome) {
        SpinLog log = SpinLogDao.buildLog(
                Instant.now(),
                config.betTotal(),
                outcome.payoutDetail().totalWin(),
                outcome.result().getStops().stops(),
                SpinLogUtil.windowToCompactString(outcome.result()),
                outcome.payoutDetail().scatterCount(),
                outcome.payoutDetail().bonusTriggered()
        );
        spinLogDao.insertAsync(log);
    }
    
}
