package wv.monstermaze.main;

public class ToiletActionHandler {

    private PoopBar poopBar;
    private SpeedParticles speedFx;
    private SpeedFXSystem speedFXSystem;
    private ToiletManager toilets;

    public ToiletActionHandler(PoopBar poopBar, SpeedParticles speedFx, SpeedFXSystem speedFXSystem, ToiletManager toilets) {
        this.poopBar = poopBar;
        this.speedFx = speedFx;
        this.speedFXSystem = speedFXSystem;
        this.toilets = toilets;
    }

    /**
     * Call this when the player presses the X button.
     * @param player the player object
     */
    public void handleToiletAction(Player player) {

        if (!toilets.isPlayerOnToilet(player)) return;

        // If PoopBar is gray → just freeze the player (attempting to poop)
        if (poopBar.isGray()) {
            player.freeze(1.5);
            return;
        }

        // PoopBar is green or red → full effect
        player.freeze(1.5);

        // Trigger VFX if enabled
        if (speedFx != null) speedFx.triggerToiletBurst(player.x, player.y);
        if (speedFXSystem != null) speedFXSystem.triggerBoost(player.x, player.y);

        // Trigger speed boost depending on PoopBar color
        if (poopBar.isGreen()) {
            PoopSound.play();
            player.triggerSpeedBoost(1.8, 10,SpeedBoost.Type.GREEN);
        } else if (poopBar.isRed()) {
            PoopSound.play();
            player.triggerSpeedBoost(1.35, 10,SpeedBoost.Type.RED);
        }

        poopBar.reset();
    }
}