package fr.azrodorza;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

@RequiredArgsConstructor
public class ConfigService {
    private final Path configFilePath;

    public MailConfig loadMailConfig() throws IOException {
        Properties props = new Properties();
        try (Reader reader = Files.newBufferedReader(configFilePath)) {
            props.load(reader);
        }
        return new MailConfig(
                props.getProperty("mail.username"),
                props.getProperty("mail.password"),
                props.getProperty("mail.smtp.host", "smtp.gmail.com"),
                Integer.parseInt(props.getProperty("mail.smtp.port", "587")),
                Integer.parseInt(props.getProperty("draw.budget", "30"))
        );
    }
}
