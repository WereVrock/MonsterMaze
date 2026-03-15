package wv.monstermaze.main;

import com.studiohartman.jamepad.ControllerManager;
import com.studiohartman.jamepad.ControllerState;

public class ControllerInput {

    private ControllerManager controllers;

    // Controller 1
    private float lx;
    private float ly;
    private float leftTrigger;
    private float rightTrigger;

    private boolean xPressed;
    private boolean lastX;

    // Controller 2
    private float lx2;
    private float ly2;
    private boolean xPressed2;
    private boolean lastX2;

    public ControllerInput() {
        controllers = new ControllerManager();
        controllers.initSDLGamepad();
    }

    public void poll() {

        // ===== Controller 1 =====
        ControllerState state = controllers.getState(0);

        if (!state.isConnected) {

            lx = 0f;
            ly = 0f;
            leftTrigger = 0f;
            rightTrigger = 0f;
            xPressed = false;

        } else {

            lx = state.leftStickX;
            ly = state.leftStickY;
            leftTrigger = state.leftTrigger;
            rightTrigger = state.rightTrigger;

            boolean currentX = state.x;

            xPressed = currentX && !lastX;
            lastX = currentX;
        }

        // ===== Controller 2 =====
        ControllerState state2 = controllers.getState(1);

        if (!state2.isConnected) {

            lx2 = 0f;
            ly2 = 0f;
            xPressed2 = false;

        } else {

            lx2 = state2.leftStickX;
            ly2 = state2.leftStickY;

            boolean currentX = state2.x;

            xPressed2 = currentX && !lastX2;
            lastX2 = currentX;
        }
    }

    public float getLX() {
        return lx;
    }

    public float getLY() {
        return ly;
    }

    public float getLeftTrigger() {
        return leftTrigger;
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