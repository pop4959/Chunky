package org.popcraft.chunky.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class DoubleTag extends Tag {
    private double value;

    protected DoubleTag(final String name) {
        super(name);
    }

    public DoubleTag(final String name, final double value) {
        super(name);
        this.value = value;
    }

    @Override
    public void read(final DataInput input) throws IOException {
        this.value = input.readDouble();
    }

    @Override
    public void write(final DataOutput output) throws IOException {
        output.writeDouble(value);
    }

    @Override
    public byte type() {
        return TagType.DOUBLE;
    }

    @Override
    public String typeName() {
        return "TAG_Double";
    }

    @Override
    public String print(final int level) {
        return "%s%s('%s'): %f".formatted(" ".repeat(level * Tag.INDENT), typeName(), name, value);
    }

    public double value() {
        return value;
    }

    public void value(final double value) {
        this.value = value;
    }
}
