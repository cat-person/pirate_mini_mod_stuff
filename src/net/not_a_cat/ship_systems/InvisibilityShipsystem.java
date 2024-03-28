package net.not_a_cat.ship_systems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;

import java.awt.Color;

public class InvisibilityShipsystem extends BaseShipSystemScript {
    ShipSystemStatsScript.State previousState = State.IDLE;
    ShipVariantAPI invincibleVariant = Global.getSettings().createEmptyVariant("not_a_cat_lobster_invincible", Global.getSettings().getHullSpec("not_a_cat_lobster_invincible"));
    ShipAPI invincibleShip = Global.getCombatEngine().createFXDrone(invincibleVariant);

    ShipAPI originalHost;

    @Override
    public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
//        Global.getLogger(InvisibilityShipsystem.class).info(String.format("::getInfoText(%s, %s)", system.getId(), ship.getId()));
        return super.getInfoText(system, ship);
    }

    @Override
    public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
//        Global.getLogger(InvisibilityShipsystem.class).info(String.format("::isUsable(%s, %s)", system.getId(), ship.getId()));
        return super.isUsable(system, ship);
    }

    @Override
    public float getActiveOverride(ShipAPI ship) {
//        Global.getLogger(InvisibilityShipsystem.class).info(String.format("::getActiveOverride(%s)", ship.getId()));
        return super.getActiveOverride(ship);
    }

    @Override
    public float getInOverride(ShipAPI ship) {
        Global.getLogger(InvisibilityShipsystem.class).info(String.format("::getInOverride(%s)", ship.getId()));
        return super.getInOverride(ship);

    }

    @Override
    public float getOutOverride(ShipAPI ship) {
//        Global.getLogger(InvisibilityShipsystem.class).info(String.format("::getOutOverride(%s)", ship.getId()));
        return super.getOutOverride(ship);
    }

    @Override
    public float getRegenOverride(ShipAPI ship) {
//        Global.getLogger(InvisibilityShipsystem.class).info(String.format("::getRegenOverride(%s)", ship.getId()));
        return super.getRegenOverride(ship);
    }

    @Override
    public int getUsesOverride(ShipAPI ship) {
//        Global.getLogger(InvisibilityShipsystem.class).info(String.format("::getUsesOverride(%s)", ship.getId()));
        return super.getUsesOverride(ship);
    }

    @Override
    public String getDisplayNameOverride(State state, float effectLevel) {
//        Global.getLogger(InvisibilityShipsystem.class).info(String.format("::getDisplayNameOverride(%s, %f)", state, effectLevel));
        return super.getDisplayNameOverride(state, effectLevel);
    }

    @Override
    public void apply(MutableShipStatsAPI stats, String id, ShipSystemStatsScript.State state, float effectLevel) {
//        Global.getLogger(InvisibilityShipsystem.class).info(String.format("::apply(%s, %s, %f)", id, state, effectLevel));
        if (!(stats.getEntity() instanceof ShipAPI)) {
            return;
        }

        ShipAPI hostShip = (ShipAPI) stats.getEntity();
        switch (state) {
            case IDLE:
                break;
            case IN:
                if(previousState == State.IDLE){
                    // Just activated
                    phaseIn(hostShip);
                }
                break;
            case ACTIVE:
                if(previousState == State.IN){
                    becomeInvisible(hostShip);
                    phaseOut(hostShip);
                }
                break;
            case OUT:
                if(previousState == State.ACTIVE){
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

    void becomeInvisible(ShipAPI hostShip) {
        Global.getLogger(InvisibilityShipsystem.class).info(String.format("::becomeInvisible(%s)", hostShip.getId()));

        invincibleShip.getLocation().set(hostShip.getLocation());
        invincibleShip.getVelocity().set(hostShip.getVelocity());
        invincibleShip.setAngularVelocity(hostShip.getAngularVelocity());
        invincibleShip.setFacing(hostShip.getFacing());
        originalHost = hostShip;
        Global.getCombatEngine().addEntity(invincibleShip);
        if(hostShip.getId() == Global.getCombatEngine().getPlayerShip().getId()) {
            Global.getCombatEngine().setPlayerShipExternal(invincibleShip);
        }
        hostShip.getLocation().set(10000f, 10000f); // Far far away
    }
    void phaseOut(ShipAPI hostShip) {
        Global.getLogger(InvisibilityShipsystem.class).info(String.format("::phaseOut(%s)", hostShip.getId()));
        ShipSystemAPI cloak = hostShip.getPhaseCloak();
        if (cloak == null || !cloak.canBeActivated()) {
            return;
        }

        cloak.forceState(ShipSystemAPI.SystemState.IDLE, 0f);
    }

    void becomeVisible(ShipAPI hostShip){
        Global.getLogger(InvisibilityShipsystem.class).info(String.format("::becomeVisible(%s)", hostShip.getId()));

        hostShip.getLocation().set(invincibleShip.getLocation());
        hostShip.getVelocity().set(invincibleShip.getVelocity());
        hostShip.setAngularVelocity(invincibleShip.getAngularVelocity());
        hostShip.setFacing(invincibleShip.getFacing());

        if(invincibleShip.getId() == Global.getCombatEngine().getPlayerShip().getId()) {
            Global.getCombatEngine().setPlayerShipExternal(hostShip);
        }

        Global.getCombatEngine().removeEntity(invincibleShip);
    }

    @Override
    public StatusData getStatusData(int index, ShipSystemStatsScript.State state, float effectLevel) {
//        Global.getLogger(InvisibilityShipsystem.class).info(String.format("::getStatusData(%d, %s, %f)", index, state, effectLevel));
        return super.getStatusData(index, state, effectLevel);
    }
}