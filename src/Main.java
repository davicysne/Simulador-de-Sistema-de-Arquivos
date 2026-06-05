import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        FileSystemSimulator simulator = new FileSystemSimulator();
        Scanner scanner = new Scanner(System.in);

        System.out.println("FileSystemSimulator iniciado. Digite 'help' para ver os comandos.");

        while (true) {
            System.out.print("fs> ");

            if (!scanner.hasNextLine()) {
                break;
            }

            String line = scanner.nextLine().trim();

            if (line.isEmpty()) {
                continue;
            }

            List<String> argsList = parseCommand(line);
            String command = argsList.get(0).toLowerCase();

            try {
                switch (command) {
                    case "mkdir":
                        requireArgs(argsList, 2, "Uso: mkdir /caminho");
                        simulator.makeDirectory(argsList.get(1));
                        break;
                    case "create":
                        requireArgs(argsList, 3, "Uso: create /arquivo.txt \"Conteudo\"");
                        simulator.createFile(argsList.get(1), argsList.get(2));
                        break;
                    case "copy":
                        requireArgs(argsList, 3, "Uso: copy /origem.txt /destino.txt");
                        simulator.copyFile(argsList.get(1), argsList.get(2));
                        break;
                    case "rename":
                        requireArgs(argsList, 3, "Uso: rename /caminho novoNome");
                        simulator.rename(argsList.get(1), argsList.get(2));
                        break;
                    case "ls":
                        requireArgs(argsList, 2, "Uso: ls /diretorio");
                        simulator.list(argsList.get(1));
                        break;
                    case "delete":
                        requireArgs(argsList, 2, "Uso: delete /arquivo.txt");
                        simulator.deleteFile(argsList.get(1));
                        break;
                    case "rmdir":
                        requireArgs(argsList, 2, "Uso: rmdir /diretorio");
                        simulator.removeDirectory(argsList.get(1));
                        break;
                    case "tree":
                        simulator.tree();
                        break;
                    case "help":
                        printHelp();
                        break;
                    case "exit":
                        System.out.println("Encerrando FileSystemSimulator.");
                        scanner.close();
                        return;
                    default:
                        System.out.println("Comando desconhecido. Digite 'help' para ver os comandos.");
                }
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }

        scanner.close();
    }

    // Permite conteudo entre aspas, como: create /a.txt "Texto com espacos".
    private static List<String> parseCommand(String line) {
        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean insideQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char character = line.charAt(i);

            if (character == '"') {
                insideQuotes = !insideQuotes;
            } else if (Character.isWhitespace(character) && !insideQuotes) {
                if (current.length() > 0) {
                    parts.add(current.toString());
                    current.setLength(0);
                }
            } else {
                current.append(character);
            }
        }

        if (current.length() > 0) {
            parts.add(current.toString());
        }

        return parts;
    }

    private static void requireArgs(List<String> argsList, int expected, String usage) {
        if (argsList.size() != expected) {
            throw new IllegalArgumentException(usage);
        }
    }

    private static void printHelp() {
        System.out.println("Comandos disponiveis:");
        System.out.println("  mkdir /diretorio");
        System.out.println("  create /diretorio/arquivo.txt \"Conteudo do arquivo\"");
        System.out.println("  copy /origem.txt /destino.txt");
        System.out.println("  rename /caminho novoNome");
        System.out.println("  ls /diretorio");
        System.out.println("  delete /arquivo.txt");
        System.out.println("  rmdir /diretorio");
        System.out.println("  tree");
        System.out.println("  help");
        System.out.println("  exit");
    }
}
