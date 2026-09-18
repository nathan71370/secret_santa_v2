package fr.azrodorza;

import lombok.RequiredArgsConstructor;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class PlayerRepository {
    private final Path playersFilePath;

    public List<Player> findAll() throws IOException {
        List<Player> players = new ArrayList<>();
        for (String line : Files.readAllLines(playersFilePath)) {
            String[] parts = line.split(";");
            if (parts.length >= 2) {
                String previousTarget = parts.length >= 3 ? parts[2].trim() : null;
                players.add(new Player(parts[0].trim(), parts[1].trim(), previousTarget));
            }
        }
        return players;
    }

    public void saveDraw(List<Assignment> draw) throws IOException {
        archiveOldFile();
        try (BufferedWriter writer = Files.newBufferedWriter(playersFilePath)) {
            for (Assignment assignment : draw) {
                Player giver = assignment.giver();
                writer.write(giver.name() + ";" + giver.email() + ";" + assignment.receiver().name());
                writer.newLine();
            }
        }
    }

    private void archiveOldFile() throws IOException {
        int currentYear = Year.now().getValue();
        Path backup = playersFilePath.resolveSibling("players-" + currentYear + ".old");
        if (Files.exists(backup)) {
            IO.println("Archive " + backup + " déjà présente : conservée telle quelle.");
            return;
        }
        Files.copy(playersFilePath, backup);
        IO.println("Ancien fichier archivé sous : " + backup);
    }
}
