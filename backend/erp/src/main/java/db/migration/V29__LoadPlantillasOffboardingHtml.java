package db.migration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public class V29__LoadPlantillasOffboardingHtml extends BaseJavaMigration {

    private static final String[] CODIGOS = {"DOC-13", "DOC-14"};

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        for (String codigo : CODIGOS) {
            String resource = "templates/onboarding/" + codigo.toLowerCase() + ".html";
            String html = readResource(resource);
            try (PreparedStatement ps = connection.prepareStatement(
                    "UPDATE plantillas_onboarding SET plantilla = ? WHERE codigo = ?")) {
                ps.setString(1, html);
                ps.setString(2, codigo);
                if (ps.executeUpdate() != 1) {
                    throw new IllegalStateException("No se actualizó plantillas_onboarding " + codigo);
                }
            }
        }
    }

    private static String readResource(String path) throws IOException {
        try (InputStream in = V29__LoadPlantillasOffboardingHtml.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                throw new IOException("No se encontró " + path);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
