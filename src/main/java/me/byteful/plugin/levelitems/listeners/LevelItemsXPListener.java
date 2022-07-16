package me.byteful.plugin.levelitems.listeners;

import com.cryptomorin.xseries.messages.ActionBar;
import me.byteful.plugin.levelitems.LevelItemsPlugin;
import me.byteful.plugin.levelitems.LevelItemsUtil;
import me.byteful.plugin.levelitems.api.event.LevelItemsLevelIncreaseEvent;
import me.byteful.plugin.levelitems.api.event.LevelItemsXPIncreaseEvent;
import me.byteful.plugin.levelitems.api.item.LeveledItem;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public abstract class LevelItemsXPListener implements Listener {
  protected void handle(LeveledItem tool, Player player, double modifier) {
    double newXp = LevelItemsUtil.round(tool.getXp() + modifier, 1);

    final LevelItemsXPIncreaseEvent xpEvent =
      new LevelItemsXPIncreaseEvent(tool, player, newXp, false);

    Bukkit.getPluginManager().callEvent(xpEvent);

    if (xpEvent.isCancelled()) {
      return;
    }

    tool.setXp(xpEvent.getNewXp());

    if (LevelItemsPlugin.getInstance().getConfig().getBoolean("actionBar.enabled")) {
      final String text =
        LevelItemsUtil.colorize(
          LevelItemsPlugin.getInstance()
            .getConfig()
            .getString("actionBar.display")
            .replace(
              "{progress_bar}",
              LevelItemsUtil.createDefaultProgressBar(tool.getXp(), tool.getMaxXp()))
            .replace("{xp}", String.valueOf(tool.getXp()))
            .replace("{max_xp}", String.valueOf(tool.getMaxXp())));

      ActionBar.sendActionBar(player, text);
    }

    if (tool.getXp() >= tool.getMaxXp()) {
      int newLevel = tool.getLevel() + 1;

      final LevelItemsLevelIncreaseEvent levelEvent =
        new LevelItemsLevelIncreaseEvent(tool, player, newLevel, false);

      if (levelEvent.isCancelled()) {
        return;
      }

      tool.setXp(LevelItemsUtil.round(Math.abs(tool.getXp() - tool.getMaxXp()), 1));
      tool.setLevel(levelEvent.getNewLevel());

      // handleReward(tool, player);

      final ConfigurationSection soundCs =
        LevelItemsPlugin.getInstance().getConfig().getConfigurationSection("level_up_sound");

      final String sound = soundCs.getString("sound", null);

      if (sound != null) {
        player.playSound(
          player.getLocation(),
          Sound.valueOf(sound),
          (float) soundCs.getDouble("pitch"),
          (float) soundCs.getDouble("volume"));
      }
    }

    LevelItemsUtil.setHand(player, tool.getItemStack());
  }

  //  private void handleReward(LevelToolsItem tool, Player player) {
  //    final ConfigurationSection rewardCs = getCsFromType(tool.getItemStack().getType());
  //
  //    for (String key : rewardCs.getKeys(false)) {
  //      if (!NumberUtils.isNumber(key) || tool.getLevel() != Integer.parseInt(key)) {
  //        continue;
  //      }
  //
  //      for (String rewardStr : rewardCs.getStringList(key)) {
  //        final String[] split = rewardStr.split(" ");
  //
  //        if (split.length < 2) {
  //          continue;
  //        }
  //
  //        RewardType.fromConfigKey(split[0].toLowerCase(Locale.ROOT).trim())
  //            .ifPresent(type -> type.apply(tool, split, player));
  //      }
  //
  //      return;
  //    }
  //  }

  //  private ConfigurationSection getCsFromType(Material material) {
  //    if (LevelToolsUtil.isSword(material)) {
  //      return
  // LevelToolsPlugin.getInstance().getConfig().getConfigurationSection("sword_rewards");
  //    } else if (LevelToolsUtil.isProjectileShooter(material)) {
  //      return LevelToolsPlugin.getInstance().getConfig().getConfigurationSection("bow_rewards");
  //    } else {
  //      return LevelToolsPlugin.getInstance().getConfig().getConfigurationSection("tool_rewards");
  //    }
  //  }
}
