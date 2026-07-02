package net.minecraft.src;

import java.util.List;
import net.minecraft.client.Minecraft;

public class GuiSelectWorld extends GuiScreen {

    // ── Кнопки ────────────────────────────────────────────────────────────────
    // ID 0-4  → слоты мира
    // ID 5    → Delete world...
    // ID 6    → Cancel
    // ID 7    → переключатель режима (новая кнопка)

    private static final int BTN_DELETE = 5;
    private static final int BTN_CANCEL = 6;
    private static final int BTN_MODE   = 7;

    // true = Creative, false = Survival.
    // static чтобы запоминать выбор между открытиями экрана.
    private static boolean creativeMode = false;

    public GuiSelectWorld(GuiScreen guiscreen) {
        screenTitle  = "Select world";
        selected     = false;
        parentScreen = guiscreen;
    }

    public void initGui() {
        java.io.File file = Minecraft.getMinecraftDir();

        // Слоты миров
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

        // Cancel — сдвигаем влево чтобы дать место кнопке режима справа
        GuiButton cancelBtn = new GuiButton(BTN_CANCEL, width / 2 - 100, height / 6 + 168, "Cancel");
        cancelBtn.width = 98;
        controlList.add(cancelBtn);

        // Кнопка переключения режима — справа от Cancel, на той же высоте
        GuiButton modeBtn = new GuiButton(BTN_MODE, width / 2 + 2, height / 6 + 168, getModeLabel());
        modeBtn.width = 98;
        controlList.add(modeBtn);
    }

    private static String getModeLabel() {
        return "Mode: " + (creativeMode ? "Creative" : "Survival");
    }

    protected void actionPerformed(GuiButton guibutton) {
        if (!guibutton.enabled) return;

        if (guibutton.id < 5) {
            selectWorld(guibutton.id + 1);

        } else if (guibutton.id == BTN_DELETE) {
            mc.displayGuiScreen(new GuiDeleteWorld(this));

        } else if (guibutton.id == BTN_CANCEL) {
            mc.displayGuiScreen(parentScreen);

        } else if (guibutton.id == BTN_MODE) {
            // Переключить режим и обновить надпись кнопки
            creativeMode = !creativeMode;
            guibutton.displayString = getModeLabel();
        }
    }

    public void selectWorld(int i) {
        mc.displayGuiScreen(null);
        if (selected) return;

        selected = true;

        // Назначаем контроллер в зависимости от выбранного режима
        if (creativeMode) {
            mc.field_6327_b = new PlayerControllerTest(mc); // Creative
        } else {
            mc.field_6327_b = new PlayerControllerSP(mc);   // Survival
        }

        mc.func_6247_b("World" + i);
        mc.displayGuiScreen(null);
    }

    public void drawScreen(int i, int j, float f) {
        drawDefaultBackground();
        drawCenteredString(fontRenderer, screenTitle, width / 2, 20, 0xffffff);

        // Подсказка о текущем режиме под кнопками
        String hint = creativeMode
                ? "Creative: fly, instant break, no damage"
                : "Survival: health, mining speed, damage";
        drawCenteredString(fontRenderer, hint, width / 2, height / 6 + 194, 0xaaaaaa);

        super.drawScreen(i, j, f);
    }

    protected GuiScreen parentScreen;
    protected String screenTitle;
    private boolean selected;
}
