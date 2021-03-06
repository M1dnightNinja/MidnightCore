package me.m1dnightninja.midnightcore.spigot.player;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.inventory.MItemStack;
import me.m1dnightninja.midnightcore.api.math.Vec3d;
import me.m1dnightninja.midnightcore.api.module.skin.ISkinModule;
import me.m1dnightninja.midnightcore.api.module.skin.Skin;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;
import me.m1dnightninja.midnightcore.api.text.ActionBar;
import me.m1dnightninja.midnightcore.api.text.Title;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.midnightcore.common.util.MojangUtil;
import me.m1dnightninja.midnightcore.spigot.util.ConversionUtil;
import me.m1dnightninja.midnightcore.spigot.util.NMSWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.UUID;

public class SpigotPlayer extends MPlayer {

    private Player player;

    protected SpigotPlayer(UUID u) {
        super(u);

        updatePlayer();
    }

    private void updatePlayer() {
        if(player == null) player = Bukkit.getPlayer(getUUID());
    }

    @Override
    public MComponent getName() {
        updatePlayer();
        if(player == null) return null;

        return MComponent.createTextComponent(player.getName());
    }

    @Override
    public MComponent getDisplayName() {
        updatePlayer();
        if(player == null) return null;

        return MComponent.createTextComponent(player.getDisplayName());
    }

    @Override
    public MIdentifier getDimension() {
        updatePlayer();
        if(player == null) return null;

        return MIdentifier.parseOrDefault(player.getWorld().getName());
    }

    @Override
    public Skin getSkin() {
        updatePlayer();
        if(player == null) return null;

        ISkinModule mod = MidnightCoreAPI.getInstance().getModule(ISkinModule.class);
        if(mod != null) {
            return mod.getSkin(MidnightCoreAPI.getInstance().getPlayerManager().getPlayer(player.getUniqueId()));
        }

        return MojangUtil.getSkinFromProfile(NMSWrapper.getGameProfile(player));
    }

    @Override
    public Vec3d getLocation() {
        updatePlayer();
        if(player == null) return null;
        return new Vec3d(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());
    }

    @Override
    public float getYaw() {
        updatePlayer();
        if(player == null) return 0.0f;
        return player.getEyeLocation().getYaw();
    }

    @Override
    public float getPitch() {
        updatePlayer();
        if(player == null) return 0.0f;
        return player.getEyeLocation().getPitch();
    }

    @Override
    public boolean isOffline() {
        updatePlayer();
        return player == null;
    }

    @Override
    public void sendMessage(MComponent comp) {
        updatePlayer();
        if(player == null) return;

        NMSWrapper.sendMessage(player, comp);
    }

    @Override
    public void sendTitle(Title title) {
        updatePlayer();
        if(player == null) return;

        NMSWrapper.sendTitle(player, title);
    }

    @Override
    public void sendActionBar(ActionBar ab) {
        updatePlayer();
        if(player == null) return;

        NMSWrapper.sendActionBar(player, ab);
    }

    @Override
    public void teleport(MIdentifier dimension, Vec3d location, float yaw, float pitch) {

        updatePlayer();
        if(player == null) return;

        World w = Bukkit.getWorld(dimension.getPath());

        Location l = new Location(w, location.getX(), location.getY(), location.getZ(), yaw, pitch);
        player.teleport(l);
    }

    @Override
    public void teleport(Vec3d location, float yaw, float pitch) {

        updatePlayer();
        if(player == null) return;

        World w = player.getWorld();

        Location l = new Location(w, location.getX(), location.getY(), location.getZ(), yaw, pitch);
        player.teleport(l);
    }

    @Override
    public void giveItem(MItemStack item) {

        updatePlayer();
        if(player == null) return;

        ItemStack is = ConversionUtil.toBukkitStack(item);
        player.getInventory().addItem(is);
    }

    @Override
    protected void cleanup() {
        player = null;
    }

    @Override
    public boolean hasPermission(String perm) {

        updatePlayer();
        return player.hasPermission(perm);
    }

    @Nullable
    public Player getSpigotPlayer() {
        updatePlayer();
        return player;
    }

    public static SpigotPlayer wrap(Player player) {
        return (SpigotPlayer) MidnightCoreAPI.getInstance().getPlayerManager().getPlayer(player.getUniqueId());
    }
}
