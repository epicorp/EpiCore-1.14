package net.epicorp.items;

import org.bukkit.event.EventPriority;

public @interface ItemEventListener {
	EventPriority value() default EventPriority.NORMAL;
	boolean ignoreCancelled() default true;
}
