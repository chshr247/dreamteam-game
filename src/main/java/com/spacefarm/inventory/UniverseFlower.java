package com.spacefarm.inventory;

public class UniverseFlower extends Item {
    public UniverseFlower() {
        super("Всесвітня квітка", "Предмет фази дерева 4. Ціна в магазині: $4000");
    }
    @Override
    public ItemType getType() { return ItemType.UNIVERSE_FLOWER; }
}