package org.delia.migrationparser.parser;

public class Reader {
    private final String input;
    private int nextIndex = 0;

    public Reader(String src) {
        this.input = src;
    }

    public char peek() {
        return peek(0);
    }

    public char peek(int k) {
        return input.charAt(nextIndex + k);
    }

    public char consume() {
        char ch = input.charAt(nextIndex++);
        return ch;
    }

    public boolean isEof() {
        int n = input.length();
        return nextIndex >= n;
    }

    public boolean nextIs(char target) {
        if (isEof()) return false;
        char ch = peek(0);
        return ch == target;
    }

    @Override
    public String toString() {
        if (isEof()) {
            return String.format("%d: eof", nextIndex);
        }
        return String.format("%d '%c'", nextIndex, peek());
    }

    public char safePeek(int i) {
        if (nextIndex + i >= input.length()) {
            return '\0';
        }
        return peek(i);
    }
}
