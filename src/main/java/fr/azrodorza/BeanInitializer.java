package fr.azrodorza;


import java.io.IOException;
import java.nio.file.Path;
import java.util.Random;

public class BeanInitializer {
    public GameService gameService(Path playersFilePath, Path configFilePath, boolean sendEmails) throws IOException {
        return new GameService(playerRepository(playersFilePath), drawService(), notifier(configFilePath, sendEmails));
    }

    private PlayerRepository playerRepository(Path playersFilePath) {
        return new PlayerRepository(playersFilePath);
    }

    private DrawService drawService() {
        return new DrawService(new Random());
    }

    private Notifier notifier(Path configFilePath, boolean sendEmails) throws IOException {
        if (!sendEmails) {
            return new ConsoleNotifier();
        }
        MailConfig mailConfig = new ConfigService(configFilePath).loadMailConfig();
        return new SmtpNotifier(mailConfig);
    }
}
