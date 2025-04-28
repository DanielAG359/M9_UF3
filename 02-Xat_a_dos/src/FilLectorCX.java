import java.io.ObjectInputStream;

public class FilLectorCX extends Thread {
    private final ObjectInputStream in;
    private static final String MSG_SORTIR = "sortir";

    public FilLectorCX(ObjectInputStream in) {
        this.in = in;
    }

    public void run() {
        try {
            String missatge = "";
            while (!missatge.equals(MSG_SORTIR)) {
                missatge = (String) in.readObject();
                System.out.println("Missatge ('sortir' per tancar): Rebut: " + missatge);
            } 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
