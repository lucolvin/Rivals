package net.sudologic.rivals;

import com.nisovin.shopkeepers.api.events.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.WorldSaveEvent;

import java.util.List;

public class EventManager implements Listener {
    private double killEntityPower, killMonsterPower, killPlayerPower, deathPowerLoss, tradePower;


    public EventManager(ConfigurationSection settings) {
        /*killEntityPower = 0;
        killMonsterPower = 1;
        killPlayerPower = 3;
        deathPowerLoss = -4;
        tradePower = 1;*/
        killEntityPower = (double) settings.get("killEntityPower");
        killMonsterPower = (double) settings.get("killMonsterPower");
        killPlayerPower = (double) settings.get("killPlayerPower");
        deathPowerLoss = (double) settings.get("deathPowerLoss");
        tradePower = (double) settings.get("tradePower");
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        FactionManager manager = Rivals.getFactionManager();
        if(e.getEntity().getKiller() != null) {
            Player killer = e.getEntity().getKiller();
            double power = Math.round(killEntityPower * 100.0) / 100.0;
            if(e.getEntity() instanceof Monster) {
                power = Math.round(killMonsterPower * 100.0) / 100.0;
            } else if(e.getEntity() instanceof Player) {
                power = Math.round(killPlayerPower * 100.0) / 100.0;
            }
            Faction killerFaction = manager.getFactionByPlayer(killer.getUniqueId());
            if(killerFaction != null) {
                killerFaction.powerChange(power);
            }
        }
        if(e.getEntity() instanceof Player) {
            Faction playerFaction = manager.getFactionByPlayer(e.getEntity().getUniqueId());
            if(playerFaction != null) {
                playerFaction.powerChange(deathPowerLoss);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        FactionManager manager = Rivals.getFactionManager();
        List<Integer> invites = manager.getInvitesForPlayer(e.getPlayer().getUniqueId());
        if(manager.getFactionByPlayer(e.getPlayer().getUniqueId()) == null && invites.size() > 0) {
            String inviteMess = "[Rivals] You're invited to join " + manager.getFactionByID(invites.get(0)).getColor() + manager.getFactionByID(invites.get(0)).getName();
            e.getPlayer().sendMessage(inviteMess);
        } else {
            if(manager.getFactionByPlayer(e.getPlayer().getUniqueId()) == null) {
                e.getPlayer().sendMessage("[Rivals] You haven't joined a faction yet!");
                return;
            }
            e.getPlayer().sendMessage("[Rivals] Faction status:");
            Rivals.getCommand().sendFactionInfo(e.getPlayer(), manager.getFactionByPlayer(e.getPlayer().getUniqueId()), "");
        }
    }

    @EventHandler
    public void onWorldSave(WorldSaveEvent e) {
        FactionManager manager = Rivals.getFactionManager();
        manager.removeInvitesOver7Days();
    }

    @EventHandler
    public void onTrade(ShopkeeperTradeEvent e) {
        Faction f = Rivals.getShopManager().getFactionForShopLocation(e.getShopkeeper().getLocation());
        Player p = e.getPlayer();
        Faction pFaction = Rivals.getFactionManager().getFactionByPlayer(p.getUniqueId());
        if(f != pFaction) {
            f.powerChange(tradePower);
        }
    }
}
