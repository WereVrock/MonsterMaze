package wv.monstermaze.main;

import com.studiohartman.jamepad.ControllerManager;
import com.studiohartman.jamepad.ControllerState;

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

public class ControllerInput {

    private ControllerManager controllers;

    private float lx;
    private float ly;
    private float startbut;
    private float rightTrigger;

    private boolean xPressed;
    private boolean lastX;

    private float lx2;
    private float ly2;
    private boolean xPressed2;
    private boolean lastX2;

    private static final Set<Integer> keys = new HashSet<>();

    public ControllerInput() {

        controllers = new ControllerManager();
        controllers.initSDLGamepad();

        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(new KeyEventDispatcher() {

                    @Override
                    public boolean dispatchKeyEvent(KeyEvent e) {

                        synchronized (keys) {

                            if (e.getID() == KeyEvent.KEY_PRESSED) {
                                keys.add(e.getKeyCode());
                            }

                            if (e.getID() == KeyEvent.KEY_RELEASED) {
                                keys.remove(e.getKeyCode());
                            }
                        }

                        return false;
                    }
                });
    }

    public void poll() {

        ControllerState state = controllers.getState(0);

        float controllerLX = 0f;
        float controllerLY = 0f;
        float controllerLT = 0f;
        float controllerRT = 0f;
        boolean controllerX = false;

        if (state.isConnected) {

            controllerLX = state.leftStickX;
            controllerLY = state.leftStickY;

            // ❌ Disable real LT, ✅ use START as LT
            controllerLT = state.start ? 1f : 0f;

            controllerRT = state.rightTrigger;
            controllerX = state.x;
        }

        float keyLX = 0f;
        float keyLY = 0f;
        float keyLT = 0f;
        float keyRT = 0f;
        boolean keyX = false;

        synchronized (keys) {

            if (keys.contains(KeyEvent.VK_A)) keyLX -= 1f;
            if (keys.contains(KeyEvent.VK_D)) keyLX += 1f;

            // FIXED Y AXIS
            if (keys.contains(KeyEvent.VK_W)) keyLY += 1f;
            if (keys.contains(KeyEvent.VK_S)) keyLY -= 1f;

            if (keys.contains(KeyEvent.VK_Q)) keyLT = 1f;
            if (keys.contains(KeyEvent.VK_E)) keyRT = 1f;

            if (keys.contains(KeyEvent.VK_SPACE)) keyX = true;
        }

        lx = Math.abs(controllerLX) > Math.abs(keyLX) ? controllerLX : keyLX;
        ly = Math.abs(controllerLY) > Math.abs(keyLY) ? controllerLY : keyLY;

        startbut = Math.max(controllerLT, keyLT);
        rightTrigger = Math.max(controllerRT, keyRT);

        boolean currentX = controllerX || keyX;

        xPressed = currentX && !lastX;
        lastX = currentX;

        // ===== CONTROLLER 2 =====

        ControllerState state2 = controllers.getState(1);

        float controllerLX2 = 0f;
        float controllerLY2 = 0f;
        boolean controllerX2 = false;

        if (state2.isConnected) {

            controllerLX2 = state2.leftStickX;
            controllerLY2 = state2.leftStickY;
            controllerX2 = state2.x;
        }

        float keyLX2 = 0f;
        float keyLY2 = 0f;
        boolean keyX2 = false;

        synchronized (keys) {

            if (keys.contains(KeyEvent.VK_LEFT)) keyLX2 -= 1f;
            if (keys.contains(KeyEvent.VK_RIGHT)) keyLX2 += 1f;

            // FIXED Y AXIS
            if (keys.contains(KeyEvent.VK_UP)) keyLY2 += 1f;
            if (keys.contains(KeyEvent.VK_DOWN)) keyLY2 -= 1f;

            if (keys.contains(KeyEvent.VK_ENTER)) keyX2 = true;
        }

        lx2 = Math.abs(controllerLX2) > Math.abs(keyLX2) ? controllerLX2 : keyLX2;
        ly2 = Math.abs(controllerLY2) > Math.abs(keyLY2) ? controllerLY2 : keyLY2;

        boolean currentX2 = controllerX2 || keyX2;

        xPressed2 = currentX2 && !lastX2;
        lastX2 = currentX2;
    }

    public float getLX() {
        return lx;
    }

    public float getLY() {
        return ly;
    }

    public float getStartButton() {
        return startbut;
    }

    public float getRightTrigger() {
        return rightTrigger;
    }

    public boolean isXPressed() {
        return xPressed;
    }

    public float getLX2() {
        return lx2;
    }

    public float getLY2() {
        return ly2;
    }

    public boolean isXPressedController2() {
        return xPressed2;
    }

    public void quit() {
        controllers.quitSDLGamepad();
    }
}