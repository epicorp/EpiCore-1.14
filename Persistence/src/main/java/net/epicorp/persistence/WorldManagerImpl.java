package net.epicorp.persistence;

import net.devtech.yajslib.persistent.PersistentRegistry;
import net.epicorp.util.OverrideHandler;
import net.epicorp.persistence.world.PWorld;
import net.epicorp.persistence.world.PWorldImpl;
import net.epicorp.persistence.world.WorldManager;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import java.io.File;
import java.util.*;
import java.util.logging.Logger;

import static org.bukkit.event.EventPriority.MONITOR;

/**
 * none of this has been tested, however I'm going to take a temporary break from this as I don't need it
 * for any immediate projects
 */
public class WorldManagerImpl implements Listener, WorldManager {
	private static final Logger LOGGER = Logger.getLogger("WorldManager");
	private final Map<UUID, PWorld> worlds = new HashMap<>();
	private final File root;
	private final PersistentRegistry registry;

	public WorldManagerImpl(File root, PersistentRegistry registry) {
		this.root = root;
		this.registry = registry;
	}



	@EventHandler(priority = MONITOR)
	public void chunkLoad(ChunkLoadEvent event) {
		Chunk chunk = event.getChunk();
		this.worlds.get(event.getWorld().getUID()).onLoad(chunk.getX(), chunk.getZ());
	}

	@EventHandler(priority = MONITOR)
	public void chunkUnload(ChunkUnloadEvent event) {
		Chunk chunk = event.getChunk();
		this.worlds.get(event.getWorld().getUID()).onUnload(chunk.getX(), chunk.getZ());
	}

	@EventHandler(priority = MONITOR)
	public void worldLoad(WorldLoadEvent event) {
		World world = event.getWorld();
		this.worlds.put(world.getUID(), new PWorldImpl(this.registry, world, this.root));
	}

	@EventHandler(priority = MONITOR)
	public void worldUnload(WorldUnloadEvent event) {
		World world = event.getWorld();
		PWorld pWorld = this.worlds.remove(world.getUID());
		if(pWorld == null)
			LOGGER.info("Tried to unload world that was never loaded! " + world.getName());
		else {
			pWorld.saveAll();
		}
	}

	@Override
	public Object get(Location location) {
		return this.getWorld(location).get(location);
	}

	@Override
	public Optional<Object> getOptional(Location location) {
		return Optional.ofNullable(this.get(location));
	}

	@Override
	public Object getForce(Location location) {
		return this.getWorld(location).getForce(location);
	}

	@Override
	public Optional<Object> getForceOptional(Location location) {
		return Optional.ofNullable(this.getForce(location));
	}

	@Override
	public OverrideHandler<Object> set(Location location, Object object) {
		return this.getWorld(location).set(location, object);
	}

	@Override
	public Optional<OverrideHandler<Object>> setOptional(Location location, Object object) {
		return Optional.ofNullable(this.set(location, object));
	}

	@Override
	public OverrideHandler<Object> setForce(Location location, Object object) {
		return this.getWorld(location).setForce(location, object);
	}

	private PWorld getWorld(Location location) {
		return this.worlds.get(Objects.requireNonNull(location.getWorld()).getUID());
	}
}
