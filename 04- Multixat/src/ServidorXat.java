import java.io.*;
import java.net.*;
import java.util.Hashtable;

public class ServidorXat {
    private static final int PORT = 9999;
    private static final String HOST = "localhost";
    private static final String MSG_SORTIR = "sortir";
    private ServerSocket serverSocket;
    private boolean sortir = false;
    private Hashtable<String, GestorClients> clients = new Hashtable<>();

    public void servidorAEscoltar() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Servidor iniciat a " + HOST + ":" + PORT);

            while (!sortir) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connectat: " + clientSocket.getInetAddress());

                GestorClients gestor = new GestorClients(clientSocket, this);
                gestor.start();
            }

        } catch (IOException e) {
            System.err.println("Error al servidor: " + e.getMessage());
        }
    }

    public void pararServidor() {
        try {
            sortir = true;
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                System.out.println("Servidor aturat.");
            }
        } catch (IOException e) {
            System.err.println("Error tancant el servidor: " + e.getMessage());
        }
    }

    public void afegirClient(GestorClients client) {
        clients.put(client.getNom(), client);
        System.out.println("DEBUG: multicast Entra: " + client.getNom());
    }

    public static void main(String[] args) {
        ServidorXat servidor = new ServidorXat();
        servidor.servidorAEscoltar();
    }

    public void enviarMissatgeGrup(String missatge) {
        for (GestorClients client : clients.values()) {
            client.enviarMissatge("Servidor", missatge);
        }
    }

    public void enviarMissatgePersonal(String destinatari, String remitent, String missatge) {
        GestorClients client = clients.get(destinatari);
        if (client != null) {
            client.enviarMissatge(remitent, missatge);
            System.out.println("Missatge personal per (" + destinatari + ") de (" + remitent + "): " + missatge);
        } else {
            System.out.println("Usuari " + destinatari + " no trobat.");
        }
    }
}
