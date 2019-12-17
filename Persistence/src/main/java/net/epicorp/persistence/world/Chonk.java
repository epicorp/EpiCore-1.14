package net.epicorp.persistence.world;

import net.epicorp.util.*;
import org.bukkit.Location;
import java.util.Optional;

public interface Chonk {
	OverrideHandler<Object> set(Location location, Object value);

	Object get(Location location);

	Optional<Object> getAsOptional(Location location);

	int unload();
}
