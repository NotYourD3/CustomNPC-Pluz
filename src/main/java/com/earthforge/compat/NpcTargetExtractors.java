package com.earthforge.compat;

import com.earthforge.klaymore.Klaymore;
import noppes.npcs.scripted.event.NpcEvent;

public class NpcTargetExtractors {
    private NpcTargetExtractors() {
    }

    public static void registerAll(){
        Klaymore.registerTargetExtractor(
            NpcEvent.class,
            event -> event.npc
        );
    }
}
