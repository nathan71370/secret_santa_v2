package fr.azrodorza;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConfigServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void loadMailConfig_shouldReadAllValues() throws IOException {
        // GIVEN
        Path configFile = tempDir.resolve("config.properties");
        Files.writeString(configFile, """
                mail.username=santa@mail.com
                mail.password=secret
                mail.smtp.host=smtp.example.com
                mail.smtp.port=2525
                draw.budget=50
                """);

        // WHEN
        MailConfig mailConfig = new ConfigService(configFile).loadMailConfig();

        // THEN
        assertThat(mailConfig).isEqualTo(new MailConfig("santa@mail.com", "secret", "smtp.example.com", 2525, 50));
    }

    @Test
    void loadMailConfig_shouldUseDefaultValuesWhenOptionalKeysAreMissing() throws IOException {
        // GIVEN
        Path configFile = tempDir.resolve("config.properties");
        Files.writeString(configFile, """
                mail.username=santa@mail.com
                mail.password=secret
                """);

        // WHEN
        MailConfig mailConfig = new ConfigService(configFile).loadMailConfig();

        // THEN
        assertThat(mailConfig).isEqualTo(new MailConfig("santa@mail.com", "secret", "smtp.gmail.com", 587, 30));
    }

    @Test
    void loadMailConfig_shouldReadUtf8Characters() throws IOException {
        // GIVEN
        Path configFile = tempDir.resolve("config.properties");
        Files.writeString(configFile, "mail.username=père.noël@mail.com\nmail.password=é€\n");

        // WHEN
        MailConfig mailConfig = new ConfigService(configFile).loadMailConfig();

        // THEN
        assertThat(mailConfig.username()).isEqualTo("père.noël@mail.com");
        assertThat(mailConfig.password()).isEqualTo("é€");
    }

    @Test
    void loadMailConfig_shouldThrowWhenFileDoesNotExist() {
        // GIVEN
        ConfigService configService = new ConfigService(tempDir.resolve("absent.properties"));

        // WHEN / THEN
        assertThatThrownBy(configService::loadMailConfig)
                .isInstanceOf(NoSuchFileException.class);
    }
}
