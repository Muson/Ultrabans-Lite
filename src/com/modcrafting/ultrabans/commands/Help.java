package com.modcrafting.ultrabans.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.modcrafting.ultrabans.UltraBan;

public class Help implements CommandExecutor{
	UltraBan plugin;
	
	public Help(UltraBan ultraBan) {
		this.plugin = ultraBan;
	}
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		boolean auth = false;
		boolean server = false;
		Player player = null;
		if (sender instanceof Player){
			player = (Player)sender;
			if (plugin.setupPermissions()){
				if (plugin.permission.has(player, "ultraban.help")) auth = true;
			}else{
			 if (player.isOp()) auth = true; //defaulting to Op if no vault doesn't take or node
			}
		}else{
			auth = true;
			server = true;
		}
		if (server){
			sender.sendMessage("Required Info {} Optional () Silent -s Anonymous -a");
			sender.sendMessage("/ban        {player} (-s/-a) {reason}");
			sender.sendMessage("/permaban   {player} (-s/-a) {reason}");		
			sender.sendMessage("/tempban    {player} (-s/-a) {amt} {sec/min/hour/day} {Reason}");
			sender.sendMessage("/ipban      {player} (-s/-a) {reason}");
			sender.sendMessage("/tempipban  {player} (-s/-a) {amt} {sec/min/hour/day} {Reason}");
			sender.sendMessage("/unban      {player}");
			sender.sendMessage("/checkban   {player}");
			sender.sendMessage("/checkip    {player}");
			sender.sendMessage("/history    {amt}");
			sender.sendMessage("/kick       {player} (-s/-a) {reason}");
			sender.sendMessage("/editban    (help)");
			sender.sendMessage("/uhelp /exportbans /importbans /ureload /uversion");
			return true;
		}
		if (auth) {
			if (plugin.setupPermissions()){
		sender.sendMessage(ChatColor.GRAY + "Ultrabans " + ChatColor.BLUE + "Required Info {}" + ChatColor.GREEN + " Optional ()" + ChatColor.RED + " Silent -s");
		if (plugin.permission.has(player, "ultraban.ban")) sender.sendMessage(ChatColor.GRAY + "/ban {player} {reason}");
		if (plugin.permission.has(player, "ultraban.permaban"))sender.sendMessage(ChatColor.GRAY + "/permaban {player} {reason}");		
		if (plugin.permission.has(player, "ultraban.tempban"))sender.sendMessage(ChatColor.GRAY + "/tempban {player} {amt} {sec/min/hour/day} {Reason}");
		if (plugin.permission.has(player, "ultraban.ipban"))sender.sendMessage(ChatColor.GRAY + "/ipban {player} {reason}");		
		if (plugin.permission.has(player, "ultraban.tempipban"))sender.sendMessage(ChatColor.GRAY + "/tempipban {player} {amt} {sec/min/hour/day} {Reason}");
		if (plugin.permission.has(player, "ultraban.unban"))sender.sendMessage(ChatColor.GRAY + "/unban {player}");
		if (plugin.permission.has(player, "ultraban.check"))sender.sendMessage(ChatColor.GRAY + "/checkban /checkip /dupeip {player}");
		if (plugin.permission.has(player, "ultraban.kick"))sender.sendMessage(ChatColor.GRAY + "/kick {player} {reason}");
		if (plugin.permission.has(player, "ultraban.warn"))sender.sendMessage(ChatColor.GRAY + "/warn {player} {reason}");
		if (plugin.permission.has(player, "ultraban.editban"))sender.sendMessage(ChatColor.GRAY + "/editban (help)");
		if (plugin.permission.has(player, "ultraban.admin")) sender.sendMessage(ChatColor.GRAY + "/uhelp /exportbans /ureload /uversion /importbans");
		return true;
			}
		}else{
		sender.sendMessage(ChatColor.RED + "You do not have the required permissions.");
		return true;
		}
		return false;
			
	}

}
