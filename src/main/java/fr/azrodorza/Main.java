
package fr.azrodorza;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

public class Main {
    void main(String[] args) throws IOException {
        boolean sendEmails = Arrays.asList(args).contains("--send");

        BeanInitializer beanInitializer = new BeanInitializer();
        Path playersFilePath = Path.of("src/main/resources/draw/players.txt");
        Path configFilePath = Path.of("src/main/resources/config.properties");
        beanInitializer.gameService(playersFilePath, configFilePath, sendEmails).startDraw();
    }
}
