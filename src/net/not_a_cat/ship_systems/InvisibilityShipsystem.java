package net.not_a_cat.ship_systems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.TimeoutTracker;
import org.lwjgl.Sys;

import java.awt.Color;

public class InvisibilityShipsystem extends BaseShipSystemScript {
    protected float getDisruptionLevel(ShipAPI ship) {
        return 0f;
    }

    Long systemActivationMillis = 0L;
    ShipSystemAPI.SystemState prevFrameState;

    protected void maintainStatus(ShipAPI playerShip, ShipSystemStatsScript.State state, float effectLevel) {
//        float f = VULNERABLE_FRACTION;
//        ShipSystemAPI cloak = playerShip.getPhaseCloak();
//        if (cloak == null) cloak = playerShip.getSystem();
//        if (cloak == null) return;
//        if (effectLevel > f) {
//            Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY2,
//                cloak.getSpecAPI().getIconSpriteName(),
//                cloak.getDisplayName(),
//                "time flow altered",
//                false);
//        } else {
//            // Logic for alternative case not provided
//        }
//        if (FLUX_LEVEL_AFFECTS_SPEED) {
//            if (effectLevel > f) {
//                if (getDisruptionLevel(playerShip) <= 0f) {
//                    Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY3,
//                        cloak.getSpecAPI().getIconSpriteName(),
//                        "phase coils stable",
//                        "top speed at 100%",
//                        false);
//                } else {
//                    float speedMult = getSpeedMult(playerShip, effectLevel);
//                    String speedPercentStr = (int)(speedMult * 100f) + "%";
//                    Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY3,
//                        cloak.getSpecAPI().getIconSpriteName(),
//                        "phase coil stress",
//                        "top speed at " + speedPercentStr,
//                        true);
//                }
//            }
//        }
    }



    @Override
    public void apply(MutableShipStatsAPI stats, String id, ShipSystemStatsScript.State state, float effectLevel) {
        ShipAPI myShip;
        boolean player;
        if (!(stats.getEntity() instanceof ShipAPI)) {
            return;
        }

        myShip = (ShipAPI) stats.getEntity();
        ShipSystemAPI cloak = myShip.getPhaseCloak();
        if (cloak == null || !cloak.canBeActivated()) {
            return;
        }

        ShipSystemAPI invisibilitySystem = myShip.getSystem();
        Global.getLogger(InvisibilityShipsystem.class).info("Invisibility: " + invisibilitySystem.getState());
        ShipSystemAPI.SystemState currentState = invisibilitySystem.getState();
        if (prevFrameState != currentState) {
            switch (currentState) {
                case IDLE:
                    myShip.resetOverloadColor();
                    // Do nothing
                    break;
                case ACTIVE:
                    if (1000L < System.currentTimeMillis() - systemActivationMillis) {
                        cloak.forceState(ShipSystemAPI.SystemState.IDLE, 0f);
                        myShip.setOverloadColor(Color.black);
                    } else {
                        myShip.setHoldFireOneFrame(true);
                    }
                    for (WeaponAPI weapon : myShip.getAllWeapons()) {
                        if (weapon.isFiring()) {
                            invisibilitySystem.deactivate();
                        }
                    }
                    break;
                case IN:
                    SettingsAPI settingsAPI = Global.getSettings();
                    CombatEngineAPI combatEngine = Global.getCombatEngine();
                    combatEngine.spawnAsteroid(3, myShip.getLocation().x, myShip.getLocation().y, 0f, 0f);
                    myShip.getLocation().set(1000f, 1000f);
                    for (ShipAPI ship : combatEngine.getShips()) {
                        ship.setShipAI(new ShipAIPlugin() {
                            @Override
                            public void setDoNotFireDelay(float amount) {

                            }

                            @Override
                            public void forceCircumstanceEvaluation() {

                            }

                            @Override
                            public void advance(float amount) {

                            }

                            @Override
                            public boolean needsRefit() {
                                return false;
                            }

                            @Override
                            public ShipwideAIFlags getAIFlags() {
                                return null;
                            }

                            @Override
                            public void cancelCurrentManeuver() {

                            }

                            @Override
                            public ShipAIConfig getConfig() {
                                return null;
                            }
                        });

                    }

                    systemActivationMillis = System.currentTimeMillis();
                    cloak.forceState(ShipSystemAPI.SystemState.ACTIVE, 0f);
                    break;
                case OUT:
//                myShip.getMutableStats().getEntity().setCollisionRadius(100);
                    myShip.resetOverloadColor();
                    systemActivationMillis = 0L;
                    break;
                case COOLDOWN:
                    myShip.resetOverloadColor();
                    // Chill
                    break;
            }
            prevFrameState = currentState;
        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        // ??
    }

    @Override
    public StatusData getStatusData(int index, ShipSystemStatsScript.State state, float effectLevel) {
        super.getStatusData(index, state, effectLevel);
        return null;
    }
}