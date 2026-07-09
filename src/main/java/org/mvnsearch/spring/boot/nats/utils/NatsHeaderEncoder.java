package org.mvnsearch.spring.boot.nats.utils;

import java.util.HashMap;
import java.util.Map;

public class NatsHeaderEncoder {

    private static final Map<String, Character> escapeMap = new HashMap<>();
    static {
        escapeMap.put("n", '\n');
        escapeMap.put("r", '\r');
        escapeMap.put("t", '\t');
        // others e.g. \" \' \\
    }

    /**
     * encode none ASCII or special ASCII chars
     */
    public static String encode(String input) {
        if (input == null) return null;
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (c > 0x7F) {
                // none ASCII -> \\uXXXX
                sb.append(String.format("\\u%04x", (int) c));
            } else if (c == '\n') {
                sb.append("\\n");
            } else if (c == '\r') {
                sb.append("\\r");
            } else if (c == '\t') {
                sb.append("\\t");
            } else if (c < 0x20 && c != ' ' && c != '\t') {
                // other control chars like  0x00-0x1F
                sb.append(String.format("\\u%04x", (int) c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * decode string to original content
     */
    public static String decode(String input) {
        if (input == null) return null;
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < input.length()) {
            char ch = input.charAt(i);
            if (ch == '\\') {
                if (i + 1 < input.length()) {
                    char next = input.charAt(i + 1);
                    if (next == 'u' && i + 5 < input.length()) {
                        // \\uXXXX
                        String hex = input.substring(i + 2, i + 6);
                        int codePoint = Integer.parseInt(hex, 16);
                        sb.append((char) codePoint);
                        i += 6;
                    } else if (next == 'n') {
                        sb.append('\n');
                        i += 2;
                    } else if (next == 'r') {
                        sb.append('\r');
                        i += 2;
                    } else if (next == 't') {
                        sb.append('\t');
                        i += 2;
                    } else {
                        // others like \\ or \ can be process here
                        sb.append(next);
                        i += 2;
                    }
                } else {
                    sb.append(ch);
                    i++;
                }
            } else {
                sb.append(ch);
                i++;
            }
        }
        return sb.toString();
    }
}