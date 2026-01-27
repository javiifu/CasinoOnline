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
    public static final String DDL = """
            CREATE TABLE [game].[spins_slot](
                [ronda_id] [uniqueidentifier] NOT NULL,
                [apuesta_id] [uniqueidentifier] NOT NULL,
                [grid_rodillos_json] [nvarchar](max) NULL,
                [lineas_json] [nvarchar](max) NULL,
                [semilla_rng] [nvarchar](128) NULL,
                [nonce_rng] [nvarchar](128) NULL,
                [es_bonus] [bit] NOT NULL,
                [multiplicador] [int] NOT NULL,
            PRIMARY KEY CLUSTERED 
            (
                [ronda_id] ASC
            )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
            UNIQUE NONCLUSTERED 
            (
                [apuesta_id] ASC
            )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
            ) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
            GO

            ALTER TABLE [game].[spins_slot] ADD  DEFAULT ((0)) FOR [es_bonus]
            GO

            ALTER TABLE [game].[spins_slot] ADD  DEFAULT ((1)) FOR [multiplicador]
            GO

            ALTER TABLE [game].[spins_slot]  WITH CHECK ADD FOREIGN KEY([apuesta_id])
            REFERENCES [game].[apuestas] ([apuesta_id])
            GO

            ALTER TABLE [game].[spins_slot]  WITH CHECK ADD FOREIGN KEY([ronda_id])
            REFERENCES [game].[rondas_juego] ([ronda_id])
            """;

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
