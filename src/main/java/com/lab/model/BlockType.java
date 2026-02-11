package com.lab.model;

public enum BlockType {
    START,      // Початок потоку
    ASSIGN,     // V1 = V2 або V1 = C
    CONDITION,  // V == C або V < C
    INPUT,      // INPUT V
    PRINT,      // PRINT V
    STOP        // Кінець
}