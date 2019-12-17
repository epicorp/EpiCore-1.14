package net.epicorp.persistence.world;

import net.devtech.yajslib.annotations.Reader;
import net.devtech.yajslib.annotations.Writer;
import net.devtech.yajslib.io.PersistentInputStream;
import net.devtech.yajslib.io.PersistentOutputStream;
import net.epicorp.util.MemPool;
import net.epicorp.util.OverrideHandler;
import org.bukkit.Location;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * a chunk of chunks
 */
public class ChonkImpl implements Chonk {
	private static final Logger LOGGER = Logger.getLogger("Chonk");
	static final int POWER = 3;
	static final int POWER_BLOCK = POWER + 4;
	static final int BY_BLOCK_MOD = (1 << POWER_BLOCK) - 1;
	static final int Y_SIZE_POWER = 8;
	static final int X_Y = POWER_BLOCK + Y_SIZE_POWER;

	/**
	 * how many chunks long the chonk should hold
	 */
	static final int DIMENSIONS = 1 << POWER;
	static final int SIZE = DIMENSIONS * DIMENSIONS;
	static final int CHUNK_DATA = 65536;

	private static final MemPool<Object[]> MEM_POOL = new MemPool<>(() -> new Object[SIZE * CHUNK_DATA], o -> Arrays.fill(o, null));

	static {
		MEM_POOL.ensureCapacity(128);
		LOGGER.info("Chonk memory pool allocated!");
	}

	private Object[] chunks = MEM_POOL.get();
	int loaded;
	private int baseX;
	private int baseZ;

	@Override
	public OverrideHandler<Object> set(Location location, Object value) {
		Optional<Object> original = this.getAsOptional(location);
		return new OverrideHandler<Object>() {
			private boolean acted;

			@Override
			public void force() {
				this.acted = true;
				ChonkImpl.this.chunks[ChonkImpl.this.asIndex(location)] = value;
			}

			@Override
			public Optional<Object> old() {
				this.acted = true;
				return original;
			}

			@Override
			public boolean ifVacant() {
				this.acted = true;
				return OverrideHandler.super.ifVacant();
			}

			@Override
			protected void finalize() throws Throwable {
				if (!this.acted) LOGGER.warning("Did not use override handler!");
			}
		};
	}

	@Override
	public Object get(Location location) {
		return this.chunks[this.asIndex(location)];
	}

	@Override
	public Optional<Object> getAsOptional(Location location) {
		return Optional.ofNullable(this.get(location));
	}

	@Override
	public int unload() {
		return --this.loaded;
	}

	void setBaseX(int baseX) {
		this.baseX = baseX;
	}

	void setBaseZ(int baseZ) {
		this.baseZ = baseZ;
	}

	private int asIndex(Location location) {
		return ((location.getBlockZ() & BY_BLOCK_MOD) << X_Y) + (location.getBlockY() << POWER_BLOCK) + (location.getBlockX() & BY_BLOCK_MOD);
	}

	private Location fromIndex(int idx) {
		final int z = idx >> X_Y;
		idx -= (z << X_Y);
		final int y = idx >> POWER_BLOCK;
		final int x = idx & BY_BLOCK_MOD;
		return new Location(null, x + (this.baseX << POWER_BLOCK), y, z + (this.baseZ << POWER_BLOCK));
	}

	@Writer(101010L)
	private void write(PersistentOutputStream output) throws IOException {
		output.writeArray(this.chunks);
	}

	@Reader(101010L)
	private void read(PersistentInputStream input) throws IOException {
		input.readArray(this.chunks);
	}
}
