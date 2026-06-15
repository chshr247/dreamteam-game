package com.spacefarm.inventory;

public class LivingDew extends Item {
    public LivingDew() {
        super("Жива роса", "Предмет фази дерева 2. Ціна в магазині: $1000");
    }
    @Override
    public ItemType getType() { return ItemType.LIVING_DEW; }
}