package org.popcraft.chunky.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

public class LongArrayTag extends Tag {
    private long[] value;

    protected LongArrayTag(final String name) {
        super(name);
    }

    public LongArrayTag(final String name, final long[] value) {
        super(name);
        this.value = value;
    }

    @Override
    public void read(final DataInput input) throws IOException {
        final int size = input.readInt();
        this.value = new long[size];
        for (int i = 0; i < size; ++i) {
            value[i] = input.readLong();
        }
    }

    @Override
    public void write(final DataOutput output) throws IOException {
        final int size = value.length;
        output.writeLong(size);
        for (long i : value) {
            output.writeLong(i);
        }
    }

    @Override
    public byte type() {
        return TagType.LONG_ARRAY;
    }

    @Override
    public String typeName() {
        return "TAG_Long_Array";
    }

    @Override
    public String print(final int level) {
        return "%s%s('%s'): %s".formatted(" ".repeat(level * Tag.INDENT), typeName(), name, Arrays.toString(value));
    }

    public long[] value() {
        return value;
    }

    public void value(final long[] value) {
        this.value = value;
    }
}
