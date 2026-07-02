package net.minecraft.src;

import java.util.List;
import net.minecraft.client.Minecraft;

/**
 * World selection screen with game mode toggle.
 *
 * What's new vs original:
 *  - "Mode: Survival / Creative" button next to Cancel.
 *  - Chosen mode is saved as "GameMode" (0=Survival, 1=Creative) into level.dat
 *    when the world is first loaded, so subsequent sessions can detect a mismatch.
 *  - If the user picks a mode that differs from the one stored in the world's
 *    level.dat, GuiModeWarning is shown first (Yes → load anyway, No → back).
 *  - Creative mode: PlayerControllerTest (instant break, EntityPlayerCreative,
 *    damageEntity() is a no-op → player takes no damage).
 *  - Survival mode: PlayerControllerSP (normal mining speed, health, fall damage).
 */
public class GuiSelectWorld extends GuiScreen {

    // Button IDs
    private static final int BTN_DELETE = 5;
    private static final int BTN_CANCEL = 6;
    private static final int BTN_MODE   = 7;

    // NBT key we store in level.dat to remember the world's original mode.
    // 0 = Survival, 1 = Creative.  Missing key → treat as Survival (legacy worlds).
    static final String NBT_GAME_MODE = "GameMode";

    // Currently selected mode on this screen (remembered between screen opens).
    private static boolean creativeMode = false;

    protected GuiScreen parentScreen;
    protected String screenTitle;
    private boolean selected;

    public GuiSelectWorld(GuiScreen guiscreen) {
        screenTitle  = "Select world";
        selected     = false;
        parentScreen = guiscreen;
    }

    // ── initGui ───────────────────────────────────────────────────────────────

    public void initGui() {
        java.io.File file = Minecraft.getMinecraftDir();

        for (int i = 0; i < 5; i++) {
            NBTTagCompound tag = World.func_629_a(file, "World" + (i + 1));
            if (tag == null) {
                controlList.add(new GuiButton(i, width / 2 - 100, height / 6 + 24 * i, "- empty -"));
            } else {
                String s = "World " + (i + 1);
                long l = tag.getLong("SizeOnDisk");
                s = s + " (" + ((float)(((l / 1024L) * 100L) / 1024L) / 100F) + " MB)";
                controlList.add(new GuiButton(i, width / 2 - 100, height / 6 + 24 * i, s));
            }
        }

        initGui2();
    }

    protected String getWorldName(int i) {
        java.io.File file = Minecraft.getMinecraftDir();
        return World.func_629_a(file, "World" + i) == null ? null : "World" + i;
    }

    public void initGui2() {
        controlList.add(new GuiButton(BTN_DELETE, width / 2 - 100, height / 6 + 120 + 12, "Delete world..."));

        // Cancel — left half of the bottom row
        GuiButton cancelBtn = new GuiButton(BTN_CANCEL, width / 2 - 100, height / 6 + 168, "Cancel");
        cancelBtn.width = 98;
        controlList.add(cancelBtn);

        // Mode toggle — right half of the bottom row
        GuiButton modeBtn = new GuiButton(BTN_MODE, width / 2 + 2, height / 6 + 168, getModeLabel());
        modeBtn.width = 98;
        controlList.add(modeBtn);
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    protected void actionPerformed(GuiButton btn) {
        if (!btn.enabled) return;

        if (btn.id < 5) {
            // World slot clicked → check for mode mismatch first
            handleWorldClick(btn.id + 1);

        } else if (btn.id == BTN_DELETE) {
            mc.displayGuiScreen(new GuiDeleteWorld(this));

        } else if (btn.id == BTN_CANCEL) {
            mc.displayGuiScreen(parentScreen);

        } else if (btn.id == BTN_MODE) {
            creativeMode = !creativeMode;
            btn.displayString = getModeLabel();
        }
    }

    /**
     * Called when the player clicks a world slot.
     * If the world already has a saved GameMode that differs from the current
     * selection, we show a warning.  Otherwise we load immediately.
     */
    private void handleWorldClick(int slot) {
        java.io.File file = Minecraft.getMinecraftDir();
        NBTTagCompound tag = World.func_629_a(file, "World" + slot);

        if (tag != null && tag.hasKey(NBT_GAME_MODE)) {
            int savedMode = tag.getInteger(NBT_GAME_MODE); // 0=Survival, 1=Creative
            int chosenMode = creativeMode ? 1 : 0;

            if (savedMode != chosenMode) {
                // Mismatch — show warning screen
                mc.displayGuiScreen(new GuiModeWarning(this, slot));
                return;
            }
        }

        // No mismatch (or brand-new world) — load directly
        selectWorld(slot);
    }

    /**
     * Called by GuiModeWarning when the player confirms they want to switch modes.
     */
    public void confirmModeSwitch(int slot) {
        selectWorld(slot);
    }

    /**
     * Actually loads the world with the currently selected controller.
     * Also writes "GameMode" into level.dat after the world is created
     * so future sessions can detect mismatches.
     */
    public void selectWorld(int i) {
        mc.displayGuiScreen(null);
        if (selected) return;
        selected = true;

        // Set the right controller
        if (creativeMode) {
            mc.field_6327_b = new PlayerControllerTest(mc);  // Creative
        } else {
            mc.field_6327_b = new PlayerControllerSP(mc);    // Survival
        }

        mc.func_6247_b("World" + i);
        mc.displayGuiScreen(null);

        // Write the chosen mode into level.dat so we can detect future mismatches.
        // We do this after func_6247_b so the world folder already exists.
        saveGameModeToNBT("World" + i, creativeMode ? 1 : 0);
    }

    // ── Draw ──────────────────────────────────────────────────────────────────

    public void drawScreen(int i, int j, float f) {
        drawDefaultBackground();
        drawCenteredString(fontRenderer, screenTitle, width / 2, 20, 0xFFFFFF);

        // Hint line below buttons
        String hint = creativeMode
                ? "Creative: fly, instant break, no damage"
                : "Survival: health, mining speed, damage";
        drawCenteredString(fontRenderer, hint, width / 2, height / 6 + 194, 0xAAAAAA);

        super.drawScreen(i, j, f);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static String getModeLabel() {
        return "Mode: " + (creativeMode ? "Creative" : "Survival");
    }

    /**
     * Reads level.dat for the given world name and writes (or overwrites)
     * the "GameMode" integer key, then saves the file back.
     *
     * We do a best-effort write: if anything fails we just skip silently
     * (the game is already loaded; this is non-critical metadata).
     */
    private static void saveGameModeToNBT(String worldName, int mode) {
        try {
            java.io.File mcDir   = Minecraft.getMinecraftDir();
            java.io.File saves   = new java.io.File(mcDir, "saves");
            java.io.File worldDir = new java.io.File(saves, worldName);
            java.io.File levelDat = new java.io.File(worldDir, "level.dat");

            if (!levelDat.exists()) return;

            NBTTagCompound root = CompressedStreamTools.func_1138_a(
                    new java.io.FileInputStream(levelDat));
            NBTTagCompound data = root.getCompoundTag("Data");
            data.setInteger(NBT_GAME_MODE, mode);
            root.setTag("Data", data);

            java.io.File tmp = new java.io.File(worldDir, "level.dat_new");
            CompressedStreamTools.writeGzippedCompoundToOutputStream(
                    root, new java.io.FileOutputStream(tmp));
            levelDat.delete();
            tmp.renameTo(levelDat);
        } catch (Exception e) {
            // Non-critical — silently ignore
        }
    }
}
