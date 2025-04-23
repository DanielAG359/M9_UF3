import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ClientXat {
    private static final int PORT = 9999;
    private static final String HOST = "localhost";
    private static final String MSG_SORTIR = "sortir";
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public void connecta() throws IOException {
        socket = new Socket(HOST, PORT);
        System.out.println("Client connectat a " + HOST + ":" + PORT);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
        System.out.println("Flux d'entrada i sortida creat.");
    }

    public void enviarMissatge(String missatge) throws IOException {
        out.writeObject(missatge);
        out.flush();
        System.out.println("Enviant missatge: " + missatge);
    }

    public void tancarClient() throws IOException {
        in.close();
        out.close();
        socket.close();
        System.out.println("Client tancat.");
    }

    public static void main(String[] args) {
        try {
            ClientXat client = new ClientXat();
            client.connecta();
            Scanner scanner = new Scanner(System.in);
            System.out.print("Escriu el teu nom: ");
            String nom = scanner.nextLine();
            client.enviarMissatge(nom);
            String missatge = "";
            while (!missatge.equals(MSG_SORTIR)){
                missatge = scanner.nextLine();
                client.enviarMissatge(missatge);
            } 
            scanner.close();
            client.tancarClient();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
