package wv.monstermaze.main;

import com.studiohartman.jamepad.ControllerManager;
import com.studiohartman.jamepad.ControllerState;

public class ControllerInput {

    private ControllerManager controllers;

    private float lx;
    private float ly;

    private float rightTrigger;

    public ControllerInput() {
        controllers = new ControllerManager();
        controllers.initSDLGamepad();
    }

    public void poll() {

        ControllerState state = controllers.getState(0);

        if (!state.isConnected) {

            lx = 0f;
            ly = 0f;
            rightTrigger = 0f;

            System.out.println("Controller not connected");
            return;
        }

        lx = state.leftStickX;
        ly = state.leftStickY;

        rightTrigger = state.rightTrigger;

    }

    public float getLX() {
        return lx;
    }

    public float getLY() {
        return ly;
    }

    public float getRightTrigger() {
        return rightTrigger;
    }

    public void quit() {
        controllers.quitSDLGamepad();
    }
}