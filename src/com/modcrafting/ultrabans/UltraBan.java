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
import com.modcrafting.ultrabans.db.SQLDatabases;
import com.modcrafting.ultrabans.util.DataHandler;
import com.modcrafting.ultrabans.util.EditBan;
import com.modcrafting.ultrabans.util.Formatting;

public class UltraBan extends JavaPlugin {

	public Logger log = Logger.getLogger("Minecraft");
	public SQLDatabases db = new SQLDatabases();
	public String maindir = "plugins/UltraBanLite/";
	public HashSet<String> bannedPlayers = new HashSet<String>();
	public HashSet<String> bannedIPs = new HashSet<String>();
	public Map<String, Long> tempBans = new HashMap<String, Long>();
	public Map<String, EditBan> banEditors = new HashMap<String, EditBan>();
	private final UltraBanPlayerListener playerListener = new UltraBanPlayerListener(this);
	public DataHandler data = new DataHandler(this);
	public Formatting util = new Formatting(this);
	public String regexAdmin = "%admin%";
	public String regexReason = "%reason%";
	public String regexVictim = "%victim%";
	public boolean autoComplete;
	
	public void onDisable() {
		tempBans.clear();
		bannedPlayers.clear();
		bannedIPs.clear();
		banEditors.clear();
		System.out.println("UltraBan disabled.");
	}
	protected void createDefaultConfiguration(String name) {
		File actual = new File(getDataFolder(), name);
		if (!actual.exists()) {

			InputStream input =
				this.getClass().getResourceAsStream("/" + name);
			if (input != null) {
				FileOutputStream output = null;

				try {
					output = new FileOutputStream(actual);
					byte[] buf = new byte[8192];
					int length = 0;
					while ((length = input.read(buf)) > 0) {
						output.write(buf, 0, length);
					}

					System.out.println(getDescription().getName()
							+ ": Default configuration file written: " + name);
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						if (input != null)
							input.close();
					} catch (IOException e) {}

					try {
						if (output != null)
							output.close();
					} catch (IOException e) {}
				}
			}
		}
	}
	public void onEnable() {
		YamlConfiguration Config = (YamlConfiguration) getConfig();
		PluginDescriptionFile pdfFile = this.getDescription();
		new File(maindir).mkdir();
		createDefaultConfiguration("config.yml"); //Swap for new setup
		this.autoComplete = Config.getBoolean("auto-complete", true);
		loadCommands();
		if (Config != null) log.log(Level.INFO, "[" + pdfFile.getName() + "]" + " Configuration: config.yml Loaded!");
		db.initialize(this);
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(playerListener, this);
		log.log(Level.INFO, "[" + pdfFile.getName() + "] Listeners enabled, Server is secured.");
		log.log(Level.INFO,"[" + pdfFile.getName() + "]" + " version " + pdfFile.getVersion() + " has been initialized!" );
		
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

		


