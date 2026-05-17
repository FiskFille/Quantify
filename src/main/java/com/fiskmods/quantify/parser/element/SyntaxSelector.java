package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxParser;

public class SyntaxSelector {
    public static SyntaxParser<?> selectSyntax(SyntaxContext context, Token next) throws QtfParseException {
        return switch (next.type()) {
            case IMPORT -> checkScope(ImportParser.INSTANCE, context, next);
            case INPUT -> checkScope(InputParser.INSTANCE, context, next);
            case PUBLIC -> checkScope(VariableParser.PUBLIC, context, next);
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

    private static SyntaxParser<?> checkScope(SyntaxParser<?> syntaxParser, SyntaxContext context, Token next)
            throws QtfParseException {
        if (context.scope().isInnerScope()) {
            throw new QtfParseException("Illegal token '" + next + "'",
                    "unavailable in inner scopes", next.range());
        }
        return syntaxParser;
    }
}
