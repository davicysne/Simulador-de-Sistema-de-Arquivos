import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class Directory implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private final Map<String, Directory> directories;
    private final Map<String, FSFile> files;

    public Directory(String name) {
        this.name = name;
        this.directories = new LinkedHashMap<>();
        this.files = new LinkedHashMap<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEmpty() {
        return directories.isEmpty() && files.isEmpty();
    }

    public boolean containsDirectory(String name) {
        return directories.containsKey(name);
    }

    public boolean containsFile(String name) {
        return files.containsKey(name);
    }

    public boolean containsAny(String name) {
        return containsDirectory(name) || containsFile(name);
    }

    public Directory getDirectory(String name) {
        return directories.get(name);
    }

    public FSFile getFile(String name) {
        return files.get(name);
    }

    public void addDirectory(Directory directory) {
        directories.put(directory.getName(), directory);
    }

    public void addFile(FSFile file) {
        files.put(file.getName(), file);
    }

    public Directory removeDirectory(String name) {
        return directories.remove(name);
    }

    public FSFile removeFile(String name) {
        return files.remove(name);
    }

    public Collection<Directory> getDirectories() {
        return directories.values();
    }

    public Collection<FSFile> getFiles() {
        return files.values();
    }
}
