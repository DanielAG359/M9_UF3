import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Fitxer {
    private String nom;
    private byte[] contingut;

    public Fitxer(String nom) {
        this.nom = nom;
    }

    public byte[] getContingut() throws IOException {
        File file = new File(nom);
        if (!file.exists()) return null;
        contingut = Files.readAllBytes(file.toPath());
        return contingut;
    }

    public String getNom() {
        return nom;
    }
}
