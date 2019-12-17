package net.epicorp.epicore;

import net.devtech.yajslib.persistent.AnnotatedPersistent;
import net.devtech.yajslib.persistent.PersistentRegistry;
import net.devtech.yajslib.persistent.SimplePersistentRegistry;
import net.epicorp.items.ItemManager;
import net.epicorp.persistence.world.ChonkImpl;
import net.epicorp.util.listeners.EventMapper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class EpiCore extends JavaPlugin implements Listener {
	public static final PersistentRegistry PERSISTENT_REGISTRY = new SimplePersistentRegistry();
	public static final EventMapper<ItemStack> MAPPER = new EventMapper<>(true);
	public static final ItemManager ITEM_MANAGER = new ItemManager(MAPPER, PERSISTENT_REGISTRY);

	static {
		try {
			PERSISTENT_REGISTRY.register(ChonkImpl.class, new AnnotatedPersistent<>(ChonkImpl::new, ChonkImpl.class, 101010L));
			PERSISTENT_REGISTRY.register(TestItem.class, new AnnotatedPersistent<>(TestItem::new, TestItem.class, 10L));
			MAPPER.register(PlayerInteractEvent.class, PlayerInteractEvent::getItem);
			ITEM_MANAGER.register(TestItem.class);
		} catch (Throwable throwable) {
			throwable.printStackTrace();
		}
	}

	@Override
	public void onEnable() {
		this.getServer().getPluginManager().registerEvents(this, this);
	}

	@EventHandler
	public void player(PlayerCommandPreprocessEvent brug) {
		
	}

	@Override
	public void onDisable() {
		// Plugin shutdown logic
	}
}
