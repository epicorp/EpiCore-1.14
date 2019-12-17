package net.epicorp.persistence.world;

import net.epicorp.util.OverrideHandler;
import org.bukkit.Location;
import java.util.Optional;

public interface WorldManager {
	Object get(Location location);

	Optional<Object> getOptional(Location location);

	Object getForce(Location location);

	Optional<Object> getForceOptional(Location location);

	OverrideHandler<Object> set(Location location, Object object);

	Optional<OverrideHandler<Object>> setOptional(Location location, Object object);

	OverrideHandler<Object> setForce(Location location, Object object);
}
