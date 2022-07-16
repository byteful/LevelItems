package me.byteful.plugin.levelitems.listeners;

import me.byteful.plugin.levelitems.LevelItemsPlugin;
import me.byteful.plugin.levelitems.LevelItemsUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import redempt.redlib.blockdata.DataBlock;

import java.util.stream.Stream;

public class BlockEventListener extends LevelItemsXPListener {
  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onBlockBreak(BlockBreakEvent e) {
    final Player player = e.getPlayer();

    if (!player.hasPermission("leveltools.enabled")) {
      return;
    }

    final Block block = e.getBlock();
    final ItemStack hand = LevelItemsUtil.getHand(player);

    if (!LevelItemsPlugin.getInstance().getConfig().getBoolean("playerPlacedBlocks")) {
      final DataBlock db = LevelItemsPlugin.getInstance().getBlockDataManager().getDataBlock(block);

      if (db.contains("level_tools") && db.getBoolean("level_tools")) {
        return;
      }
    }

    final String type = LevelItemsPlugin.getInstance().getConfig().getString("block_list_type", "blacklist");
    final Stream<Material> stream = LevelItemsPlugin.getInstance().getConfig().getStringList("block_list").stream()
      .map(Material::getMaterial);

    if (type.equalsIgnoreCase("whitelist") && stream.noneMatch(material -> block.getType() == material)) {
      return;
    }

    if (type.equalsIgnoreCase("blacklist") && stream.anyMatch(material -> block.getType() == material)) {
      return;
    }

    //    if ((LevelToolsUtil.isAxe(hand.getType())
    //            || LevelToolsUtil.isPickaxe(hand.getType())
    //            || LevelToolsUtil.isShovel(hand.getType()))
    //        && !block.getDrops(hand).isEmpty()) {
    //      handle(
    //          LevelToolsUtil.createLevelToolsItem(hand),
    //          player,
    //          LevelToolsUtil.getBlockModifier(block.getType()));
    //    }
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onBlockPlace(BlockPlaceEvent e) {
    if (!LevelItemsPlugin.getInstance().getConfig().getBoolean("playerPlacedBlocks")) {
      final DataBlock db =
        LevelItemsPlugin.getInstance().getBlockDataManager().getDataBlock(e.getBlockPlaced());
      db.set("level_tools", true);
    }
  }
}
