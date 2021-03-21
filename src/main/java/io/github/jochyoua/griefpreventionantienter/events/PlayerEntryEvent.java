package io.github.jochyoua.griefpreventionantienter.events;

import io.github.jochyoua.griefpreventionantienter.GriefPreventionAntiEnter;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Objects;

public class PlayerEntryEvent implements Listener {
    private final GriefPreventionAntiEnter plugin;


    // This is the constructor, this is where we dependency inject the main class
    // into this class so we may access information from the main class
    public PlayerEntryEvent(GriefPreventionAntiEnter plugin) {
        this.plugin = plugin;
    }

    // This is our PlayerMoveEvent, this event is called whenever the Player moves,
    // here we are using it to detect if the player is moving into a GriefPrevention claim.
    @EventHandler
    public void enterClaim(PlayerMoveEvent e) {

        // Retrieves the claim at the current location, returns null if no claim exists
        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(e.getTo(), true, null);

        // Checks to see if the claim exists or if the player has the bypass permission, returns if either are true
        if (claim == null || (!Objects.requireNonNull(plugin.getConfig().getString("settings.permission")).equalsIgnoreCase("") && e.getPlayer().hasPermission(Objects.requireNonNull(plugin.getConfig().getString("settings.permission"))))) {
            return;
        }

        // Checks to see if the player is the claim owner or they are inside of an administrator claim
        if ((!plugin.getConfig().getBoolean("settings.ignore admin claims") && claim.isAdminClaim())
                || e.getPlayer().getUniqueId().equals(claim.ownerID)) {
            return;
        }

        // Checks if the player has container access, building access or permission access. Returns if any of these are true
        // You can disable any of these checks in the config under the "settings.claim permissions to check" path
        if (claim.allowContainers(e.getPlayer()) == null && plugin.getConfig().getBoolean("settings.claim permissions to check.containers"))
            return;

        if (claim.allowAccess(e.getPlayer()) == null && plugin.getConfig().getBoolean("settings.claim permissions to check.access"))
            return;

        if ((claim.allowBuild(e.getPlayer(), Material.GRASS_BLOCK) == null || claim.allowBreak(e.getPlayer(), Material.GRASS_BLOCK) == null) && plugin.getConfig().getBoolean("settings.claim permissions to check.block"))
            return;

        if (claim.allowGrantPermission(e.getPlayer()) == null && plugin.getConfig().getBoolean("settings.claim permissions to check.permission"))
            return;

        // Asks the config if it should teleport the player to the edge of the claim, if not it will teleport the player to where they were
        // before attempting to enter the claim
        if (plugin.getConfig().getBoolean("settings.teleport player to edge of claim")) {
            e.getPlayer().teleport(claim.getLesserBoundaryCorner().toHighestLocation());
        } else {
            e.getPlayer().teleport(e.getFrom());
        }

        // Asks the config if the message is set, and if so send the player the message with an owner variable
        // The owner variable is always occupied by the owner's username
        if (!Objects.requireNonNull(plugin.getConfig().getString("message")).equalsIgnoreCase("")) {
            e.getPlayer().sendMessage(plugin.polishString(plugin.getConfig().getString("settings.message"), "owner:" + claim.getOwnerName()));
        }

        // This loops through the commands set in the config after polishing them
        for (String command : plugin.getConfig().getStringList("settings.commands to execute")) {
            command = plugin.polishString(command, null);
            String finalCommand = command;

            // This is scheduled in sync, this is because you should never use the Bukkit API async, and
            // it will cause issues with the API
            Bukkit.getScheduler().runTask(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand.replaceFirst("/", "")));
        }
    }
}
