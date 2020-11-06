package org.popcraft.chunky;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.plugin.Plugin;

public class Milk implements Listener {
    @EventHandler
    public void onItemConsume(PlayerItemConsumeEvent event){
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Chunky");


        if(event.getItem().getType().equals(Material.MILK_BUCKET)){
            event.getPlayer().setFoodLevel(event.getPlayer().getFoodLevel()+plugin.getConfig().getInt("Food-Amount")); //set player food level

            event.getPlayer().setSaturation(event.getPlayer().getSaturation()+plugin.getConfig().getInt("Saturation-Amount")); //set player saturation value
        }

    }
}
