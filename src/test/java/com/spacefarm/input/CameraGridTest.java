package com.spacefarm.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3NativesLoader;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.spacefarm.world.TileCoord;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.*;

class CameraGridTest {
    private OrthographicCamera camera;
    private Viewport viewport;
    private WorldBounds bounds;

    @BeforeAll
    static void initNatives() {
        Lwjgl3NativesLoader.load();
    }

    @BeforeEach
    void setUp() {
        // Use a Proxy to mock Graphics - avoids implementing 50+ methods
        Gdx.graphics = (Graphics) Proxy.newProxyInstance(
                Graphics.class.getClassLoader(),
                new Class[]{Graphics.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("getWidth")) return 800;
                    if (method.getName().equals("getHeight")) return 600;
                    if (method.getName().equals("getPpiX")) return 96f;
                    if (method.getName().equals("getPpiY")) return 96f;
                    return null;
                }
        );

        camera = new OrthographicCamera();
        camera.viewportWidth = 800;
        camera.viewportHeight = 600;
        viewport = new ExtendViewport(800, 600, camera);
        bounds = new WorldBounds(0, 0, 2000, 2000);
        viewport.setWorldSize(800, 600);
        viewport.setScreenSize(800, 600);
        camera.position.set(400, 300, 0);
        camera.update();
    }

    @Test
    void testTilePickerStandard() {
        TilePicker picker = new TilePicker(camera, 32, 32, 0, 0, 100, 100);
        
        // Camera at (400, 300) looking at world (400, 300)
        // Screen (400, 300) should be world (400, 300)
        TileCoord coord = picker.screenToTile(400, 300);
        
        assertNotNull(coord);
        assertEquals(400 / 32, coord.x());
        assertEquals(300 / 32, coord.y());
    }

    @Test
    void testTilePickerOutOfBounds() {
        TilePicker picker = new TilePicker(camera, 32, 32, 0, 0, 10, 10);
        
        // Far out of bounds
        assertNull(picker.screenToTile(5000, 5000));
        assertNull(picker.screenToTile(-100, -100));
        
        // Just outside the 10x10 tile limit (320x320 units)
        assertNull(picker.screenToTile(321, 160));
    }

    @Test
    void testCameraControllerClamping() {
        CameraController controller = new CameraController(camera, viewport, bounds, 0.5f, 2.0f);
        
        // Move camera way out
        camera.position.set(5000, 5000, 0);
        controller.clamp();
        
        // Should be clamped to max bounds minus half viewport
        // Viewport is 800x600, zoom is 1.0 (default)
        // maxX = 2000, maxY = 2000
        // clampedX = 2000 - 400 = 1600
        // clampedY = 2000 - 300 = 1700
        assertEquals(1600f, camera.position.x, 0.1f);
        assertEquals(1700f, camera.position.y, 0.1f);
        
        camera.position.set(-1000, -1000, 0);
        controller.clamp();
        assertEquals(400f, camera.position.x, 0.1f);
        assertEquals(300f, camera.position.y, 0.1f);
    }

    @Test
    void testCameraControllerZoomClamping() {
        CameraController controller = new CameraController(camera, viewport, bounds, 0.5f, 5.0f);
        
        // Small zoom
        camera.zoom = 0.1f;
        controller.clamp();
        assertTrue(camera.zoom >= 0.5f);
        
        // Large zoom (maxZoomOut is calculated based on bounds/viewport)
        camera.zoom = 100f;
        controller.clamp();
        // world 2000x2000, viewport 800x600
        // maxZoomX = 2000/800 = 2.5
        // maxZoomY = 2000/600 = 3.33
        // maxZoomOut = min(2.5, 3.33) = 2.5
        assertEquals(2.5f, camera.zoom, 0.1f);
    }
}
