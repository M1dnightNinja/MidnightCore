package me.m1dnightninja.midnightcore.fabric.inventory;

import me.m1dnightninja.midnightcore.api.inventory.AbstractInventoryGUI;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.midnightcore.fabric.MidnightCore;
import me.m1dnightninja.midnightcore.fabric.api.event.ContainerClickEvent;
import me.m1dnightninja.midnightcore.fabric.api.event.MenuCloseEvent;
import me.m1dnightninja.midnightcore.fabric.api.event.PlayerDisconnectEvent;
import me.m1dnightninja.midnightcore.fabric.event.Event;
import me.m1dnightninja.midnightcore.fabric.mixin.AccessorServerPlayer;
import me.m1dnightninja.midnightcore.fabric.util.ConversionUtil;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class InventoryGUI extends AbstractInventoryGUI {

    public InventoryGUI(MComponent title) {
        super(title);
    }

    @Override
    protected void onClosed(UUID u) {
        ServerPlayer pl = MidnightCore.getServer().getPlayerList().getPlayer(u);
        if(pl != null && pl.containerMenu != pl.inventoryMenu) {
            pl.closeContainer();
        }
    }

    @Override
    protected void onOpened(UUID u, int page) {

        int offset = page * 54;

        int max = 0;
        for(Entry ent : entries.values()) {
            if(ent.slot > max) {
                max = ent.slot;
            }
            if(max >= offset + 54) {
                max = offset + 54;
                break;
            }
        }

        if(offset > max) {
            return;
        }

        ServerPlayer player = MidnightCore.getServer().getPlayerList().getPlayer(u);
        if(player == null || player.hasDisconnected()) return;

        int items = (max - offset) + 1;
        int rows = Math.min(6, (items / 9) + 1);

        SimpleContainer inv = new SimpleContainer(rows * 9);

        for(Entry ent : entries.values()) {

            if(ent.slot < offset || ent.slot > max || ent.item == null) {
                continue;
            }

            ItemStack is = ConversionUtil.toMinecraftStack(ent.item);

            inv.setItem(ent.slot - offset, is);
        }

        ChestMenu handler = createScreen(rows, player, inv);
        if(player.containerMenu != player.inventoryMenu) {
            player.closeContainer();
        }

        player.connection.send(new ClientboundOpenScreenPacket(handler.containerId, handler.getType(), ConversionUtil.toMinecraftComponent(title)));
        player.containerMenu = handler;
        player.refreshContainer(handler);
    }

    private static ClickType getActionType(int action, net.minecraft.world.inventory.ClickType type) {
        switch(type) {
            case PICKUP:
                return action == 0 ? ClickType.LEFT : ClickType.RIGHT;
            case QUICK_MOVE:
                return action == 0 ? ClickType.SHIFT_LEFT : ClickType.SHIFT_RIGHT;
            case SWAP:
                return ClickType.NUMBER_KEY;
            case CLONE:
                return ClickType.MIDDLE;
            case THROW:
                return action == 0 ? ClickType.THROW : ClickType.THROW_ALL;
            case PICKUP_ALL:
                return ClickType.DOUBLE_CLICK;
        }

        return null;
    }

    private ChestMenu createScreen(int rows, ServerPlayer player, Container inv) {

        ((AccessorServerPlayer) player).callNextContainerCounter();
        int syncId = ((AccessorServerPlayer) player).getContainerCounter();

        switch(rows) {
            case 1:
                return new ChestMenu(MenuType.GENERIC_9x1, syncId, player.inventory, inv, rows);
            case 2:
                return new ChestMenu(MenuType.GENERIC_9x2, syncId, player.inventory, inv, rows);
            case 3:
                return new ChestMenu(MenuType.GENERIC_9x3, syncId, player.inventory, inv, rows);
            case 4:
                return new ChestMenu(MenuType.GENERIC_9x4, syncId, player.inventory, inv, rows);
            case 5:
                return new ChestMenu(MenuType.GENERIC_9x5, syncId, player.inventory, inv, rows);
            default:
                return new ChestMenu(MenuType.GENERIC_9x6, syncId, player.inventory, inv, rows);
        }
    }

    private static void onClick(ContainerClickEvent event) {

        AbstractInventoryGUI gui = openGuis.get(event.getPlayer().getUUID());
        if(gui == null) {
            return;
        }

        event.setCancelled(true);

        int offset = gui.getPlayerPage(event.getPlayer().getUUID()) * 54;
        int slot = event.getSlot();

        gui.onClick(event.getPlayer().getUUID(), getActionType(event.getClickType(), event.getAction()), offset + slot);
    }

    private static void onClose(MenuCloseEvent event) {
        ServerPlayer pl = MidnightCore.getServer().getPlayerList().getPlayer(event.getPlayer().getUUID());
        if(pl != null && pl.containerMenu != pl.inventoryMenu) {
            closeMenu(event.getPlayer().getUUID());
        }
    }

    private static void onLeave(PlayerDisconnectEvent event) {
        closeMenu(event.getPlayer().getUUID());
    }

    public static void registerEvents(Object owner) {

        Event.register(ContainerClickEvent.class, owner, InventoryGUI::onClick);
        Event.register(MenuCloseEvent.class, owner, InventoryGUI::onClose);

    }

}