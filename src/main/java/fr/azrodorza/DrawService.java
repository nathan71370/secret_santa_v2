package fr.azrodorza;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.util.Collections.shuffle;

@RequiredArgsConstructor
public class DrawService {
    private static final int MAX_ATTEMPTS = 1000;

    private final Random random;

    public List<Assignment> draw(List<Player> players) {
        if (players.size() < 2) {
            throw new IllegalArgumentException("Il faut au moins 2 joueurs pour faire un tirage (reçu : " + players.size() + ").");
        }

        List<Player> shuffled = new ArrayList<>(players);

        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            shuffle(shuffled, random);

            if (isValidDistribution(shuffled)) {
                List<Assignment> draw = new ArrayList<>();
                for (int i = 0; i < shuffled.size(); i++) {
                    draw.add(new Assignment(shuffled.get(i), shuffled.get((i + 1) % shuffled.size())));
                }
                return draw;
            }
        }
        throw new NoValidDrawException("Impossible de trouver un tirage valide après " + MAX_ATTEMPTS + " essais. Vérifiez les contraintes.");
    }

    private boolean isValidDistribution(List<Player> players) {
        for (int i = 0; i < players.size(); i++) {
            Player giver = players.get(i);
            Player receiver = players.get((i + 1) % players.size());

            if (giver.previousTarget() != null && giver.previousTarget().equalsIgnoreCase(receiver.name())) {
                return false;
            }
        }
        return true;
    }
}
