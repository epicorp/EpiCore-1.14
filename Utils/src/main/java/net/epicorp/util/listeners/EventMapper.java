package net.epicorp.util.listeners;

import net.devtech.structures.inheritance.InheritedMap;
import org.bukkit.event.Event;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;

public class EventMapper<T> {
	private static final Logger LOGGER = Logger.getLogger("EventMapper");
	private Map<Class<?>, Function<Event, T>> converters = new HashMap<>();
	private InheritedMap<Event, Class<?>> supers = new InheritedMap<>(Event.class, c -> new ArrayList<>(Collections.singletonList(c)));
	private boolean warn;

	public EventMapper(boolean warn) {
		this.warn = warn;
	}

	public <E> void register(Class<E> eventType, Function<E, T> converter) {
		this.converters.put(eventType, (Function) converter);
	}

	public T map(Event event) {
		for (Class<?> attribute : this.supers.getAttributes(event.getClass())) {
			Function<Event, T> conversion = this.converters.get(attribute);
			if (conversion != null) return conversion.apply(event);
		}
		if (this.warn) LOGGER.warning("No mapping for: " + event);
		return null;
	}
}
