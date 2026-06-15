package com.spacefarm.inventory;

public class LivingDew extends Item {
    public LivingDew() {
        super("Жива Роса", "Артефакт для фази дерева №2");
    }
    @Override
    public ItemType getType() { return ItemType.LIVING_DEW; }
}