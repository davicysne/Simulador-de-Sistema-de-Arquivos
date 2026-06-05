import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Journal {
    private static final String JOURNAL_FILE = "journal.log";
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void begin(String operation, String detail) {
        write("BEGIN", operation, detail);
    }

    public void commit(String operation, String detail) {
        write("COMMIT", operation, detail);
    }

    public void rollback(String operation, String detail) {
        write("ROLLBACK", operation, detail);
    }

    private void write(String status, String operation, String detail) {
        String timestamp = LocalDateTime.now().format(FORMATTER);

        try (PrintWriter writer = new PrintWriter(new FileWriter(JOURNAL_FILE, true))) {
            writer.println(timestamp + " | " + status + " | " + operation + " | " + detail);
        } catch (IOException e) {
            System.out.println("Erro ao escrever no journal: " + e.getMessage());
        }
    }
}
