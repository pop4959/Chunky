package org.popcraft.chunky.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class StringTag extends Tag {
    private String value;

    protected StringTag(final String name) {
        super(name);
    }

    public StringTag(final String name, final String value) {
        super(name);
        this.value = value;
    }

    @Override
    public void read(final DataInput input) throws IOException {
        this.value = input.readUTF();
    }

    @Override
    public void write(final DataOutput output) throws IOException {
        output.writeUTF(value);
    }

    @Override
    public byte type() {
        return TagType.STRING;
    }

    @Override
    public String typeName() {
        return "TAG_String";
    }

    @Override
    public String print(final int level) {
        return "%s%s('%s'): '%s'".formatted(" ".repeat(level * Tag.INDENT), typeName(), name, value);
    }

    public String value() {
        return value;
    }

    public void value(final String value) {
        this.value = value;
    }
}
