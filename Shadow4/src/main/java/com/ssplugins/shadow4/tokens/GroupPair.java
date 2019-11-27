package com.ssplugins.shadow4.tokens;

public enum GroupPair {
    
    PAREN('(', ')'),
    BRACKET('{', '}'),
    SQUARE('[', ']');
    
    private char openChar;
    private char closeChar;
    
    GroupPair(char openChar, char closeChar) {
        this.openChar = openChar;
        this.closeChar = closeChar;
    }
    
    public static GroupPair from(char c) {
        switch (c) {
            case '(':
            case ')':
                return PAREN;
            case '{':
            case '}':
                return BRACKET;
            case '[':
            case ']':
                return SQUARE;
            default:
                return null;
        }
    }
    
    public static boolean isPair(char c) {
        return from(c) != null;
    }
    
    public char getOpenChar() {
        return openChar;
    }
    
    public char getCloseChar() {
        return closeChar;
    }
    
}
