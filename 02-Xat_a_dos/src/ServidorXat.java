import java.io.*;
import java.net.*;

public class ServidorXat {
    private static final int PORT = 9999;
    private static final String HOST = "localhost";
    private static final String MSG_SORTIR = "sortir";
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public void iniciarServidor() throws IOException {
        serverSocket = new ServerSocket(PORT);
        System.out.println("Servidor iniciat a " + HOST + ":" + PORT);
    }

    public void pararServidor() throws IOException {
        serverSocket.close();
        System.out.println("Servidor aturat.");
    }

    public String getNom() throws IOException, ClassNotFoundException {
        in = new ObjectInputStream(clientSocket.getInputStream());
        out = new ObjectOutputStream(clientSocket.getOutputStream());
        String nom = (String) in.readObject();
        System.out.println("Nom rebut: " + nom);
        return nom;
    }

    public static void main(String[] args) {
        try {
            ServidorXat servidor = new ServidorXat();
            servidor.iniciarServidor();
            servidor.clientSocket = servidor.serverSocket.accept();
            System.out.println("Client connectat: " + servidor.clientSocket.getInetAddress());
            String nom = servidor.getNom();
            BufferedReader consola = new BufferedReader(new InputStreamReader(System.in));
            String missatge = "";
            while(!missatge.equals(MSG_SORTIR)){
                missatge = consola.readLine();
                servidor.out.writeObject(missatge);
                servidor.out.flush();
            }
            servidor.clientSocket.close();
            servidor.pararServidor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
