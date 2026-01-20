package slot.db;

import DAO.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SpinLogDao {
    public static final String DDL = """
            CREATE TABLE spin_log (
                spin_id BIGINT IDENTITY(1,1) PRIMARY KEY,
                ts DATETIME2 NOT NULL,
                bet_total INT NOT NULL,
                win_total INT NOT NULL,
                stops NVARCHAR(64) NOT NULL,
                window NVARCHAR(128) NOT NULL,
                scatter_count INT NOT NULL,
                bonus_triggered BIT NOT NULL
            );
            """;

    private static final Logger LOGGER = Logger.getLogger(SpinLogDao.class.getName());
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public void insertAsync(SpinLog log) {
        executor.submit(() -> insert(log));
    }

    public void insert(SpinLog log) {
        String sql = """
                INSERT INTO spin_log (ts, bet_total, win_total, stops, window, scatter_count, bonus_triggered)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, log.timestamp());
            statement.setInt(2, log.betTotal());
            statement.setInt(3, log.winTotal());
            statement.setString(4, log.stops());
            statement.setString(5, log.window());
            statement.setInt(6, log.scatterCount());
            statement.setBoolean(7, log.bonusTriggered());
            statement.executeUpdate();
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "No se pudo insertar spin log", ex);
        }
    }

    public static SpinLog buildLog(Instant ts, int betTotal, int winTotal,
                                   int[] stops, String window,
                                   int scatterCount, boolean bonusTriggered) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < stops.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(stops[i]);
        }
        return new SpinLog(ts, betTotal, winTotal, sb.toString(), window, scatterCount, bonusTriggered);
    }
}
