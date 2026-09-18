package fr.azrodorza;

import java.util.List;

public class ConsoleNotifier implements Notifier {
    @Override
    public void notify(List<Assignment> assignments) {
        IO.println("Mode console : aucun mail envoyé. Tirage :");
        for (Assignment assignment : assignments) {
            IO.println("  " + assignment.giver().name() + " -> " + assignment.receiver().name());
        }
    }
}
