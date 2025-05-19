import java.io.*;
import java.net.*;
import java.util.*;

public class ServidorXat {
    private static final int PORT = 9999;
    private static final String HOST = "localhost";
    private static final String MSG_SORTIR = "sortir";
    ServerSocket serverSocket;
    private Hashtable<String, GestorClients> clients = new Hashtable<>();
    boolean sortir = false;

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

    public synchronized void afegirClient(GestorClients client) {
        clients.put(client.getNom(), client);
        enviarMissatgeGrup(Missatge.getMissatgeGrup("Entra: " + client.getNom()));
    }

    public synchronized void eliminarClient(String nom) {
        if (clients.containsKey(nom)) {
            clients.remove(nom);
            enviarMissatgeGrup(Missatge.getMissatgeGrup(nom + " ha sortit."));
        }
    }

    public synchronized void enviarMissatgeGrup(String missatge) {
        for (GestorClients gc : clients.values()) {
            gc.enviarMissatge("Servidor", missatge);
        }
    }

    public synchronized void enviarMissatgePersonal(String desti, String remitent, String missatge) {
        GestorClients receptor = clients.get(desti);
        if (receptor != null) {
            receptor.enviarMissatge(remitent, missatge);
        }
    }

    public synchronized void finalitzarXat() {
        enviarMissatgeGrup(Missatge.getMissatgeSortirTots("Adéu"));
        clients.clear();
        sortir = true;
        try {
            pararServidor();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Tancant tots els clients.");
    }

    public static void main(String[] args) {
        ServidorXat servidor = new ServidorXat();
        try {
            servidor.servidorAEscoltar();
            while (!servidor.sortir) {
                Socket clientSocket = servidor.serverSocket.accept();
                System.out.println("Client connectat: " + clientSocket.getInetAddress());
                GestorClients gc = new GestorClients(clientSocket, servidor);
                gc.start();
            }
        } catch (IOException e) {
            System.out.println("Servidor finalitzat.");
        } finally {
            try {
                servidor.pararServidor();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}