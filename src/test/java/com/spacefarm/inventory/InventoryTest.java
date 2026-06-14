package com.spacefarm.inventory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InventoryTest {
    private Inventory inventory;

    @BeforeEach
    void setUp() {
        inventory = new Inventory();
    }

    @Test
    void testInitialState() {
        assertEquals(24, inventory.getSize());
        assertNotNull(inventory.getItem(0));
        assertTrue(inventory.getItem(0) instanceof WateringCan);
        assertEquals(0, inventory.getSelectedSlot());
    }

    @Test
    void testAddItemToSpecificSlot() {
        Item item = new Seed(10);
        assertTrue(inventory.addItem(1, item));
        assertEquals(item, inventory.getItem(1));
    }

    @Test
    void testAddItemToOccupiedSlot() {
        Item item1 = new Seed(10);
        Item item2 = new Seed(5);
        inventory.addItem(1, item1);
        assertFalse(inventory.addItem(1, item2));
        assertEquals(item1, inventory.getItem(1));
    }

    @Test
    void testAddItemAutoFindSlot() {
        // Fill slots except one
        for (int i = 0; i < inventory.getSize() - 1; i++) {
            if (inventory.getItem(i) == null) {
                inventory.addItem(i, new Seed(1));
            }
        }
        
        Item newItem = new Seed(5);
        assertTrue(inventory.addItem(newItem));
        
        // Find where it was added
        boolean found = false;
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == newItem) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }

    @Test
    void testAddItemToFullInventory() {
        // Fill all slots
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.addItem(i, new Seed(1));
            }
        }
        
        assertFalse(inventory.addItem(new Seed(5)));
    }

    @Test
    void testRemoveItem() {
        Item item = new Seed(10);
        inventory.addItem(1, item);
        Item removed = inventory.removeItem(1);
        assertEquals(item, removed);
        assertNull(inventory.getItem(1));
    }

    @Test
    void testSwapItems() {
        Item item1 = new Seed(10);
        Item item2 = new Seed(5);
        inventory.addItem(1, item1);
        inventory.addItem(2, item2);
        
        inventory.swapItems(1, 2);
        
        assertEquals(item2, inventory.getItem(1));
        assertEquals(item1, inventory.getItem(2));
    }

    @Test
    void testSwapWithInvalidIndices() {
        Item item = new Seed(10);
        inventory.addItem(1, item);
        
        // Should not throw exception and item should stay
        inventory.swapItems(1, 100);
        assertEquals(item, inventory.getItem(1));
        
        inventory.swapItems(-1, 1);
        assertEquals(item, inventory.getItem(1));
    }

    @Test
    void testExpandInventory() {
        int oldSize = inventory.getSize();
        inventory.expandInventory();
        assertEquals(oldSize + 8, inventory.getSize());
        
        // Verify items are preserved
        assertTrue(inventory.getItem(0) instanceof WateringCan);
    }

    @Test
    void testPlantFoodStacking() {
        inventory.addPlantFood(5);
        inventory.addPlantFood(3);
        
        int plantFoodCount = 0;
        int totalQuantity = 0;
        for (Item item : inventory.getSlots()) {
            if (item instanceof PlantFood) {
                plantFoodCount++;
                totalQuantity += ((PlantFood) item).getQuantity();
            }
        }
        
        assertEquals(1, plantFoodCount, "Should stack plant food in one slot");
        assertEquals(8, totalQuantity);
    }

    @Test
    void testPlantFoodWhenFull() {
        // Fill inventory with Seeds
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.addItem(i, new Seed(1));
            }
        }
        
        // Should fail to add new plant food stack if inventory is full
        assertFalse(inventory.addPlantFood(5));
    }

    @Test
    void testTreePhaseItems() {
        assertFalse(inventory.hasTreePhaseItem(0));
        
        Item compost = new BioCompost();
        inventory.addItem(compost);
        
        assertTrue(inventory.hasTreePhaseItem(0));
        assertTrue(inventory.removeTreePhaseItem(0));
        assertFalse(inventory.hasTreePhaseItem(0));
    }
}
