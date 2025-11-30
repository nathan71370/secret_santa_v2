import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import static java.util.Collections.shuffle;


record Player(String name, String email, String previousTarget) {
    @Override
    public String toString() {
        return name; // Pour garder le nom dans les clés de Map si besoin
    }
}

void main() throws Exception {
    IO.println("Génération in progress...");

    Properties config = loadConfig();
    final String username = config.getProperty("mail.username");
    final String password = config.getProperty("mail.password");

    List<Player> players = getPlayers();

    Map<Player, Player> draw = draw(players);

    updatePlayersFile(draw);

    for (Map.Entry<Player, Player> entry : draw.entrySet()) {
        Player giver = entry.getKey();
        Player receiver = entry.getValue();

        sendEmail(username, password, giver, receiver);

        Thread.sleep(1000);
    }

    IO.println("Génération done.");
}


private static Properties loadConfig() throws IOException {
    Properties props = new Properties();
    try (InputStream input = new FileInputStream("src/main/resources/config.properties")) {
        props.load(input);
    }
    return props;
}

private static List<Player> getPlayers() throws IOException {
    BufferedReader bufferedReader = new BufferedReader(new FileReader("src/main/resources/draw/players.txt"));
    List<Player> players = new ArrayList<>();
    String line;
    while ((line = bufferedReader.readLine()) != null) {
        String[] parts = line.split(";");
        if (parts.length >= 2) {
            String previousTarget = parts.length >= 3 ? parts[2].trim() : null;
            players.add(new Player(parts[0].trim(), parts[1].trim(), previousTarget));
        }
    }
    return players;
}

private static Map<Player, Player> draw(List<Player> players) {
    int maxAttempts = 1000; // Sécurité pour éviter une boucle infinie

    for (int attempt = 0; attempt < maxAttempts; attempt++) {
        shuffle(players);

        if (isValidDistribution(players)) {
            Map<Player, Player> draw = new HashMap<>();
            for (int i = 0; i < players.size() - 1; i++) {
                draw.put(players.get(i), players.get(i + 1));
            }
            draw.put(players.getLast(), players.getFirst());
            return draw;
        }
    }

    throw new RuntimeException("Impossible de trouver un tirage valide après " + maxAttempts + " essais. Vérifiez les contraintes.");
}

private static boolean isValidDistribution(List<Player> players) {
    for (int i = 0; i < players.size(); i++) {
        Player giver = players.get(i);
        // Dans notre logique circulaire, le receveur est le suivant dans la liste
        // (ou le premier si on est à la fin)
        Player receiver = players.get((i + 1) % players.size());

        if (giver.previousTarget() != null && giver.previousTarget().equalsIgnoreCase(receiver.name())) {
            return false; // Ce tirage est invalide
        }
    }
    return true;
}

private static void updatePlayersFile(Map<Player, Player> draw) throws IOException {
    String playersFilePath = "src/main/resources/draw/players.txt";
    Path source = Path.of(playersFilePath);

    // 1. Archivage de l'ancien fichier
    int currentYear = Year.now().getValue();
    Path backup = Path.of("src/main/resources/draw/players-" + currentYear + ".old");

    // Copie le fichier actuel (avant modification) vers le backup
    Files.copy(source, backup, StandardCopyOption.REPLACE_EXISTING);
    IO.println("Ancien fichier archivé sous : " + backup);

    // 2. Mise à jour du fichier avec les nouveaux tirages
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(playersFilePath))) {
        for (Map.Entry<Player, Player> entry : draw.entrySet()) {
            Player giver = entry.getKey();
            Player receiver = entry.getValue();
            // Format : Nom;Email;NouvelleCible
            writer.write(giver.name() + ";" + giver.email() + ";" + receiver.name());
            writer.newLine();
        }
    }
    IO.println("Fichier players.txt mis à jour avec les cibles de cette année.");
}

private static void sendEmail(String username, String password, Player giver, Player receiver) {
    Properties prop = new Properties();
    prop.put("mail.smtp.host", "smtp.gmail.com"); // Ou smtp.outlook.com, etc.
    prop.put("mail.smtp.port", "587");
    prop.put("mail.smtp.auth", "true");
    prop.put("mail.smtp.starttls.enable", "true"); // TLS

    Session session = Session.getInstance(prop, new javax.mail.Authenticator() {
        protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
            return new javax.mail.PasswordAuthentication(username, password);
        }
    });

    try {
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username));
        message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(giver.email())
        );
        message.setSubject("🎄 Secret Santa : Ton tirage !");
        message.setText("Bonjour " + giver.name() + ",\n\n"
                + "Pour ce Secret Santa, tu devras offrir un cadeau à : " + receiver.name() + " !\n\n"
                + "Pour rappel, le budget défini est de 30€.\n\n"
                + "Joyeuses fêtes ! 🎅");

        Transport.send(message);

        IO.println("Email envoyé à " + giver.name());

    } catch (MessagingException e) {
        e.printStackTrace();
        IO.println("Erreur lors de l'envoi à " + giver.name());
    }
}
