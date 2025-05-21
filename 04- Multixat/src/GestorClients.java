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
            out = new ObjectOutputStream(client.getOutputStream());
            in  = new ObjectInputStream(client.getInputStream());
        } catch (IOException e) {
            System.err.println("Error creant fluxos");
        }
    }

    public String getNom() {
        return nom;
    }

    public void enviarMissatgeFormatat(String msg) throws IOException {
        out.writeObject(msg);
        out.flush();
    }

    @Override
    public void run() {
        try {
            while (!sortir) {
                String raw = (String) in.readObject();
                if (raw == null) break;
                processaMissatge(raw);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error rebent missatge de " + nom + ". Sortint...");
        } finally {
            sortir = true;
            servidor.eliminarClient(nom);
            try { client.close(); } catch (IOException e){}
            System.out.println("Client " + nom + " desconnectat.");
        }
    }

    private void processaMissatge(String raw) throws IOException {
        String codi = Missatge.getCodiMissatge(raw);
        String[] parts = Missatge.getPartsMissatge(raw);
        if (codi == null || parts == null) return;

        switch (codi) {
            case Missatge.CODI_CONECTAR:
                nom = parts[1];
                servidor.afegirClient(this);
                break;

            case Missatge.CODI_MSG_PERSONAL:
                servidor.enviarMissatgePersonal(parts[1], nom, parts[2]);
                break;

            case Missatge.CODI_MSG_GRUP:
                servidor.enviarMissatgeGrup(nom + ": " + parts[1]);
                break;

            case Missatge.CODI_SORTIR_CLIENT:
                sortir = true;
                servidor.eliminarClient(nom);
                break;

            case Missatge.CODI_SORTIR_TOTS:
                sortir = true;
                servidor.finalitzarXat();
                break;

            default:
                System.out.println("Codi no reconegut: " + raw);
        }
    }
}
