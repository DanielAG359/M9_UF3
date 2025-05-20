import java.io.*;
import java.net.*;
import java.util.Hashtable;

public class ServidorXat {
    public static final int PORT = 9999;
    public static final String HOST = "localhost";
    public static final String MSG_SORTIR = "sortir";

    private ServerSocket serverSocket;
    private Hashtable<String, GestorClients> clients = new Hashtable<>();
    private boolean sortir = false;

    public void servidorAEscoltar() throws IOException {
        serverSocket = new ServerSocket(PORT);
        System.out.println("Servidor iniciat a " + HOST + ":" + PORT);
    }

    public void pararServidor() throws IOException {
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
            System.out.println("Servidor aturat.");
        }
    }

    public void finalitzarXat() throws IOException {
        enviarMissatgeGrup(MSG_SORTIR);
        clients.clear();
        sortir = true;
        System.out.println("Tancant tots els clients.");
    }

    public void afegirClient(GestorClients gc) throws IOException {
        clients.put(gc.getNom(), gc);
        enviarMissatgeGrup("DEBUG: multicast Entra: " + gc.getNom());
        System.out.println(gc.getNom() + " connectat.");
    }

    public void eliminarClient(String nom) {
        if (nom != null && clients.containsKey(nom)) {
            clients.remove(nom);
            System.out.println("Client eliminat: " + nom);
        }
    }

    public void enviarMissatgeGrup(String missatge) throws IOException {
        String msgFormatat = Missatge.getMissatgeGrup(missatge);
        for (GestorClients gc : clients.values()) {
            gc.enviarMissatgeFormatat(msgFormatat);
        }
    }

    public void enviarMissatgePersonal(String destinatari, String remitent, String missatge) throws IOException {
        GestorClients gc = clients.get(destinatari);
        if (gc != null) {
            String msgFormatat = Missatge.getMissatgePersonal(remitent, missatge);
            gc.enviarMissatgeFormatat(msgFormatat);
            System.out.println("Missatge personal per (" + destinatari + ") de (" + remitent + "): " + missatge);
        } else {
            System.out.println("Client destinatari (" + destinatari + ") no trobat.");
        }
    }

    public void gestionarClients() {
        while (!sortir) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connectat: " + clientSocket.getInetAddress());
                GestorClients gc = new GestorClients(clientSocket, this);
                gc.start();
            } catch (IOException e) {
                if (!sortir) {
                    System.err.println("Error acceptant client: " + e.getMessage());
                }
            }
        }
    }

    public static void main(String[] args) {
        ServidorXat servidor = new ServidorXat();
        try {
            servidor.servidorAEscoltar();
            servidor.gestionarClients();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                servidor.pararServidor();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
