package me.byteful.plugin.levelitems;

import com.google.common.base.Strings;
import de.tr7zw.changeme.nbtapi.NBTItem;
import me.byteful.plugin.levelitems.api.item.LeveledItem;
import me.byteful.plugin.levelitems.api.item.impl.NBTLeveledItem;
import me.byteful.plugin.levelitems.api.item.impl.PDCLeveledItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import redempt.redlib.RedLib;
import redempt.redlib.misc.FormatUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public final class LevelItemsUtil {
  public static String getProgressBar(
    double percent,
    int totalBars,
    String prefixSymbol,
    String suffixSymbol,
    String barSymbol,
    ChatColor prefixColor,
    ChatColor suffixColor,
    ChatColor completedColor,
    ChatColor placeholderColor) {
    int progressBars = roundDown(totalBars * percent);

    return colorize(
      ""
        + prefixColor
        + prefixSymbol
        + Strings.repeat("" + completedColor + barSymbol, progressBars)
        + Strings.repeat("" + placeholderColor + barSymbol, Math.abs(totalBars - progressBars))
        + suffixColor
        + suffixSymbol);
  }

  public static String colorize(String str) {
    return FormatUtils.color(str, true);
  }

  public static double getCombatModifier(EntityType entityType) {
    final ConfigurationSection combat_xp_modifiers =
      LevelItemsPlugin.getInstance().getConfig().getConfigurationSection("combat_xp_modifiers");

    for (String modifier : combat_xp_modifiers.getKeys(false)) {
      if (modifier.equalsIgnoreCase(entityType.name())) {
        final ConfigurationSection modifierCs =
          combat_xp_modifiers.getConfigurationSection(modifier);

        return round(
          ThreadLocalRandom.current()
            .nextDouble(modifierCs.getDouble("min"), modifierCs.getDouble("max")),
          1);
      }
    }

    final ConfigurationSection default_combat_xp_modifier =
      LevelItemsPlugin.getInstance()
        .getConfig()
        .getConfigurationSection("default_combat_xp_modifier");

    return round(
      ThreadLocalRandom.current()
        .nextDouble(
          default_combat_xp_modifier.getDouble("min"),
          default_combat_xp_modifier.getDouble("max")),
      1);
  }

  public static double getBlockModifier(Material material) {
    final ConfigurationSection block_xp_modifiers =
      LevelItemsPlugin.getInstance().getConfig().getConfigurationSection("block_xp_modifiers");

    for (String modifier : block_xp_modifiers.getKeys(false)) {
      if (modifier.equalsIgnoreCase(material.name())) {
        final ConfigurationSection modifierCs =
          block_xp_modifiers.getConfigurationSection(modifier);

        return round(
          ThreadLocalRandom.current()
            .nextDouble(modifierCs.getDouble("min"), modifierCs.getDouble("max")),
          1);
      }
    }

    final ConfigurationSection default_block_xp_modifier =
      LevelItemsPlugin.getInstance()
        .getConfig()
        .getConfigurationSection("default_block_xp_modifier");

    return round(
      ThreadLocalRandom.current()
        .nextDouble(
          default_block_xp_modifier.getDouble("min"),
          default_block_xp_modifier.getDouble("max")),
      1);
  }

  public static boolean isSupportedItem(Material material) {
    return false;
  }

  public static ItemStack getHand(Player player) {
    return RedLib.MID_VERSION >= 9
      ? player.getInventory().getItemInMainHand().clone()
      : player.getItemInHand().clone();
  }

  public static void setHand(Player player, ItemStack stack) {
    if (RedLib.MID_VERSION >= 9) {
      player.getInventory().setItemInMainHand(stack);
    } else {
      player.setItemInHand(stack);
    }
  }

  public static String createDefaultProgressBar(double xp, double maxXp) {
    ConfigurationSection cs =
      LevelItemsPlugin.getInstance().getConfig().getConfigurationSection("progress_bar");

    return LevelItemsUtil.getProgressBar(
      (xp / maxXp),
      cs.getInt("total_bars"),
      cs.getString("prefix_symbol"),
      cs.getString("suffix_symbol"),
      cs.getString("bar_symbol"),
      ChatColor.getByChar(cs.getString("prefix_color")),
      ChatColor.getByChar(cs.getString("suffix_color")),
      ChatColor.getByChar(cs.getString("completed_color")),
      ChatColor.getByChar(cs.getString("placeholder_color")));
  }

  public static double round(double value, int places) {
    if (places < 0) throw new IllegalArgumentException();

    BigDecimal bd = BigDecimal.valueOf(value);
    bd = bd.setScale(places, RoundingMode.HALF_UP);

    return bd.doubleValue();
  }

  public static int roundDown(double value) {
    BigDecimal bd = BigDecimal.valueOf(value);
    bd = bd.setScale(1, RoundingMode.DOWN);

    return bd.intValue();
  }

  public static LeveledItem createLeveledItem(ItemStack stack) {
    if (RedLib.MID_VERSION >= 14) {
      if (RedLib.MID_VERSION < 18) {
        final NBTItem nbt = new NBTItem(stack);
        if (nbt.getKeys().stream().anyMatch(s -> s.startsWith("levelTools"))) {
          return new NBTLeveledItem(
            stack); // Support tools created with "old" NBT system for 1.14+.
        }
      }

      return new PDCLeveledItem(stack);
    } else {
      return new NBTLeveledItem(stack);
    }
  }

  public static ItemStack buildItemStack(
    ItemStack stack, Map<Enchantment, Integer> enchantments, int level, double xp, double maxXp) {
    final ConfigurationSection cs =
      LevelItemsPlugin.getInstance().getConfig().getConfigurationSection("display");
    List<String> lore = cs.getStringList("default");

    for (String key : cs.getKeys(false)) {
      if (key.equalsIgnoreCase(stack.getType().name())) {
        lore = cs.getStringList(key);
      }
    }

    lore =
      lore.stream()
        .map(
          str ->
            colorize(
              str.replace("{level}", String.valueOf(level))
                .replace("{xp}", String.valueOf(xp))
                .replace("{max_xp}", String.valueOf(maxXp))
                .replace(
                  "{progress_bar}",
                  LevelItemsUtil.createDefaultProgressBar(xp, maxXp))))
        .collect(Collectors.toList());

    final ItemMeta meta = stack.getItemMeta();
    assert meta != null : "ItemMeta is null! Should not happen.";
    meta.setLore(lore);
    for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
      meta.addEnchant(entry.getKey(), entry.getValue(), true);
    }
    if (LevelItemsPlugin.getInstance().getConfig().getBoolean("hide_attributes", true)) {
      meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
    }
    stack.setItemMeta(meta);

    return stack;
  }
}
