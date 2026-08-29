package com.earthforge.compat;

import com.earthforge.klaymore.Klaymore;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.scripted.entity.ScriptEntity;
import noppes.npcs.scripted.entity.ScriptNpc;
import noppes.npcs.scripted.event.NpcEvent;
import net.minecraft.entity.Entity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class NpcTargetExtractors {
    private NpcTargetExtractors() {
    }

    public static void registerAll(){
        Klaymore.registerTargetExtractor(
            NpcEvent.class,
            NpcTargetExtractors::extractNpcEntity
        );
        Klaymore.LOG.info("[Klaymore-CNpcCompat] Extractor registered: NpcEvent -> EntityNPCInterface (UNWRAPPED, matches wand-bind target)");
    }

    /**
     * 根因修复：NpcEvent.npc 是 ICustomNpc（通常每次事件都是新建的 ScriptNpc wrapper），
     * 而 wand 绑定脚本时传入的 target 是 EntityCustomNpc（Minecraft 原生 Entity）。
     * 两者类型完全不同，如果直接把 ICustomNpc 当目标，SubscriberRegistry 的 HashMap
     * 用 "Entity 对象" 当 key 存、用 "ICustomNpc 对象" 当 key 查，永远匹配不上。
     * <p>
     * 这里把 ICustomNpc wrapper 解包回底层的 EntityNPCInterface（Entity 的子类），
     * 同一个 NPC 在同一个世界里 Entity 对象永远是同一个引用，保证匹配成功。
     */
    private static Entity extractNpcEntity(NpcEvent event) {
        if (event == null || event.npc == null) return null;
        Object wrapped = event.npc;

        // 1. 最快路径：ScriptNpc 有 public 字段 npc = EntityNPCInterface
        if (wrapped instanceof ScriptNpc) {
            EntityNPCInterface npcEntity = ((ScriptNpc<?>) wrapped).npc;
            if (npcEntity != null) return npcEntity;
        }

        // 2. 兜底：ScriptEntity 所有子类都有 protected T entity（父类字段）
        try {
            Field f = ScriptEntity.class.getDeclaredField("entity");
            f.setAccessible(true);
            Object entity = f.get(wrapped);
            if (entity instanceof Entity) return (Entity) entity;
        } catch (Throwable ignored) {
        }

        // 3. 再兜底：反射尝试常见 getter 名
        for (String methodName : new String[]{"getEntity", "getMCEntity", "getNpcEntity", "getEntityInstance"}) {
            try {
                Method m = wrapped.getClass().getMethod(methodName);
                m.setAccessible(true);
                Object res = m.invoke(wrapped);
                if (res instanceof Entity) return (Entity) res;
            } catch (Throwable ignored) {
            }
        }

        // 4. 彻底解不出来：返回 null（此时这条事件不会被定向派发）
        Klaymore.LOG.warn("[Klaymore-CNpcCompat] Failed to unwrap ICustomNpc to Entity: "
            + wrapped.getClass().getName() + ". You will lose target-specific dispatch "
            + "for NpcEvents of this type.");
        return null;
    }
}
