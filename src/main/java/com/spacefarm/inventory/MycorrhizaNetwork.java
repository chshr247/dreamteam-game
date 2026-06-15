package com.spacefarm.inventory;

public class MycorrhizaNetwork extends Item {
    public MycorrhizaNetwork() {
        super("Мікоризна мережа", "Предмет фази дерева 3. Ціна в магазині: $2000");
    }
    @Override
    public ItemType getType() { return ItemType.MYCORRHIZA_NETWORK; }
}