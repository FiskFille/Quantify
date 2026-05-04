package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.jvm.JvmFunction;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.jvm.assignable.VarInfo;
import com.fiskmods.quantify.jvm.assignable.VarType;
import com.fiskmods.quantify.lexer.token.Operator;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxParser;
import org.objectweb.asm.MethodVisitor;

public interface Assignable extends JvmFunction {
    void set(MethodVisitor mv, Value value);

    default void init(final MethodVisitor mv) {
        set(mv, Value.ZERO);
    }

    void modify(MethodVisitor mv, Value value, Operator op);

    void lerp(MethodVisitor mv, Value value, Value progress, boolean rotational);

    static <T extends VarAddress> SyntaxParser<Assignable> parse(final VarType<T> type, final int modifiers) {
        return (parser, context) -> {
            final T var = nextVariable(parser, type, modifiers);
            return parser.next(parse(var, modifiers));
        };
    }

    static <T extends VarAddress> SyntaxParser<Assignable> parse(final T firstVar, final int modifiers) {
        return (parser, context) -> {
            if (parser.isNext(TokenClass.COMMA)) {
                return parser.next(VariableList.parse(firstVar, modifiers));
            }
            return firstVar;
        };
    }

    @SuppressWarnings("unchecked")
    static <T extends VarAddress> T nextVariable(final QtfParser parser, final VarType<T> type, final int modifiers) throws QtfParseException {
        final boolean isNegated = isNegated(parser, (modifiers & VarInfo.DEFINITION) != 0);
        final T var = parser.next(VariableParser.refOrDef(type, modifiers));
        if (isNegated) {
            return (T) var.negate();
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
