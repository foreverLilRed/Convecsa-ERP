package db.migration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public class V70__ReloadPlantillaBoletaDualA4 extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        String html = readResource("templates/nominas/boleta-dual-a4.html");
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE plantillas_boleta SET plantilla = ? WHERE codigo = ?")) {
            ps.setString(1, html);
            ps.setString(2, "BOLETA-01");
            if (ps.executeUpdate() != 1) {
                throw new IllegalStateException("No se actualizó plantillas_boleta BOLETA-01");
            }
        }
    }

    private static String readResource(String path) throws IOException {
        try (InputStream in = V70__ReloadPlantillaBoletaDualA4.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                throw new IOException("No se encontró " + path);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
