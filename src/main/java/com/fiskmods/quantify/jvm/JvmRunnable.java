package com.fiskmods.quantify.jvm;

import com.fiskmods.quantify.QtfScript;
import com.fiskmods.quantify.member.VarReference;

public abstract class JvmRunnable implements QtfScript {
    private final double[] output;

    protected JvmRunnable(final double[] output) {
        this.output = output;
    }

    protected abstract void run(double[] input, double[] output);

    @Override
    public void run(final double... input) {
        run(input, output);
    }

    @Override
    public void print() {
        for (int i = 0; i < output.length; ++i) {
            System.out.println("V " + i + ": " + output[i]);
        }
    }

    public VarReference resolve(final int id) {
        if (id < 0 || id >= output.length) {
            return VarReference.EMPTY;
        }
        return new VarReference() {
            @Override
            public double get() {
                return output[id];
            }

            @Override
            public void set(final double value) {
                output[id] = value;
            }
        };
    }
}
