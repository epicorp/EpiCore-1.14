package net.epicorp.items;

import com.hervian.lambda.Lambda;
import com.hervian.lambda.LambdaFactory;
import net.devtech.functions.TriConsumer;
import net.devtech.structures.inheritance.InheritedMap;
import net.devtech.yajslib.persistent.PersistentRegistry;
import net.epicorp.util.listeners.EventMapper;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class ItemManager implements Listener {
	public static final Function<Plugin, NamespacedKey> PERSISTENT_DATA =  p -> new NamespacedKey(p, "net.epicorp.persistentdata");
	private EventMapper<ItemStack> eventMapper;
	private InheritedMap<CustomItem, Method> annotated = InheritedMap.getMethodsAnnotated(CustomItem.class, ItemEventListener.class);
	private Map<ItemListener, TriConsumer<CustomItem, ItemStack, Event>> consume = new HashMap<>();
	private PersistentRegistry registry;
	private Set<ListenerContainer> listenerContainers = new HashSet<>();
	private Plugin plugin;

	public ItemManager(EventMapper<ItemStack> eventMapper, PersistentRegistry registry) {
		this.eventMapper = eventMapper;
		this.registry = registry;
	}

	public void register(Class<? extends CustomItem> item) throws Throwable {
		for (Method attribute : this.annotated.getAttributes(item)) {
			ItemEventListener annotation = attribute.getDeclaredAnnotation(ItemEventListener.class);
			Class[] params = attribute.getParameterTypes();
			ItemListener listener = new ItemListener(params[0], item, annotation.value(), annotation.ignoreCancelled());

			Lambda lambda = LambdaFactory.create(attribute);
			if (Event.class.isAssignableFrom(params[0]))
				if (params.length == 1) this.consume.put(listener, (c, i, e) -> lambda.invoke_for_void(c, e));
				else if (params.length == 2 && params[1] == ItemStack.class)
					this.consume.put(listener, (c, i, e) -> lambda.invoke_for_void(c, e, i));
				else throw new IllegalArgumentException(attribute + " has incorrect arguments!");
			else throw new IllegalArgumentException(attribute + " has incorrect arguments!");

			ListenerContainer container = new ListenerContainer(params[0], annotation.value(), annotation.ignoreCancelled());
			if (!this.listenerContainers.contains(container)) {
				this.plugin.getServer().getPluginManager().registerEvent(params[0], this, annotation.value(), (l, e) -> {
					try {
						ItemStack stack = this.eventMapper.map(e);
						if (stack != null) {
							PersistentDataContainer persist = stack.getItemMeta().getPersistentDataContainer();
							NamespacedKey key = PERSISTENT_DATA.apply(this.plugin);
							byte[] data = persist.get(key, PersistentDataType.BYTE_ARRAY);
							if (data != null) {
								CustomItem citem = (CustomItem) this.registry.fromByteArray(data);
								ItemListener listener1 = new ItemListener(e.getClass(), citem.getClass(), annotation.value(), annotation.ignoreCancelled());
								TriConsumer<CustomItem, ItemStack, Event> consumer = this.consume.get(listener1);
								consumer.accept(citem, stack, e);
							}
						}
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}, this.plugin, annotation.ignoreCancelled());
			}
		}
	}

	class ListenerContainer {
		Class<? extends Event> type;
		EventPriority priority;
		boolean ignoreCancelled;

		public ListenerContainer(Class<? extends Event> type, EventPriority priority, boolean ignoreCancelled) {
			this.type = type;
			this.priority = priority;
			this.ignoreCancelled = ignoreCancelled;
		}

		@Override
		public boolean equals(Object object) {
			if (this == object) return true;
			if (!(object instanceof ListenerContainer)) return false;

			ListenerContainer container = (ListenerContainer) object;

			if (this.ignoreCancelled != container.ignoreCancelled) return false;
			if (!this.type.equals(container.type)) return false;
			return this.priority == container.priority;
		}

		@Override
		public int hashCode() {
			int result = this.type.hashCode();
			result = 31 * result + this.priority.hashCode();
			result = 31 * result + (this.ignoreCancelled ? 1 : 0);
			return result;
		}
	}

	class ItemListener {
		Class<? extends Event> event;
		Class<? extends CustomItem> item;
		EventPriority priority;
		boolean ignoreCancelled;

		public ItemListener(Class<? extends Event> event, Class<? extends CustomItem> item, EventPriority priority, boolean ignoreCancelled) {
			this.event = event;
			this.item = item;
			this.priority = priority;
			this.ignoreCancelled = ignoreCancelled;
		}

		@Override
		public boolean equals(Object object) {
			if (this == object) return true;
			if (!(object instanceof ItemListener)) return false;

			ItemListener listener = (ItemListener) object;

			return this.ignoreCancelled == listener.ignoreCancelled && this.event.equals(listener.event) && this.item.equals(listener.item) && this.priority == listener.priority;
		}

		@Override
		public int hashCode() {
			int result = this.event.hashCode();
			result = 31 * result + this.item.hashCode();
			result = 31 * result + this.priority.hashCode();
			result = 31 * result + (this.ignoreCancelled ? 1 : 0);
			return result;
		}
	}

	public void setPlugin(Plugin plugin) {
		this.plugin = plugin;
	}
}
