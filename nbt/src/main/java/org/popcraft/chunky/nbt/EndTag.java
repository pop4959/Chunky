package org.popcraft.chunky.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class EndTag extends Tag {
    public EndTag() {
        super("");
    }

    @Override
    public void read(final DataInput input) throws IOException {
        // No data
    }

    @Override
    public void skip(final DataInput input) throws IOException {
        // No data
    }

    @Override
    public void write(final DataOutput output) throws IOException {
        // No data
    }

    @Override
    public Tag search(final DataInput input, final byte type, final String name) throws IOException {
        skip(input);
        return null;
    }

    @Override
    public byte type() {
        return TagType.END;
    }

    @Override
    public String typeName() {
        return "TAG_End";
    }

    @Override
    public String print(final int level) {
        return "";
    }
}
