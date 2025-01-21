package ru.sber.df.epmp.netty_postgres.server.postgres.tcp.domain;

public enum FrontendMessageTypeJ {
    Bind('B'),
    Close('C'),
    CopyData('d'),
    CopyDone('c'),
    CopyFail('f'),
    Describe('D'),
    Execute('E'),
    Flush('H'),
    FunctionCall('F'),
    Parse('P'),
    Password('p'),
    Query('Q'),
    Sync('S'),
    Terminate('X'),
    Unknown('_');

    private final char id;

    FrontendMessageTypeJ(final char id) {
        this.id = id;
    }

    public static FrontendMessageTypeJ fromId(final char id) {
        for (final FrontendMessageTypeJ messageType : values()) {
            if (messageType.getId() == id) {
                return messageType;
            }
        }
        return Unknown;
    }

    public char getId() {
        return id;
    }
}
