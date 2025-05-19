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
            System.err.println("Error creant fluxos del client.");
        }
    }

    public String getNom() {
        return nom;
    }

    public void enviarMissatge(String remitent, String missatge) {
        try {
            out.writeObject("Missatge de (" + remitent + "): " + missatge);
            out.flush();
        } catch (IOException e) {
            System.err.println("Error enviant missatge a " + nom);
        }
    }

    @Override
    public void run() {
        try {
            Object input;
            while (!sortir && (input = in.readObject()) != null) {
                String missatge = input.toString();
                processaMissatge(missatge);
            }
        } catch (Exception e) {
            System.err.println("Error amb client " + nom + ". Tancant connexió.");
        } finally {
            try {
                client.close();
            } catch (IOException e) {}
        }
    }
    public void processaMissatge(String missatge) {
        String codi = Missatge.getCodiMissatge(missatge);
        String[] parts = Missatge.getPartsMissatge(missatge);

        if (codi == null || parts == null) return;

        switch (codi) {
            case Missatge.CODI_CONECTAR:
                nom = parts[1];
                servidor.afegirClient(this);
                break;

            case Missatge.CODI_MSG_PERSONAL:
                String destinatari = parts[1];
                String msg = parts[2];
                servidor.enviarMissatgePersonal(destinatari, nom, msg);
                break;

            case Missatge.CODI_MSG_GRUP:
                String missatgeGrup = parts[1];
                servidor.enviarMissatgeGrup(nom + ": " + missatgeGrup);
                break;

            default:
                System.out.println("Codi no reconegut: " + missatge);
        }
    }
}
