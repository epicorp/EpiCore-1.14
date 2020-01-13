package net.epicorp.items;

import net.devtech.yajslib.persistent.PersistentRegistry;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import java.io.IOException;

public abstract class CustomItem {
	public static <T extends CustomItem> ItemStack createNewStack(PersistentRegistry registry, Class<T> type) throws IOException {
		CustomItem blank = registry.forClass(type).blank();
		ItemStack stack = blank.baseStack();
		ItemMeta meta = stack.getItemMeta();
		PersistentDataContainer persist = meta.getPersistentDataContainer();
		byte[] data = registry.toByteArray(blank);
		persist.set(ItemManager.PERSISTENT_DATA.apply(blank.plugin), PersistentDataType.BYTE_ARRAY, data);
		stack.setItemMeta(meta);
		return stack;
	}

	protected final Plugin plugin;

	public CustomItem(Plugin plugin) {
		this.plugin = plugin;
	}

	protected abstract ItemStack baseStack();
}
