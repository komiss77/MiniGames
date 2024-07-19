package ru.ostrov77.minigames;

import ru.ostrov77.minigames.event.OpenMapSelectorEvent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import ru.komiss77.ApiOstrov;
import ru.komiss77.modules.menuItem.MenuItem;
import ru.komiss77.modules.menuItem.MenuItemBuilder;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.player.profile.Section;
import ru.komiss77.objects.CaseInsensitiveMap;
import ru.komiss77.utils.ItemBuilder;
import ru.ostrov77.minigames.event.LobbyJoinEvent;



public class MG extends JavaPlugin implements Listener {

    public static MG main;
    public static CaseInsensitiveMap <IArena> arenas;
    public static MenuItem mapSelector, exit, music, leaveArena, forceStart;
    
    static {
        arenas = new CaseInsensitiveMap<>();
    }
    
    @Override
    public void onLoad() {
        main = this;
    }


    @Override
    public void onEnable() {
        loadItems();
        Bukkit.getServer().getPluginManager().registerEvents(new MiniGamesLst(main), this);
        Bukkit.getServer().getPluginManager().registerEvents(new ChatLst(), this);
    }


    public static void lobbyJoin(final Player p) {
        if (p==null || !p.isOnline()) return;
        final Oplayer op = PM.getOplayer(p);
        if (op == null) return;
        //if (lobbyLocation != null) {
            ApiOstrov.teleportSave(p, Bukkit.getWorld("lobby").getSpawnLocation(), false);
        //}
        p.setGameMode(GameMode.ADVENTURE);
        p.getInventory().setArmorContents(new ItemStack[4]);
        p.getInventory().clear();
        mapSelector.giveForce(p);
        music.giveForce(p);
        exit.giveForce(p);
        p.updateInventory();

        p.setAllowFlight(false);
        p.setFlying(false);
        p.setExp(0.0F);
        p.setLevel(0);
        p.setSneaking(false);
        p.setSprinting(false);
        p.setFoodLevel(20);
        p.setSaturation(10.0F);
        p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
        p.setHealth(20.0D);
        p.setFireTicks(0);
        p.setExp(1.0F);
        p.setLevel(0);
        p.getActivePotionEffects().stream().forEach((effect) -> {
            p.removePotionEffect(effect.getType());
        });
        p.setWalkSpeed((float) 0.2);
        op.tag("", ": §8Не выбрана");
        op.tabSuffix(": §8Не выбрана", p);
        op.score.getSideBar().reset();
        op.score.getSideBar().title("§7Лобби");
        Bukkit.getPluginManager().callEvent(new LobbyJoinEvent(p));
    }

    public static IArena getArena (final Player p) {
        for (IArena ia : arenas.values()) {
            if (ia.hasPlayer(p)) {
                return ia;
            }
        }
        return null;
    }

    public static void log_ok(String s) {   Bukkit.getConsoleSender().sendMessage("§fMG: §2"+ s); }
    public static void log_err(String s) {   Bukkit.getConsoleSender().sendMessage("§fMG: §c"+ s); }



    private void loadItems() {
       final ItemStack is1=new ItemBuilder(Material.CAMPFIRE)
            .name("§aВыбор Карты")
            .build();
        mapSelector = new MenuItemBuilder("mapSelector", is1)
            .slot(0)
            .giveOnJoin(false)
            .giveOnRespavn(false)
            .giveOnWorld_change(false)
            .anycase(true)
            .canDrop(false)
            .duplicate(false)
            .canPickup(false)
            .canMove(false)
            .interact( e -> {
                    if (e.getAction()==Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        final OpenMapSelectorEvent ev = new OpenMapSelectorEvent(e.getPlayer());
                        Bukkit.getPluginManager().callEvent(ev);
                        if (!ev.canceled) {
                            PM.getOplayer(e.getPlayer()).menu.open(e.getPlayer(), Section.МИНИИГРЫ);
                        }
                    }
                }
            )
            .create();

        final ItemStack is2=new ItemBuilder(Material.MAGMA_CREAM)
            .name("§4Вернуться в лобби")
            .build();
        exit = new MenuItemBuilder("exit", is2)
            .slot(7)
            .giveOnJoin(false)
            .giveOnRespavn(false)
            .giveOnWorld_change(false)
            .anycase(true)
            .canDrop(false)
            .duplicate(false)
            .canPickup(false)
            .canMove(false)
            .interact( e -> {
                    if (e.getAction()==Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        ApiOstrov.sendToServer(e.getPlayer(), "lobby0", "");
                    }
                }
            )
            .create();

        final ItemStack is3=new ItemBuilder(Material.NOTE_BLOCK)
            .name("§4Музыка")
            .addLore("§eЛКМ §7- §aвкл§7/§4выкл")
            .addLore("§eПКМ §7- меню")
            .build();
        music = new MenuItemBuilder("music", is3)
            .slot(4)
            .giveOnJoin(false)
            .giveOnRespavn(false)
            .giveOnWorld_change(false)
            .anycase(true)
            .canDrop(false)
            .canPickup(false)
            .canMove(false)
            .duplicate(false)
            .leftClickCmd("music switch")
            .rightClickCmd("music")
            .create();

        final ItemStack is4=new ItemBuilder(Material.SLIME_BALL)
            .name("§4Покинуть Арену")
            .build();
        leaveArena = new MenuItemBuilder("leaveArena", is4)
            .slot(8)
            .giveOnJoin(false)
            .giveOnRespavn(false)
            .giveOnWorld_change(false)
            .anycase(true)
            .canDrop(false)
            .canPickup(false)
            .canMove(false)
            .duplicate(false)
            .interact( e -> {
                    if (e.getAction()==Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        for (IArena ia : MG.arenas.values()) {
                            if (ia.hasPlayer(e.getPlayer())) {
                                e.getPlayer().performCommand(ia.leaveCmd());
                            }
                        }
                    }
                }
            )
            .create();

        final ItemStack is5=new ItemBuilder(Material.DIAMOND)
            .name("§4Ускорить старт")
            .build();
        forceStart = new MenuItemBuilder("forceStart", is5)
            .slot(6)
            .giveOnJoin(false)
            .giveOnRespavn(false)
            .giveOnWorld_change(false)
            .anycase(true)
            .canDrop(false)
            .canPickup(false)
            .duplicate(false)
            .canMove(false)
            .interact( e -> {
                    if (e.getAction()==Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        for (IArena ia : MG.arenas.values()) {
                            if (ia.hasPlayer(e.getPlayer())) {
                                //forceStart.remove(e.getPlayer());
                                e.getPlayer().performCommand(ia.forceStartCmd());
                            }
                        }
                    }
                }
            )
            .create();
        
    }
 

    
}
