package com.spacefarm.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

public class TutorialOverlay {

    public enum Action { NONE, YES, NO, NEXT, CLOSE }

    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch   batch;
    private final BitmapFont    titleFont;
    private final BitmapFont    bodyFont;
    private final BitmapFont    btnFont;

    private final Rectangle btnLeft  = new Rectangle();
    private final Rectangle btnRight = new Rectangle();
    private boolean prevTouch = true;

    private boolean isVisible = false;
    private String title = "";
    private String message = "";
    private boolean isPrompt = false;
    private boolean showNext = true;

    public TutorialOverlay() {
        shapeRenderer = new ShapeRenderer();
        batch         = new SpriteBatch();
        titleFont = FontUtils.createFont("fonts/ArialBold.ttf", 36);
        bodyFont  = FontUtils.createFont("fonts/ArialBold.ttf", 20);
        btnFont   = FontUtils.createFont("fonts/ArialBold.ttf", 22);
    }

    public void showPrompt(String title, String message) {
        this.title = title;
        this.message = message;
        this.isPrompt = true;
        this.showNext = true;
        this.isVisible = true;
        this.prevTouch = true;
    }

    public void showStep(String title, String message, boolean showNext) {
        this.title = title;
        this.message = message;
        this.showNext = showNext;
        this.isPrompt = false;
        this.isVisible = true;
        this.prevTouch = true;
    }

    public void hide() {
        this.isVisible = false;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public Action handleInput() {
        if (!isVisible) return Action.NONE;

        boolean touched     = Gdx.input.isTouched();
        boolean justClicked = touched && !prevTouch;
        prevTouch = touched;
        if (!justClicked) return Action.NONE;

        int mx = Gdx.input.getX();
        int my = Gdx.graphics.getHeight() - Gdx.input.getY();

        if (isPrompt) {
            if (btnLeft.contains(mx, my))  return Action.YES;
            if (btnRight.contains(mx, my)) return Action.NO;
        } else if (showNext) {
            if (btnRight.contains(mx, my)) return Action.NEXT;
            if (btnLeft.contains(mx, my))  return Action.CLOSE;
        }
        return Action.NONE;
    }

    public void render(int sw, int sh) {
        if (!isVisible) return;

        float panelW = sw * 0.45f;
        float panelH = sh * 0.28f;
        float px = 20f;
        float py = sh - panelH - 20f;
        float cx = px + panelW * 0.5f;

        drawPanel(px, py, panelW, panelH);

        GlyphLayout layout = new GlyphLayout();

        batch.begin();
        titleFont.setColor(0.12f, 0.75f, 0.63f, 1f); // #1FBFA1
        layout.setText(titleFont, title);
        titleFont.draw(batch, title, cx - layout.width * 0.5f, py + panelH - 25f);

        bodyFont.setColor(0.85f, 0.95f, 1.00f, 1f);
        layout.setText(bodyFont, message, bodyFont.getColor(), panelW * 0.92f, com.badlogic.gdx.utils.Align.center, true);
        bodyFont.draw(batch, message, cx - (panelW * 0.92f) * 0.5f, py + panelH - 65f, panelW * 0.92f, com.badlogic.gdx.utils.Align.center, true);
        batch.end();

        float btnW = panelW * 0.38f;
        float btnH = 42f;
        float btnGap = 20f;

        if (isPrompt) {
            btnLeft.set(cx - btnW - btnGap / 2f, py + 20f, btnW, btnH);
            btnRight.set(cx + btnGap / 2f, py + 20f, btnW, btnH);
            drawButton(btnLeft, "ТАК", 0.05f, 0.25f, 0.15f, 0.12f, 0.75f, 0.63f);
            drawButton(btnRight, "НІ", 0.15f, 0.05f, 0.05f, 0.88f, 0.20f, 0.20f);
        } else if (showNext) {
            btnRight.set(cx - btnW / 2f, py + 20f, btnW, btnH);
            drawButton(btnRight, "ДАЛІ", 0.05f, 0.22f, 0.27f, 0.12f, 0.75f, 0.63f);
        }
    }

    private void drawPanel(float px, float py, float pw, float ph) {
        float border = 4f;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.05f, 0.08f, 0.12f, 0.95f);
        shapeRenderer.rect(px, py, pw, ph);
        shapeRenderer.setColor(0.12f, 0.75f, 0.63f, 1f);
        shapeRenderer.rect(px, py + ph - border, pw, border);
        shapeRenderer.rect(px, py,               pw, border);
        shapeRenderer.setColor(0.07f, 0.38f, 0.48f, 1f);
        shapeRenderer.rect(px,             py, border, ph);
        shapeRenderer.rect(px + pw - border, py, border, ph);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.12f, 0.75f, 0.63f, 0.8f);
        shapeRenderer.rect(px, py, pw, ph);
        shapeRenderer.end();
    }

    private void drawButton(Rectangle r, String label, float bgR, float bgG, float bgB, float fgR, float fgG, float fgB) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(bgR, bgG, bgB, 0.9f);
        shapeRenderer.rect(r.x, r.y, r.width, r.height);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(fgR, fgG, fgB, 1f);
        shapeRenderer.rect(r.x, r.y, r.width, r.height);
        shapeRenderer.end();

        GlyphLayout layout = new GlyphLayout();
        batch.begin();
        btnFont.setColor(fgR, fgG, fgB, 1f);
        layout.setText(btnFont, label);
        btnFont.draw(batch, label, r.x + (r.width - layout.width) * 0.5f, r.y + (r.height + layout.height) * 0.5f);
        batch.end();
    }

    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        titleFont.dispose();
        bodyFont.dispose();
        btnFont.dispose();
    }
}
