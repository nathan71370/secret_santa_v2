package fr.azrodorza;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ConsoleNotifierTest {

    private final PrintStream originalOut = System.out;
    private final ByteArrayOutputStream output = new ByteArrayOutputStream();

    @BeforeEach
    void captureOutput() {
        System.setOut(new PrintStream(output, true));
    }

    @AfterEach
    void restoreOutput() {
        System.setOut(originalOut);
    }

    @Test
    void notify_shouldPrintEveryAssignment() {
        // GIVEN
        Player alice = new Player("Alice", "alice@mail.com", null);
        Player bob = new Player("Bob", "bob@mail.com", null);
        List<Assignment> draw = List.of(new Assignment(alice, bob), new Assignment(bob, alice));

        // WHEN
        new ConsoleNotifier().notify(draw);

        // THEN
        assertThat(output.toString())
                .contains("aucun mail envoyé")
                .contains("Alice -> Bob")
                .contains("Bob -> Alice");
    }
}
