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

    default void init(MethodVisitor mv) {
        set(mv, Value.ZERO);
    }

    void modify(MethodVisitor mv, Value value, Operator op);

    void lerp(MethodVisitor mv, Value value, Value progress, boolean rotational);

    static <T extends Value & Assignable> SyntaxParser<Assignable> parse(VarType<T> type, int modifiers) {
        return (parser, context) -> {
            VarAddress<T> var = nextVariable(parser, type, modifiers);
            return parser.next(parse(var, modifiers));
        };
    }

    static SyntaxParser<Assignable> parse(VarAddress<?> firstVar, int modifiers) {
        return (parser, context) -> {
            if (parser.isNext(TokenClass.COMMA)) {
                return parser.next(VariableList.parse(firstVar, modifiers));
            }
            return firstVar;
        };
    }

    static <T extends Value & Assignable> VarAddress<T> nextVariable(
            QtfParser parser, VarType<T> type, int modifiers) throws QtfParseException {

        boolean isNegated = isNegated(parser, (modifiers & VarInfo.DEFINITION) != 0);
        VarAddress<T> var = parser.next(VariableParser.refOrDef(type, modifiers));
        if (isNegated) {
            return VarAddress.create(var, true);
        }
        return var;
    }

    private static boolean isNegated(QtfParser parser, boolean isDefinition) {
        // Negated LHS variables are not allowed in assignments
        if (!isDefinition && parser.isNext(TokenClass.OPERATOR, Operator.SUB)) {
            parser.clearPeekedToken();
            return true;
        }
        return false;
    }
}
