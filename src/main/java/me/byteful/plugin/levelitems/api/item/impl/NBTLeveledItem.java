package me.byteful.plugin.levelitems.api.item.impl;

import de.tr7zw.changeme.nbtapi.NBTCompoundList;
import de.tr7zw.changeme.nbtapi.NBTItem;
import de.tr7zw.changeme.nbtapi.NBTListCompound;
import me.byteful.plugin.levelitems.LevelItemsUtil;
import me.byteful.plugin.levelitems.api.item.LeveledItem;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class NBTLeveledItem implements LeveledItem {
  @NotNull
  public static final String LEVEL_KEY = "levelToolsLevel", XP_KEY = "levelToolsXp";

  @NotNull
  private NBTItem nbt;
  @NotNull
  private Map<Enchantment, Integer> enchantments;
  @NotNull
  private Map<String, Double> attributes;

  public NBTLeveledItem(@NotNull ItemStack stack) {
    this.nbt = new NBTItem(stack);
    this.enchantments = new HashMap<>();
    this.attributes = new HashMap<>();
  }

  @NotNull
  @Override
  public ItemStack getItemStack() {
    final ItemStack stack =
      LevelItemsUtil.buildItemStack(
        nbt.getItem().clone(), enchantments, getLevel(), getXp(), getMaxXp());

    nbt = new NBTItem(stack);
    final NBTCompoundList attr = nbt.getCompoundList("AttributeModifiers");
    attributes.forEach(
      (attribute, modifier) -> {
        final NBTListCompound list = attr.addCompound();
        list.setDouble("Amount", modifier);
        list.setString("AttributeName", attribute);
        list.setString("Name", attribute);
        list.setInteger("Operation", 0);
        list.setInteger("UUIDLeast", 59664);
        list.setInteger("UUIDMost", 31453);
      });

    return nbt.getItem();
  }

  @Override
  public int getLevel() {
    if (!nbt.hasKey(LEVEL_KEY)) {
      setLevel(0);
    }

    return nbt.getInteger(LEVEL_KEY);
  }

  @Override
  public void setLevel(int level) {
    if (level < 0) {
      setLevel0(0);

      return;
    }

    setLevel0(level);
  }

  @Override
  public double getXp() {
    if (!nbt.hasKey(XP_KEY)) {
      setXp(0.0D);
    }

    return nbt.getDouble(XP_KEY);
  }

  @Override
  public void setXp(double xp) {
    if (xp < 0.0D) {
      setXp0(0.0D);

      return;
    }

    setXp0(xp);
  }

  private void setLevel0(int level) {
    nbt.setInteger(LEVEL_KEY, level);
  }

  private void setXp0(double xp) {
    nbt.setDouble(XP_KEY, xp);
  }

  @Override
  public void enchant(Enchantment enchantment, int level) {
    enchantments.put(enchantment, level);
  }

  @Override
  public void modifyAttribute(String attribute, double modifier) {
    attributes.put(attribute, modifier);
  }

  @NotNull
  public NBTItem getNBT() {
    return nbt;
  }

  public void setNBT(@NotNull NBTItem nbt) {
    this.nbt = nbt;
  }

  @NotNull
  public Map<Enchantment, Integer> getEnchantments() {
    return enchantments;
  }

  public void setEnchantments(@NotNull Map<Enchantment, Integer> enchantments) {
    this.enchantments = enchantments;
  }

  public @NotNull Map<String, Double> getAttributes() {
    return attributes;
  }

  public void setAttributes(@NotNull Map<String, Double> attributes) {
    this.attributes = attributes;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    NBTLeveledItem that = (NBTLeveledItem) o;
    return nbt.equals(that.nbt)
      && enchantments.equals(that.enchantments)
      && attributes.equals(that.attributes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(nbt, enchantments, attributes);
  }

  @Override
  public String toString() {
    return "NBTLevelToolsItem{"
      + "nbt="
      + nbt
      + ", enchantments="
      + enchantments
      + ", attributes="
      + attributes
      + '}';
  }
}
