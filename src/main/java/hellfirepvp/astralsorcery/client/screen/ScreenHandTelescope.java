/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2020
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.screen;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import hellfirepvp.astralsorcery.client.ClientScheduler;
import hellfirepvp.astralsorcery.client.lib.TexturesAS;
import hellfirepvp.astralsorcery.client.screen.base.ConstellationDiscoveryScreen;
import hellfirepvp.astralsorcery.client.screen.base.SkyScreen;
import hellfirepvp.astralsorcery.client.screen.telescope.PlayerAngledConstellationInformation;
import hellfirepvp.astralsorcery.client.util.Blending;
import hellfirepvp.astralsorcery.client.util.RenderingConstellationUtils;
import hellfirepvp.astralsorcery.client.util.RenderingDrawUtils;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.IMajorConstellation;
import hellfirepvp.astralsorcery.common.constellation.SkyHandler;
import hellfirepvp.astralsorcery.common.constellation.star.StarLocation;
import hellfirepvp.astralsorcery.common.constellation.world.DayTimeHelper;
import hellfirepvp.astralsorcery.common.constellation.world.WorldContext;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.LogicalSide;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ScreenHandTelescope
 * Created by HellFirePvP
 * Date: 15.02.2020 / 15:37
 */
public class ScreenHandTelescope extends ConstellationDiscoveryScreen<ConstellationDiscoveryScreen.DrawArea> {

    private static final int randomStars = 40;
    private List<Point.Float> usedStars = new ArrayList<>(randomStars);

    public ScreenHandTelescope() {
        super(new TranslationTextComponent("screen.astralsorcery.hand_telescope"), 216, 216);
    }

    @Nonnull
    @Override
    protected List<DrawArea> createDrawAreas() {
        return Lists.newArrayList(new DrawArea(this.getGuiBox()));
    }

    @Override
    protected void fillConstellations(WorldContext ctx, List<DrawArea> drawAreas) {
        Random gen = ctx.getDayRandom();

        Set<IConstellation> used = new HashSet<>();
        for (DrawArea area : drawAreas) {
            Collection<IConstellation> available = Lists.newArrayList(ctx.getActiveCelestialsHandler().getActiveConstellations());
            available.removeIf(c -> !(c instanceof IMajorConstellation) ||
                    used.contains(c) ||
                    !c.canDiscover(Minecraft.getInstance().player, ResearchHelper.getClientProgress()));
            IConstellation cst = MiscUtils.getRandomEntry(available, gen);
            if (cst instanceof IMajorConstellation) {
                used.add(cst);
                float yaw = (gen.nextFloat() * 360F) - 180F;
                float pitch = -90F + gen.nextFloat() * 25F;
                area.addConstellationToArea(cst, new PlayerAngledConstellationInformation(DEFAULT_CONSTELLATION_SIZE, yaw, pitch));
            }
        }

        int offsetX = 6, offsetY = 6;
        int width = guiWidth - 6, height = guiHeight - 6;
        for (int i = 0; i < randomStars; i++) {
            usedStars.add(new Point.Float(offsetX + gen.nextFloat() * width, offsetY + gen.nextFloat() * height));
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float pTicks) {
        super.render(mouseX, mouseY, pTicks);

        this.drawWHRect(TexturesAS.TEX_GUI_HAND_TELESCOPE);

        this.blitOffset -= 10;
        this.drawTelescopeCell(pTicks);
        this.blitOffset += 10;
    }

    private void drawTelescopeCell(float pTicks) {
        boolean canSeeSky = this.canObserverSeeSky(Minecraft.getInstance().player.getPosition(), 1);
        float pitch = Minecraft.getInstance().player.getPitch(pTicks);
        float angleOpacity = 0F;
        if (pitch < -60F) {
            angleOpacity = 1F;
        } else if (pitch < -10F) {
            angleOpacity = (Math.abs(pitch) - 10F) / 50F;
            if (DayTimeHelper.isNight(Minecraft.getInstance().world)) {
                angleOpacity *= angleOpacity;
            }
        }
        float brMultiplier = angleOpacity;

        GlStateManager.enableBlend();
        Blending.DEFAULT.applyStateManager();
        GlStateManager.disableAlphaTest();

        this.drawSkyBackground(pTicks, canSeeSky, angleOpacity);

        if (!this.isInitialized()) {
            GlStateManager.enableAlphaTest();
            GlStateManager.disableBlend();
            return;
        }

        WorldContext ctx = SkyHandler.getContext(Minecraft.getInstance().world, LogicalSide.CLIENT);
        if (ctx != null && canSeeSky) {
            Random gen = ctx.getDayRandom();
            Tessellator tes = Tessellator.getInstance();
            BufferBuilder buf = tes.getBuffer();
            double guiFactor = Minecraft.getInstance().mainWindow.getGuiScaleFactor();

            float playerYaw = Minecraft.getInstance().player.rotationYaw % 360F;
            if (playerYaw < 0) {
                playerYaw += 360F;
            }
            if (playerYaw >= 180F) {
                playerYaw -= 360F;
            }
            float playerPitch = Minecraft.getInstance().player.rotationPitch;

            this.blitOffset += 1;
            buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            TexturesAS.TEX_STAR_1.bindTexture();
            float starSize = 5F;
            for (Point.Float pos : this.usedStars) {
                float brightness = 0.4F + (RenderingConstellationUtils.stdFlicker(ClientScheduler.getClientTick(), pTicks, 10 + gen.nextInt(20))) * 0.5F;
                brightness = this.multiplyStarBrightness(pTicks, brightness);
                brightness *= brMultiplier;
                Color rColor = new Color(brightness, brightness, brightness, brightness);
                this.drawRect(buf).at(pos.x + this.getGuiLeft(), pos.y + this.getGuiTop()).dim(starSize, starSize).color(rColor).draw();
            }
            tes.draw();
            this.blitOffset -= 1;

            this.blitOffset += 3;
            for (DrawArea areas : this.getVisibleDrawAreas()) {
                for (IConstellation cst : areas.getDisplayMap().keySet()) {
                    ConstellationDisplayInformation info = areas.getDisplayMap().get(cst);
                    info.getFrameDrawInformation().clear();
                    if (!(info instanceof PlayerAngledConstellationInformation)) {
                        continue;
                    }
                    PlayerAngledConstellationInformation cstInfo = (PlayerAngledConstellationInformation) info;
                    float diffYaw = playerYaw - cstInfo.getYaw();
                    float diffPitch = playerPitch - cstInfo.getPitch();

                    float maxDistance = 35F;
                    if ((Math.abs(diffYaw) <= maxDistance || Math.abs(playerYaw + 360F) <= maxDistance) &&
                            Math.abs(diffPitch) <= maxDistance) {

                        float rainBr = 1F - Minecraft.getInstance().world.getRainStrength(pTicks);
                        int wPart = MathHelper.floor(this.getGuiWidth() * 0.1F);
                        int hPart = MathHelper.floor(this.getGuiHeight() * 0.1F);
                        float xFactor = diffYaw   / 8F;
                        float yFactor = diffPitch / 8F;

                        GL11.glEnable(GL11.GL_SCISSOR_TEST);
                        GL11.glScissor(MathHelper.floor((this.getGuiLeft() + 5) * guiFactor),
                                MathHelper.floor((this.getGuiTop() + 5) * guiFactor),
                                MathHelper.floor((this.getGuiWidth() - 10) * guiFactor),
                                MathHelper.floor((this.getGuiHeight() - 10) * guiFactor));

                        Map<StarLocation, Rectangle> cstRenderInfo = RenderingConstellationUtils.renderConstellationIntoGUI(
                                cst,
                                this.getGuiLeft() + wPart + MathHelper.floor((xFactor / guiFactor) * this.getGuiWidth()),
                                this.getGuiTop() + hPart + MathHelper.floor((yFactor / guiFactor) * this.getGuiHeight()),
                                this.blitOffset,
                                this.getGuiWidth() - MathHelper.floor(wPart * 1.5F),
                                this.getGuiHeight() - MathHelper.floor(hPart * 1.5F),
                                2F,
                                () -> (0.3F + 0.7F * RenderingConstellationUtils.conCFlicker(ClientScheduler.getClientTick(), pTicks, 5 + gen.nextInt(15))) * rainBr * brMultiplier,
                                ResearchHelper.getClientProgress().hasConstellationDiscovered(cst),
                                true
                        );

                        GL11.glDisable(GL11.GL_SCISSOR_TEST);

                        info.getFrameDrawInformation().putAll(cstRenderInfo);
                    }
                }
            }
            this.blitOffset -= 3;

            this.blitOffset += 5;
            this.renderDrawnLines(gen, pTicks);
            this.blitOffset -= 5;
        }

        GlStateManager.enableAlphaTest();
        GlStateManager.disableBlend();
    }

    @Override
    public void mouseMoved(double xPos, double yPos) {
        if (!Minecraft.getInstance().mouseHelper.isMouseGrabbed()) {
            return;
        }

        int offsetX = 6, offsetY = 6;
        int width = guiWidth - 12, height = guiHeight - 12;

        Minecraft mc = Minecraft.getInstance();
        double xDiff = mc.mouseHelper.getMouseX() - (xPos / ((double) mc.mainWindow.getScaledWidth()  / mc.mainWindow.getWidth()));
        double yDiff = mc.mouseHelper.getMouseY() - (yPos / ((double) mc.mainWindow.getScaledHeight() / mc.mainWindow.getHeight()));
        if (Minecraft.getInstance().player != null &&
                Minecraft.getInstance().player.getPitch(1.0F) <= -89.99F && yDiff > 0) {
            yDiff = 0;
        }

        for (Point.Float sl : usedStars) {
            sl.x -= xDiff;
            sl.y += yDiff;

            if (sl.x < offsetX) {
                sl.x += width;
            } else if (sl.x > (offsetX + width)) {
                sl.x -= width;
            }
            if (sl.y < offsetY) {
                sl.y += height;
            } else if (sl.y > (offsetY + height)) {
                sl.y -= height;
            }
        }
    }

    private void drawSkyBackground(float pTicks, boolean canSeeSky, float angleOpacity) {
        Tuple<Color, Color> rgbFromTo = SkyScreen.getSkyGradient(canSeeSky, angleOpacity, pTicks);
        RenderingDrawUtils.drawGradientRect(this.blitOffset,
                this.guiLeft + 4, this.guiTop + 4,
                this.guiLeft + this.guiWidth - 8, this.guiTop + this.guiHeight - 8,
                rgbFromTo.getA().getRGB(), rgbFromTo.getB().getRGB());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected boolean shouldRightClickCloseScreen(double mouseX, double mouseY) {
        return true;
    }
}