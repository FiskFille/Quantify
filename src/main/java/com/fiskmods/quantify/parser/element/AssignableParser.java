package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.jvm.assignable.VarInfo;
import com.fiskmods.quantify.jvm.assignable.VarType;
import com.fiskmods.quantify.lexer.token.Operator;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxParser;
import com.fiskmods.quantify.parser.tree.Assignable;
import com.fiskmods.quantify.parser.tree.Value;

class AssignableParser {
    public static <T extends VarAddress> SyntaxParser<Assignable> parse(final VarType<T> type, final int modifiers) {
        return (parser, context) -> {
            final T var = nextVariable(parser, context, type, modifiers);
            return parse(var, modifiers).accept(parser, context);
        };
    }

    public static <T extends VarAddress> SyntaxParser<Assignable> parse(final T firstVar, final int modifiers) {
        return (parser, context) -> {
            if (parser.isNext(TokenClass.COMMA)) {
                return VariableParser.parseList(firstVar, modifiers).accept(parser, context);
            }
            return firstVar;
        };
    }

    @SuppressWarnings("unchecked")
    public static <T extends VarAddress> T nextVariable(final QtfParser parser, final SyntaxContext context, final VarType<T> type, final int modifiers) throws QtfParseException {
        final boolean isNegated = isNegated(parser, (modifiers & VarInfo.DEFINITION) != 0);
        final T var = VariableParser.refOrDef(type, modifiers).accept(parser, context);
        if (isNegated) {
            return (T) Value.negate(var);
        }
        return var;
    }

    private static boolean isNegated(final QtfParser parser, final boolean isDefinition) {
        // Negated LHS variables are not allowed in assignments
        if (!isDefinition && parser.isNext(TokenClass.OPERATOR, Operator.SUB)) {
            parser.clearPeekedToken();
            return true;
        }
        return false;
    }
}
