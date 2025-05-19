import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ClientXat {
    private static final int PORT = 9999;
    private static final String HOST = "localhost";
    private static final String MSG_SORTIR = "sortir";
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private boolean sortir = false;

    public void connecta() throws IOException {
        socket = new Socket(HOST, PORT);
        System.out.println("Client connectat a " + HOST + ":" + PORT);
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        System.out.println("Flux de sortida creat.");
    }

    public void enviarMissatge(String missatge) throws IOException {
        if (out != null) {
            out.writeObject(missatge);
            out.flush();
            System.out.println("Enviant missatge: " + missatge);
        }
    }

    public void tancarClient() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.out.println("Error tancant client: " + e.getMessage());
        }
        System.out.println("Client tancat.");
    }

    public void ajuda() {
        System.out.println("---------------------");
        System.out.println("Comandes disponibles:");
        System.out.println("1.- Conectar al servidor (primer pass obligatori)");
        System.out.println("2.- Enviar missatge personal");
        System.out.println("3.- Enviar missatge al grup");
        System.out.println("4.- (o línia en blanc)-> Sortir del client");
        System.out.println("5.- Finalitzar tothom");
        System.out.println("---------------------");
    }

    public void rebreMissatges() {
        try {
            in = new ObjectInputStream(socket.getInputStream());
            System.out.println("Flux d'entrada creat.");
            while (!sortir) {
                String missatgeCru = (String) in.readObject();
                String codi = Missatge.getCodiMissatge(missatgeCru);
                String[] parts = Missatge.getPartsMissatge(missatgeCru);

                if (codi == null || parts == null) continue;

                switch (codi) {
                    case Missatge.CODI_SORTIR_TOTS:
                        sortir = true;
                        break;
                    case Missatge.CODI_MSG_PERSONAL:
                        System.out.println("Missatge de (" + parts[1] + "): " + parts[2]);
                        break;
                    case Missatge.CODI_MSG_GRUP:
                        System.out.println("Missatge grupal: " + parts[1]);
                        break;
                    default:
                        System.out.println("Error: codi desconegut.");
                }
            }
        } catch (Exception e) {
            System.out.println("Error rebent missatge. Sortint...");
        }
    }

    public void mostraAjuda() {
        System.out.println("---------------------");
        System.out.println("Comandes disponibles:");
        System.out.println("1.- Conectar al servidor (primer pass obligatori)");
        System.out.println("2.- Enviar missatge personal");
        System.out.println("3.- Enviar missatge al grup");
        System.out.println("4.- (o línia en blanc)-> Sortir del client");
        System.out.println("5.- Finalitzar tothom");
        System.out.println("---------------------");
    }

    public String getLinea(Scanner scanner, String missatge, boolean obligatori) {
        String input = null;
        do {
            System.out.print(missatge);
            input = scanner.nextLine();
        } while (obligatori && (input == null || input.trim().isEmpty()));
        return input;
    }

    public static void main(String[] args) {
        ClientXat client = new ClientXat();
        Scanner sc = new Scanner(System.in);

        try {
            client.connecta();
            client.in = new ObjectInputStream(client.socket.getInputStream());
            System.out.println("Flux d'entrada creat.");
            Thread lectura = new Thread(() -> {
                try {
                    System.out.println("DEBUG: Iniciant rebuda de missatges...");
                    while (!client.sortir) {
                        String missatgeCru = (String) client.in.readObject();
                        String codi = Missatge.getCodiMissatge(missatgeCru);
                        String[] parts = Missatge.getPartsMissatge(missatgeCru);

                        if (codi == null || parts == null) continue;

                        switch (codi) {
                            case Missatge.CODI_SORTIR_TOTS:
                                client.sortir = true;
                                break;
                            case Missatge.CODI_MSG_PERSONAL:
                                System.out.println("Missatge de (" + parts[1] + "): " + parts[2]);
                                break;
                            case Missatge.CODI_MSG_GRUP:
                                System.out.println("[GRUP]: " + parts[1]);
                                break;
                            default:
                                System.out.println("Error: codi desconegut.");
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error rebent missatge. Sortint...");
                }
            });

            lectura.start();

            client.ajuda();

            while (!client.sortir) {
                String opcio = client.getLinea(sc, "Opció: ", false);
                if (opcio.isEmpty()) {
                    client.sortir = true;
                    client.enviarMissatge(Missatge.getMissatgeSortirClient("Adéu"));
                    break;
                }

                switch (opcio) {
                    case "1":
                        String nom = client.getLinea(sc, "Introdueix el nom: ", true);
                        client.enviarMissatge(Missatge.getMissatgeConectar(nom));
                        break;
                    case "2":
                        String dest = client.getLinea(sc, "Destinatari:: ", true);
                        String msgP = client.getLinea(sc, "Missatge a enviar: ", true);
                        client.enviarMissatge(Missatge.getMissatgePersonal(dest, msgP));
                        break;
                    case "3":
                        String msgG = client.getLinea(sc, "Missatge per al grup: ", true);
                        client.enviarMissatge(Missatge.getMissatgeGrup(msgG));
                        break;
                    case "4":
                        client.sortir = true;
                        client.enviarMissatge(Missatge.getMissatgeSortirClient("Adéu"));
                        break;
                    case "5":
                        client.sortir = true;
                        client.enviarMissatge(Missatge.getMissatgeSortirTots("Adéu"));
                        break;
                    default:
                        System.out.println("Opció no vàlida.");
                        client.ajuda();
                }
            }

            lectura.join();
            client.tancarClient();

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            sc.close();
        }
    }

}
