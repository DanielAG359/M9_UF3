import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    private static final int PORT = 7777;
    private static final String HOST = "localhost";
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public void connecta() throws IOException {
        socket = new Socket(HOST, PORT);
        System.out.println("Connectat a servidor en " + HOST + ":" + PORT);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(System.in));
    }
    public void envia(String missatge) {
        out.println(missatge);
        System.out.println("Enviat al servidor: " + missatge);
    }
    public void tanca() throws IOException {
        out.close();
        in.close();
        socket.close();
        System.out.println("Client tancat");
    }
    public static void main(String[] args) throws IOException {
        Client client = new Client();
        client.connecta();
        client.envia("Prova d'enviament 1");
        client.envia("Prova d'enviament 2");
        client.envia("Adeu!");
        System.out.println("Prem Enter per tancar el client...");
        client.in.readLine();
        client.tanca();
    }
}