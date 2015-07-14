package me.electroid.incrementor;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by ElectroidFilms on 6/15/15.
 */
public class SignIncrementor extends JavaPlugin implements Listener {

    private static final Material MATERIAL = Material.FLINT;
    private static final String NAME = "Sign Paster";
    private static final String PERMISSION = "signincrementor.use";

    private static Map<UUID, String[]> playerSignMap;
    private static Map<UUID, Integer> playerCountMap;

    @Override
    public void onEnable() {
        playerSignMap = new HashMap<UUID, String[]>();
        playerCountMap = new HashMap<UUID, Integer>();
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        playerSignMap = null;
        playerCountMap = null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (cmd.getName().equalsIgnoreCase("sign")) {
            if (sender instanceof Player) {
                if (sender.hasPermission(PERMISSION)) {
                    Player player = (Player) sender;
                    if (args.length == 1) {
                        if (args[0].equalsIgnoreCase("copy")) {
                            Block target = player.getTargetedBlock(true, true).getBlock();
                            if (target != null && target.getState() instanceof Sign) {
                                UUID id = player.getUniqueId();
                                playerSignMap.put(id, ((Sign) target.getState()).getLines());
                                playerCountMap.put(id, 1);
                                player.sendMessage("Sign has been copied!");
                            } else {
                                player.sendMessage("Unable to locate sign to copy.");
                            }
                        } else if (args[0].equalsIgnoreCase("clear")) {
                            clearPlayer(player);
                            player.sendMessage("Cleared sign information.");
                        } else if (args[0].equalsIgnoreCase("paste")) {
                            ItemStack item = new ItemStack(MATERIAL);
                            ItemMeta meta = item.getItemMeta();
                            meta.setDisplayName(ChatColor.DARK_AQUA + NAME);
                            item.setItemMeta(meta);
                            player.setItemInHand(item);
                            player.sendMessage("Click on sign to paste the new sign data.");
                        }
                    } else {
                        sender.sendMessage("Invalid arguments. /sign <copy|paste|clear>");
                    }
                } else {
                    sender.sendMessage("You do not have permission to use this command!");
                }
            } else {
                sender.sendMessage("Only player can use this command!");
            }
        }
        return true;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onClick(PlayerInteractEvent event) {
        Block target = event.getClickedBlock();
        Player player = event.getPlayer();
        UUID id = player.getUniqueId();
        if (target.getState() instanceof Sign &&
                player.hasPermission(PERMISSION) &&
                player.getItemInHand().getType().equals(MATERIAL) &&
                playerSignMap.containsKey(id) &&
                playerCountMap.containsKey(id)) {
            event.setCancelled(true);
            Sign sign = (Sign) target.getState();
            int count = 0;
            for (String line : playerSignMap.get(id)) {
                sign.setLine(count, line.replaceAll("%", playerCountMap.get(id).toString()));
                count++;
            }
            sign.update();
            playerCountMap.put(id, playerCountMap.get(id) + 1);
        }
    }

    private void clearPlayer(Player player) {
        UUID id = player.getUniqueId();
        playerSignMap.remove(id);
        playerCountMap.remove(id);
    }

}
