import java.io.*;
import java.net.*;

public class GestorClients extends Thread {
    private Socket client;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private ServidorXat servidor;
    private String nom;
    private boolean sortir = false;

    public GestorClients(Socket client, ServidorXat servidor) {
        this.client = client;
        this.servidor = servidor;
        try {
            this.out = new ObjectOutputStream(client.getOutputStream());
            this.in = new ObjectInputStream(client.getInputStream());
        } catch (IOException e) {
            System.err.println("Error creant fluxos");
        }
    }

    public String getNom() {
        return nom;
    }

    public void enviarMissatgeFormatat(String missatge) {
        try {
            out.writeObject(missatge);
            out.flush();
        } catch (IOException e) {
            System.err.println("Error enviant missatge a " + nom);
        }
    }

    public void enviarMissatge(String remitent, String missatge) {
        // No usado directamente, enviamos mensajes ya formateados con códigos
    }

    @Override
    public void run() {
        try {
            while (!sortir) {
                String missatge = (String) in.readObject();
                if (missatge == null) break;
                processaMissatge(missatge);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error rebent missatge de " + nom + ". Sortint...");
        } finally {
            sortir = true;
            servidor.eliminarClient(nom);
            try {
                client.close();
            } catch (IOException e) {
            }
            System.out.println("Client " + nom + " desconnectat.");
        }
    }

    public void processaMissatge(String missatge) throws IOException {
        String codi = Missatge.getCodiMissatge(missatge);
        String[] parts = Missatge.getPartsMissatge(missatge);

        if (codi == null || parts == null) {
            System.out.println("Missatge incorrecte rebut: " + missatge);
            return;
        }

        switch (codi) {
            case Missatge.CODI_CONECTAR:
                nom = parts[1];
                servidor.afegirClient(this);
                break;

            case Missatge.CODI_SORTIR_CLIENT:
                sortir = true;
                servidor.eliminarClient(nom);
                break;

            case Missatge.CODI_SORTIR_TOTS:
                sortir = true;
                servidor.finalitzarXat();
                break;

            case Missatge.CODI_MSG_PERSONAL:
                if (parts.length >= 3) {
                    String destinatari = parts[1];
                    String msg = parts[2];
                    servidor.enviarMissatgePersonal(destinatari, nom, msg);
                } else {
                    System.out.println("Missatge personal incorrecte: " + missatge);
                }
                break;

            case Missatge.CODI_MSG_GRUP:
                if (parts.length >= 2) {
                    String msgGrup = parts[1];
                    servidor.enviarMissatgeGrup(nom + ": " + msgGrup);
                }
                break;

            default:
                System.out.println("Codi no reconegut: " + missatge);
        }
    }
}
