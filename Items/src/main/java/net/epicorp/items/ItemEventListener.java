package net.epicorp.items;

import org.bukkit.event.EventPriority;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ItemEventListener {
	EventPriority value() default EventPriority.NORMAL;
	boolean ignoreCancelled() default true;
}
