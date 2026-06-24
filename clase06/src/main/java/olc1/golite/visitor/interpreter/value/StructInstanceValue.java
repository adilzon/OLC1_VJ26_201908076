package olc1.golite.visitor.interpreter.value;

import java.util.Map;

public record StructInstanceValue(String structName, Map<String, ValueWrapper> fields, int line, int column) implements ValueWrapper {
    @Override
    public String getTypeName() {
        return structName;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(structName + "{");
        int count = 0;
        for (Map.Entry<String, ValueWrapper> entry : fields.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue().toString());
            if (++count < fields.size()) {
                sb.append(", ");
            }
        }
        sb.append("}");
        return sb.toString();
    }
}
