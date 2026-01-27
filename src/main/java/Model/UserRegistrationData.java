package Model;

import java.time.LocalDate;

public record UserRegistrationData(
        String nombre,
        String apellidos,
        LocalDate fechaNacimiento,
        String nif,
        String email,
        String telefono,
        String direccion,
        String codigoPostal,
        String codigoPais,
        String ciudad,
        String provincia
) {
}
