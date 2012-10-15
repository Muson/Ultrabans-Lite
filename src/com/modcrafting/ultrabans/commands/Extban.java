package com.modcrafting.ultrabans.commands;

import com.modcrafting.ultrabans.UltraBan;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;


public class Extban implements CommandExecutor{
	UltraBan plugin;
	public Extban(UltraBan ultraBan) {
		this.plugin = ultraBan;
	}
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    	YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
		boolean broadcast = true;
		Player player = null;
		String admin = config.getString("defAdminName", "server");
		String reason = config.getString("defReason", "not sure");
		if (sender instanceof Player){
			player = (Player)sender;
			admin = player.getName();
		}
		if(!sender.hasPermission(command.getPermission())){
			sender.sendMessage(ChatColor.RED + "You do not have the required permissions.");
			return true;
		}
		if (args.length < 1) return false;
                reason = plugin.util.combineSplit(1, args, " ");
		long tempTime = 0;
                
		String p = args[0];
		if(plugin.autoComplete) p = plugin.util.expandName(p);
                
                int count = plugin.db.countRecordsByType(p, 10);
                
                tempTime = plugin.util.parseTimeSpec(config.getString("defExtTime.amt", "5"), config.getString("defExtTime.mode", "min"));
                // Scale time on ban number
                tempTime *= count;
		
		Player victim = plugin.getServer().getPlayer(p);
		long temp = System.currentTimeMillis()/1000+tempTime; //epoch time
		
		if(victim != null){
			if(victim.getName() == admin){
				sender.sendMessage(ChatColor.RED + "You cannot tempban yourself!");
				return true;
			}
			if(victim.hasPermission( "ultraban.override.tempban")){
				sender.sendMessage(ChatColor.RED + "Your tempban has been denied! Player Notified!");
				victim.sendMessage(ChatColor.RED + "Player: " + admin + " Attempted to tempban you!");
				return true;
			}	
			if(plugin.bannedPlayers.contains(victim.getName().toLowerCase())){
				sender.sendMessage(ChatColor.BLUE + victim.getName() +  ChatColor.GRAY + " is already banned for " + reason);
				return true;
			}
			plugin.tempBans.put(victim.getName().toLowerCase(), temp);
			plugin.db.addPlayer(victim.getName(), reason, admin, temp, 0);
                        plugin.db.addPlayer(victim.getName(), reason, admin, temp, 10);
			
			String tempbanMsgBroadcast = config.getString("messages.tempbanMsgBroadcast");
			if(tempbanMsgBroadcast.contains(plugin.regexAdmin)) tempbanMsgBroadcast = tempbanMsgBroadcast.replaceAll(plugin.regexAdmin, admin);
			if(tempbanMsgBroadcast.contains(plugin.regexReason)) tempbanMsgBroadcast = tempbanMsgBroadcast.replaceAll(plugin.regexReason, reason);
			if(tempbanMsgBroadcast.contains(plugin.regexVictim)) tempbanMsgBroadcast = tempbanMsgBroadcast.replaceAll(plugin.regexVictim, victim.getName());
			if(tempbanMsgBroadcast != null){
				if(broadcast){
					plugin.getServer().broadcastMessage(plugin.util.formatMessage(tempbanMsgBroadcast));
				}else{
					sender.sendMessage(ChatColor.ITALIC + "Silent: " + plugin.util.formatMessage(tempbanMsgBroadcast));
				}
			}

			String tempbanMsgVictim = config.getString("messages.tempbanMsgVictim", "You have been temp. banned by %admin%. Reason: %reason%!");
			if(tempbanMsgVictim.contains(plugin.regexAdmin)) tempbanMsgVictim = tempbanMsgVictim.replaceAll(plugin.regexAdmin, admin);
			if(tempbanMsgVictim.contains(plugin.regexReason)) tempbanMsgVictim = tempbanMsgVictim.replaceAll(plugin.regexReason, reason);
			victim.kickPlayer(plugin.util.formatMessage(tempbanMsgVictim));
			plugin.getLogger().info(admin + " tempbanned player " + victim.getName() + ".");
			return true;
		}else{
			victim = plugin.getServer().getOfflinePlayer(p).getPlayer();
			if(victim != null){
				if(victim.hasPermission( "ultraban.override.tempban")){
					sender.sendMessage(ChatColor.RED + "Your tempban has been denied!");
					return true;
				}
			}
			if(plugin.bannedPlayers.contains(p.toLowerCase())){
				sender.sendMessage(ChatColor.BLUE + p +  ChatColor.GRAY + " is already banned for " + reason);
				return true;
			}
			plugin.tempBans.put(p.toLowerCase(), temp);
			plugin.db.addPlayer(p, reason, admin, temp, 0);
                        plugin.db.addPlayer(p, reason, admin, temp, 10);
			
			String tempbanMsgBroadcast = config.getString("messages.tempbanMsgBroadcast");
			if(tempbanMsgBroadcast.contains(plugin.regexAdmin)) tempbanMsgBroadcast = tempbanMsgBroadcast.replaceAll(plugin.regexAdmin, admin);
			if(tempbanMsgBroadcast.contains(plugin.regexReason)) tempbanMsgBroadcast = tempbanMsgBroadcast.replaceAll(plugin.regexReason, reason);
			if(tempbanMsgBroadcast.contains(plugin.regexVictim)) tempbanMsgBroadcast = tempbanMsgBroadcast.replaceAll(plugin.regexVictim, p);
			if(tempbanMsgBroadcast != null){
				if(broadcast){
					plugin.getServer().broadcastMessage(plugin.util.formatMessage(tempbanMsgBroadcast));
				}else{
					sender.sendMessage(ChatColor.ITALIC + "Silent: " + plugin.util.formatMessage(tempbanMsgBroadcast));
				}
			}
			

			plugin.getLogger().info(admin + " tempbanned player " + p + ".");
			return true;
		}
	}
}
