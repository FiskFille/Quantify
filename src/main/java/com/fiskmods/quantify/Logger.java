package com.fiskmods.quantify;

public interface Logger {
    void logError(String message);

    void logError(String message, int pos);
}
