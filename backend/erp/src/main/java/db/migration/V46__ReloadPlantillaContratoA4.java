package db.migration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public class V46__ReloadPlantillaContratoA4 extends BaseJavaMigration {

    private static final String METADATA = """
            [
              {"clave": "cargo", "label": "Cargo", "tipo": "text", "requerido": true},
              {"clave": "ciudad_firma", "label": "Ciudad de firma", "tipo": "text", "requerido": true}
            ]
            """;

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        String html = readResource("templates/contratos/plantillas/contrato-servicio-especifico.html");
        try (PreparedStatement ps = connection.prepareStatement("""
                UPDATE plantillas_contrato
                SET nombre = ?, plantilla = ?, metadata = ?::jsonb
                WHERE codigo = ?
                """)) {
            ps.setString(1, "Contrato A4 — obra o servicio específico");
            ps.setString(2, html);
            ps.setString(3, METADATA);
            ps.setString(4, "CSE");
            if (ps.executeUpdate() != 1) {
                throw new IllegalStateException("No se actualizó plantillas_contrato CSE");
            }
        }
    }

    private static String readResource(String path) throws IOException {
        try (InputStream in = V46__ReloadPlantillaContratoA4.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                throw new IOException("No se encontró " + path);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
