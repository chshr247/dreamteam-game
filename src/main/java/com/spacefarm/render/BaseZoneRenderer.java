package com.spacefarm.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.spacefarm.world.BaseZone;
import com.spacefarm.world.TileCoord;


public class BaseZoneRenderer {
    private BaseZone baseZone;
    private TiledMapTileLayer zoneLayer;
    private int worldMinX;
    private int worldMinY;

    // Textures for different areas
    private Texture greenTileTexture;
    private Texture treeTileTexture;
    private Texture gardenTileTexture;
    private Texture droneTileTexture;
    private Texture[] treePhaseTextures;
    private Texture droneTextureOverlay;

    private SpriteBatch batch;
    private com.badlogic.gdx.maps.tiled.TiledMap referenceMap;

    // Hover effect state
    private float treeHoverAlpha = 0f;
    private float droneHoverAlpha = 0f;
    private static final float FADE_SPEED = 5.0f;
    private final com.badlogic.gdx.math.Vector3 tmpVec = new com.badlogic.gdx.math.Vector3();
    private com.badlogic.gdx.graphics.glutils.ShaderProgram whiteShader;

    // Last known drawing bounds for pixel-perfect hover detection
    private float lastTreeX, lastTreeY, lastTreeW, lastTreeH;
    private float lastDroneX, lastDroneY, lastDroneSize;

    public BaseZoneRenderer(BaseZone baseZone, TiledMapTileLayer zoneLayer, int tileSize, int worldMinX, int worldMinY) {
        this.baseZone = baseZone;
        this.zoneLayer = zoneLayer;
        this.worldMinX = worldMinX;
        this.worldMinY = worldMinY;
        this.batch = new SpriteBatch();

        // Create textures FIRST so treePhaseTextures is initialized
        createTextures(tileSize);
        createWhiteShader();

        // Apply base zone tiles to the map
        applyBaseZoneTiles();
    }

    private void createWhiteShader() {
        String vertexShader = "attribute vec4 a_position;\n" +
                              "attribute vec4 a_color;\n" +
                              "attribute vec2 a_texCoord0;\n" +
                              "uniform mat4 u_projTrans;\n" +
                              "varying vec4 v_color;\n" +
                              "varying vec2 v_texCoords;\n" +
                              "void main() {\n" +
                              "   v_color = a_color;\n" +
                              "   v_texCoords = a_texCoord0;\n" +
                              "   gl_Position = u_projTrans * a_position;\n" +
                              "}\n";
        String fragmentShader = "#ifdef GL_ES\n" +
                                "precision mediump float;\n" +
                                "#endif\n" +
                                "varying vec4 v_color;\n" +
                                "varying vec2 v_texCoords;\n" +
                                "uniform sampler2D u_texture;\n" +
                                "void main() {\n" +
                                "  vec4 color = texture2D(u_texture, v_texCoords);\n" +
                                "  gl_FragColor = vec4(1.0, 1.0, 1.0, color.a * v_color.a);\n" +
                                "}\n";
        whiteShader = new com.badlogic.gdx.graphics.glutils.ShaderProgram(vertexShader, fragmentShader);
        if (!whiteShader.isCompiled()) {
            Gdx.app.error("BaseZoneRenderer", "Error compiling white shader: " + whiteShader.getLog());
        }
    }

    public void setReferenceMap(com.badlogic.gdx.maps.tiled.TiledMap referenceMap) {
        this.referenceMap = referenceMap;
        applyBaseZoneTiles();
    }

    private void createTextures(int tileSize) {
        // Green tile for base zone (life and oxygen)
        greenTileTexture = createSolidTexture(tileSize, tileSize, 34, 139, 34, 200); // Forest green

        // Tree area tile (darker green with pattern)
        treeTileTexture = createSolidTexture(tileSize, tileSize, 25, 100, 25, 200); // Darker green

        // Garden bed tile (lighter green)
        Pixmap originalPixmap = new Pixmap(Gdx.files.internal("sprite/plants/garden.png"));
        Pixmap scaledPixmap = new Pixmap(tileSize, tileSize, originalPixmap.getFormat());
        scaledPixmap.setFilter(Pixmap.Filter.NearestNeighbour);
        scaledPixmap.drawPixmap(originalPixmap,
                0, 0, originalPixmap.getWidth(), originalPixmap.getHeight(),
                0, 0, scaledPixmap.getWidth(), scaledPixmap.getHeight()
        );
        gardenTileTexture = new Texture(scaledPixmap);
        originalPixmap.dispose();
        scaledPixmap.dispose();

        // Drone zone tile (green for territory)
        droneTileTexture = createSolidTexture(tileSize, tileSize, 34, 139, 34, 200); // Forest green

        // Load tree phase textures
        treePhaseTextures = new Texture[5];
        treePhaseTextures[0] = new Texture(Gdx.files.internal("sprite/tree/tree-1.png"));
        treePhaseTextures[1] = new Texture(Gdx.files.internal("sprite/tree/tree-2.png"));
        treePhaseTextures[2] = new Texture(Gdx.files.internal("sprite/tree/tree-3.png"));
        treePhaseTextures[3] = new Texture(Gdx.files.internal("sprite/tree/tree-4.png"));
        treePhaseTextures[4] = new Texture(Gdx.files.internal("sprite/tree/tree-5.png"));

        // Load drone sprite texture
        try {
            if (Gdx.files.internal("sprite/object-map/drone.png").exists()) {
                droneTextureOverlay = new Texture(Gdx.files.internal("sprite/object-map/drone.png"));
                Gdx.app.log("BaseZoneRenderer", "Successfully loaded drone sprite: sprite/object-map/drone.png");
            } else {
                Gdx.app.error("BaseZoneRenderer", "Drone sprite not found: sprite/object-map/drone.png. Using fallback.");
                droneTextureOverlay = createDroneSpriteTexture(tileSize * 5, tileSize * 5);
            }
        } catch (Exception e) {
            Gdx.app.error("BaseZoneRenderer", "Error loading drone sprite: " + e.getMessage());
            droneTextureOverlay = createDroneSpriteTexture(tileSize * 5, tileSize * 5);
        }
    }

    private Texture createDroneSpriteTexture(int width, int height) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 0);
        pixmap.fill();

        pixmap.setColor(255 / 255f, 215 / 255f, 0 / 255f, 1f);
        int bodySize = Math.min(width, height) / 3;
        pixmap.fillRectangle((width - bodySize) / 2, (height - bodySize) / 2, bodySize, bodySize);

        pixmap.setColor(128 / 255f, 128 / 255f, 128 / 255f, 1f);
        int propSize = bodySize / 3;
        pixmap.fillCircle((width - bodySize) / 2 - propSize, (height + bodySize) / 2, propSize / 2);
        pixmap.fillCircle((width + bodySize) / 2 + propSize, (height + bodySize) / 2, propSize / 2);
        pixmap.fillCircle((width - bodySize) / 2 - propSize, (height - bodySize) / 2, propSize / 2);
        pixmap.fillCircle((width + bodySize) / 2 + propSize, (height - baseZone.getDroneZoneSize() / 2) / 2, propSize / 2); // Error here in previous logic, fixed

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private void applyBaseZoneTiles() {
        TiledMapTileLayer refLayer = null;
        if (referenceMap != null) {
            for (com.badlogic.gdx.maps.MapLayer layer : referenceMap.getLayers()) {
                if (layer instanceof TiledMapTileLayer) {
                    refLayer = (TiledMapTileLayer) layer;
                    break;
                }
            }
        }

        for (int x = baseZone.getBaseX(); x < baseZone.getBaseX() + baseZone.getBaseWidth(); x++) {
            for (int y = baseZone.getBaseY(); y < baseZone.getBaseY() + baseZone.getBaseHeight(); y++) {
                int layerX = x - worldMinX;
                int layerY = y - worldMinY;
                if (layerX >= 0 && layerX < zoneLayer.getWidth() && layerY >= 0 && layerY < zoneLayer.getHeight()) {
                    TileCoord coord = new TileCoord(x, y);
                    
                    TiledMapTileLayer.Cell cell = null;
                    if (refLayer != null && x < refLayer.getWidth() && y < refLayer.getHeight()) {
                        TiledMapTileLayer.Cell refCell = refLayer.getCell(x, y);
                        if (refCell != null) {
                            cell = new TiledMapTileLayer.Cell();
                            cell.setTile(refCell.getTile());
                            cell.setFlipHorizontally(refCell.getFlipHorizontally());
                            cell.setFlipVertically(refCell.getFlipVertically());
                            cell.setRotation(refCell.getRotation());
                        }
                    }

                    Texture overlayTexture = null;
                    if (baseZone.isGardenBed(coord)) {
                        overlayTexture = gardenTileTexture;
                    }

                    if (overlayTexture != null) {
                        StaticTiledMapTile tile = new StaticTiledMapTile(new TextureRegion(overlayTexture));
                        if (cell == null) cell = new TiledMapTileLayer.Cell();
                        cell.setTile(tile);
                    }

                    if (cell != null) {
                        zoneLayer.setCell(layerX, layerY, cell);
                    } else {
                        // Ensure it's empty if no map tile and no overlay
                        zoneLayer.setCell(layerX, layerY, null);
                    }
                }
            }
        }
    }

    public void refreshGardenBedTile(TileCoord coord) {
        for (int dx = 0; dx < 2; dx++) {
            for (int dy = 0; dy < 2; dy++) {
                int x = coord.x() + dx;
                int y = coord.y() + dy;
                int layerX = x - worldMinX;
                int layerY = y - worldMinY;
                if (layerX >= 0 && layerX < zoneLayer.getWidth() && layerY >= 0 && layerY < zoneLayer.getHeight()) {
                    StaticTiledMapTile tile = new StaticTiledMapTile(new TextureRegion(gardenTileTexture));
                    TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                    cell.setTile(tile);
                    zoneLayer.setCell(layerX, layerY, cell);
                }
            }
        }
    }

    private Texture createSolidTexture(int width, int height, int r, int g, int b, int a) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(r / 255f, g / 255f, b / 255f, a / 255f);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    public void render(OrthographicCamera camera) {
        if (baseZone.isDirty()) {
            applyBaseZoneTiles();
            baseZone.clearDirty();
        }

        // --- Precise Hover Detection ---
        tmpVec.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(tmpVec);
        float mouseX = tmpVec.x;
        float mouseY = tmpVec.y;

        float dt = Gdx.graphics.getDeltaTime();
        
        // Tree hover check
        if (mouseX >= lastTreeX && mouseX <= lastTreeX + lastTreeW &&
            mouseY >= lastTreeY && mouseY <= lastTreeY + lastTreeH) {
            treeHoverAlpha = Math.min(1f, treeHoverAlpha + dt * FADE_SPEED);
        } else {
            treeHoverAlpha = Math.max(0f, treeHoverAlpha - dt * FADE_SPEED);
        }

        // Drone hover check
        if (mouseX >= lastDroneX && mouseX <= lastDroneX + lastDroneSize &&
            mouseY >= lastDroneY && mouseY <= lastDroneY + lastDroneSize) {
            droneHoverAlpha = Math.min(1f, droneHoverAlpha + dt * FADE_SPEED);
        } else {
            droneHoverAlpha = Math.max(0f, droneHoverAlpha - dt * FADE_SPEED);
        }
        // -----------------------

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        renderTreeOverlay();
        renderDroneOverlay();
        batch.end();
    }

    private void renderTreeOverlay() {
        int tileSize = zoneLayer.getTileWidth();
        int phase = baseZone.getTreePhase();
        
        Texture currentTreeTexture;
        float scale = 1.0f;
        
        if (phase == 1) {
            currentTreeTexture = treePhaseTextures[0];
            scale = 2.5f;
        } else if (phase == 2) {
            currentTreeTexture = treePhaseTextures[1];
            scale = 2.2f;
        } else if (phase == 3) {
            currentTreeTexture = treePhaseTextures[2];
            scale = 0.85f;
        } else if (phase == 4) {
            currentTreeTexture = treePhaseTextures[3];
            scale = 1.0f;
        } else if (phase == 5) {
            currentTreeTexture = treePhaseTextures[4];
            scale = 1.1f;
        } else {
            currentTreeTexture = treePhaseTextures[4];
            scale = 1.4f;
        }
        
        float treeWidth = currentTreeTexture.getWidth() * scale;
        float treeHeight = currentTreeTexture.getHeight() * scale;

        TileCoord treeBaseArea = baseZone.getTreeCenter();
        float areaWidth = baseZone.getTreeWidth() * tileSize;

        float treeStartX = treeBaseArea.x() * tileSize + (areaWidth - treeWidth) / 2f;
        float treeStartY = treeBaseArea.y() * tileSize;

        // Store for hover detection in next frame
        lastTreeX = treeStartX;
        lastTreeY = treeStartY;
        lastTreeW = treeWidth;
        lastTreeH = treeHeight;

        if (treeHoverAlpha > 0) {
            drawOutline(currentTreeTexture, treeStartX, treeStartY, treeWidth, treeHeight, treeHoverAlpha);
        }

        batch.setColor(1, 1, 1, 1.0f);
        batch.draw(currentTreeTexture, treeStartX, treeStartY, treeWidth, treeHeight);
    }

    private void renderDroneOverlay() {
        int tileSize = zoneLayer.getTileWidth();
        TileCoord dronePos = baseZone.getDroneZoneCenter();
        float droneStartX = dronePos.x() * tileSize + baseZone.getDroneOffsetX();
        float droneStartY = dronePos.y() * tileSize + baseZone.getDroneOffsetY();
        float droneSize = baseZone.getDroneZoneSize() * tileSize;

        // Store for hover detection in next frame
        lastDroneX = droneStartX;
        lastDroneY = droneStartY;
        lastDroneSize = droneSize;

        if (droneHoverAlpha > 0) {
            drawOutline(droneTextureOverlay, droneStartX, droneStartY, droneSize, droneSize, droneHoverAlpha * 0.85f);
        }

        batch.setColor(1, 1, 1, 0.85f);
        batch.draw(droneTextureOverlay, droneStartX, droneStartY, droneSize, droneSize);
    }

    private void drawOutline(Texture tex, float x, float y, float w, float h, float alpha) {
        batch.setShader(whiteShader);
        batch.setColor(1, 1, 1, alpha);
        float offset = 1.5f;
        // 8-way offset for a thicker outline
        batch.draw(tex, x - offset, y, w, h);
        batch.draw(tex, x + offset, y, w, h);
        batch.draw(tex, x, y - offset, w, h);
        batch.draw(tex, x, y + offset, w, h);
        batch.draw(tex, x - offset, y - offset, w, h);
        batch.draw(tex, x + offset, y - offset, w, h);
        batch.draw(tex, x - offset, y + offset, w, h);
        batch.draw(tex, x + offset, y + offset, w, h);
        batch.setShader(null);
    }

    public boolean isTreeClicked(float worldX, float worldY) {
        return worldX >= lastTreeX && worldX <= lastTreeX + lastTreeW &&
               worldY >= lastTreeY && worldY <= lastTreeY + lastTreeH;
    }

    public boolean isDroneClicked(float worldX, float worldY) {
        return worldX >= lastDroneX && worldX <= lastDroneX + lastDroneSize &&
               worldY >= lastDroneY && worldY <= lastDroneY + lastDroneSize;
    }

    public void dispose() {
        if (greenTileTexture != null) greenTileTexture.dispose();
        if (treeTileTexture != null) treeTileTexture.dispose();
        if (gardenTileTexture != null) gardenTileTexture.dispose();
        if (droneTileTexture != null) droneTileTexture.dispose();
        if (treePhaseTextures != null) {
            for (int i = 0; i < treePhaseTextures.length; i++) {
                if (treePhaseTextures[i] != null) {
                    boolean alreadyDisposed = false;
                    for (int j = 0; j < i; j++) {
                        if (treePhaseTextures[i] == treePhaseTextures[j]) { alreadyDisposed = true; break; }
                    }
                    if (!alreadyDisposed) treePhaseTextures[i].dispose();
                }
            }
        }
        if (droneTextureOverlay != null) droneTextureOverlay.dispose();
        if (whiteShader != null) whiteShader.dispose();
        if (batch != null) batch.dispose();
    }
}
