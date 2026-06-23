package olc1.golite.visitor.interpreter.transfer;

import olc1.golite.visitor.interpreter.value.ValueWrapper;

public class ReturnException extends RuntimeException {
    private final ValueWrapper value;

    public ReturnException(ValueWrapper value) {
        super("Return statement");
        this.value = value;
    }

    public ValueWrapper getValue() {
        return value;
    }
}
