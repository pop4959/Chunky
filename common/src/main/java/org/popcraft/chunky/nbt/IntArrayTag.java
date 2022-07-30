package org.popcraft.chunky.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

public class IntArrayTag extends Tag {
    private int[] value;

    protected IntArrayTag(final String name) {
        super(name);
    }

    public IntArrayTag(final String name, final int[] value) {
        super(name);
        this.value = value;
    }

    @Override
    public void read(final DataInput input) throws IOException {
        final int size = input.readInt();
        this.value = new int[size];
        for (int i = 0; i < size; ++i) {
            value[i] = input.readInt();
        }
    }

    @Override
    public void write(final DataOutput output) throws IOException {
        final int size = value.length;
        output.writeInt(size);
        for (int i : value) {
            output.writeInt(i);
        }
    }

    @Override
    public byte type() {
        return TagType.INT_ARRAY;
    }

    @Override
    public String typeName() {
        return "TAG_Int_Array";
    }

    @Override
    public String print(final int level) {
        return "%s%s('%s'): %s".formatted(" ".repeat(level * Tag.INDENT), typeName(), name, Arrays.toString(value));
    }

    public int[] value() {
        return value;
    }

    public void value(final int[] value) {
        this.value = value;
    }
}
