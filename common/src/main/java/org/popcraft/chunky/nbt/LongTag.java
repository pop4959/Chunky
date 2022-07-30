package org.popcraft.chunky.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class LongTag extends Tag {
    private long value;

    protected LongTag(final String name) {
        super(name);
    }

    public LongTag(final String name, final long value) {
        super(name);
        this.value = value;
    }

    @Override
    public void read(final DataInput input) throws IOException {
        this.value = input.readLong();
    }

    @Override
    public void write(final DataOutput output) throws IOException {
        output.writeLong(value);
    }

    @Override
    public byte type() {
        return TagType.LONG;
    }

    @Override
    public String typeName() {
        return "TAG_Long";
    }

    @Override
    public String print(final int level) {
        return "%s%s('%s'): %d".formatted(" ".repeat(level * Tag.INDENT), typeName(), name, value);
    }

    public long value() {
        return value;
    }

    public void value(final long value) {
        this.value = value;
    }
}
