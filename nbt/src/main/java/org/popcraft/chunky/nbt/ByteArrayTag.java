package org.popcraft.chunky.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

public class ByteArrayTag extends Tag {
    private byte[] value;

    protected ByteArrayTag(final String name) {
        super(name);
    }

    public ByteArrayTag(final String name, final byte[] value) {
        super(name);
        this.value = value;
    }

    @Override
    public void read(final DataInput input) throws IOException {
        final int size = input.readInt();
        this.value = new byte[size];
        input.readFully(value);
    }

    @Override
    public void write(final DataOutput output) throws IOException {
        output.writeInt(value.length);
        output.write(value);
    }

    @Override
    public byte type() {
        return TagType.BYTE_ARRAY;
    }

    @Override
    public String typeName() {
        return "TAG_Byte_Array";
    }

    @Override
    public String print(final int level) {
        return "%s%s('%s'): %s".formatted(" ".repeat(level * Tag.INDENT), typeName(), name, Arrays.toString(value));
    }

    public byte[] value() {
        return value;
    }

    public void value(final byte[] value) {
        this.value = value;
    }
}
