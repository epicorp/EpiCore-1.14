package net.epicorp.epicore;

import net.devtech.yajslib.annotations.Reader;
import net.devtech.yajslib.annotations.Writer;
import net.devtech.yajslib.io.PersistentInputStream;
import net.devtech.yajslib.io.PersistentOutputStream;
import net.epicorp.items.CustomItem;
import net.epicorp.items.ItemEventListener;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import java.io.IOException;

public class TestItem extends CustomItem {
	int hey;

	public TestItem(Plugin plugin) {
		super(plugin);
	}

	@ItemEventListener
	public void listen(PlayerInteractEvent event) {
		System.out.println(this.hey);
	}

	@Writer(10L)
	public void write(PersistentOutputStream out) throws IOException {
		out.writeInt(this.hey);
	}

	@Reader(10L)
	public void read(PersistentInputStream in) throws IOException {
		this.hey = in.readInt();
	}

	@Override
	protected ItemStack baseStack() {
		return new ItemStack(Material.STONE);
	}
}
