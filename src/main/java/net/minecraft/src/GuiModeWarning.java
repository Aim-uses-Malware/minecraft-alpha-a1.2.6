package net.minecraft.src;

/**
 * Warning screen shown when the player tries to load a world
 * with a different game mode than it was originally created with.
 *
 * Shown by GuiSelectWorld before actually loading the world.
 */
public class GuiModeWarning extends GuiScreen {

    private final GuiSelectWorld parent;
    private final int worldSlot;

    public GuiModeWarning(GuiSelectWorld parent, int worldSlot) {
        this.parent    = parent;
        this.worldSlot = worldSlot;
    }

    public void initGui() {
        // Yes  — left
        this.controlList.add(new GuiSmallButton(0, this.width / 2 - 155,     this.height / 6 + 96, "Yes"));
        // No   — right
        this.controlList.add(new GuiSmallButton(1, this.width / 2 - 155 + 160, this.height / 6 + 96, "No"));
    }

    protected void actionPerformed(GuiButton btn) {
        if (btn.id == 0) {
            // Yes — load anyway
            parent.confirmModeSwitch(worldSlot);
        } else {
            // No — go back to select world
            mc.displayGuiScreen(parent);
        }
    }

    public void drawScreen(int mx, int my, float f) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRenderer,
                "The game world may malfunction if the game mode is changed.",
                this.width / 2, 70, 0xFFFFFF);
        this.drawCenteredString(this.fontRenderer,
                "Are you sure you want to continue?",
                this.width / 2, 90, 0xAAAAAA);
        super.drawScreen(mx, my, f);
    }
}
