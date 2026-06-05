import java.io.Serializable;

public class FSFile implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private String content;

    public FSFile(String name, String content) {
        this.name = name;
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public FSFile copy(String newName) {
        return new FSFile(newName, content);
    }
}
