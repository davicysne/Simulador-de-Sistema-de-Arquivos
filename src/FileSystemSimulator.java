import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class FileSystemSimulator {
    private static final String DATA_FILE = "filesystem.dat";

    private Directory root;
    private final Journal journal;

    public FileSystemSimulator() {
        this.journal = new Journal();
        this.root = loadState();
    }

    public void createFile(String path, String content) {
        execute("CREATE", path, () -> {
            PathInfo info = resolveParent(path);
            validateName(info.name);

            if (info.parent.containsAny(info.name)) {
                throw new IllegalArgumentException("Ja existe arquivo ou diretorio com esse nome.");
            }

            info.parent.addFile(new FSFile(info.name, content));
        });
    }

    public void copyFile(String sourcePath, String destinationPath) {
        execute("COPY", sourcePath + " -> " + destinationPath, () -> {
            PathInfo source = resolveParent(sourcePath);
            FSFile sourceFile = source.parent.getFile(source.name);

            if (sourceFile == null) {
                throw new IllegalArgumentException("Arquivo de origem nao encontrado.");
            }

            PathInfo destination = resolveParent(destinationPath);
            validateName(destination.name);

            if (destination.parent.containsAny(destination.name)) {
                throw new IllegalArgumentException("Destino ja existe.");
            }

            destination.parent.addFile(sourceFile.copy(destination.name));
        });
    }

    public void deleteFile(String path) {
        execute("DELETE", path, () -> {
            PathInfo info = resolveParent(path);

            if (info.parent.removeFile(info.name) == null) {
                throw new IllegalArgumentException("Arquivo nao encontrado.");
            }
        });
    }

    public void rename(String path, String newName) {
        execute("RENAME", path + " -> " + newName, () -> {
            validateName(newName);
            PathInfo info = resolveParent(path);

            if (info.parent.containsAny(newName)) {
                throw new IllegalArgumentException("Ja existe arquivo ou diretorio com o novo nome.");
            }

            FSFile file = info.parent.removeFile(info.name);
            if (file != null) {
                file.setName(newName);
                info.parent.addFile(file);
                return;
            }

            Directory directory = info.parent.removeDirectory(info.name);
            if (directory != null) {
                directory.setName(newName);
                info.parent.addDirectory(directory);
                return;
            }

            throw new IllegalArgumentException("Arquivo ou diretorio nao encontrado.");
        });
    }

    public void makeDirectory(String path) {
        execute("MKDIR", path, () -> {
            PathInfo info = resolveParent(path);
            validateName(info.name);

            if (info.parent.containsAny(info.name)) {
                throw new IllegalArgumentException("Ja existe arquivo ou diretorio com esse nome.");
            }

            info.parent.addDirectory(new Directory(info.name));
        });
    }

    public void removeDirectory(String path) {
        execute("RMDIR", path, () -> {
            if ("/".equals(path)) {
                throw new IllegalArgumentException("A raiz nao pode ser removida.");
            }

            PathInfo info = resolveParent(path);
            Directory directory = info.parent.getDirectory(info.name);

            if (directory == null) {
                throw new IllegalArgumentException("Diretorio nao encontrado.");
            }

            if (!directory.isEmpty()) {
                throw new IllegalArgumentException("Diretorio nao esta vazio.");
            }

            info.parent.removeDirectory(info.name);
        });
    }

    public void list(String path) {
        try {
            Directory directory = resolveDirectory(path);

            if (directory.getDirectories().isEmpty() && directory.getFiles().isEmpty()) {
                System.out.println("(vazio)");
                return;
            }

            for (Directory child : directory.getDirectories()) {
                System.out.println("[DIR]  " + child.getName());
            }

            for (FSFile file : directory.getFiles()) {
                System.out.println("[FILE] " + file.getName());
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    public void tree() {
        System.out.println("/");
        printTree(root, "");
    }

    private void printTree(Directory directory, String indent) {
        for (Directory child : directory.getDirectories()) {
            System.out.println(indent + "|-- " + child.getName() + "/");
            printTree(child, indent + "|   ");
        }

        for (FSFile file : directory.getFiles()) {
            System.out.println(indent + "|-- " + file.getName());
        }
    }

    // Envolve operacoes modificadoras com BEGIN, COMMIT/ROLLBACK e persistencia.
    private void execute(String operation, String detail, FileSystemOperation action) {
        journal.begin(operation, detail);

        try {
            action.run();
            saveState();
            journal.commit(operation, detail);
            System.out.println("Operacao realizada com sucesso.");
        } catch (Exception e) {
            journal.rollback(operation, detail);
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private Directory loadState() {
        File file = new File(DATA_FILE);

        if (!file.exists()) {
            return new Directory("/");
        }

        try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(file))) {
            return (Directory) input.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Nao foi possivel carregar filesystem.dat. Criando sistema novo.");
            return new Directory("/");
        }
    }

    private void saveState() throws IOException {
        try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            output.writeObject(root);
        }
    }

    private Directory resolveDirectory(String path) {
        validateAbsolutePath(path);

        if ("/".equals(path)) {
            return root;
        }

        Directory current = root;
        String[] parts = splitPath(path);

        for (String part : parts) {
            current = current.getDirectory(part);

            if (current == null) {
                throw new IllegalArgumentException("Diretorio inexistente: " + path);
            }
        }

        return current;
    }

    private PathInfo resolveParent(String path) {
        validateAbsolutePath(path);

        if ("/".equals(path)) {
            throw new IllegalArgumentException("Operacao invalida para a raiz.");
        }

        int lastSlash = path.lastIndexOf('/');
        String parentPath = lastSlash == 0 ? "/" : path.substring(0, lastSlash);
        String name = path.substring(lastSlash + 1);

        validateName(name);
        return new PathInfo(resolveDirectory(parentPath), name);
    }

    private void validateAbsolutePath(String path) {
        if (path == null || path.isBlank() || !path.startsWith("/")) {
            throw new IllegalArgumentException("O caminho deve ser absoluto e iniciar com '/'.");
        }
    }

    private void validateName(String name) {
        if (name == null || name.isBlank() || name.contains("/")) {
            throw new IllegalArgumentException("Nome invalido.");
        }
    }

    private String[] splitPath(String path) {
        return path.substring(1).split("/");
    }

    private interface FileSystemOperation {
        void run() throws Exception;
    }

    private static class PathInfo {
        private final Directory parent;
        private final String name;

        private PathInfo(Directory parent, String name) {
            this.parent = parent;
            this.name = name;
        }
    }
}
