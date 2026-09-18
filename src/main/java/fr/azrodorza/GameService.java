package fr.azrodorza;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.List;


@RequiredArgsConstructor
public class GameService {
    private final PlayerRepository playerRepository;
    private final DrawService drawService;
    private final Notifier notifier;

    public void startDraw() throws IOException {
        List<Assignment> draw = drawService.draw(playerRepository.findAll());
        playerRepository.saveDraw(draw);
        notifier.notify(draw);
    }
}
