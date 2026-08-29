package com.earthforge.compat;

import com.earthforge.klaymore.Klaymore;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import noppes.npcs.scripted.event.NpcEvent;


public class NpcEventHandler {

    @SubscribeEvent
    public void onNpcEvent(NpcEvent event) {
        Klaymore.postScriptEvent(event);
    }

}
