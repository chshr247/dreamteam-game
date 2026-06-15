package com.spacefarm.farming;
import com.spacefarm.world.TileCoord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FarmingSystemTest {

    private FarmingSystem farmingSystem;

    @BeforeEach
    void setUp() {
        farmingSystem = new FarmingSystem(0, 0, 10, 10);
    }

    @Test
    void testPlantSeedAtValidCoordinate() {
        assertTrue(farmingSystem.plantSeed(2, 3, FarmingConstants.CropType.DEFAULT));
        assertTrue(farmingSystem.hasCrop(2, 3));
        assertEquals(1, farmingSystem.getCropCount());

        Crop crop = farmingSystem.getCrop(2, 3);
        assertNotNull(crop);
        assertEquals(FarmingConstants.CropType.DEFAULT, crop.getType());
        assertEquals(FarmingConstants.GrowthStage.SEED, crop.getGrowthStage());
    }

    @Test
    void testPlantSeedOutOfBoundsFails() {
        assertFalse(farmingSystem.plantSeed(new TileCoord(20, 20), FarmingConstants.CropType.DEFAULT));
        assertFalse(farmingSystem.plantSeed(new TileCoord(-1, 0), FarmingConstants.CropType.DEFAULT));
        assertEquals(0, farmingSystem.getCropCount());
    }

    @Test
    void testPlantSeedOnOccupiedTileFails() {
        assertTrue(farmingSystem.plantSeed(1, 1, FarmingConstants.CropType.DEFAULT));
        assertFalse(farmingSystem.plantSeed(1, 1, FarmingConstants.CropType.EPIC));
        assertEquals(1, farmingSystem.getCropCount());

        // Оригінальна рослина має лишитись недоторканою
        assertEquals(FarmingConstants.CropType.DEFAULT, farmingSystem.getCrop(1, 1).getType());
    }

    @Test
    void testWaterCropSetsWellWateredState() {
        farmingSystem.plantSeed(4, 4, FarmingConstants.CropType.DEFAULT);
        Crop crop = farmingSystem.getCrop(4, 4);

        // Спочатку дамо рослині засохнути
        crop.update(FarmingConstants.DRYING_DURATION + 1f);
        assertEquals(FarmingConstants.WaterState.DYING, crop.getWaterState());

        assertTrue(farmingSystem.waterCrop(new TileCoord(4, 4)));
        assertEquals(FarmingConstants.WaterState.WELL_WATERED, crop.getWaterState());
        assertEquals(0f, crop.getTimeSinceWatered());
    }

    @Test
    void testWaterCropOnEmptyOrInvalidTileFails() {
        // Немає рослини на цій клітинці
        assertFalse(farmingSystem.waterCrop(new TileCoord(5, 5)));

        // Поза межами карти
        assertFalse(farmingSystem.waterCrop(new TileCoord(50, 50)));
    }

    @Test
    void testHarvestMatureCropRemovesIt() {
        farmingSystem.plantSeed(3, 3, FarmingConstants.CropType.DEFAULT);
        Crop crop = farmingSystem.getCrop(3, 3);

        // Проходимо всі стадії росту до MATURE
        crop.update(FarmingConstants.STAGE_1_DURATION + 0.1f);
        crop.update(FarmingConstants.STAGE_2_DURATION + 0.1f);
        crop.update(FarmingConstants.STAGE_3_DURATION + 0.1f);

        assertEquals(FarmingConstants.GrowthStage.MATURE, crop.getGrowthStage());
        assertTrue(farmingSystem.harvestCrop(new TileCoord(3, 3)));
        assertFalse(farmingSystem.hasCrop(3, 3));
        assertEquals(0, farmingSystem.getCropCount());
    }

    @Test
    void testHarvestImmatureCropFails() {
        farmingSystem.plantSeed(6, 6, FarmingConstants.CropType.DEFAULT);

        assertFalse(farmingSystem.harvestCrop(new TileCoord(6, 6)));
        assertTrue(farmingSystem.hasCrop(6, 6));
        assertEquals(1, farmingSystem.getCropCount());
    }

    @Test
    void testRemoveCrop() {
        farmingSystem.plantSeed(7, 7, FarmingConstants.CropType.LEGENDARY);
        assertTrue(farmingSystem.removeCrop(new TileCoord(7, 7)));
        assertFalse(farmingSystem.hasCrop(7, 7));
        assertEquals(0, farmingSystem.getCropCount());

        // Повторне видалення має повернути false
        assertFalse(farmingSystem.removeCrop(new TileCoord(7, 7)));
    }

    @Test
    void testCropGrowthStageProgression() {
        Crop crop = new Crop(FarmingConstants.CropType.DEFAULT);
        assertEquals(FarmingConstants.GrowthStage.SEED, crop.getGrowthStage());

        crop.update(FarmingConstants.STAGE_1_DURATION + 0.1f);
        assertEquals(FarmingConstants.GrowthStage.SPROUT, crop.getGrowthStage());

        crop.update(FarmingConstants.STAGE_2_DURATION + 0.1f);
        assertEquals(FarmingConstants.GrowthStage.YOUNG, crop.getGrowthStage());

        crop.update(FarmingConstants.STAGE_3_DURATION + 0.1f);
        assertEquals(FarmingConstants.GrowthStage.MATURE, crop.getGrowthStage());

        // MATURE — кінцева стадія, далі не змінюється
        crop.update(FarmingConstants.STAGE_3_DURATION + 0.1f);
        assertEquals(FarmingConstants.GrowthStage.MATURE, crop.getGrowthStage());
    }

    @Test
    void testCropDiesWithoutWaterAndIsRemovedOnSystemUpdate() {
        farmingSystem.plantSeed(8, 8, FarmingConstants.CropType.DEFAULT);
        Crop crop = farmingSystem.getCrop(8, 8);
        assertFalse(crop.isDead());

        // Не поливаємо набагато довше за DRYING_DURATION
        float deadlyDelta = FarmingConstants.DRYING_DURATION * 1.5f + 1f;

        assertEquals(1, farmingSystem.getCropCount());
        farmingSystem.update(deadlyDelta);

        assertEquals(FarmingConstants.WaterState.DYING, crop.getWaterState());
        assertTrue(crop.isDead());
        assertEquals(0, farmingSystem.getCropCount());
        assertFalse(farmingSystem.hasCrop(8, 8));
    }
}