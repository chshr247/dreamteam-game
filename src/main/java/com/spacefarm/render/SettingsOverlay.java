package com.spacefarm.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.spacefarm.audio.AudioManager;

/**
 * Кнопка-шестеренка у правому верхньому куті + панель налаштувань.
 *
 * Панель містить:
 *   • повзунок гучності фонової музики
 *   • повзунок гучності фонових звуків
 *   • кнопку "Зберегти гру"
 *   • кнопку "Вийти з гри" (повернення у головне меню)
 *
 * Координати в обробниках — екранні (top-left), всередині переводяться у
 * bottom-left, як і скрізь у проєкті.
 */
public class SettingsOverlay {

    public enum Action { NONE, SAVE, EXIT }

    private final AudioManager audio;

    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch   batch;
    private final BitmapFont    titleFont;
    private final BitmapFont    labelFont;
    private final BitmapFont    btnFont;
    private final BitmapFont    smallFont;

    private boolean open = false;
    private Action  pending = Action.NONE;
    private float   savedMessageTimer = 0f; // показ "Гру збережено!" після натискання

    private boolean draggingMusic = false;
    private boolean draggingSfx   = false;

    // Геометрія (bottom-left), перераховується кожного кадру
    private final Rectangle gearRect   = new Rectangle();
    private final Rectangle panelRect  = new Rectangle();
    private final Rectangle musicTrack = new Rectangle();
    private final Rectangle sfxTrack   = new Rectangle();
    private final Rectangle btnSave    = new Rectangle();
    private final Rectangle btnExit    = new Rectangle();
    private final Rectangle btnClose   = new Rectangle();

    private static final float KNOB_R = 11f;

    public SettingsOverlay(AudioManager audio) {
        this.audio    = audio;
        shapeRenderer = new ShapeRenderer();
        batch         = new SpriteBatch();
        titleFont = FontUtils.createFont("fonts/ArialBold.ttf", 26);
        labelFont = FontUtils.createFont("fonts/ArialBold.ttf", 18);
        btnFont   = FontUtils.createFont("fonts/ArialBold.ttf", 20);
        smallFont = FontUtils.createFont("fonts/ArialBold.ttf", 15);
    }

    public boolean isOpen() { return open; }

    public void toggle() { open = !open; }

    /** GameApp читає й скидає дію (зберегти / вийти). */
    public Action pollAction() {
        Action a = pending;
        pending = Action.NONE;
        return a;
    }

    // ── Layout ────────────────────────────────────────────────────────────────

    private void computeLayout(float sw, float sh) {
        float g = 46f, m = 16f;
        gearRect.set(sw - g - m, sh - g - m, g, g);

        float pw = Math.min(sw * 0.62f, 460f);
        float ph = Math.min(sh * 0.72f, 400f);
        float px = (sw - pw) * 0.5f;
        float py = (sh - ph) * 0.5f;
        panelRect.set(px, py, pw, ph);

        float pad = pw * 0.10f;
        float trackW = pw - pad * 2f;
        float trackH = 8f;

        // зверху вниз
        float musicTrackY = py + ph - ph * 0.42f;
        float sfxTrackY   = py + ph - ph * 0.62f;
        musicTrack.set(px + pad, musicTrackY, trackW, trackH);
        sfxTrack.set(px + pad, sfxTrackY, trackW, trackH);

        float btnW = pw - pad * 2f;
        float btnH = ph * 0.13f;
        float saveY = py + ph * 0.20f;
        float exitY = py + ph * 0.05f;
        btnSave.set(px + pad, saveY, btnW, btnH);
        btnExit.set(px + pad, exitY, btnW, btnH);

        float cs = 30f;
        btnClose.set(px + pw - cs - 10f, py + ph - cs - 10f, cs, cs);
    }

    // ── Input ───────────────────────────────────────────────────────────────

    /**
     * Сумісність зі старим кодом GameInteractionService: повертає true, якщо
     * клік припадає на шестеренку/панель (і одразу його обробляє — перемикання
     * панелі, повзунки, кнопки). Висота екрана береться з Gdx.graphics.
     */
    public boolean isConsuming(int screenX, int screenY) {
        return handleTouchDown(screenX, screenY, Gdx.graphics.getHeight());
    }

    public boolean handleTouchDown(int screenX, int screenY, int screenHeight) {
        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();
        computeLayout(sw, sh);

        float x = screenX;
        float y = screenHeight - screenY;

        if (gearRect.contains(x, y)) {
            open = !open;
            return true;
        }

        if (!open) return false;

        if (btnClose.contains(x, y)) { open = false; return true; }
        if (btnSave.contains(x, y))  { pending = Action.SAVE; savedMessageTimer = 2.0f; return true; }
        if (btnExit.contains(x, y))  { pending = Action.EXIT; open = false; return true; }

        if (hitsTrack(musicTrack, x, y)) {
            draggingMusic = true;
            updateMusicFromX(x);
            return true;
        }
        if (hitsTrack(sfxTrack, x, y)) {
            draggingSfx = true;
            updateSfxFromX(x);
            return true;
        }

        if (panelRect.contains(x, y)) return true; // клік всередині панелі — поглинаємо

        // клік поза панеллю — закриваємо
        open = false;
        return true;
    }

    public boolean handleTouchDragged(int screenX, int screenY, int screenHeight) {
        if (!open) return false;
        float x = screenX;
        if (draggingMusic) { updateMusicFromX(x); return true; }
        if (draggingSfx)   { updateSfxFromX(x);   return true; }
        return false;
    }

    public boolean handleTouchUp() {
        boolean was = draggingMusic || draggingSfx;
        draggingMusic = false;
        draggingSfx = false;
        return was;
    }

    private boolean hitsTrack(Rectangle track, float x, float y) {
        return x >= track.x - KNOB_R && x <= track.x + track.width + KNOB_R
                && y >= track.y - KNOB_R && y <= track.y + track.height + KNOB_R;
    }

    private void updateMusicFromX(float x) {
        float v = MathUtils.clamp((x - musicTrack.x) / musicTrack.width, 0f, 1f);
        audio.setMusicVolume(v);
    }

    private void updateSfxFromX(float x) {
        float v = MathUtils.clamp((x - sfxTrack.x) / sfxTrack.width, 0f, 1f);
        audio.setSfxVolume(v);
    }

    // ── Render ────────────────────────────────────────────────────────────────

    public void render(int sw, int sh) {
        computeLayout(sw, sh);
        if (savedMessageTimer > 0f) savedMessageTimer -= Gdx.graphics.getDeltaTime();
        Gdx.gl.glEnable(GL20.GL_BLEND);

        drawGear();

        if (open) {
            drawDim(sw, sh);
            drawPanel();
        }
    }

    private void drawGear() {
        float cx = gearRect.x + gearRect.width * 0.5f;
        float cy = gearRect.y + gearRect.height * 0.5f;
        float r  = gearRect.width * 0.40f;

        // фон під кнопкою
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.05f, 0.10f, 0.16f, 0.92f);
        shapeRenderer.rect(gearRect.x, gearRect.y, gearRect.width, gearRect.height);
        shapeRenderer.end();

        // зубці
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.20f, 0.85f, 0.95f, 1f);
        int teeth = 8;
        float toothSize = r * 0.34f;
        for (int i = 0; i < teeth; i++) {
            float a = (float) (Math.PI * 2 * i / teeth);
            float tx = cx + MathUtils.cos(a) * (r + toothSize * 0.3f);
            float ty = cy + MathUtils.sin(a) * (r + toothSize * 0.3f);
            shapeRenderer.rect(tx - toothSize * 0.5f, ty - toothSize * 0.5f, toothSize, toothSize);
        }
        // тіло шестерні
        shapeRenderer.circle(cx, cy, r, 28);
        // отвір
        shapeRenderer.setColor(0.05f, 0.10f, 0.16f, 1f);
        shapeRenderer.circle(cx, cy, r * 0.42f, 24);
        shapeRenderer.end();

        // рамка
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.18f, 0.70f, 0.82f, 0.9f);
        shapeRenderer.rect(gearRect.x, gearRect.y, gearRect.width, gearRect.height);
        shapeRenderer.end();
    }

    private void drawDim(int sw, int sh) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, 0.45f);
        shapeRenderer.rect(0, 0, sw, sh);
        shapeRenderer.end();
    }

    private void drawPanel() {
        float px = panelRect.x, py = panelRect.y, pw = panelRect.width, ph = panelRect.height;
        float border = 4f;
        float cs = Math.min(pw, ph) * 0.03f;

        // фон + рамки
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.05f, 0.10f, 0.18f, 0.98f);
        shapeRenderer.rect(px, py, pw, ph);
        shapeRenderer.setColor(0.10f, 0.55f, 0.65f, 1f);
        shapeRenderer.rect(px, py + ph - border, pw, border);
        shapeRenderer.rect(px, py, pw, border);
        shapeRenderer.setColor(0.07f, 0.38f, 0.48f, 1f);
        shapeRenderer.rect(px, py, border, ph);
        shapeRenderer.rect(px + pw - border, py, border, ph);
        // кутові акценти
        shapeRenderer.setColor(0.18f, 0.80f, 0.92f, 1f);
        shapeRenderer.rect(px - 2f, py + ph - cs, cs, cs);
        shapeRenderer.rect(px + pw - cs + 2f, py + ph - cs, cs, cs);
        shapeRenderer.rect(px - 2f, py, cs, cs);
        shapeRenderer.rect(px + pw - cs + 2f, py, cs, cs);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.10f, 0.55f, 0.65f, 0.85f);
        shapeRenderer.rect(px, py, pw, ph);
        shapeRenderer.end();

        // повзунки (фон + заповнення)
        drawTrack(musicTrack, audio.getMusicVolume());
        drawTrack(sfxTrack, audio.getSfxVolume());

        // кнопки
        drawButtonBg(btnSave, 0.06f, 0.28f, 0.36f, 0.16f, 0.70f, 0.85f);
        drawButtonBg(btnExit, 0.22f, 0.06f, 0.06f, 0.85f, 0.22f, 0.22f);

        // кнопка закриття (X)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.8f, 0.4f, 0.4f, 1f);
        shapeRenderer.rect(btnClose.x, btnClose.y, btnClose.width, btnClose.height);
        shapeRenderer.line(btnClose.x + 7, btnClose.y + 7,
                btnClose.x + btnClose.width - 7, btnClose.y + btnClose.height - 7);
        shapeRenderer.line(btnClose.x + 7, btnClose.y + btnClose.height - 7,
                btnClose.x + btnClose.width - 7, btnClose.y + 7);
        shapeRenderer.end();

        // ── Текст ──
        GlyphLayout layout = new GlyphLayout();
        float cx = px + pw * 0.5f;
        batch.begin();

        // заголовок
        titleFont.setColor(0.20f, 0.88f, 1.00f, 1f);
        layout.setText(titleFont, "НАЛАШТУВАННЯ");
        titleFont.draw(batch, "НАЛАШТУВАННЯ", cx - layout.width * 0.5f, py + ph - ph * 0.07f);

        // підписи повзунків
        labelFont.setColor(0.75f, 0.90f, 0.95f, 1f);
        labelFont.draw(batch, "Музика", musicTrack.x, musicTrack.y + 34f);
        labelFont.draw(batch, "Фонові звуки", sfxTrack.x, sfxTrack.y + 34f);

        // відсотки
        smallFont.setColor(0.55f, 0.80f, 0.88f, 1f);
        String mp = Math.round(audio.getMusicVolume() * 100) + "%";
        String sp = Math.round(audio.getSfxVolume() * 100) + "%";
        layout.setText(smallFont, mp);
        smallFont.draw(batch, mp, musicTrack.x + musicTrack.width - layout.width, musicTrack.y + 33f);
        layout.setText(smallFont, sp);
        smallFont.draw(batch, sp, sfxTrack.x + sfxTrack.width - layout.width, sfxTrack.y + 33f);

        // текст кнопок
        drawButtonLabel(layout, btnSave, "ЗБЕРЕГТИ ГРУ", 0.50f, 0.95f, 1.00f);
        drawButtonLabel(layout, btnExit, "ВИЙТИ З ГРИ", 1.00f, 0.55f, 0.55f);

        // підтвердження збереження
        if (savedMessageTimer > 0f) {
            smallFont.setColor(0.45f, 0.95f, 0.55f, 1f);
            String msg = "Гру збережено!";
            layout.setText(smallFont, msg);
            smallFont.draw(batch, msg, cx - layout.width * 0.5f, btnSave.y + btnSave.height + 20f);
        }

        batch.end();
    }

    private void drawTrack(Rectangle track, float value) {
        float fillW = track.width * value;
        float knobX = track.x + fillW;
        float knobY = track.y + track.height * 0.5f;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        // фон
        shapeRenderer.setColor(0.10f, 0.16f, 0.22f, 1f);
        shapeRenderer.rect(track.x, track.y, track.width, track.height);
        // заповнення
        shapeRenderer.setColor(0.16f, 0.75f, 0.88f, 1f);
        shapeRenderer.rect(track.x, track.y, fillW, track.height);
        // повзунок
        shapeRenderer.setColor(0.30f, 0.90f, 1.00f, 1f);
        shapeRenderer.circle(knobX, knobY, KNOB_R, 20);
        shapeRenderer.setColor(0.05f, 0.20f, 0.28f, 1f);
        shapeRenderer.circle(knobX, knobY, KNOB_R * 0.45f, 16);
        shapeRenderer.end();
    }

    private void drawButtonBg(Rectangle r,
                              float bgR, float bgG, float bgB,
                              float fgR, float fgG, float fgB) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(bgR, bgG, bgB, 0.95f);
        shapeRenderer.rect(r.x, r.y, r.width, r.height);
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(fgR, fgG, fgB, 0.9f);
        shapeRenderer.rect(r.x, r.y, r.width, r.height);
        shapeRenderer.end();
    }

    private void drawButtonLabel(GlyphLayout layout, Rectangle r, String label,
                                 float fr, float fg, float fb) {
        btnFont.setColor(fr, fg, fb, 1f);
        layout.setText(btnFont, label);
        btnFont.draw(batch, label,
                r.x + (r.width - layout.width) * 0.5f,
                r.y + (r.height + layout.height) * 0.5f);
    }

    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        titleFont.dispose();
        labelFont.dispose();
        btnFont.dispose();
        smallFont.dispose();
    }
}