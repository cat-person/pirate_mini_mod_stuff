package net.not_a_cat.ship_systems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.combat.listeners.DamageListener;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.loading.WeaponGroupSpec;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;

import java.awt.Color;
import java.util.*;

public class InvisibilityShipsystem extends BaseShipSystemScript {
    ShipSystemStatsScript.State previousState = State.IDLE;
//    ShipVariantAPI invincibleVariant = Global.getSettings().createEmptyVariant("not_a_cat_lobster_invincible", Global.getSettings().getHullSpec("not_a_cat_lobster_invincible"));
//    ShipAPI invincibleShip = Global.getCombatEngine().createFXDrone(invincibleVariant);

    ShipVariantAPI fakeTarget = Global.getSettings().createEmptyVariant("fake_target", Global.getSettings().getHullSpec("fake_target"));

    Random random = new Random();
    List<ShipAPI> fakeTargetDrones = Arrays.asList(
            Global.getCombatEngine().createFXDrone(fakeTarget),
            Global.getCombatEngine().createFXDrone(fakeTarget),
            Global.getCombatEngine().createFXDrone(fakeTarget),
            Global.getCombatEngine().createFXDrone(fakeTarget),
            Global.getCombatEngine().createFXDrone(fakeTarget),
            Global.getCombatEngine().createFXDrone(fakeTarget),
            Global.getCombatEngine().createFXDrone(fakeTarget),
            Global.getCombatEngine().createFXDrone(fakeTarget),
            Global.getCombatEngine().createFXDrone(fakeTarget),
            Global.getCombatEngine().createFXDrone(fakeTarget),
            Global.getCombatEngine().createFXDrone(fakeTarget),
            Global.getCombatEngine().createFXDrone(fakeTarget),
            Global.getCombatEngine().createFXDrone(fakeTarget),
            Global.getCombatEngine().createFXDrone(fakeTarget),
            Global.getCombatEngine().createFXDrone(fakeTarget),
            Global.getCombatEngine().createFXDrone(fakeTarget),
            Global.getCombatEngine().createFXDrone(fakeTarget),
            Global.getCombatEngine().createFXDrone(fakeTarget),
            Global.getCombatEngine().createFXDrone(fakeTarget),
            Global.getCombatEngine().createFXDrone(fakeTarget));


    Map<ShipAPI, Long> fakeTargetsMap = new HashMap<>();

    ShipAPI originalHost;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, ShipSystemStatsScript.State state, float effectLevel) {
        if (!(stats.getEntity() instanceof ShipAPI)) {
            return;
        }

        ShipAPI hostShip = (ShipAPI) stats.getEntity();
        switch (state) {
            case IDLE:
                break;
            case IN:
                if (previousState != State.IN) {
                    // Just activated
                    phaseIn(hostShip);
                }
                break;
            case ACTIVE:
                if (previousState != State.ACTIVE) {
                    phaseOut(hostShip);
                    becomeInvisible(hostShip, stats, id);
                } else {
                    for (ShipAPI fakeTarget : fakeTargetDrones) {
                        if (!fakeTargetsMap.containsKey(fakeTarget)) {
                            float dx = 400f * random.nextFloat() - 200f;
                            float dy = 400f * random.nextFloat() - 200f;

                            float x = hostShip.getLocation().x + dx;
                            float y = hostShip.getLocation().y + dy;

                            Global.getCombatEngine().addEntity(fakeTarget);
                            fakeTarget.getVelocity().set(hostShip.getVelocity());

                            fakeTarget.getLocation().set(x, y);

                            fakeTargetsMap.put(fakeTarget, System.currentTimeMillis() + (long) (500f * random.nextFloat()));
                        } else if (1000 < System.currentTimeMillis() - fakeTargetsMap.get(fakeTarget)) {
                            float dx = 400f * random.nextFloat() - 200f;
                            float dy = 400f * random.nextFloat() - 200f;

                            float x = hostShip.getLocation().x + dx;
                            float y = hostShip.getLocation().y + dy;

                            fakeTarget.getLocation().set(x, y);
                            fakeTarget.getVelocity().set(hostShip.getVelocity());

                            fakeTargetsMap.put(fakeTarget, System.currentTimeMillis() + (long) (500f * random.nextFloat()));
                        }
                    }
                }

                for (WeaponAPI weapon : hostShip.getAllWeapons()) {
                    if (weapon.isFiring()) {
                        becomeVisible(hostShip);
                        hostShip.getSystem().deactivate();
                    }
                }
                break;
            case OUT:
                if (previousState != State.OUT) {
                    becomeVisible(hostShip);
                }
                break;
            case COOLDOWN:
                break;
        }
        previousState = state;
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
//        Global.getLogger(InvisibilityShipsystem.class).info(String.format("::unapply(%s)", id));
    }

    void phaseIn(ShipAPI hostShip) {
        Global.getLogger(InvisibilityShipsystem.class).info(String.format("::phaseIn(%s)", hostShip.getId()));
        ShipSystemAPI cloak = hostShip.getPhaseCloak();
        if (cloak == null || !cloak.canBeActivated()) {
            return;
        }
        cloak.forceState(ShipSystemAPI.SystemState.ACTIVE, 0f);
        hostShip.setHoldFireOneFrame(true);
    }

    DamageListener damageTakenListener = new DamageListener() {
        @Override
        public void reportDamageApplied(Object source, CombatEntityAPI target, ApplyDamageResultAPI result) {
            becomeVisible(originalHost);
        }
    };

    void becomeInvisible(final ShipAPI hostShip, final MutableShipStatsAPI stats, final String id) {
        Global.getLogger(InvisibilityShipsystem.class).info(String.format("::becomeInvisible(%s)", hostShip.getId()));
        originalHost = hostShip;
        hostShip.getMutableStats().getHullDamageTakenMult().modifyMult("invisibility", 0f);
        hostShip.getMutableStats().getWeaponDamageTakenMult().modifyMult("invisibility", 0f);
        hostShip.getMutableStats().getEngineDamageTakenMult().modifyMult("invisibility", 0f);
        hostShip.setAlphaMult(0.5f);
        hostShip.setHoldFire(true);

        hostShip.addListener(damageTakenListener);
    }

    void phaseOut(ShipAPI hostShip) {
        Global.getLogger(InvisibilityShipsystem.class).info(String.format("::phaseOut(%s)", hostShip.getId()));
        ShipSystemAPI cloak = hostShip.getPhaseCloak();
        if (cloak == null || !cloak.canBeActivated()) {
            return;
        }

        cloak.forceState(ShipSystemAPI.SystemState.IDLE, 0f);
    }

    void becomeVisible(ShipAPI hostShip) {
        Global.getLogger(InvisibilityShipsystem.class).info(String.format("::becomeVisible(%s)", hostShip.getId()));
        hostShip.getMutableStats().getHullDamageTakenMult().modifyMult("invisibility", 1f);
        hostShip.getMutableStats().getWeaponDamageTakenMult().modifyMult("invisibility", 1f);
        hostShip.getMutableStats().getEngineDamageTakenMult().modifyMult("invisibility", 1f);
        hostShip.setAlphaMult(1f);
        hostShip.removeListener(damageTakenListener);

        if(hostShip.getSystem().isActive()) {
            hostShip.getSystem().deactivate();
        }

        for (ShipAPI fakeTarget : fakeTargetDrones) {
            Global.getCombatEngine().removeEntity(fakeTarget);
        }
        fakeTargetsMap.clear();
    }
}