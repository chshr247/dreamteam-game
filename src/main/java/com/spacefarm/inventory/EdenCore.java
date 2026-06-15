package com.spacefarm.inventory;

public class EdenCore extends Item {
    public EdenCore() {
        super("Ядро Едему", "Предмет фази дерева 5. Ціна в магазині: $8000");
    }
    @Override
    public ItemType getType() { return ItemType.EDEN_CORE; }
}