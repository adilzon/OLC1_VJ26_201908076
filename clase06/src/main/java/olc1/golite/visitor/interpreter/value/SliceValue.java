package olc1.golite.visitor.interpreter.value;

import java.util.List;

public record SliceValue(List<ValueWrapper> value, String elementType, int line, int column) implements ValueWrapper {
    
    @Override
    public String getTypeName() {
        return "[]" + elementType;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < value.size(); i++) {
            sb.append(value.get(i).toString());
            if (i < value.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
