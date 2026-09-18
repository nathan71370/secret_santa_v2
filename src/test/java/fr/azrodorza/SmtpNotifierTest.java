package fr.azrodorza;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SmtpNotifierTest {

    @Test
    void notify_shouldThrowWithFailedGiversWhenSmtpServerIsUnreachable() {
        // GIVEN
        MailConfig unreachableServer = new MailConfig("santa@mail.com", "secret", "localhost", 1, 30);
        SmtpNotifier smtpNotifier = new SmtpNotifier(unreachableServer);
        Player alice = new Player("Alice", "alice@mail.com", null);
        Player bob = new Player("Bob", "bob@mail.com", null);
        List<Assignment> draw = List.of(new Assignment(alice, bob));

        // WHEN / THEN
        assertThatThrownBy(() -> smtpNotifier.notify(draw))
                .isInstanceOfSatisfying(NotificationException.class, exception ->
                        assertThat(exception.getFailedGivers()).containsExactly(alice))
                .hasMessageContaining("Alice");
    }
}
