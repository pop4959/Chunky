package org.popcraft.chunky.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class ShortTag extends Tag {
    private short value;

    protected ShortTag(final String name) {
        super(name);
    }

    public ShortTag(final String name, final short value) {
        super(name);
        this.value = value;
    }

    @Override
    public void read(final DataInput input) throws IOException {
        this.value = input.readShort();
    }

    @Override
    public void write(final DataOutput output) throws IOException {
        output.writeShort(value);
    }

    @Override
    public byte type() {
        return TagType.SHORT;
    }

    @Override
    public String typeName() {
        return "TAG_Short";
    }

    @Override
    public String print(final int level) {
        return "%s%s('%s'): %d".formatted(" ".repeat(level * Tag.INDENT), typeName(), name, value);
    }

    public short value() {
        return value;
    }

    public void value(final short value) {
        this.value = value;
    }
}
