package com.spacefarm.farming;

public class FarmingConstants {
    // Growth stage timing (in seconds)
    public static float STAGE_1_DURATION = 20f;  // Seed to sprout
    public static float STAGE_2_DURATION = 20f;  // Sprout to young plant
    public static float STAGE_3_DURATION = 15f;  // Young plant to mature

    // Water system timing (in seconds)
    public static float WATERING_DURATION = 10f;  // How long water effect lasts
    public static float DRYING_DURATION = 15f;    // How long until plant dries without water

    // After this total time, the plant dies even if it was watered on time
    public static float MAX_PLANT_LIFETIME = 80f;

    // Growth stages
    public enum GrowthStage {
        SEED,      // Stage 0 - Initial seed
        SPROUT,    // Stage 1 - After STAGE_1_DURATION
        YOUNG,     // Stage 2 - After STAGE_2_DURATION
        MATURE     // Stage 3 - After STAGE_3_DURATION
    }

    // Water states
    public enum WaterState {
        WELL_WATERED,  // Recently watered
        NORMAL,        // Has water but not recently watered
        THIRSTY,       // Needs water soon
        DYING          // Critical need for water
    }
    // Type of seeds
    public enum CropType {
        DEFAULT,
        EPIC,
        LEGENDARY
    }

}

