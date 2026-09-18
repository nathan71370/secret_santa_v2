package fr.azrodorza;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DrawServiceTest {

    private static final Player ALICE = new Player("Alice", "alice@mail.com", null);
    private static final Player BOB = new Player("Bob", "bob@mail.com", null);
    private static final Player CHLOE = new Player("Chloe", "chloe@mail.com", null);
    private static final Player DAVID = new Player("David", "david@mail.com", null);

    @Test
    void draw_shouldMakeEveryPlayerGiveOnceAndReceiveOnce() {
        // GIVEN
        List<Player> players = List.of(ALICE, BOB, CHLOE, DAVID);
        DrawService drawService = new DrawService(new Random(42));

        // WHEN
        List<Assignment> draw = drawService.draw(players);

        // THEN
        assertThat(draw).hasSize(4);
        assertThat(draw).extracting(Assignment::giver).containsExactlyInAnyOrderElementsOf(players);
        assertThat(draw).extracting(Assignment::receiver).containsExactlyInAnyOrderElementsOf(players);
    }

    @Test
    void draw_shouldNeverAssignAPlayerToThemself() {
        // GIVEN
        List<Player> players = List.of(ALICE, BOB, CHLOE, DAVID);

        // WHEN
        List<Assignment> assignments = IntStream.range(0, 100)
                .mapToObj(seed -> new DrawService(new Random(seed)).draw(players))
                .flatMap(List::stream)
                .toList();

        // THEN
        assertThat(assignments).allSatisfy(assignment ->
                assertThat(assignment.giver()).isNotEqualTo(assignment.receiver()));
    }

    @Test
    void draw_shouldNeverAssignLastYearTarget() {
        // GIVEN
        Player alice = new Player("Alice", "alice@mail.com", "Bob");
        Player bob = new Player("Bob", "bob@mail.com", "Chloe");
        Player chloe = new Player("Chloe", "chloe@mail.com", "David");
        Player david = new Player("David", "david@mail.com", "Alice");
        List<Player> players = List.of(alice, bob, chloe, david);

        // WHEN
        List<Assignment> assignments = IntStream.range(0, 100)
                .mapToObj(seed -> new DrawService(new Random(seed)).draw(players))
                .flatMap(List::stream)
                .toList();

        // THEN
        assertThat(assignments).allSatisfy(assignment ->
                assertThat(assignment.receiver().name()).isNotEqualTo(assignment.giver().previousTarget()));
    }

    @Test
    void draw_shouldCompareLastYearTargetIgnoringCase() {
        // GIVEN
        Player alice = new Player("Alice", "alice@mail.com", "BOB");
        Player bob = new Player("Bob", "bob@mail.com", null);
        DrawService drawService = new DrawService(new Random(42));

        // WHEN / THEN
        assertThatThrownBy(() -> drawService.draw(List.of(alice, bob)))
                .isInstanceOf(NoValidDrawException.class);
    }

    @Test
    void draw_shouldThrowWhenNoValidDrawExists() {
        // GIVEN
        Player alice = new Player("Alice", "alice@mail.com", "Bob");
        Player bob = new Player("Bob", "bob@mail.com", "Alice");
        DrawService drawService = new DrawService(new Random(42));

        // WHEN / THEN
        assertThatThrownBy(() -> drawService.draw(List.of(alice, bob)))
                .isInstanceOf(NoValidDrawException.class)
                .hasMessageContaining("1000 essais");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void draw_shouldThrowWhenLessThanTwoPlayers(int playerCount) {
        // GIVEN
        List<Player> players = List.of(ALICE, BOB).subList(0, playerCount);
        DrawService drawService = new DrawService(new Random(42));

        // WHEN / THEN
        assertThatThrownBy(() -> drawService.draw(players))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("au moins 2 joueurs");
    }

    @Test
    void draw_shouldReturnSameDrawWithSameSeed() {
        // GIVEN
        List<Player> players = List.of(ALICE, BOB, CHLOE, DAVID);

        // WHEN
        List<Assignment> firstDraw = new DrawService(new Random(42)).draw(players);
        List<Assignment> secondDraw = new DrawService(new Random(42)).draw(players);

        // THEN
        assertThat(firstDraw).isEqualTo(secondDraw);
    }

    @Test
    void draw_shouldNotModifyGivenList() {
        // GIVEN
        List<Player> players = new ArrayList<>(List.of(ALICE, BOB, CHLOE, DAVID));
        DrawService drawService = new DrawService(new Random(42));

        // WHEN
        drawService.draw(players);

        // THEN
        assertThat(players).containsExactly(ALICE, BOB, CHLOE, DAVID);
    }
}
