package org.popcraft.chunky.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class FloatTag extends Tag {
    private float value;

    protected FloatTag(final String name) {
        super(name);
    }

    public FloatTag(final String name, final float value) {
        super(name);
        this.value = value;
    }

    @Override
    public void read(final DataInput input) throws IOException {
        this.value = input.readFloat();
    }

    @Override
    public void write(final DataOutput output) throws IOException {
        output.writeFloat(value);
    }

    @Override
    public byte type() {
        return TagType.FLOAT;
    }

    @Override
    public String typeName() {
        return "TAG_Float";
    }

    @Override
    public String print(final int level) {
        return "%s%s('%s'): %f".formatted(" ".repeat(level * Tag.INDENT), typeName(), name, value);
    }

    public float value() {
        return value;
    }

    public void value(final float value) {
        this.value = value;
    }
}
