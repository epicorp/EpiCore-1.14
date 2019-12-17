package net.epicorp.persistence.world;

import net.devtech.yajslib.io.PersistentInputStream;
import net.devtech.yajslib.io.PersistentOutputStream;
import net.devtech.yajslib.persistent.PersistentRegistry;
import net.epicorp.util.OverrideHandler;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class PWorldImpl implements PWorld {
	private static final Logger LOGGER = Logger.getLogger("PWorld");
	private PersistentRegistry registry;
	private Long2ObjectMap<ChonkImpl> chonks = new Long2ObjectOpenHashMap<>();
	private File worldRoot;

	public PWorldImpl(PersistentRegistry registry, World world, File root) {
		this.registry = registry;
		this.worldRoot = new File(root, world.getName());
		// I couldn't be fucked making this faster
		// this rarely needs to happen anyways
		for (Chunk chunk : world.getLoadedChunks())
			this.onLoad(chunk.getX(), chunk.getZ());
	}

	@Override
	public Object get(Location location) {
		ChonkImpl of = this.getFor(location.getBlockX() >> ChonkImpl.POWER_BLOCK, location.getBlockZ() >> ChonkImpl.POWER_BLOCK);
		return of == null ? null : of.get(location);
	}

	@Override
	public Optional<Object> getOptional(Location location) {
		return Optional.ofNullable(this.get(location));
	}

	@Override
	public Object getForce(Location location) {
		ChonkImpl of = this.onLoad(location.getBlockX() >> ChonkImpl.POWER_BLOCK, location.getBlockZ() >> ChonkImpl.POWER_BLOCK);
		return of.get(location);
	}

	@Override
	public Optional<Object> getForceOptional(Location location) {
		return Optional.ofNullable(this.getForce(location));
	}

	@Override
	public OverrideHandler<Object> set(Location location, Object object) {
		ChonkImpl of = this.getFor(location.getBlockX() >> ChonkImpl.POWER_BLOCK, location.getBlockZ() >> ChonkImpl.POWER_BLOCK);
		return of == null ? null : of.set(location, object);
	}

	@Override
	public Optional<OverrideHandler<Object>> setOptional(Location location, Object object) {
		return Optional.ofNullable(this.set(location, object));
	}

	@Override
	public OverrideHandler<Object> setForce(Location location, Object object) {
		ChonkImpl of = this.onLoad(location.getBlockX() >> ChonkImpl.POWER_BLOCK, location.getBlockZ() >> ChonkImpl.POWER_BLOCK);
		return of.set(location, object);
	}

	@Override
	public ChonkImpl onLoad(int chunkX, int chunkZ) {
		int chonkX = chunkX >> ChonkImpl.POWER;
		int chonkZ = chunkZ >> ChonkImpl.POWER;
		ChonkImpl chonk = this.getFor(chunkX, chunkZ);
		if (chonk == null) {
			File file = this.chonkFile(chunkX >> ChonkImpl.POWER, chunkZ >> ChonkImpl.POWER);
			if (file.exists()) {
				try (PersistentInputStream input = new PersistentInputStream(new GZIPInputStream(new FileInputStream(file)), this.registry)) {
					chonk = (ChonkImpl) input.readPersistent();
				} catch (IOException e) {
					LOGGER.warning("Exception in loading chunk from disk!");
					throw new RuntimeException(e);
				}
			} else chonk = new ChonkImpl();

			chonk.setBaseX(chonkX);
			chonk.setBaseZ(chonkZ);
			this.chonks.put(getKey(chonkX, chonkZ), chonk);
		}
		chonk.loaded++;
		return chonk;
	}

	@Override
	public void onUnload(int chunkX, int chunkZ) {
		Chonk chonk = this.getFor(chunkX, chunkZ);
		if (chonk.unload() == 0) {
			try (PersistentOutputStream output = new PersistentOutputStream(new GZIPOutputStream(new FileOutputStream(this.chonkFile(chunkX >> ChonkImpl.POWER, chunkZ >> ChonkImpl.POWER))), this.registry)) {
				output.writePersistent(chonk);
			} catch (IOException e) {
				LOGGER.info("Error in serializing chonk data");
				throw new RuntimeException(e);
			}
			this.remove(getKey(chunkX >> ChonkImpl.POWER, chunkZ >> ChonkImpl.POWER), chonk);
		}
	}

	@Override
	public void saveAll() {
		this.chonks.forEach((l, c) -> {
			try (PersistentOutputStream output = new PersistentOutputStream(new GZIPOutputStream(new FileOutputStream(this.chonkFile((int) (l >> 32), (int) (long) l))), this.registry)) {
				output.writePersistent(c);
			} catch (IOException e) {
				LOGGER.info("Error in serializing chonk data");
				throw new RuntimeException(e);
			}
		});
	}

	private ChonkImpl getFor(int chunkX, int chunkZ) {
		chunkX >>= ChonkImpl.POWER;
		chunkZ >>= ChonkImpl.POWER;
		long key = getKey(chunkX, chunkZ);
		return this.chonks.get(key);
	}

	private Chonk remove(long key, Chonk chonk) {
		if (chonk == null) LOGGER.warning("removing null chunk at " + (int) (key >> 32) + ", " + (int) key);
		else if (chonk.unload() > 0)
			LOGGER.warning("removing chonk with loaded chunks at " + (int) (key >> 32) + ", " + (int) key);
		return chonk;
	}

	private File chonkFile(int chonkX, int chonkZ) {
		return new File(this.worldRoot, getKey(chonkX, chonkZ) + ".pbd");
	}

	// https://stackoverflow.com/questions/10686178/convert-long-to-two-int-and-vice-versa
	private static final long getKey(int a, int b) {
		return (long) a << 32 | b & 0xFFFFFFFFL;
	}
}
