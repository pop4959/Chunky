package org.popcraft.chunky.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class ByteTag extends Tag {
    private byte value;

    protected ByteTag(final String name) {
        super(name);
    }

    public ByteTag(final String name, final byte value) {
        super(name);
        this.value = value;
    }

    @Override
    public void read(final DataInput input) throws IOException {
        this.value = input.readByte();
    }

    @Override
    public void write(final DataOutput output) throws IOException {
        output.writeByte(value);
    }

    @Override
    public byte type() {
        return TagType.BYTE;
    }

    @Override
    public String typeName() {
        return "TAG_Byte";
    }

    @Override
    public String print(final int level) {
        return "%s%s('%s'): %d".formatted(" ".repeat(level * Tag.INDENT), typeName(), name, value);
    }

    public byte value() {
        return value;
    }

    public void value(final byte value) {
        this.value = value;
    }
}
