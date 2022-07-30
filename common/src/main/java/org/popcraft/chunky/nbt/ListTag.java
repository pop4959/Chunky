package org.popcraft.chunky.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ListTag extends Tag {
    private byte type;
    private List<Tag> value = new ArrayList<>();

    protected ListTag(final String name) {
        super(name);
    }

    public ListTag(final String name, final byte type, final List<Tag> value) {
        super(name);
        this.type = type;
        this.value = value;
    }

    @Override
    public void read(final DataInput input) throws IOException {
        this.type = input.readByte();
        final int size = input.readInt();
        this.value = new ArrayList<>();
        for (int i = 0; i < size; ++i) {
            final Tag tag = Tag.create(type, "");
            tag.read(input);
            value.add(tag);
        }
    }

    @Override
    public void write(final DataOutput output) throws IOException {
        output.writeByte(type);
        final int size = value.size();
        output.writeInt(size);
        for (final Tag tag : value) {
            tag.write(output);
        }
    }

    @Override
    public byte type() {
        return TagType.LIST;
    }

    @Override
    public String typeName() {
        return "TAG_List";
    }

    @Override
    public String print(final int level) {
        final int size = value.size();
        final String entry = size == 1 ? "entry" : "entries";
        final String indent = " ".repeat(level * Tag.INDENT);
        final StringBuilder listBuilder = new StringBuilder("%s%s('%s'): %d %s".formatted(indent, typeName(), name, size, entry));
        listBuilder.append('\n').append(indent).append("{\n");
        for (final Tag tag : value) {
            listBuilder.append(tag.print(level + 1)).append('\n');
        }
        listBuilder.append(indent).append('}');
        return listBuilder.toString();
    }

    public List<Tag> value() {
        return value;
    }

    public void value(final List<Tag> value) {
        if (!value.isEmpty()) {
            this.type = value.get(0).type();
        }
        this.value = value;
    }
}
