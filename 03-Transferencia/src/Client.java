import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static final String DIR_ARRIBADA = "C:\\tmp";
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public void connectar() throws IOException {
        socket = new Socket("localhost", 9999);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
    }

    public void rebreFitxers() throws IOException, ClassNotFoundException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Nom del fitxer a rebre: ");
        String ruta = scanner.nextLine();
        if ("sortir".equalsIgnoreCase(ruta)) {
            scanner.close();
            return;
        }
        out.writeObject(ruta);
        out.flush();

        byte[] contingut = (byte[]) in.readObject();
        if (contingut != null) {
            String nomFitxer = new File(ruta).getName();
            FileOutputStream fos = new FileOutputStream(DIR_ARRIBADA + "/" + nomFitxer);
            fos.write(contingut);
            fos.close();
            System.out.println("Fitxer guardat com: " + DIR_ARRIBADA + "/" + nomFitxer);
        } else {
            System.out.println("No s'ha rebut contingut.");
        }
        scanner.close();
    }

    public void tancarConnexio() throws IOException {
        if (socket != null) socket.close();
    }

    public static void main(String[] args) {
        Client client = new Client();
        try {
            client.connectar();
            client.rebreFitxers();
            client.tancarConnexio();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
