package fr.azrodorza;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    private static final Player ALICE = new Player("Alice", "alice@mail.com", null);
    private static final Player BOB = new Player("Bob", "bob@mail.com", null);
    private static final List<Player> PLAYERS = List.of(ALICE, BOB);
    private static final List<Assignment> DRAW = List.of(new Assignment(ALICE, BOB), new Assignment(BOB, ALICE));

    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private DrawService drawService;
    @Mock
    private Notifier notifier;

    @InjectMocks
    private GameService gameService;

    @Test
    void startDraw_shouldDrawSaveThenNotify() throws IOException {
        // GIVEN
        when(playerRepository.findAll()).thenReturn(PLAYERS);
        when(drawService.draw(PLAYERS)).thenReturn(DRAW);

        // WHEN
        gameService.startDraw();

        // THEN
        InOrder inOrder = inOrder(playerRepository, notifier);
        inOrder.verify(playerRepository).saveDraw(DRAW);
        inOrder.verify(notifier).notify(DRAW);
    }

    @Test
    void startDraw_shouldNotSaveNorNotifyWhenDrawFails() throws IOException {
        // GIVEN
        when(playerRepository.findAll()).thenReturn(PLAYERS);
        when(drawService.draw(PLAYERS)).thenThrow(new NoValidDrawException("tirage impossible"));

        // WHEN / THEN
        assertThatThrownBy(() -> gameService.startDraw())
                .isInstanceOf(NoValidDrawException.class);
        verify(playerRepository, org.mockito.Mockito.never()).saveDraw(any());
        verifyNoInteractions(notifier);
    }

    @Test
    void startDraw_shouldNotNotifyWhenSaveFails() throws IOException {
        // GIVEN
        when(playerRepository.findAll()).thenReturn(PLAYERS);
        when(drawService.draw(PLAYERS)).thenReturn(DRAW);
        doThrow(new IOException("disque plein")).when(playerRepository).saveDraw(DRAW);

        // WHEN / THEN
        assertThatThrownBy(() -> gameService.startDraw())
                .isInstanceOf(IOException.class);
        verifyNoInteractions(notifier);
    }

    @Test
    void startDraw_shouldNotDrawWhenPlayersCannotBeRead() throws IOException {
        // GIVEN
        when(playerRepository.findAll()).thenThrow(new IOException("fichier introuvable"));

        // WHEN / THEN
        assertThatThrownBy(() -> gameService.startDraw())
                .isInstanceOf(IOException.class);
        verifyNoInteractions(drawService, notifier);
    }
}
