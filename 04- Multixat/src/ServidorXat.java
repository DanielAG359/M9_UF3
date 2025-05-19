import java.io.*;
import java.net.*;
import java.util.Hashtable;

public class ServidorXat {
    private static final int PORT = 9999;
    private static final String HOST = "localhost";
    private ServerSocket serverSocket;
    private Hashtable<String, GestorClients> clients = new Hashtable<>();
    private boolean sortir = false;

    public void servidorAEscoltar() throws IOException {
        serverSocket = new ServerSocket(PORT);
        System.out.println("Servidor iniciat a " + HOST + ":" + PORT);
    }

    public void pararServidor() throws IOException {
        serverSocket.close();
        System.out.println("Servidor aturat.");
    }
    
    public void afegirClient(GestorClients gc) throws IOException {
        clients.put(gc.getNom(), gc);
        enviarMissatgeGrup("DEBUG: multicast Entra: " + gc.getNom());
    }

    public void eliminarClient(String nom) {
        if (clients.containsKey(nom)) {
            clients.remove(nom);
            System.out.println("Client eliminat: " + nom);
        }
    }

    public void enviarMissatgePersonal(String destinatari, String remitent, String missatge) throws IOException {
        GestorClients gc = clients.get(destinatari);
        if (gc != null) {
            gc.enviarMissatge(remitent, missatge);
        } else {
            System.out.println("WARNING: client destinatari \"" + destinatari + "\" no trobat.");
        }
    }

    public void enviarMissatgeGrup(String missatge) throws IOException {
        for (GestorClients client : clients.values()) {
            client.enviarMissatge(client.getNom(), missatge);
        }
    }
    public void finalitzarXat() throws IOException {
        enviarMissatgeGrup("Tancant tots els clients.");
        clients.clear();
        sortir = true;
    }
    public void gestionarClients() {
        while (!sortir) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connectat: " + clientSocket.getInetAddress());
                GestorClients gc = new GestorClients(clientSocket, this);
                gc.start();
                afegirClient(gc);
            } catch (IOException e) {
                System.out.println("Error al connectar el client.");
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