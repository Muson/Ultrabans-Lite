/* COPYRIGHT (c) 2012 Joshua McCurry
 * This work is licensed under the
 * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License
 * and use of this software or its code is an agreement to this license.
 * A full copy of this license can be found at
 * http://creativecommons.org/licenses/by-nc-sa/3.0/. 
 */
package com.modcrafting.ultrabans;

import java.util.Date;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import com.modcrafting.ultrabans.UltraBan;

public class UltraBanPlayerListener implements Listener{
	UltraBan plugin;
	String spamcheck = null;
	int spamCount = 0;
	public UltraBanPlayerListener(UltraBan ultraBans) {
		this.plugin = ultraBans;
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerLogin(PlayerLoginEvent event){
		FileConfiguration config = plugin.getConfig();
		Player player = event.getPlayer();
		if(plugin.bannedPlayers.contains(player.getName().toLowerCase())){
			String reason = plugin.db.getBanReason(player.getName());
			String admin = plugin.db.getAdmin(player.getName());
			String banMsgBroadcast = config.getString("messages.LoginBan", "%admin% banned you from this server! Reason: %reason%!");
			banMsgBroadcast = banMsgBroadcast.replaceAll(plugin.regexAdmin, admin);
			banMsgBroadcast = banMsgBroadcast.replaceAll(plugin.regexReason, reason);
			event.disallow(PlayerLoginEvent.Result.KICK_BANNED, banMsgBroadcast);
		}
		if(plugin.tempBans.get(player.getName().toLowerCase()) != null){
			long tempTime = plugin.tempBans.get(player.getName().toLowerCase());
			long diff = tempTime - (System.currentTimeMillis()/1000);
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
		if(config.getBoolean("lockdown", false)){
			String lockMsgLogin = config.getString("messages.lockMsgLogin", "Server is under a lockdown, Try again later!");
			if(player.hasPermission("ultraban.override.lockdown") || player.isOp()) event.disallow(PlayerLoginEvent.Result.KICK_OTHER, lockMsgLogin);
			plugin.getLogger().info(player.getName() + " attempted to join during lockdown.");
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerJoin(PlayerJoinEvent event){
		YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
		final Player player = event.getPlayer();
		String ip = player.getAddress().getAddress().getHostAddress();
		plugin.db.setAddress(player.getName().toLowerCase(), ip);
		plugin.getLogger().info("Logged " + player.getName() + " connecting from ip:" + ip);
		
		
		if(plugin.bannedIPs.contains(ip)){
			plugin.getLogger().info("Banned player attempted Login!");
			event.setJoinMessage(null);
			String adminMsg = config.getString("messages.LoginIPBan", "Your IP is banned!");
			player.kickPlayer(adminMsg);
		}
		if(!plugin.db.matchAddress(player.getName(), ip)){
			plugin.db.updateAddress(player.getName(), ip);
		}
		if(player.hasPermission("ultraban.override.dupeip")||!config.getBoolean("enableLoginDupeCheck", true)) return;
		plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin,new Runnable(){

			@Override
			public void run() {
				String ip = plugin.db.getAddress(player.getName());
				List<String> list = plugin.db.listPlayers(ip);
				int ping = ((CraftPlayer) player).getHandle().ping;
				for(Player admin:plugin.getServer().getOnlinePlayers()){
					if(admin.hasPermission("ultraban.dupeip")){
						if(ip == null){
							admin.sendMessage(ChatColor.RED + "Unable to view ip for " + player.getName() + " !");
							return;
						}
						for(String name:list){
							if(!name.equalsIgnoreCase(player.getName())) admin.sendMessage(ChatColor.GRAY + "Player: " + name + " duplicates player: " + player.getName() + "!");
						}
					}
					if(admin.hasPermission("ultraban.ping")){
						if(checkPlayerPing(player)){
							admin.sendMessage(ChatColor.GRAY + "Player: " + player.getName() + " was kicked for High Ping!");
						}else{
							admin.sendMessage(ChatColor.GRAY + "Player: " + player.getName() + " Ping: "+String.valueOf(ping)+"ms");						
						}
						
					}
				}
			}
		},20L);
	}

	
	public boolean checkPlayerPing(Player player){
		boolean pingout = ((CraftPlayer) player).getHandle().ping>plugin.getConfig().getInt("MaxPing",200);
		if(pingout&&!player.hasPermission("ultraban.override.pingcheck")){
			player.kickPlayer(plugin.getConfig().getString("kickMsgVictim","You have been kicked by %admin%. Reason: %reason%!").replaceAll("%admin%", "Ultrabans").replaceAll("%reason%", "High Ping Rate"));
			return true;
		}
		//pass
		return false;
	}
}
