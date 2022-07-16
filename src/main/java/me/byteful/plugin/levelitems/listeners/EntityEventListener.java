package me.byteful.plugin.levelitems.listeners;

import me.byteful.plugin.levelitems.LevelItemsPlugin;
import me.byteful.plugin.levelitems.LevelItemsUtil;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.stream.Stream;

public class EntityEventListener extends LevelItemsXPListener {
  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onEntityKillEntity(EntityDeathEvent e) {
    Player killer = e.getEntity().getKiller();

    if (killer == null || !killer.hasPermission("leveltools.enabled")) {
      return;
    }

    final ItemStack hand = LevelItemsUtil.getHand(killer);


    final String ltype = LevelItemsPlugin.getInstance().getConfig().getString("entity_list_type", "blacklist");
    final Stream<EntityType> stream = LevelItemsPlugin.getInstance().getConfig().getStringList("entity_list").stream()
      .map(EntityType::valueOf);

    if (ltype.equalsIgnoreCase("whitelist") && stream.noneMatch(type -> e.getEntityType() == type)) {
      return;
    }

    if (ltype.equalsIgnoreCase("blacklist") && stream.anyMatch(type -> e.getEntityType() == type)) {
      return;
    }

    //    if (LevelToolsUtil.isSword(hand.getType())
    //        || LevelToolsUtil.isProjectileShooter(hand.getType())) {
    //      handle(
    //          LevelToolsUtil.createLevelToolsItem(hand),
    //          killer,
    //          LevelToolsUtil.getCombatModifier(e.getEntityType()));
    //    }
  }
}
