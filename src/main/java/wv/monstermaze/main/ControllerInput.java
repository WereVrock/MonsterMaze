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

    // Controller 2
    private float lx2;
    private float ly2;

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
        } else {
            lx = state.leftStickX;
            ly = state.leftStickY;

            leftTrigger = state.leftTrigger;
            rightTrigger = state.rightTrigger;
        }

        // ===== Controller 2 =====
        ControllerState state2 = controllers.getState(1);

        if (!state2.isConnected) {
            lx2 = 0f;
            ly2 = 0f;
        } else {
            lx2 = state2.leftStickX;
            ly2 = state2.leftStickY;
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

    public float getLX2() {
        return lx2;
    }

    public float getLY2() {
        return ly2;
    }

    public void quit() {
        controllers.quitSDLGamepad();
    }
}