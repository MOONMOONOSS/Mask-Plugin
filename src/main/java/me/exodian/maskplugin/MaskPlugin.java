package me.exodian.maskplugin;

import com.codingforcookies.armorequip.ArmorEquipEvent;
import com.codingforcookies.armorequip.ArmorListener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;


public class MaskPlugin extends JavaPlugin {
    private Manager m = new Manager();

    @Override
    public void onEnable() {
        getLogger().info("MaskPlugin has started.");

        saveDefaultConfig();

        getServer().getPluginManager().registerEvents(m, this);
        getServer().getPluginManager().registerEvents(new ArmorListener(getConfig().getStringList("blocked")), this);
        getCommand("helm").setExecutor(this::onCommand);
        m.setTeamName(getConfig().getString("scoreboardTeam.name"));

        m.prepareScoreboardTeam();
    }


    @Override
    public void onDisable() {}

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("helm")) {
            final Player p = (Player) sender;

            if (p.hasPermission("invishelm.use"))
                p.getInventory().addItem(m.getInvisHelm());
            return true;
        }
        return false;
    }

    private class Manager implements Listener {
        private String teamName;
        private ItemStack invisHelm;

        private void prepareScoreboardTeam() {
            prepareArmor();

            final Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
            Team team = scoreboard.getTeam(teamName);
            if (team == null && scoreboard != null)
                team = scoreboard.registerNewTeam(teamName);

            team.setDisplayName(getConfig().getString("scoreboardTeam.displayName"));
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
        }

        private void setTeamName(String s) {
            teamName = s;
        }

        private ItemStack getInvisHelm() {
            return invisHelm;
        }

        private void prepareArmor() {
            final ItemStack invisHelmTEMP = new ItemStack(Material.SKELETON_SKULL);
            final ItemMeta invisHelmMETA = invisHelmTEMP.getItemMeta();
            final ArrayList<String> lore = new ArrayList<String>();

            invisHelmMETA.setDisplayName(ChatColor.DARK_RED + "" + ChatColor.ITALIC + "Helm of Occlusion");
            invisHelmMETA.setLocalizedName("InvisHelm");

            lore.add("A holy remnant of the Ancient One.");

            invisHelmMETA.setLore(lore);
            invisHelmTEMP.setItemMeta(invisHelmMETA);

            invisHelm = invisHelmTEMP;
        }

        @EventHandler(priority = EventPriority.HIGH)
        private void onArmorUsage(ArmorEquipEvent event) {
            final Player p = event.getPlayer();
            final Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

            //System.out.print("EVENT STARTED");
            ItemStack OldArmor = event.getOldArmorPiece();
            ItemStack NewArmor = event.getNewArmorPiece();


            if(OldArmor != null && OldArmor.equals(invisHelm)){
                if (scoreboard.getTeam(teamName).hasEntry(p.getName())) {
                    scoreboard.getTeam(teamName).removeEntry(p.getName());
                    final String oldTeam = getConfig().getString(p.getName());
                    scoreboard.getTeam(oldTeam).addEntry(p.getName());
                    p.sendMessage(ChatColor.DARK_RED + "[InvisHelm] Your name-tag is unHidden!");
                    /////////////// Essentials Hider ///////////////////////////
                    User user = ess.getUser(p.getName());
                    for (Player pl : ess.getOnlinePlayers()) {
                       pl.showPlayer(user.getBase());
                    }
                    user.setHidden(false);
                    ess.getVanishedPlayers().remove(p.getName());  
                }
            }



            else if(NewArmor != null && NewArmor.equals(invisHelm)){
                final Team team = p.getScoreboard().getPlayerTeam(p);
                final String oldName = team.getName();
                getConfig().set(p.getName(), oldName);
                scoreboard.getTeam(teamName).addEntry(p.getName());
                p.sendMessage(ChatColor.DARK_RED + "[InvisHelm] Your name-tag is Hidden!");
                /////////////// Essentials Hider ///////////////////////////
                    for (User user : ess.getOnlineUsers()) {
                        if (!user.isAuthorized("essentials.vanish.see")) {
                            user.getBase().hidePlayer(user.getBase());
                        }
                    }
                    User user2 = ess.getUser(p.getName());
                    user2.setHidden(true);
                    ess.getVanishedPlayers().add(p.getName());
            }
            saveConfig();
        }
}
