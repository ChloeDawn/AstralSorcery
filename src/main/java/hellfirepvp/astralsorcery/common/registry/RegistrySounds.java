/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2019
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.registry;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.util.sound.CategorizedSoundEvent;
import net.minecraft.client.audio.Sound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;

import static hellfirepvp.astralsorcery.common.lib.SoundsAS.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistrySounds
 * Created by HellFirePvP
 * Date: 02.08.2019 / 21:20
 */
public class RegistrySounds {

    private RegistrySounds() {}

    public static void init() {
        BLOCK_COLOREDLENS_ATTACH = registerSound("block_coloredlens_attach", SoundCategory.BLOCKS);

        CRAFT_ATTUNEMENT = registerSound("craft_attunement", SoundCategory.MASTER);
        CRAFT_FINISH = registerSound("craft_finish", SoundCategory.BLOCKS);

        ALTAR_CRAFT_START = registerSound("altar_craft_start", SoundCategory.BLOCKS);
        ALTAR_CRAFT_FINISH = registerSound("altar_craft_finish", SoundCategory.BLOCKS);
        ALTAR_CRAFT_LOOP_T1 = registerSound("altar_craft_loop_t1", SoundCategory.BLOCKS);
        ALTAR_CRAFT_LOOP_T2 = registerSound("altar_craft_loop_t2", SoundCategory.BLOCKS);
        ALTAR_CRAFT_LOOP_T3 = registerSound("altar_craft_loop_t3", SoundCategory.BLOCKS);
        ALTAR_CRAFT_LOOP_T4 = registerSound("altar_craft_loop_t4", SoundCategory.BLOCKS);
        ALTAR_CRAFT_LOOP_T4_WAITING = registerSound("altar_craft_loop_t4_waiting", SoundCategory.BLOCKS);

        INFUSER_CRAFT_START = registerSound("infuser_craft_start", SoundCategory.BLOCKS);
        INFUSER_CRAFT_LOOP = registerSound("infuser_craft_loop", SoundCategory.BLOCKS);
        INFUSER_CRAFT_FINISH = registerSound("infuser_craft_finish", SoundCategory.BLOCKS);

        PERK_SEAL = registerSound("perk_seal", SoundCategory.MASTER);
        PERK_UNSEAL = registerSound("perk_unseal", SoundCategory.MASTER);
        PERK_UNLOCK = registerSound("perk_unlock", SoundCategory.MASTER);

        GUI_JOURNAL_CLOSE = registerSound("gui_journal_close", SoundCategory.MASTER);
        GUI_JOURNAL_PAGE = registerSound("gui_journal_page", SoundCategory.MASTER);
    }

    private static <T extends SoundEvent> T registerSound(String jsonName, SoundCategory predefinedCategory) {
        ResourceLocation res = AstralSorcery.key(jsonName);
        CategorizedSoundEvent se = new CategorizedSoundEvent(res, predefinedCategory);
        se.setRegistryName(res);
        return registerSound((T) se);
    }

    private static <T extends SoundEvent> T registerSound(String jsonName) {
        ResourceLocation res = AstralSorcery.key(jsonName);
        SoundEvent se = new SoundEvent(res);
        se.setRegistryName(res);
        return registerSound((T) se);
    }

    private static <T extends SoundEvent> T registerSound(T soundEvent) {
        AstralSorcery.getProxy().getRegistryPrimer().register(soundEvent);
        return soundEvent;
    }

}
