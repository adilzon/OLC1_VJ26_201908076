package olc1.golite.ast;

import olc1.golite.visitor.Visitor;

// Interfaz base para todos los nodos de mi Árbol de Sintaxis Abstracta (AST).
// Define el método accept para implementar la doble distribución del patrón Visitor.
public interface ASTNode {
    <T> T accept(Visitor<T> visitor);
}
