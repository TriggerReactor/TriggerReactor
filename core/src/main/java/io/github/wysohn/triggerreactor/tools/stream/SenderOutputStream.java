package io.github.wysohn.triggerreactor.tools.stream;

import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;

import java.io.IOException;
import java.io.OutputStream;

public class SenderOutputStream extends OutputStream {
    private final ICommandSender sender;

    /**
     * The internal memory for the written bytes.
     */
    private String mem = "";

    public SenderOutputStream(ICommandSender sender) {
        this.sender = sender;
    }

    /**
     * https://stackoverflow.com/questions/6995946/log4j-how-do-i-redirect-an-outputstream-or-writer-to-loggers-writers
     *
     * @param i
     * @throws IOException
     */
    @Override
    public void write(int i) throws IOException {
        byte[] bytes = new byte[1];
        bytes[0] = (byte) (i & 0xff);
        mem = mem + new String(bytes);

        if (mem.charAt(mem.length() - 1) == '\n') {
            mem = mem.substring(0, mem.length() - 1);
            flush();
        }
    }

    /**
     * Flushes the output stream.
     */
    public void flush() {
        sender.sendMessage(mem);
        mem = "";
    }
}
