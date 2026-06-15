package com.spacefarm.inventory;

public class BioCompost extends Item {
    public BioCompost() {
        super("Біокомпост", "Предмет фази дерева 1. Ціна в магазині: $500");
    }
    @Override
    public ItemType getType() { return ItemType.BIO_COMPOST; }
}