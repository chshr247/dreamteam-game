package com.spacefarm.save;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.spacefarm.inventory.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SaveManagerTest {

    private static Gson gson;

    @BeforeAll
    static void init() {
        // Initialize GSON with the same adapter as SaveManager
        gson = new GsonBuilder()
                .registerTypeAdapter(Item.class, new SaveManager.ItemAdapter())
                .setPrettyPrinting()
                .create();
    }

    @Test
    void testItemSerializationPolymorphism() {
        Item[] originalSlots = new Item[4];
        originalSlots[0] = WateringCan.getInstance();
        originalSlots[1] = new Seed(10);
        originalSlots[2] = Sickle.getInstance();
        originalSlots[3] = new Crystal();

        SaveState.InventoryData data = new SaveState.InventoryData();
        data.slots = originalSlots;
        data.selectedSlot = 1;

        String json = gson.toJson(data);
        SaveState.InventoryData deserialized = gson.fromJson(json, SaveState.InventoryData.class);

        assertEquals(originalSlots.length, deserialized.slots.length);
        assertTrue(deserialized.slots[0] instanceof WateringCan);
        assertTrue(deserialized.slots[1] instanceof Seed);
        assertEquals(10, ((Seed)deserialized.slots[1]).getQuantity());
        assertTrue(deserialized.slots[2] instanceof Sickle);
        assertTrue(deserialized.slots[3] instanceof Crystal);
        assertEquals(1, deserialized.selectedSlot);
    }

    @Test
    void testItemSerializationAllTypes() {
        Item[] originalSlots = new Item[]{
            new BioCompost(),
            new LivingDew(),
            new MycorrhizaNetwork(),
            new UniverseFlower(),
            new EdenCore(),
            new RareSeed(5),
            new LegendarySeed(1),
            new PlantFood(20)
        };

        for (Item item : originalSlots) {
            String json = gson.toJson(item, Item.class);
            Item deserialized = gson.fromJson(json, Item.class);
            assertNotNull(deserialized, "Failed to deserialize " + item.getType());
            assertEquals(item.getClass(), deserialized.getClass());
        }
    }

    @Test
    void testNullItemSerialization() {
        Item[] slots = new Item[2];
        slots[0] = null;
        slots[1] = new Seed(5);

        String json = gson.toJson(slots);
        Item[] deserialized = gson.fromJson(json, Item[].class);

        assertNull(deserialized[0]);
        assertNotNull(deserialized[1]);
    }

    @Test
    void testUnknownItemTypeThrowsException() {
        String badJson = "{\"itemType\":\"NON_EXISTENT_TYPE\"}";
        assertThrows(Exception.class, () -> {
            gson.fromJson(badJson, Item.class);
        });
    }

    @Test
    void testEmptyJsonReturnsNull() {
        Item item = gson.fromJson("{}", Item.class);
        assertNull(item);
    }
}
