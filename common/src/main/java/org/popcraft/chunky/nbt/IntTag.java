package org.popcraft.chunky.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class IntTag extends Tag {
    private int value;

    protected IntTag(final String name) {
        super(name);
    }

    public IntTag(final String name, final int value) {
        super(name);
        this.value = value;
    }

    @Override
    public void read(final DataInput input) throws IOException {
        this.value = input.readInt();
    }

    @Override
    public void write(final DataOutput output) throws IOException {
        output.writeInt(value);
    }

    @Override
    public byte type() {
        return TagType.INT;
    }

    @Override
    public String typeName() {
        return "TAG_Int";
    }

    @Override
    public String print(final int level) {
        return "%s%s('%s'): %d".formatted(" ".repeat(level * Tag.INDENT), typeName(), name, value);
    }

    public int value() {
        return value;
    }

    public void value(final int value) {
        this.value = value;
    }
}
