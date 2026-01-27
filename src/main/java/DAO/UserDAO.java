package DAO;

import Model.UserRegistrationData;
import Utils.PasswordHasher;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserDAO {

    private static final Logger LOGGER = Logger.getLogger(UserDAO.class.getName());

    private static final String PASSWORD_ALGO = "SHA-256";

    public boolean validarCredenciales(String email, char[] password) {
        String sql = """
                SELECT au.password_hash, au.password_algo
                FROM auth.autenticacion_usuario au
                INNER JOIN auth.usuarios u ON u.user_id = au.user_id
                WHERE u.email = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return false;
                }

                byte[] storedHash = resultSet.getBytes("password_hash");
                String algo = resultSet.getString("password_algo");
                if (storedHash == null || algo == null || !algo.equalsIgnoreCase(PASSWORD_ALGO)) {
                    return false;
                }

                byte[] inputHash = PasswordHasher.hash(password, PASSWORD_ALGO);
                return PasswordHasher.matches(inputHash, storedHash);
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al consultar credenciales", ex);
            throw new RuntimeException("Error de base de datos", ex);
        }
    }

    public Optional<UUID> registrarUsuario(UserRegistrationData data, char[] password) {
        String insertUsuario = """
                INSERT INTO auth.usuarios (email, telefono)
                OUTPUT inserted.user_id
                VALUES (?, ?)
                """;
        String insertPerfil = """
                INSERT INTO usr.user_profile (
                    user_id, nombre, apellidos, fecha_nacimiento, nif, codigo_pais,
                    provincia, ciudad, direccion, codigo_postal
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        String insertAuth = """
                INSERT INTO auth.autenticacion_usuario (user_id, password_hash, password_algo, failed_login_count)
                VALUES (?, ?, ?, 0)
                """;

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement usuarioStmt = connection.prepareStatement(insertUsuario);
                 PreparedStatement perfilStmt = connection.prepareStatement(insertPerfil);
                 PreparedStatement authStmt = connection.prepareStatement(insertAuth)) {

                usuarioStmt.setString(1, data.email());
                if (data.telefono() == null || data.telefono().isBlank()) {
                    usuarioStmt.setNull(2, Types.NVARCHAR);
                } else {
                    usuarioStmt.setString(2, data.telefono());
                }

                UUID userId;
                try (ResultSet resultSet = usuarioStmt.executeQuery()) {
                    if (!resultSet.next()) {
                        connection.rollback();
                        return Optional.empty();
                    }
                    userId = resultSet.getObject(1, UUID.class);
                }

                perfilStmt.setObject(1, userId);
                perfilStmt.setString(2, data.nombre());
                perfilStmt.setString(3, data.apellidos());
                LocalDate nacimiento = data.fechaNacimiento();
                if (nacimiento == null) {
                    perfilStmt.setNull(4, Types.DATE);
                } else {
                    perfilStmt.setDate(4, java.sql.Date.valueOf(nacimiento));
                }
                perfilStmt.setString(5, data.nif());
                perfilStmt.setString(6, data.codigoPais());
                perfilStmt.setString(7, data.provincia());
                perfilStmt.setString(8, data.ciudad());
                perfilStmt.setString(9, data.direccion());
                perfilStmt.setString(10, data.codigoPostal());
                perfilStmt.executeUpdate();

                byte[] passwordHash = PasswordHasher.hash(password, PASSWORD_ALGO);
                authStmt.setObject(1, userId);
                authStmt.setBytes(2, passwordHash);
                authStmt.setString(3, PASSWORD_ALGO);
                authStmt.executeUpdate();

                connection.commit();
                return Optional.of(userId);
            } catch (SQLException ex) {
                connection.rollback();
                LOGGER.log(Level.SEVERE, "Error al registrar usuario", ex);
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error de base de datos", ex);
            throw new RuntimeException("Error de base de datos", ex);
        }
    }

    public boolean existeEmail(String email) {
        String sql = "SELECT 1 FROM auth.usuarios WHERE email = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al validar email", ex);
            throw new RuntimeException("Error de base de datos", ex);
        }
    }
}
