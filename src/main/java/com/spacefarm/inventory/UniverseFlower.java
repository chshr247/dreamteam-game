package com.spacefarm.inventory;

public class UniverseFlower extends Item {
    public UniverseFlower() {
        super("Квітка Всесвіту", "Артефакт для фази дерева №4");
    }
    @Override
    public ItemType getType() { return ItemType.UNIVERSE_FLOWER; }
}
