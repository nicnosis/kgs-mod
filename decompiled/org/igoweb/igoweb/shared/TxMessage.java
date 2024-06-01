/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.shared;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.io.OutputStream;
import org.igoweb.igoweb.shared.MsgTypesDown;
import org.igoweb.igoweb.shared.MsgTypesUp;

public class TxMessage
extends ByteArrayOutputStream
implements DataOutput {
    public static final int HEADER_SIZE = 4;
    public static final int CHANNEL_HEADER_SIZE = 8;
    public static final int MAX_UPLOAD_MESSAGE_SIZE = 8192;

    private TxMessage(short type) {
        this(type, 32);
    }

    public TxMessage(MsgTypesDown type) {
        this(type.id);
    }

    public TxMessage(MsgTypesUp type) {
        this(type.id);
    }

    private TxMessage(short type, int expectedLength) {
        super(expectedLength);
        this.writeShort(0);
        this.writeShort(type);
    }

    public TxMessage(MsgTypesDown type, int expectedLength) {
        this(type.id, expectedLength);
    }

    public TxMessage() {
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        int lcount = this.count - 2;
        this.buf[0] = (byte)(lcount >> 8);
        this.buf[1] = (byte)lcount;
        if (lcount >= Short.MAX_VALUE) {
            byte[] len = new byte[]{127, -1, (byte)(lcount >> 24), (byte)(lcount >> 16)};
            out.write(len);
        }
        out.write(this.buf, 0, this.count);
    }

    public byte[] closeAndGetBytes() {
        byte[] result = this.buf;
        this.buf = null;
        return result;
    }

    public byte[] closeAndGetExactBytes() {
        byte[] result = new byte[this.count];
        System.arraycopy(this.buf, 0, result, 0, this.count);
        this.buf = null;
        return result;
    }

    @Override
    public int size() {
        return this.count;
    }

    @Override
    public void writeUTF(String s) {
        int c;
        int i;
        int len = 0;
        for (i = 0; i < s.length(); ++i) {
            c = 0xFFFF & s.charAt(i);
            if (c > 0 && c < 127) {
                ++len;
                continue;
            }
            if (c == 0 || c < 2047) {
                len += 2;
                continue;
            }
            len += 3;
        }
        this.writeShort(len);
        for (i = 0; i < s.length(); ++i) {
            c = 0xFFFF & s.charAt(i);
            if (c > 0 && c < 127) {
                this.write(c);
                continue;
            }
            if (c == 0 || c < 2047) {
                this.write(0xC0 | c >> 6);
                this.write(0x80 | 0x3F & c);
                continue;
            }
            this.write(0xE0 | c >> 12);
            this.write(0x80 | 0x3F & c >> 6);
            this.write(0x80 | 0x3F & c);
        }
    }

    @Override
    public final void writeBoolean(boolean value) {
        this.write(value ? 1 : 0);
    }

    @Override
    public final void writeByte(int value) {
        this.write(value);
    }

    @Override
    public final void writeChar(int value) {
        this.write(value >> 8);
        this.write(value);
    }

    @Override
    public void writeShort(int value) {
        this.write(value >> 8);
        this.write(value);
    }

    @Override
    public void writeInt(int value) {
        this.write(value >> 24);
        this.write(value >> 16);
        this.write(value >> 8);
        this.write(value);
    }

    @Override
    public void writeLong(long value) {
        this.write((int)(value >> 56));
        this.write((int)(value >> 48));
        this.write((int)(value >> 40));
        this.write((int)(value >> 32));
        this.write((int)value >> 24);
        this.write((int)value >> 16);
        this.write((int)value >> 8);
        this.write((int)value);
    }

    @Override
    public void writeFloat(float value) {
        this.writeInt(Float.floatToIntBits(value));
    }

    @Override
    public void writeDouble(double value) {
        this.writeLong(Double.doubleToLongBits(value));
    }

    @Override
    public void writeBytes(String s) {
        for (int i = 0; i < s.length(); ++i) {
            this.write(s.charAt(i));
        }
    }

    @Override
    public void writeChars(String s) {
        for (int i = 0; i < s.length(); ++i) {
            this.writeChar(s.charAt(i));
        }
    }

    public void copyIn(DataInputStream in) throws IOException {
        int amt = in.available();
        while (this.buf.length - this.count < amt) {
            byte[] newBuf = new byte[this.buf.length * 2];
            System.arraycopy(this.buf, 0, newBuf, 0, this.count);
            this.buf = newBuf;
        }
        this.count += in.read(this.buf, this.count, amt);
    }

    @Override
    public String toString() {
        return "TxMessage[type=" + this.getType() + ", len=" + this.count + "]";
    }

    public short getType() {
        return (short)(this.buf[3] & 0xFF | this.buf[2] << 8);
    }

    public byte[] getBytes() {
        return this.buf;
    }
}
