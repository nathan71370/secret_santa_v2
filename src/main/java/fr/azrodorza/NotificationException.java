package fr.azrodorza;

import lombok.Getter;

import java.util.List;

@Getter
public class NotificationException extends RuntimeException {
    private final List<Player> failedGivers;

    public NotificationException(List<Player> failedGivers) {
        super("Échec de l'envoi pour : " + failedGivers
                + ". Le tirage est déjà enregistré dans players.txt : ne relance pas le programme,"
                + " envoie-leur leur tirage manuellement.");
        this.failedGivers = List.copyOf(failedGivers);
    }

}
