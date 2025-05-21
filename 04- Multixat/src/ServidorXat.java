import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
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

    public synchronized void finalitzarXat() {
        try {
            String msg = Missatge.getMissatgeSortirTots(MSG_SORTIR);
            for (GestorClients gc : clients.values()) {
                gc.enviarMissatgeFormatat(msg);
            }
            clients.clear();
            sortir = true;
            System.out.println("Tancant tots els clients.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void afegirClient(GestorClients gc) throws IOException {
        clients.put(gc.getNom(), gc);
        String entrada = "Entra: " + gc.getNom();
        String msg = Missatge.getMissatgeGrup(entrada);
        for (GestorClients c : clients.values()) {
            c.enviarMissatgeFormatat(msg);
        }
        System.out.println(gc.getNom() + " connectat.");
    }

    public synchronized void eliminarClient(String nom) {
        if (nom != null && clients.containsKey(nom)) {
            clients.remove(nom);
            System.out.println("Client eliminat: " + nom);
        }
    }

    public synchronized void enviarMissatgeGrup(String text) throws IOException {
        String msg = Missatge.getMissatgeGrup(text);
        for (GestorClients gc : clients.values()) {
            gc.enviarMissatgeFormatat(msg);
        }
    }

    public synchronized void enviarMissatgePersonal(String dest, String remet, String text) throws IOException {
        GestorClients gc = clients.get(dest);
        if (gc != null) {
            String msg = Missatge.getMissatgePersonal(remet, text);
            gc.enviarMissatgeFormatat(msg);
            System.out.println("Missatge personal per (" + dest + ") de (" + remet + "): " + text);
        } else {
            System.out.println("Client destinatari (" + dest + ") no trobat.");
        }
    }

    public void gestionarClients() {
        while (!sortir) {
            try {
                Socket sock = serverSocket.accept();
                System.out.println("Client connectat: " + sock.getInetAddress());
                GestorClients gc = new GestorClients(sock, this);
                gc.start();
            } catch (IOException e) {
                if (!sortir) System.err.println("Error acceptant client: " + e.getMessage());
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
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
