package com.spacefarm.inventory;

public class BioCompost extends Item {
    public BioCompost() {
        super("Біо-компост", "Артефакт для фази дерева №1");
    }
    @Override
    public ItemType getType() { return ItemType.BIO_COMPOST; }
}
