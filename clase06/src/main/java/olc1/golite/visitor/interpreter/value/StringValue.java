package olc1.golite.visitor.interpreter.value;

public record StringValue(String value, int line, int column) implements ValueWrapper {

    @Override
    public String getTypeName() {
        return "string";
    }

    @Override
    public String toString() {
        String inner = value.substring(1, value.length() - 1);
        return unescape(inner);
    }

    private String unescape(String s) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char next = s.charAt(i + 1);
                switch (next) {
                    case 'n': sb.append('\n'); break;
                    case 't': sb.append('\t'); break;
                    case 'r': sb.append('\r'); break;
                    case '"': sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    default: sb.append('\\').append(next); break;
                }
                i += 2;
            } else {
                sb.append(c);
                i++;
            }
        }
        return sb.toString();
    }
}
