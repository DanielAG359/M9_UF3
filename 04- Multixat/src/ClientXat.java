import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ClientXat extends Thread {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private boolean sortir = false;

    public void connecta() throws IOException {
        socket = new Socket("localhost", 9999);
        out = new ObjectOutputStream(socket.getOutputStream());
        System.out.println("Client connectat a localhost:9999");
        System.out.println("Flux d'entrada i sortida creat.");
    }

    public void enviarMissatge(String missatge) throws IOException {
        out.writeObject(missatge);
        out.flush();
        System.out.println("Enviant missatge: " + missatge);
    }

    public void tancarClient() {
        System.out.println("Tancant client...");
        try {
            if (in != null) {
                in.close();
                System.out.println("Flux d'entrada tancat.");
            }
        } catch (IOException e) {
            System.err.println("Error tancant flux d'entrada: " + e.getMessage());
        }

        try {
            if (out != null) {
                out.close();
                System.out.println("Flux de sortida tancat.");
            }
        } catch (IOException e) {
            System.err.println("Error tancant flux de sortida: " + e.getMessage());
        }

        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                System.out.println("Socket tancat.");
            }
        } catch (IOException e) {
            System.err.println("Error tancant socket: " + e.getMessage());
        }
    }


    @Override
    public void run() {
        try {
            in = new ObjectInputStream(socket.getInputStream());
            System.out.println("DEBUG: Iniciant rebuda de missatges...");
            while (!sortir) {
                String missatgeCru = (String) in.readObject();
                if (missatgeCru == null) break;

                String codi = Missatge.getCodiMissatge(missatgeCru);
                String[] parts = Missatge.getPartsMissatge(missatgeCru);

                if (codi == null || parts == null) {
                    System.out.println("Missatge rebut incorrecte.");
                    continue;
                }

                switch (codi) {
                    case Missatge.CODI_SORTIR_TOTS:
                        sortir = true;
                        System.out.println("Tancant per ordre del servidor...");
                        break;

                    case Missatge.CODI_MSG_PERSONAL:
                        if (parts.length >= 3) {
                            String remitent = parts[1];
                            String msg = parts[2];
                            System.out.println("Missatge de (" + remitent + "): " + msg);
                        }
                        break;

                    case Missatge.CODI_MSG_GRUP:
                        if (parts.length >= 2) {
                            String msgGrup = parts[1];
                            System.out.println("Missatge de grup: " + msgGrup);
                        }
                        break;

                    default:
                        System.out.println("Missatge desconegut: " + missatgeCru);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error rebent missatge. Sortint...");
        } finally {
            sortir = true;
            tancarClient();
        }
    }

    public void ajuda() {
        System.out.println("---------------------");
        System.out.println("Comandes disponibles:");
        System.out.println("1.- Conectar al servidor (primer pas obligatori)");
        System.out.println("2.- Enviar missatge personal");
        System.out.println("3.- Enviar missatge al grup");
        System.out.println("4.- (o línia en blanc)-> Sortir del client");
        System.out.println("5.- Finalitzar tothom");
        System.out.println("---------------------");
    }

    public String getLinea(Scanner scanner, String missatge, boolean obligatori) {
        String input = "";
        do {
            System.out.print(missatge);
            input = scanner.nextLine().trim();
        } while (obligatori && (input == null || input.isEmpty()));
        return input;
    }

    public static void main(String[] args) {
        ClientXat client = new ClientXat();
        Scanner scanner = new Scanner(System.in);
        boolean sortir = false;
        boolean connectat = false;

        try {
            client.connecta();
            client.start();
            client.ajuda();

            while (!sortir) {
                String linea = client.getLinea(scanner, "", false);
                if (linea.isEmpty()) linea = "4";

                switch (linea) {
                    case "1":
                        if (!connectat) {
                            String nom = client.getLinea(scanner, "Introdueix el nom: ", true);
                            String missatgeConectar = Missatge.getMissatgeConectar(nom);
                            client.enviarMissatge(missatgeConectar);
                            connectat = true;
                        } else {
                            System.out.println("Ja estàs connectat.");
                        }
                        break;

                    case "2":
                        if (!connectat) {
                            System.out.println("Primer connecta al servidor (opció 1).");
                            break;
                        }
                        String destinatari = client.getLinea(scanner, "Destinatari:: ", true);
                        String msgPersonal = client.getLinea(scanner, "Missatge a enviar: ", true);
                        String missatgePersonal = Missatge.getMissatgePersonal(destinatari, msgPersonal);
                        client.enviarMissatge(missatgePersonal);
                        break;

                    case "3":
                        if (!connectat) {
                            System.out.println("Primer connecta al servidor (opció 1).");
                            break;
                        }
                        String msgGrup = client.getLinea(scanner, "Missatge a enviar al grup: ", true);
                        String missatgeGrup = Missatge.getMissatgeGrup(msgGrup);
                        client.enviarMissatge(missatgeGrup);
                        break;

                    case "4":
                        if (!connectat) {
                            sortir = true;
                        } else {
                            String missatgeSortirClient = Missatge.getMissatgeSortirClient("Adéu");
                            client.enviarMissatge(missatgeSortirClient);
                            sortir = true;
                        }
                        break;

                    case "5":
                        if (!connectat) {
                            sortir = true;
                        } else {
                            String missatgeSortirTots = Missatge.getMissatgeSortirTots("Adéu");
                            client.enviarMissatge(missatgeSortirTots);
                            sortir = true;
                        }
                        break;

                    default:
                        System.out.println("Comanda no reconeguda.");
                }

                if (!sortir) {
                    client.ajuda();
                }
            }
        } catch (IOException e) {
            System.out.println("Error de connexió: " + e.getMessage());
        } finally {
            client.sortir = true;
            client.tancarClient();
            scanner.close();
        }
    }
}
