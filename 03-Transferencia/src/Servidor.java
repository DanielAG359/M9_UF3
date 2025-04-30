import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor {
    private static final int PORT = 9999;
    private static final String HOST = "localhost";
    private ServerSocket serverSocket;

    public Socket connectar() throws IOException {
        serverSocket = new ServerSocket(PORT);
        System.out.println("Acceptant connexions en -> " + HOST + ":" + PORT);
        return serverSocket.accept();
    }

    public void tancarConnexio(Socket socket) throws IOException {
        if (socket != null) socket.close();
        if (serverSocket != null) serverSocket.close();
    }

    public void enviarFitxers(Socket socket) throws IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

        String nomFitxer = (String) in.readObject();
        System.out.println("Nom fitxer rebut: " + nomFitxer);

        Fitxer fitxer = new Fitxer(nomFitxer);
        byte[] contingut = fitxer.getContingut();
        if (contingut != null) {
            out.writeObject(contingut);
            out.flush();
            System.out.println("Fitxer enviat: " + nomFitxer);
        } else {
            System.out.println("Error llegint el fitxer.");
            out.writeObject(null);
        }
    }

    public static void main(String[] args) {
        Servidor servidor = new Servidor();
        try {
            Socket socket = servidor.connectar();
            servidor.enviarFitxers(socket);
            servidor.tancarConnexio(socket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
