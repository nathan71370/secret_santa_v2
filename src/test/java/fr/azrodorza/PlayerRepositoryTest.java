package fr.azrodorza;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.Year;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlayerRepositoryTest {

    @TempDir
    Path tempDir;

    private Path playersFile;
    private Path archiveFile;
    private PlayerRepository playerRepository;

    @BeforeEach
    void setUp() {
        playersFile = tempDir.resolve("players.txt");
        archiveFile = tempDir.resolve("players-" + Year.now().getValue() + ".old");
        playerRepository = new PlayerRepository(playersFile);
    }

    @Test
    void findAll_shouldReadPlayersWithLastYearTarget() throws IOException {
        // GIVEN
        Files.writeString(playersFile, """
                Alice;alice@mail.com;Bob
                Bob;bob@mail.com;Alice
                """);

        // WHEN
        List<Player> players = playerRepository.findAll();

        // THEN
        assertThat(players).containsExactly(
                new Player("Alice", "alice@mail.com", "Bob"),
                new Player("Bob", "bob@mail.com", "Alice"));
    }

    @Test
    void findAll_shouldSetNullTargetWhenNoLastYearTarget() throws IOException {
        // GIVEN
        Files.writeString(playersFile, "Alice;alice@mail.com\n");

        // WHEN
        List<Player> players = playerRepository.findAll();

        // THEN
        assertThat(players).containsExactly(new Player("Alice", "alice@mail.com", null));
    }

    @Test
    void findAll_shouldTrimSpaces() throws IOException {
        // GIVEN
        Files.writeString(playersFile, "  Alice ; alice@mail.com ;  Bob  \n");

        // WHEN
        List<Player> players = playerRepository.findAll();

        // THEN
        assertThat(players).containsExactly(new Player("Alice", "alice@mail.com", "Bob"));
    }

    @Test
    void findAll_shouldIgnoreBlankAndInvalidLines() throws IOException {
        // GIVEN
        Files.writeString(playersFile, """
                Alice;alice@mail.com

                ligne invalide
                Bob;bob@mail.com
                """);

        // WHEN
        List<Player> players = playerRepository.findAll();

        // THEN
        assertThat(players).extracting(Player::name).containsExactly("Alice", "Bob");
    }

    @Test
    void findAll_shouldThrowWhenFileDoesNotExist() {
        // WHEN / THEN
        assertThatThrownBy(() -> playerRepository.findAll())
                .isInstanceOf(NoSuchFileException.class);
    }

    @Test
    void saveDraw_shouldWriteGiverEmailAndReceiver() throws IOException {
        // GIVEN
        Files.writeString(playersFile, "ancien contenu\n");
        Player alice = new Player("Alice", "alice@mail.com", null);
        Player bob = new Player("Bob", "bob@mail.com", null);
        List<Assignment> draw = List.of(new Assignment(alice, bob), new Assignment(bob, alice));

        // WHEN
        playerRepository.saveDraw(draw);

        // THEN
        assertThat(Files.readAllLines(playersFile)).containsExactly(
                "Alice;alice@mail.com;Bob",
                "Bob;bob@mail.com;Alice");
    }

    @Test
    void saveDraw_thenFindAll_shouldUseDrawAsLastYearTarget() throws IOException {
        // GIVEN
        Files.writeString(playersFile, "Alice;alice@mail.com\nBob;bob@mail.com\n");
        List<Player> players = playerRepository.findAll();
        List<Assignment> draw = List.of(
                new Assignment(players.get(0), players.get(1)),
                new Assignment(players.get(1), players.get(0)));

        // WHEN
        playerRepository.saveDraw(draw);
        List<Player> nextYearPlayers = playerRepository.findAll();

        // THEN
        assertThat(nextYearPlayers).containsExactly(
                new Player("Alice", "alice@mail.com", "Bob"),
                new Player("Bob", "bob@mail.com", "Alice"));
    }

    @Test
    void saveDraw_shouldArchivePreviousFile() throws IOException {
        // GIVEN
        String previousContent = "Alice;alice@mail.com;Bob\n";
        Files.writeString(playersFile, previousContent);

        // WHEN
        playerRepository.saveDraw(List.of());

        // THEN
        assertThat(archiveFile).exists().hasContent(previousContent);
    }

    @Test
    void saveDraw_shouldNotOverwriteExistingArchive() throws IOException {
        // GIVEN
        Files.writeString(archiveFile, "tirage de l'an dernier\n");
        Files.writeString(playersFile, "premier tirage de cette année\n");

        // WHEN
        playerRepository.saveDraw(List.of());

        // THEN
        assertThat(archiveFile).hasContent("tirage de l'an dernier\n");
    }
}
