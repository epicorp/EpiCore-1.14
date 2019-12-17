package net.epicorp.util.listeners;

import org.bukkit.event.Event;

public @interface Filtered {
	/**
	 * the filterer type
	 * @return
	 */
	Class<? extends Filter<? extends Event>> value();
}
