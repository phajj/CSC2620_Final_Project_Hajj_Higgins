package server.parser;

// Interpreter pattern interface
public interface CommandExpression {

    String interpret(String context);
}
