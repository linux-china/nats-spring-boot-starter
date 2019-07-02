package org.mvnsearch.spring.boot.nats.streaming;

import java.util.UUID;

/**
 * NATS Streaming clientId random generator
 *
 * @author wisni
 */
public class NatsStreamingClientIdGenerator {

    private NatsStreamingClientIdGenerator() {
    }

    private static final char[] DIGISTS = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r',
        's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
        'U', 'V', 'W', 'X', 'Y', 'Z', '-', '_'
    };

    public static String getId() {
        UUID u = UUID.randomUUID();

        return toIDString(u.getMostSignificantBits()) + toIDString(u.getLeastSignificantBits());
    }

    private static String toIDString(long i) {
        char[] buf = new char[32];
        int z = 64;
        int cp = 32;

        long b = z - 1L;

        do {
            buf[--cp] = NatsStreamingClientIdGenerator.DIGISTS[(int) (i & b)];
            i >>>= 6;
        } while (i != 0);

        return new String(buf, cp, (32 - cp));
    }
}