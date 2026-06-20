package olc1.golite.ast.stm;

public class Param {
    private String id;
    private String tipo;

    public Param(String id, String tipo) {
        this.id = id;
        this.tipo = tipo;
    }

    public String getId() { return id; }
    public String getTipo() { return tipo; }
}
