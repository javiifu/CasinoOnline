package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SpinLogDao {
    
    private static final Logger LOGGER = Logger.getLogger(SpinLogDao.class.getName());
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public void insertAsync(SpinLog log) {
        executor.submit(() -> insert(log));
    }

    public void insert(SpinLog log) {
        String sql = """
                INSERT INTO game.spins_slot (ronda_id, apuesta_id, grid_rodillos_json, lineas_json, semilla_rng, nonce_rng, es_bonus, multiplicador)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, log.roundId());
            statement.setObject(2, log.betId());
            statement.setString(3, log.gridJson());
            statement.setString(4, log.linesJson());
            statement.setString(5, log.rngSeed());
            statement.setString(6, log.rngNonce());
            statement.setBoolean(7, log.bonusTriggered());
            statement.setInt(8, log.multiplier());
            statement.executeUpdate();
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "No se pudo insertar spin log", ex);
        }
    }

    public static SpinLog buildLog(UUID roundId,
                                   UUID betId,
                                   String gridJson,
                                   String linesJson,
                                   String rngSeed,
                                   String rngNonce,
                                   boolean bonusTriggered,
                                   int multiplier) {
        return new SpinLog(roundId, betId, gridJson, linesJson, rngSeed, rngNonce, bonusTriggered, multiplier);
    }
}
