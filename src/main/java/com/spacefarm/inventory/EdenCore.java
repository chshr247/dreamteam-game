package com.spacefarm.inventory;

public class EdenCore extends Item {
    public EdenCore() {
        super("Ядро Едему", "Артефакт для фази дерева №5");
    }
    @Override
    public ItemType getType() { return ItemType.EDEN_CORE; }
}