package net.not_a_cat.ship_systems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;

import java.awt.Color;

public class InvisibilityShipsystem extends BaseShipSystemScript {
    Long systemActivationMillis = 0L;

    @Override
    public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
        Global.getLogger(InvisibilityShipsystem.class).info(String.format("::getInfoText(%s, %s)", system.getId(), ship.getId()));
        return super.getInfoText(system, ship);
    }

    @Override
    public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
        Global.getLogger(InvisibilityShipsystem.class).info(String.format("::isUsable(%s, %s)", system.getId(), ship.getId()));
        return super.isUsable(system, ship);
    }

    @Override
    public float getActiveOverride(ShipAPI ship) {
        Global.getLogger(InvisibilityShipsystem.class).info(String.format("::getActiveOverride(%s)", ship.getId()));
        return super.getActiveOverride(ship);
    }

    @Override
    public float getInOverride(ShipAPI ship) {
        Global.getLogger(InvisibilityShipsystem.class).info(String.format("::getInOverride(%s)", ship.getId()));
        return super.getInOverride(ship);

    }

    @Override
    public float getOutOverride(ShipAPI ship) {
        Global.getLogger(InvisibilityShipsystem.class).info(String.format("::getOutOverride(%s)", ship.getId()));
        return super.getOutOverride(ship);
    }

    @Override
    public float getRegenOverride(ShipAPI ship) {
        Global.getLogger(InvisibilityShipsystem.class).info(String.format("::getRegenOverride(%s)", ship.getId()));
        return super.getRegenOverride(ship);
    }

    @Override
    public int getUsesOverride(ShipAPI ship) {
        Global.getLogger(InvisibilityShipsystem.class).info(String.format("::getUsesOverride(%s)", ship.getId()));
        return super.getUsesOverride(ship);
    }

    @Override
    public String getDisplayNameOverride(State state, float effectLevel) {
        Global.getLogger(InvisibilityShipsystem.class).info(String.format("::getDisplayNameOverride(%s, %f)", state, effectLevel));
        return super.getDisplayNameOverride(state, effectLevel);
    }

    @Override
    public void apply(MutableShipStatsAPI stats, String id, ShipSystemStatsScript.State state, float effectLevel) {
        Global.getLogger(InvisibilityShipsystem.class).info(String.format("::apply(%s, %s, %s, %f)", stats, id, state, effectLevel));
        if (!(stats.getEntity() instanceof ShipAPI)) {
            return;
        }

        Long currentTime = System.currentTimeMillis();
        ShipAPI hostShip = (ShipAPI) stats.getEntity();
        Global.getLogger(InvisibilityShipsystem.class).info("Invisibility: " + state);
        switch (state) {
            case IDLE:
                break;
            case ACTIVE:
                for (WeaponAPI weapon : hostShip.getAllWeapons()) {
                    if (weapon.isFiring()) {
                        hostShip.getSystem().deactivate();
                    }
                }
                break;
            case IN:
                if(systemActivationMillis == 0L){
                    systemActivationMillis = phaseIn(hostShip);
                } else if (300L < currentTime - systemActivationMillis){
                    becomeInvisible(hostShip);
                    phaseOut(hostShip);
                } else if (3000L < currentTime - systemActivationMillis){
                    becomeVisible(hostShip);
                }
                break;
            case OUT:
                becomeVisible(hostShip);
                systemActivationMillis = 0L;
                break;
            case COOLDOWN:
                systemActivationMillis = 0L;
                break;
        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        Global.getLogger(InvisibilityShipsystem.class).info(String.format("::unapply(%s, %s, %s, %f)", stats, id));
    }

    Long phaseIn(ShipAPI hostShip) {
        Global.getLogger(InvisibilityShipsystem.class).info(String.format("::phaseIn(%s)", hostShip.getId()));
        ShipSystemAPI cloak = hostShip.getPhaseCloak();
        if (cloak == null || !cloak.canBeActivated()) {
            return 0L;
        }
        cloak.forceState(ShipSystemAPI.SystemState.ACTIVE, 0f);
        hostShip.setHoldFireOneFrame(true);
        return System.currentTimeMillis();
    }

    Long becomeInvisible(ShipAPI hostShip) {
        Global.getLogger(InvisibilityShipsystem.class).info(String.format("::becomeInvisible(%s)", hostShip.getId()));
//        hostShip.getMutableStats().getHullDamageTakenMult().modifyMult("invisibility", 0f);
//        hostShip.getMutableStats().getArmorDamageTakenMult().modifyMult("invisibility", 0f);
//        hostShip.getMutableStats().getEngineDamageTakenMult().modifyMult("invisibility", 0f);

        Global.getCombatEngine().removeEntity(hostShip);

        Global.getCombatEngine().getPlayerShip();

//        Global.getCombatEngine().addEntity();

        return System.currentTimeMillis();
    }
    Long phaseOut(ShipAPI hostShip) {
        Global.getLogger(InvisibilityShipsystem.class).info(String.format("::phaseOut(%s)", hostShip.getId()));
        ShipSystemAPI cloak = hostShip.getPhaseCloak();
        if (cloak == null || !cloak.canBeActivated()) {
            return 0L;
        }

        cloak.forceState(ShipSystemAPI.SystemState.IDLE, 0f);
        return 0L;
    }

    void becomeVisible(ShipAPI hostShip){
        Global.getLogger(InvisibilityShipsystem.class).info(String.format("::becomeVisible(%s)", hostShip.getId()));

//        hostShip.getMutableStats().getHullDamageTakenMult().modifyMult("invisibility", 1f);
//        hostShip.getMutableStats().getArmorDamageTakenMult().modifyMult("invisibility", 1f);
//        hostShip.getMutableStats().getEngineDamageTakenMult().modifyMult("invisibility", 1f);
    }

    @Override
    public StatusData getStatusData(int index, ShipSystemStatsScript.State state, float effectLevel) {
        Global.getLogger(InvisibilityShipsystem.class).info(String.format("::getStatusData(%d, %s, %f)", index, state, effectLevel));
        return super.getStatusData(index, state, effectLevel);
    }
}