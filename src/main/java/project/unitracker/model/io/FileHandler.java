package project.unitracker.model.io;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// La classe che si occupa di gestire file
// Legge, scrive e aggiunge contenuto a un file, e può verificare l'esistenza di certi file all'interno dei dati
// del progetto, creandoli da zero o usando dei backup contenuti in resources/backup/ per avere dei contenuti di default
public class FileHandler {

    // Estrae cartelle e nome del file
    // Utile per verificare l'esistenza basandosi sugli URL
    static Pattern pattern = Pattern.compile("^(.+)/([^/]+)$");

    // Verifica l'esistenza del file
    // Se non esiste, lo crea copiandolo da BACKUP_URL
    // Questa viene chiamata da DatabaseHandler per verificare l'esistenza del database, e in caso
    public static void checkFileExistenceCopy(String URL, String BACKUP_URL) {
        Matcher matcher = pattern.matcher(URL);

        if ( ! matcher.matches() )
            return;

        Path dir = Paths.get(matcher.group(1));
        Path file = dir.resolve(matcher.group(2));

        try {
            // Se non esiste la cartella, la crea
            if (!Files.exists(dir))
                Files.createDirectories(dir);

            // Se il file esiste e non è corrotto, non fa nulla
            if ( Files.exists(file) && Files.size(file) > 0 )
                return;


            try (InputStream in = FileHandler.class.getResourceAsStream(BACKUP_URL)) {
                if (in == null) throw new IllegalStateException("File di backup non trovato");

                Files.copy(in, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Verifica l'esistenza del file (e della cartella)
    // In loro assenza vengono create con un testo di default, se fornito
    public static void checkFileExistence(String URL, List<String> defaultText) {
        Matcher matcher = pattern.matcher(URL);

        if ( ! matcher.matches() )
            return;

        Path dir = Paths.get(matcher.group(1));
        Path file = dir.resolve(matcher.group(2));

        try {
            // Se non esiste la cartella, la crea
            if (!Files.exists(dir))
                Files.createDirectories(dir);

            // Se il file non esiste o è corrotto, viene creato
            // Se è stato fornito un text di default, il file viene riempito con quello
            if ( !Files.exists(file) ) {
                Files.createFile(file);

                if (defaultText != null)
                    writeFile(URL, defaultText);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // La parte comune a tutti i metodi che scrivono su file
    private static void writerWrite(BufferedWriter writer, List<String> text) throws IOException {
        for (String line : text ) {
            writer.write(line);
            writer.newLine();
        }
    }

    // Questa viene chiamata solo quando si vuole fare append veloce di una riga
    public static boolean appendFile(String URL, List<String> text) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(URL, true))) {
            writerWrite(writer, text);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // Scrive su un file basandosi su un URL fornito
    public static boolean writeFile(String URL, List<String> text) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(URL))) {
            writerWrite(writer, text);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // Scrive su un file basandosi sul File fornito
    public static boolean writeFile(File file, List<String> text) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writerWrite(writer, text);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // La parte comune a tutti i metodi che leggono un file
    // Popola una List<String> con il contenuto del file, dove ogni elemento rappresenta una riga del file
    private static List<String> readerRead(BufferedReader reader) throws IOException {
        List<String> fileContent = new ArrayList<>();

        String line;

        while ( (line = reader.readLine()) != null )
            fileContent.add(line);

        return fileContent;
    }

    // Legge un file basandosi su un URL fornito
    public static List<String> readFile(String URL) {
        try (BufferedReader reader = new BufferedReader(new FileReader(URL))) {
            return readerRead(reader);
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Legge un file basandosi sul File fornito
    public static List<String> readFile(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return readerRead(reader);
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
