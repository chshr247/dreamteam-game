package com.spacefarm.inventory;

// Об'єкт який випадає після зачистки звичайної локації
public class Crystal extends Item {
    public Crystal() {
        super("Кристал", "Цінний космічний кристал, здобутий під час пошуку");
    }

    @Override
    public ItemType getType() {
        return ItemType.CRYSTAL;
    }
}