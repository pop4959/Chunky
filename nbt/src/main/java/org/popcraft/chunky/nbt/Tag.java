package org.popcraft.chunky.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

public abstract class Tag {
    protected static final int INDENT = 2;
    protected final String name;

    protected Tag(final String name) {
        this.name = name;
    }

    public static Tag load(final DataInput input) throws IOException {
        final byte type = input.readByte();
        if (TagType.END == type) {
            return new EndTag();
        }
        final String name = input.readUTF();
        final Tag tag = create(type, name);
        tag.read(input);
        return tag;
    }

    public static byte pass(final DataInput input) throws IOException {
        final byte type = input.readByte();
        if (TagType.END == type) {
            return type;
        }
        final int size = input.readUnsignedShort();
        input.skipBytes(size);
        skip(input, type);
        return type;
    }

    static void skip(final DataInput input, final byte type) throws IOException {
        if (type < 0 || type >= SkipTags.TAGS.length) {
            throw new IllegalArgumentException("Invalid tag type %d".formatted(type));
        }
        SkipTags.TAGS[type].skip(input);
    }

    public static void save(final DataOutput output, final Tag tag) throws IOException {
        final byte type = tag.type();
        output.writeByte(type);
        if (TagType.END == type) {
            return;
        }
        output.writeUTF(tag.name());
        tag.write(output);
    }

    public static Tag find(final DataInput input, final byte type, final String name) throws IOException {
        final byte t = input.readByte();
        if (TagType.END == t) {
            return new EndTag();
        }
        final String n = input.readUTF();
        final Tag tag = create(t, n);
        if (type == t && name.equals(n)) {
            tag.read(input);
            return tag;
        }
        return tag.search(input, type, name);
    }

    public static CompoundTag findMany(final DataInput input, final Set<String> tagNames) throws IOException {
        if (TagType.COMPOUND != input.readByte()) {
            return null;
        }
        input.readUTF();
        final CompoundTag results = new CompoundTag("", new HashMap<>());
        int found = 0;
        byte type;
        while (found < tagNames.size() && TagType.END != (type = input.readByte())) {
            final String name = input.readUTF();
            if (tagNames.contains(name)) {
                final Tag tag = create(type, name);
                tag.read(input);
                results.put(tag);
                ++found;
            } else {
                skip(input, type);
            }
        }
        return results;
    }

    public static Tag create(final byte type, final String name) {
        return switch (type) {
            case TagType.END -> new EndTag();
            case TagType.BYTE -> new ByteTag(name);
            case TagType.SHORT -> new ShortTag(name);
            case TagType.INT -> new IntTag(name);
            case TagType.LONG -> new LongTag(name);
            case TagType.FLOAT -> new FloatTag(name);
            case TagType.DOUBLE -> new DoubleTag(name);
            case TagType.BYTE_ARRAY -> new ByteArrayTag(name);
            case TagType.STRING -> new StringTag(name);
            case TagType.LIST -> new ListTag(name);
            case TagType.COMPOUND -> new CompoundTag(name);
            case TagType.INT_ARRAY -> new IntArrayTag(name);
            case TagType.LONG_ARRAY -> new LongArrayTag(name);
            default -> throw new IllegalArgumentException("Invalid tag type %d".formatted(type));
        };
    }

    public String name() {
        return name;
    }

    abstract void read(final DataInput input) throws IOException;

    abstract void skip(final DataInput input) throws IOException;

    abstract void write(final DataOutput output) throws IOException;

    abstract Tag search(final DataInput input, final byte type, final String name) throws IOException;

    abstract byte type();

    abstract String typeName();

    abstract String print(final int level);

    @Override
    public String toString() {
        return print(0);
    }

    private static final class SkipTags {
        private static final Tag[] TAGS = new Tag[TagType.LONG_ARRAY + 1];

        static {
            for (byte type = TagType.END; type <= TagType.LONG_ARRAY; ++type) {
                TAGS[type] = create(type, "");
            }
        }

        private SkipTags() {
        }
    }
}
