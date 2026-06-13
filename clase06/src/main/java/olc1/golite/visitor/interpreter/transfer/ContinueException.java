package olc1.golite.visitor.interpreter.transfer;

public class ContinueException extends RuntimeException {
    public ContinueException() {
        super("Continue statement");
    }
}
