package me.byteful.plugin.levelitems.listeners;

import me.byteful.plugin.levelitems.LevelItemsPlugin;
import me.byteful.plugin.levelitems.LevelItemsUtil;
import me.byteful.plugin.levelitems.api.AnvilCombineMode;
import me.byteful.plugin.levelitems.api.item.LeveledItem;
import me.byteful.plugin.levelitems.model.LevelAndXPModel;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;

public class AnvilListener implements Listener {
  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onAnvilCombine(PrepareAnvilEvent e) {
    final AnvilInventory inv = e.getInventory();
    final ItemStack firstItem = inv.getItem(0);
    final ItemStack secondItem = inv.getItem(1);
    final ItemStack result = e.getResult();

    if (result == null
      || !LevelItemsUtil.isSupportedItem(result.getType())
      || firstItem == null
      || secondItem == null
      || !LevelItemsUtil.isSupportedItem(firstItem.getType())
      || !LevelItemsUtil.isSupportedItem(secondItem.getType())) {
      return;
    }

    final AnvilCombineMode mode = LevelItemsPlugin.getInstance().getAnvilCombineMode();
    final LevelAndXPModel first =
      LevelAndXPModel.fromItem(LevelItemsUtil.createLeveledItem(firstItem));
    final LevelAndXPModel second =
      LevelAndXPModel.fromItem(LevelItemsUtil.createLeveledItem(secondItem));
    final LevelAndXPModel finished = mode.getHandler().apply(first, second);
    final LeveledItem finalItem = LevelItemsUtil.createLeveledItem(result);
    finalItem.setLevel(finished.getLevel());
    finalItem.setXp(finished.getXp());

    e.setResult(finalItem.getItemStack());
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onAnvilRepair(PrepareAnvilEvent e) {
    final AnvilInventory inv = e.getInventory();
    final ItemStack firstItem = inv.getItem(0);
    final ItemStack secondItem = inv.getItem(1);
    final ItemStack result = e.getResult();

    if (result == null
      || !LevelItemsUtil.isSupportedItem(result.getType())
      || firstItem == null
      || secondItem == null
      || !LevelItemsUtil.isSupportedItem(firstItem.getType())) {
      return;
    }

    final LeveledItem finalItem = LevelItemsUtil.createLeveledItem(result);
    e.setResult(finalItem.getItemStack()); // This has to be done to patch lore issues.
  }
}
