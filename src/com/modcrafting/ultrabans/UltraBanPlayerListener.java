/* COPYRIGHT (c) 2012 Joshua McCurry
 * This work is licensed under the
 * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License
 * and use of this software or its code is an agreement to this license.
 * A full copy of this license can be found at
 * http://creativecommons.org/licenses/by-nc-sa/3.0/. 
 */

package com.modcrafting.ultrabans;

import java.util.Date;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

public class UltraBanPlayerListener implements Listener{
	public static final Logger log = Logger.getLogger("Minecraft");
	UltraBan plugin;
	public UltraBanPlayerListener(UltraBan ultraBans) {
		this.plugin = ultraBans;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerLogin(PlayerLoginEvent event){
		YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
		Player player = event.getPlayer();
		if(plugin.bannedPlayers.contains(player.getName().toLowerCase())){
			System.out.println("banned player joined");
			String reason = plugin.db.getBanReason(player.getName());
			String admin = plugin.db.getAdmin(player.getName());
			String banMsgBroadcast = config.getString("messages.LoginBan", "%admin% banned you from this server! Reason: %reason%!");
			banMsgBroadcast = banMsgBroadcast.replaceAll("%admin%", admin);
			banMsgBroadcast = banMsgBroadcast.replaceAll("%reason%", reason);
			event.disallow(PlayerLoginEvent.Result.KICK_OTHER, banMsgBroadcast);
		}
		if(plugin.tempBans.get(player.getName().toLowerCase()) != null){
			long tempTime = plugin.tempBans.get(player.getName().toLowerCase());
			long now = System.currentTimeMillis()/1000;
			long diff = tempTime - now;
			if(diff <= 0){
				String ip = plugin.db.getAddress(player.getName());
				if(plugin.bannedIPs.contains(ip)){
					plugin.bannedIPs.remove(ip);
					Bukkit.unbanIP(ip);
					System.out.println("Also removed the IP ban!");
				}
				plugin.tempBans.remove(player.getName().toLowerCase());
				plugin.bannedPlayers.remove(player.getName().toLowerCase());
				plugin.db.removeFromBanlist(player.getName().toLowerCase());
				String admin = config.getString("defAdminName", "server");
				String reason = plugin.db.getBanReason(player.getName());
				plugin.db.addPlayer(player.getName(), "Untempbanned: " + reason, admin, 0, 5);
				return;
			}
			Date date = new Date();
			date.setTime(tempTime*1000);
			String dateStr = date.toString();
			String reason = plugin.db.getBanReason(player.getName());
			String adminMsg = "You've been tempbanned for " + reason + " Remaining:" + dateStr;
			event.disallow(PlayerLoginEvent.Result.KICK_OTHER, adminMsg);
			return;
		}
		
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerJoin(PlayerJoinEvent event){
		YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
		Player player = event.getPlayer();
		String ip = player.getAddress().getAddress().getHostAddress();
		plugin.db.setAddress(player.getName().toLowerCase(), ip);
		System.out.println("[UltraBan] Logged " + player.getName() + " connecting from ip:" + ip);
		
		
		if(plugin.bannedIPs.contains(ip)){
			System.out.println("[UltraBan] Banned player attempted Login!");
			event.setJoinMessage(null);
			String adminMsg = config.getString("messages.LoginIPBan", "Your IP is banned!");
			player.kickPlayer(adminMsg);
		}
		if(!plugin.db.matchAddress(player.getName(), ip)){
			plugin.db.updateAddress(player.getName(), ip);
		}
	}

		 
}
