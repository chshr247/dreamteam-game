package com.spacefarm.inventory;

// Об'єкт який випадає після зачистки звичайної локації
public class Crystal extends Item {
    public Crystal() {
        super("Кристал", "Цінний космічний кристал, отриманий після зачистки великого кристалу");
    }

    @Override
    public ItemType getType() {
        return ItemType.CRYSTAL;
    }
}