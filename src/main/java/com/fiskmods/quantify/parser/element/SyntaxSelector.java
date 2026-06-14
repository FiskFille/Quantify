package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.parser.SyntaxParser;
import com.fiskmods.quantify.parser.tree.Statement;

public class SyntaxSelector {
    public static SyntaxParser<? extends Statement> selectSyntax(final Token next) {
        return switch (next.type()) {
            case IMPORT -> ImportParser.INSTANCE;
            case INPUT -> InputParser.INSTANCE;
            case PUBLIC -> VariableParser.PUBLIC;
            case IF -> IfStatementParser.PARSER;
            case INTERPOLATE -> InterpolateStatementParser.PARSER;
            case NAMESPACE -> NamespaceParser.INSTANCE;
            case VAR -> VariableParser.LOCAL;
            case CONST -> ConstDefParser.INSTANCE;
            case FUNC -> FunctionDefParser.PARSER;
            case RETURN -> ReturnParser.INSTANCE;

            default -> IdentifierParser::parseStatementIdentifier;
        };
    }
}
