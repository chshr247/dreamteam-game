package com.spacefarm;

public enum DifficultyLevel {
    EASY   (5f,               10,                   15000f),
    NORMAL (10f,               5,                    100000f),
    HARD   (15f,               2,                    10000f);

    public final float oxygenDecreaseAmount;
    public final int startingGardenBeds;
    public final float startingMoney;

    DifficultyLevel(float oxygenDecreaseAmount, int startingGardenBeds, float startingMoney) {
        this.oxygenDecreaseAmount = oxygenDecreaseAmount;
        this.startingGardenBeds  = startingGardenBeds;
        this.startingMoney       = startingMoney;
    }
}

