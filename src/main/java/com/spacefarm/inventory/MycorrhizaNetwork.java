package com.spacefarm.inventory;

public class MycorrhizaNetwork extends Item {
    public MycorrhizaNetwork() {
        super("Мережа Мікоризи", "Артефакт для фази дерева №3");
    }
    @Override
    public ItemType getType() { return ItemType.MYCORRHIZA_NETWORK; }
}