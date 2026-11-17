package com.technova.client;

import com.technova.chemical.Chemical;
import com.technova.menu.MachineMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

/**
 * Ecran generique des machines. Tout est dessine avec des rectangles pleins (pas de texture
 * de GUI), ce qui evite les differences d'API de rendu entre versions.
 */
public class MachineScreen extends AbstractContainerScreen<MachineMenu> {
    private static final int ENERGY_X = 8;
    private static final int BAR_Y = 16;
    private static final int BAR_W = 10;
    private static final int BAR_H = 52;
    private static final int TANK_X = 22;
    private static final int TANK_STEP = 14;

    public MachineScreen(MachineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        this.renderTooltip(g, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        // Fond du panneau avec un liseré clair/sombre.
        g.fill(x, y, x + imageWidth, y + imageHeight, 0xFFC6C6C6);
        g.fill(x, y, x + imageWidth, y + 2, 0xFFFFFFFF);
        g.fill(x, y, x + 2, y + imageHeight, 0xFFFFFFFF);
        g.fill(x, y + imageHeight - 2, x + imageWidth, y + imageHeight, 0xFF555555);
        g.fill(x + imageWidth - 2, y, x + imageWidth, y + imageHeight, 0xFF555555);

        for (Slot s : this.menu.slots) {
            g.fill(x + s.x - 1, y + s.y - 1, x + s.x + 17, y + s.y + 17, 0xFF373737);
            g.fill(x + s.x, y + s.y, x + s.x + 16, y + s.y + 16, 0xFF8B8B8B);
        }

        if (this.menu.be.usesEnergy()) {
            int cap = this.menu.energyCapacity();
            bar(g, x + ENERGY_X, y + BAR_Y, cap <= 0 ? 0f : (float) this.menu.energy() / cap, 0xFFD83A3A);
        }

        int tanks = this.menu.be.tankCount();
        for (int t = 0; t < Math.min(3, tanks); t++) {
            int cap = this.menu.tankCapacity(t);
            Chemical chemical = this.menu.tankChemical(t);
            float ratio = cap <= 0 ? 0f : (float) this.menu.tankAmount(t) / cap;
            bar(g, x + TANK_X + t * TANK_STEP, y + BAR_Y, chemical == Chemical.EMPTY ? 0f : ratio, chemical.color);
        }

        if (this.menu.be.showsProgress()) {
            int max = this.menu.maxProgress();
            float ratio = max <= 0 ? 0f : (float) this.menu.progress() / max;
            int ax = x + 100;
            int ay = y + 38;
            g.fill(ax - 1, ay - 1, ax + 25, ay + 9, 0xFF373737);
            g.fill(ax, ay, ax + 24, ay + 8, 0xFF1E1E1E);
            g.fill(ax, ay, ax + (int) (24 * ratio), ay + 8, 0xFF3BA0E2);
        }

        if (this.menu.be.showsAux()) {
            int max = this.menu.auxMax();
            float ratio = max <= 0 ? 0f : (float) this.menu.aux() / max;
            int ax = x + 78;
            int ay = y + 55;
            g.fill(ax - 1, ay - 1, ax + 17, ay + 5, 0xFF373737);
            g.fill(ax, ay, ax + 16, ay + 4, 0xFF1E1E1E);
            g.fill(ax, ay, ax + (int) (16 * ratio), ay + 4, 0xFFF08A24);
        }
    }

    private static void bar(GuiGraphics g, int bx, int by, float ratio, int color) {
        g.fill(bx - 1, by - 1, bx + BAR_W + 1, by + BAR_H + 1, 0xFF373737);
        g.fill(bx, by, bx + BAR_W, by + BAR_H, 0xFF1E1E1E);
        int fill = Math.round(BAR_H * Math.max(0f, Math.min(1f, ratio)));
        g.fill(bx, by + BAR_H - fill, bx + BAR_W, by + BAR_H, color);
    }

    /** Affiche le detail (quantite exacte) de la jauge survolee, a droite du libelle d'inventaire. */
    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        super.renderLabels(g, mouseX, mouseY);
        int mx = mouseX - this.leftPos;
        int my = mouseY - this.topPos;
        String info = null;

        if (my >= BAR_Y && my < BAR_Y + BAR_H) {
            if (this.menu.be.usesEnergy() && mx >= ENERGY_X && mx < ENERGY_X + BAR_W) {
                info = this.menu.energy() + " / " + this.menu.energyCapacity() + " FE";
            }
            int tanks = Math.min(3, this.menu.be.tankCount());
            for (int t = 0; t < tanks; t++) {
                int tx = TANK_X + t * TANK_STEP;
                if (mx >= tx && mx < tx + BAR_W) {
                    Chemical chemical = this.menu.tankChemical(t);
                    String name = chemical == Chemical.EMPTY
                            ? Component.translatable("gui.technova.empty").getString()
                            : Component.translatable(chemical.translationKey()).getString();
                    info = name + " " + this.menu.tankAmount(t) + "/" + this.menu.tankCapacity(t) + " mB";
                }
            }
        }
        if (info != null) {
            g.drawString(this.font, info, 68, 72, 0xFF202020, false);
        }
    }
}
