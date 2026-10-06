package db.migration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public class V13__LoadPlantillasOnboardingHtml extends BaseJavaMigration {

    private static final String[] CODIGOS = {
        "DOC-01", "DOC-02", "DOC-03", "DOC-04", "DOC-05", "DOC-06",
        "DOC-07", "DOC-08", "DOC-09", "DOC-10", "DOC-11", "DOC-12"
    };

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
        try (InputStream in = V13__LoadPlantillasOnboardingHtml.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                throw new IOException("No se encontró " + path);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
