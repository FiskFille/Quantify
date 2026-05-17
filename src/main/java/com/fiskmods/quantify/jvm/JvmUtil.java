package com.fiskmods.quantify.jvm;

import com.fiskmods.quantify.lexer.token.Operator;
import com.fiskmods.quantify.library.QtfMath;
import com.fiskmods.quantify.parser.tree.Expression;
import com.fiskmods.quantify.parser.tree.NumLiteral;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class JvmUtil {
    public static void iconst(final MethodVisitor mv, final int i) {
        switch (i) {
            case 0 -> mv.visitInsn(ICONST_0);
            case 1 -> mv.visitInsn(ICONST_1);
            case 2 -> mv.visitInsn(ICONST_2);
            case 3 -> mv.visitInsn(ICONST_3);
            case 4 -> mv.visitInsn(ICONST_4);
            case 5 -> mv.visitInsn(ICONST_5);
            default -> mv.visitLdcInsn(i);
        }
    }

    public static boolean optimizePow(final MethodVisitor mv, final JvmTreeVisitor visitor, final Expression left, final double exponent) {
        if (VarAddress.isVar(left)) {
            if (exponent == 2) {
                visitor.visitTree(left);
                visitor.visitTree(left);
                Operator.MUL.apply(mv);
                return true;
            } if (exponent == 3) {
                visitor.visitTree(left);
                visitor.visitTree(left);
                visitor.visitTree(left);
                Operator.MUL.apply(mv);
                Operator.MUL.apply(mv);
                return true;
            }
        } else if (!(left instanceof NumLiteral)) {
            if (exponent == 2) {
                visitor.visitTree(left);
                QtfMath.SQUARE.visit(mv, INVOKESTATIC, false);
                return true;
            } if (exponent == 3) {
                visitor.visitTree(left);
                QtfMath.CUBE.visit(mv, INVOKESTATIC, false);
                return true;
            }
        }
        return false;
    }
}
