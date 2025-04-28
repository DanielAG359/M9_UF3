import java.io.ObjectInputStream;

public class FilServidorXat extends Thread {
    private final String nom;
    private final ObjectInputStream in;
    private static final String MSG_SORTIR = "sortir";

    public FilServidorXat(String nom, ObjectInputStream in) {
        this.nom = nom;
        this.in = in;
    }

    public void run() {
        try {
            System.out.println("Fil de " + nom + " iniciat");
            String missatge = "";
            while (!missatge.equals(MSG_SORTIR)){
                missatge = (String) in.readObject();
                System.out.println("Missatge ('sortir' per tancar): Rebut: " + missatge);
            }
            System.out.println("Fil de xat finalitzat.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
