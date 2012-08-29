/* COPYRIGHT (c) 2012 Joshua McCurry
 * This work is licensed under the
 * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License
 * and use of this software or its code is an agreement to this license.
 * A full copy of this license can be found at
 * http://creativecommons.org/licenses/by-nc-sa/3.0/. 
 */
package com.modcrafting.ultrabans;
/**
 * Wickity Wickity Wooh
 * Got to love the magic!
 */
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import com.modcrafting.ultrabans.commands.Ban;
import com.modcrafting.ultrabans.commands.Check;
import com.modcrafting.ultrabans.commands.CheckIP;
import com.modcrafting.ultrabans.commands.DupeIP;
import com.modcrafting.ultrabans.commands.Edit;
import com.modcrafting.ultrabans.commands.Export;
import com.modcrafting.ultrabans.commands.Help;
import com.modcrafting.ultrabans.commands.History;
import com.modcrafting.ultrabans.commands.Import;
import com.modcrafting.ultrabans.commands.Ipban;
import com.modcrafting.ultrabans.commands.Kick;
import com.modcrafting.ultrabans.commands.Perma;
import com.modcrafting.ultrabans.commands.Reload;
import com.modcrafting.ultrabans.commands.Tempban;
import com.modcrafting.ultrabans.commands.Tempipban;
import com.modcrafting.ultrabans.commands.Unban;
import com.modcrafting.ultrabans.commands.Version;
import com.modcrafting.ultrabans.commands.Warn;
import com.modcrafting.ultrabans.db.Database;
import com.modcrafting.ultrabans.db.SQL;
import com.modcrafting.ultrabans.db.SQLite;
import com.modcrafting.ultrabans.util.DataHandler;
import com.modcrafting.ultrabans.util.EditBan;
import com.modcrafting.ultrabans.util.Formatting;

public class UltraBan extends JavaPlugin {
	public String maindir = "plugins/UltraBanLite/";
	public HashSet<String> bannedPlayers = new HashSet<String>();
	public HashSet<String> bannedIPs = new HashSet<String>();
	public Map<String, Long> tempBans = new HashMap<String, Long>();
	public Map<String, EditBan> banEditors = new HashMap<String, EditBan>();
	public Database db;
	private final UltraBanPlayerListener playerListener = new UltraBanPlayerListener(this);
	public DataHandler data = new DataHandler(this);
	public Formatting util = new Formatting(this);
	public String regexAdmin = "%admin%";
	public String regexReason = "%reason%";
	public String regexVictim = "%victim%";
	public boolean autoComplete;
	public void onDisable() {
		this.getServer().getScheduler().cancelTasks(this);
		tempBans.clear();
		bannedPlayers.clear();
		bannedIPs.clear();
		banEditors.clear();
	}
	public void onEnable() {
		this.getDataFolder().mkdir();
		data.createDefaultConfiguration("config.yml");
		FileConfiguration config = getConfig();
		autoComplete = config.getBoolean("auto-complete", true);
		long l = config.getLong("serverSync.timing", 72000L); 
		long time = System.currentTimeMillis();
		if(this.getConfig().getString("Database").equalsIgnoreCase("mysql")){
			db = new SQL(this);
		}else{
			db = new SQLite(this);
		}
		db.load();
		
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new UltraBanPlayerListener(this), this);
		if(config.getBoolean("serverSync.enable", false)) this.getServer().getScheduler().scheduleAsyncRepeatingTask(this,new Runnable(){
			@Override
			public void run() {
				onDisable();
				db.load();
				System.out.println("UltraBans Sync is Enabled!");
			}
			
		},l,l);	
		loadCommands();
		long diff = System.currentTimeMillis()-time;
		this.getLogger().info(" Loaded. "+diff+"ms");
	}
	public void loadCommands(){

		getCommand("ban").setExecutor(new Ban(this));
		getCommand("checkban").setExecutor(new Check(this));
		getCommand("checkip").setExecutor(new CheckIP(this));
		getCommand("dupeip").setExecutor(new DupeIP(this));
		getCommand("editban").setExecutor(new Edit(this));
		getCommand("importbans").setExecutor(new Import(this));
		getCommand("exportbans").setExecutor(new Export(this));
		getCommand("uhelp").setExecutor(new Help(this));
		getCommand("ipban").setExecutor(new Ipban(this));
		getCommand("kick").setExecutor(new Kick(this));
		getCommand("ureload").setExecutor(new Reload(this));
		getCommand("tempban").setExecutor(new Tempban(this));
		getCommand("tempipban").setExecutor(new Tempipban(this));
		getCommand("unban").setExecutor(new Unban(this));
		getCommand("uversion").setExecutor(new Version(this));
		getCommand("warn").setExecutor(new Warn(this));
		getCommand("permaban").setExecutor(new Perma(this));
		getCommand("history").setExecutor(new History(this));
	}
}

		


