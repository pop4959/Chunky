package org.popcraft.chunky.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.visitors.CollectFields;
import net.minecraft.nbt.visitors.FieldSelector;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.LevelResource;
import org.popcraft.chunky.ChunkyNeoForge;
import org.popcraft.chunky.ducks.MinecraftServerExtension;
import org.popcraft.chunky.platform.util.Location;
import org.popcraft.chunky.util.Input;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class NeoForgeWorld implements World {
    private static final int TICKING_LOAD_DURATION = Input.tryInteger(System.getProperty("chunky.tickingLoadDuration")).orElse(0);
    private static final TicketType CHUNKY = new TicketType(0L, TicketType.FLAG_LOADING);
    private static final TicketType CHUNKY_TICKING = new TicketType(TICKING_LOAD_DURATION * 20L, TicketType.FLAG_LOADING | TicketType.FLAG_SIMULATION);
    private static final boolean UPDATE_CHUNK_NBT = Boolean.getBoolean("chunky.updateChunkNbt");
    private final ServerLevel world;
    private final Border worldBorder;

    public NeoForgeWorld(final ServerLevel world) {
        this.world = world;
        this.worldBorder = new NeoForgeBorder(world.getWorldBorder());
    }

    @Override
    public String getName() {
        return world.dimension().identifier().toString();
    }

    @Override
    public String getKey() {
        return getName();
    }

    @Override
    public CompletableFuture<Boolean> isChunkGenerated(final int x, final int z) {
        if (Thread.currentThread() != world.getServer().getRunningThread()) {
            return CompletableFuture.supplyAsync(() -> isChunkGenerated(x, z), world.getServer()).thenCompose(Function.identity());
        } else {
            final ChunkPos chunkPos = new ChunkPos(x, z);
            final ServerChunkCache serverChunkCache = world.getChunkSource();
            final ChunkMap chunkStorage = serverChunkCache.chunkMap;
            final ChunkHolder loadedChunkHolder = chunkStorage.getVisibleChunkIfPresent(chunkPos.toLong());
            if (loadedChunkHolder != null && loadedChunkHolder.getLatestStatus() == ChunkStatus.FULL) {
                return CompletableFuture.completedFuture(true);
            }
            if (UPDATE_CHUNK_NBT) {
                return chunkStorage.readChunk(chunkPos)
                        .thenApply(optionalNbt -> optionalNbt
                                .filter(chunkNbt -> chunkNbt.contains("Status"))
                                .flatMap(chunkNbt -> chunkNbt.getString("Status"))
                                .map(status -> "minecraft:full".equals(status) || "full".equals(status))
                                .orElse(false));
            }
            final FieldSelector statusSelector = new FieldSelector(StringTag.TYPE, "Status");
            final CollectFields statusCollector = new CollectFields(statusSelector);
            return serverChunkCache.chunkScanner().scanChunk(chunkPos, statusCollector)
                    .thenApply(ignored -> {
                        if (statusCollector.getResult() instanceof final CompoundTag chunkNbt) {
                            final String status = chunkNbt.getString("Status").orElse(null);
                            return "minecraft:full".equals(status) || "full".equals(status);
                        }
                        return false;
                    });
        }
    }

    @Override
    public CompletableFuture<Void> getChunkAtAsync(final int x, final int z) {
        if (Thread.currentThread() != world.getServer().getRunningThread()) {
            return CompletableFuture.supplyAsync(() -> getChunkAtAsync(x, z), world.getServer()).thenCompose(Function.identity());
        } else {
            final ChunkPos chunkPos = new ChunkPos(x, z);
            final ServerChunkCache serverChunkCache = world.getChunkSource();
            serverChunkCache.addTicketWithRadius(CHUNKY, chunkPos, 0);
            if (TICKING_LOAD_DURATION > 0) {
                serverChunkCache.addTicketWithRadius(CHUNKY_TICKING, chunkPos, 1);
            }
            serverChunkCache.runDistanceManagerUpdates();
            // note: when Moonrise is present, holders do not get created most of the time even after explicit distance manager update
            // so we force `create = true` *only if* Moonrise is present, as it breaks pausing for everyone else
            boolean create = ChunkyNeoForge.ENABLE_MOONRISE_WORKAROUNDS;
            return serverChunkCache.getChunkFutureMainThread(x, z, ChunkStatus.FULL, create)
                    .whenCompleteAsync((ignored, throwable) -> {
                        serverChunkCache.removeTicketWithRadius(CHUNKY, chunkPos, 0);
                        ((MinecraftServerExtension) world.getServer()).chunky$markChunkSystemHousekeeping();
                        if (ChunkyNeoForge.ENABLE_MOONRISE_WORKAROUNDS) {
                            // note: to prevent pausing on dedicated server when Moonrise is present
                            world.getServer().emptyTicks = 0;
                        }
                    }, world.getServer())
                    .thenApply(ignored -> null);
        }
    }

    @Override
    public UUID getUUID() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getSeaLevel() {
        return world.getSeaLevel();
    }

    @Override
    public Location getSpawn() {
        final BlockPos pos = world.getRespawnData().pos();
        final float yaw = world.getRespawnData().yaw();
        final float pitch = world.getRespawnData().pitch();
        return new Location(this, pos.getX(), pos.getY(), pos.getZ(), yaw, pitch);
    }

    @Override
    public Border getWorldBorder() {
        return worldBorder;
    }

    @Override
    public int getElevation(final int x, final int z) {
        final int height = world.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z) + 1;
        final int logicalHeight = world.getLogicalHeight();
        final CompletableFuture loadedChunk = getChunkAtAsync(x >> 4, y >> 4);
        if (height >= logicalHeight) {
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, logicalHeight, z);
            int air = 0;
            while (pos.getY() > world.getMinY()) {
                pos = pos.move(Direction.DOWN);
                final BlockState blockState = world.getBlockState(pos);
                if (blockState.isSolid() && air > 1) {
                    return pos.getY() + 1;
                }
                air = blockState.isAir() ? air + 1 : 0;
            }
        }
        return height;
    }

    @Override
    public int getMaxElevation() {
        return world.getLogicalHeight();
    }

    @Override
    public void playEffect(final Player player, final String effect) {
        final Location location = player.getLocation();
        final BlockPos pos = BlockPos.containing(location.getX(), location.getY(), location.getZ());
        Input.tryInteger(effect).ifPresent(eventId -> world.levelEvent(eventId, pos, 0));
    }

    @Override
    public void playSound(final Player player, final String sound) {
        final Location location = player.getLocation();
        world.getServer()
                .registryAccess()
                .get(Registries.SOUND_EVENT)
                .flatMap(soundEventRegistry -> soundEventRegistry.value().getOptional(Identifier.tryParse(sound)))
                .ifPresent(soundEvent -> world.playSound(null, location.getX(), location.getY(), location.getZ(), soundEvent, SoundSource.MASTER, 2f, 1f));
    }

    @Override
    public Optional<Path> getDirectory(final String name) {
        if (name == null) {
            return Optional.empty();
        }
        final Path directory = DimensionType.getStorageFolder(world.dimension(), world.getServer().getWorldPath(LevelResource.ROOT)).normalize().resolve(name);
        return Files.exists(directory) ? Optional.of(directory) : Optional.empty();
    }

    public ServerLevel getWorld() {
        return world;
    }
}
