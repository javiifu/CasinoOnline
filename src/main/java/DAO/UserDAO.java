package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mindrot.jbcrypt.BCrypt;

public class UserDAO {

    private static final Logger LOGGER = Logger.getLogger(UserDAO.class.getName());

    public boolean validarCredenciales(String usuario, char[] password) {
        String sql = "SELECT password_hash FROM usuarios WHERE (username = ? OR email = ?) AND rol = ?";
        String adminRole = "ADMIN";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, usuario);
            statement.setString(2, usuario);
            statement.setString(3, adminRole);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return false;
                }

                String passwordHash = resultSet.getString("password_hash");
                return passwordHash != null && BCrypt.checkpw(new String(password), passwordHash);
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al consultar credenciales", ex);
            throw new RuntimeException("Error de base de datos", ex);
        }
    }
}
