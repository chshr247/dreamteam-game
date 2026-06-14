package com.spacefarm.session;

import com.badlogic.gdx.Gdx;
import com.spacefarm.inventory.Item;
import com.spacefarm.inventory.PlantFood;
import com.spacefarm.render.TutorialOverlay;
import java.util.ArrayList;
import java.util.List;

public class TutorialManager {

    public enum TaskType {
        INFO,
        SELECT_SCYTHE,
        OPEN_SEED_WHEEL,
        SPIN_WHEEL,
        WAIT_WHEEL_REWARD,
        PLANT_SEED,
        WATER_AND_HARVEST,
        OPEN_TREE_MENU,
        SCAVENGE_CRYSTAL,
        OPEN_DRONE_CONSOLE,
        FINAL
    }

    private static class Step {
        String title;
        String message;
        TaskType taskType;
        boolean isPrompt;

        Step(String title, String message, TaskType taskType, boolean isPrompt) {
            this.title = title;
            this.message = message;
            this.taskType = taskType;
            this.isPrompt = isPrompt;
        }
    }

    private final GameSession session;
    private final TutorialOverlay overlay;
    private final List<Step> steps = new ArrayList<>();
    private int currentStepIndex = -1;
    private boolean isActive = false;

    private int initialCropCount = 0;
    private int initialPlantFoodCount = 0;
    private int initialCrystalCount = 0;

    public TutorialManager(GameSession session) {
        this.session = session;
        this.overlay = new TutorialOverlay();
        initSteps();
    }

    private void initSteps() {
        steps.add(new Step("Вітаємо, Фермере!", "Ваша місія - відновити життя на цій планеті. Готові розпочати навчання?", TaskType.INFO, true));
        steps.add(new Step("Інструменти", "Для збору врожаю вам знадобиться Серп. Виберіть його, натиснувши клавішу '3' або клікнувши по 3-му слоту.", TaskType.SELECT_SCYTHE, false));
        steps.add(new Step("Колесо Фортуни", "Щоб отримати насіння, знайдіть у пустелі Колесо Фортуни та відкрийте його меню (клікніть по ньому).", TaskType.OPEN_SEED_WHEEL, false));
        steps.add(new Step("Крути Колесо", "Натисніть центральну кнопку 'КРУТИТИ', щоб запустити обертання та отримати свою нагороду.", TaskType.SPIN_WHEEL, false));
        steps.add(new Step("Нагорода", "Зачекайте, поки ви отримаєте насіння, і закрийте вікно Колеса.", TaskType.WAIT_WHEEL_REWARD, false));
        steps.add(new Step("Посадка", "Тепер посадіть насіння на грядку. Оберіть насіння (клавіша '2') та клікніть по вільній грядці.", TaskType.PLANT_SEED, false));
        steps.add(new Step("Догляд та Збір", "Полийте рослину лійкою (клавіша '1'), дочекайтеся дозрівання та зберіть урожай Серпом (клавіша '3').", TaskType.WATER_AND_HARVEST, false));
        steps.add(new Step("Світове Дерево", "Відкрийте меню Світового Дерева в центрі бази, щоб побачити прогрес відновлення атмосфери.", TaskType.OPEN_TREE_MENU, false));
        steps.add(new Step("Скавенджинг", "Знайдіть поклади кристалів у пустелі та розпочніть пошук. Під час пошуку витрачається кисень, для його поповнення оберіть врожай в інвентарі і натисніть E. Кристали потрібні для торгівлі з дроном. Дочекайтеся появи кристала в інвентарі.", TaskType.SCAVENGE_CRYSTAL, false));
        steps.add(new Step("Консоль Дрона", "Відкрийте консоль торгового дрона на краю бази. Тут ви зможете обмінювати ресурси.", TaskType.OPEN_DRONE_CONSOLE, false));
        steps.add(new Step("Фінал", "Чудово! Ви засвоїли основні механіки. Пам'ятайте про рівень кисню! Удачі, Фермере!", TaskType.FINAL, false));
    }

    public void start() {
        isActive = true;
        currentStepIndex = 0;
        showCurrentStep();
    }

    public void update(float dt) {
        if (!isActive || currentStepIndex < 0 || currentStepIndex >= steps.size()) return;

        Step step = steps.get(currentStepIndex);
        TutorialOverlay.Action action = overlay.handleInput();

        if (step.isPrompt) {
            if (action == TutorialOverlay.Action.YES) {
                nextStep();
            } else if (action == TutorialOverlay.Action.NO) {
                stop();
            }
            return;
        }

        if (step.taskType == TaskType.INFO || step.taskType == TaskType.FINAL) {
            if (action == TutorialOverlay.Action.NEXT) {
                if (step.taskType == TaskType.FINAL) stop();
                else nextStep();
            }
            return;
        }

        checkTaskCompletion(step);
    }

    private void checkTaskCompletion(Step step) {
        boolean completed = false;
        switch (step.taskType) {
            case SELECT_SCYTHE:
                if (session.getInventory().getSelectedSlot() == 2) completed = true;
                break;
            case OPEN_SEED_WHEEL:
                if (session.getSeedWheelOverlay().isVisible()) completed = true;
                break;
            case SPIN_WHEEL:
                if (session.getSeedWheelOverlay().isSpinning()) completed = true;
                break;
            case WAIT_WHEEL_REWARD:
                if (!session.getSeedWheelOverlay().isVisible()) completed = true;
                break;
            case PLANT_SEED:
                if (session.getFarmingSystem().getCropCount() > initialCropCount) completed = true;
                break;
            case WATER_AND_HARVEST:
                if (getPlantFoodCount() > initialPlantFoodCount) completed = true;
                break;
            case OPEN_TREE_MENU:
                if (session.getTreeBoxUI().isVisible()) completed = true;
                break;
            case SCAVENGE_CRYSTAL:
                if (getCrystalCount() > initialCrystalCount) completed = true;
                break;
            case OPEN_DRONE_CONSOLE:
                if (session.getDroneConsoleOverlay().isVisible()) completed = true;
                break;
        }

        if (completed) {
            nextStep();
        }
    }

    private void nextStep() {
        currentStepIndex++;
        if (currentStepIndex < steps.size()) {
            initialCropCount = session.getFarmingSystem().getCropCount();
            initialPlantFoodCount = getPlantFoodCount();
            initialCrystalCount = getCrystalCount();
            showCurrentStep();
        } else {
            stop();
        }
    }

    private void showCurrentStep() {
        Step step = steps.get(currentStepIndex);
        if (step.isPrompt) {
            overlay.showPrompt(step.title, step.message);
        } else {
            boolean showNext = (step.taskType == TaskType.INFO || step.taskType == TaskType.FINAL);
            overlay.showStep(step.title, step.message, showNext);
        }
    }

    public void stop() {
        isActive = false;
        overlay.hide();
    }

    public boolean isActive() {
        return isActive;
    }

    public boolean isBlockInput() {
        if (!isActive) return false;
        Step step = steps.get(currentStepIndex);
        return step.taskType == TaskType.INFO || step.taskType == TaskType.FINAL || step.isPrompt;
    }

    private int getPlantFoodCount() {
        int count = 0;
        for (Item item : session.getInventory().getSlots()) {
            if (item != null && item.getType() == Item.ItemType.PLANT_FOOD) {
                count += ((PlantFood) item).getQuantity();
            }
        }
        return count;
    }

    private int getCrystalCount() {
        int count = 0;
        for (Item item : session.getInventory().getSlots()) {
            if (item != null && item.getType() == Item.ItemType.CRYSTAL) {
                count++;
            }
        }
        return count;
    }

    public void render(int sw, int sh) {
        if (isActive) overlay.render(sw, sh);
    }

    public void dispose() {
        overlay.dispose();
    }
}
