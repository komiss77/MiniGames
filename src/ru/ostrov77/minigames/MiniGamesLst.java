package ru.ostrov77.minigames;

import java.util.Iterator;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.FluidLevelChangeEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.Timer;
import ru.komiss77.enums.Data;
import ru.komiss77.events.BsignLocalArenaClick;
import ru.komiss77.events.FriendTeleportEvent;
import ru.komiss77.events.LocalDataLoadEvent;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.SmartInventory;
import ru.ostrov77.minigames.event.SpectatorMenuEvent;


public class MiniGamesLst implements Listener {

    private static Plugin plugin;

    public MiniGamesLst(final Plugin plugin) {
        MiniGamesLst.plugin = plugin;
    }
    
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent e) {
//Ostrov.log("CreatureSpawnEvent "+e.getSpawnReason());
        switch (e.getSpawnReason()) {
            case CUSTOM, EGG, SPAWNER_EGG, DEFAULT -> {
                return;
            }
            case NATURAL -> {
                e.setCancelled(true);
            }
            default -> {
                if (e.getEntity().getWorld().getName().equals("lobby")) {
                    e.setCancelled(true);
                }
            }
        }
    }
 
    //@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
   // public void onSpawn(CreatureSpawnEvent e) {
//Ostrov.log("CreatureSpawnEvent MONITOR isCancelled?"+e.isCancelled());
   // }
    
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onLocalDataLoadEvent(final LocalDataLoadEvent e) {

        //AM.onDataRecieved(e.getPlayer());    //load
        MG.lobbyJoin(e.getPlayer());

        String wantArena = "";
        if (ApiOstrov.hasParty(e.getPlayer()) && !ApiOstrov.isPartyLeader(e.getPlayer())) {
            final String partyLeaderName = ApiOstrov.getPartyLeader(e.getPlayer());
            if (!partyLeaderName.isEmpty()) {
                //if (AM.getGRplayer(partyLeaderName)!=null && AM.getGRplayer(partyLeaderName).arena!=null && 
                //         (AM.getGRplayer(partyLeaderName).arena.gameState==GameState.ОЖИДАНИЕ || AM.getGRplayer(partyLeaderName).arena.gameState==GameState.СТАРТ) ) {
                //    wantArena = AM.getGRplayer(partyLeaderName).arena.name;
                e.getPlayer().sendMessage("§aВы перенаправлены к арене лидера вашей Команды.");
                //    AM.getGRplayer(partyLeaderName).getPlayer().sendMessage("§aУчастиник вашей Команды "+(ApiOstrov.isFemale(e.getPlayer().getName())?"зашла":"зашел")+" на арену.");
                //}
            }
        }

        if (wantArena.isEmpty()) {
            wantArena = PM.getOplayer(e.getPlayer().getName()).getDataString(Data.WANT_ARENA_JOIN);
        }

        if (!wantArena.isEmpty()) {
            final IArena ia = MG.arenas.get(wantArena);
            if (ia!=null) {
                e.getPlayer().performCommand(ia.joinCmd());
            }
        }
    }

    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBsignLocalArenaClick(final BsignLocalArenaClick e) {
//Ostrov.log("---- BsignLocalArenaClick --- "+e.player.getName()+" "+e.arenaName);
        //Kitbattle.join(e.player, , 10);
        if (!Timer.has(e.player, "joinCmd")) {
            Timer.add(e.player, "joinCmd", 1);
            final IArena ia = MG.arenas.get(e.arenaName);
            if (ia!=null) {
                e.player.performCommand(ia.joinCmd());
            }
        }
        
    }


    @EventHandler
    public void FriendTeleport(FriendTeleportEvent e) {
        if (!e.target.getWorld().getName().equals("lobby")) {
            e.setCanceled(true, "§f" + e.target.getName() + " §eиграет, не будем мешать!");
        }
    }

    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    public static void onInteract(PlayerInteractEvent e) {
        final Player p = e.getPlayer();
        if (p.getGameMode() == GameMode.SPECTATOR && (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK)) {
            if (p.getOpenInventory().getType() != InventoryType.CHEST) {
                SmartInventory.builder()
                        .type(InventoryType.HOPPER)
                        .id("spectator")
                        .provider(new SpectatorMenu())
                        .title("§fМеню зрителя")
                        .build()
                        .open(p);
            }
        }
    }
    
    
    public static void spectatorPrepare(final Player p) {
        p.closeInventory();
        p.getInventory().clear();
        final Iterator<PotionEffect> iterator = p.getActivePotionEffects().iterator();
        while (iterator.hasNext()) {
            p.removePotionEffect(iterator.next().getType());
        }
        p.setGameMode(GameMode.SPECTATOR);
        ApiOstrov.sendTitle(p, "§fРежим зрителя", "§a ЛКМ - открыть меню");
        PM.getOplayer(p).tabSuffix("§8Зритель", p);
        p.sendMessage("§fРежим зрителя. §aЛевый клик -> открыть меню");
        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
    }

    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR) //стираем наметаг, или не даёт отображать скореб.команды!
    public void onTeleportChange(final PlayerTeleportEvent e) {
        if (!e.getFrom().getWorld().getName().equals(e.getTo().getWorld().getName())) {
            if (e.getFrom().getWorld().getName().equals("lobby")) {
                //if (PM.nameTagManager!=null) 
                PM.getOplayer(e.getPlayer()).tag("", "");
                PM.getOplayer(e.getPlayer()).score.getSideBar().reset();
            }
        }
    }

    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerDamage(EntityDamageEvent e) {
        if (e.getEntityType() != EntityType.PLAYER) {
            return;
        }
        final Player p = (Player) e.getEntity();

        if (p.getWorld().getName().equals("lobby")) {
            e.setDamage(0);
            p.setFireTicks(0);
            if (e.getCause() == EntityDamageEvent.DamageCause.VOID || e.getCause() == EntityDamageEvent.DamageCause.LAVA) {
                p.setFallDistance(0);
                //p.setFireTicks(0);
                //p.teleport(Bukkit.getServer().getWorlds().get(0).getSpawnLocation(), PlayerTeleportEvent.TeleportCause.COMMAND); //от PLUGIN блокируются
                Ostrov.sync(() -> p.teleport(Bukkit.getWorld("lobby").getSpawnLocation(), PlayerTeleportEvent.TeleportCause.COMMAND), 0);
                //return;
            } else {
                e.setCancelled(true);
            }
        }

    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if ((e.getEntity().getType() == EntityType.PLAYER) && (e.getDamager() instanceof Firework)) {
            e.setDamage(0);
            e.setCancelled(true);
        }
    }


    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onFly(PlayerToggleFlightEvent e) {
        e.setCancelled(!ApiOstrov.isLocalBuilder(e.getPlayer(), false));
    }
    

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent e) {
        if (e.getEntityType() == EntityType.PLAYER && e.getEntity().getWorld().getName().equals("lobby") 
                && !ApiOstrov.isLocalBuilder((Player) e.getEntity(), false)) {
            e.setCancelled(true);
            e.getItem().remove();
        }
    }

    
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent e) {
        if (e.getPlayer().getWorld().getName().equals("lobby") && !ApiOstrov.isLocalBuilder(e.getPlayer(), false)) {
            e.setCancelled(true);
            e.getItemDrop().remove();
        }
    }

    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlace(BlockPlaceEvent e) {
        //PM.getOplayer(e.getPlayer().getName()).last_breack=Timer.Единое_время();
        if (!ApiOstrov.isLocalBuilder(e.getPlayer(), false) && e.getPlayer().getWorld().getName().equals("lobby")) {
            e.setCancelled(true);
        }
    }

    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBreak(BlockBreakEvent e) {
        if (!ApiOstrov.isLocalBuilder(e.getPlayer(), false) && e.getPlayer().getWorld().getName().equals("lobby")) {
            e.setCancelled(true);
        }
    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void PlayerArmorStandManipulateEvent(PlayerArmorStandManipulateEvent e){
        if (!e.getPlayer().isOp()) e.setCancelled(true);
    }    
   

    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onHungerChange(FoodLevelChangeEvent e) {
        e.setCancelled(true);
        ((Player)e.getEntity()).setFoodLevel(20);
    }
        

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onPlayerDeath(PlayerDeathEvent pde) {
       final Player p = pde.getEntity(); 
       p.teleport (Bukkit.getWorlds().get(0).getSpawnLocation());
    }   
   
     
    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onPlayerSwapoffHand(PlayerSwapHandItemsEvent e) {
        if (e.getPlayer().getWorld().getName().equals("lobby")) {
            e.setCancelled(true);
        }
    }    
  

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void strucGrow(StructureGrowEvent e) {
          e.setCancelled(true);
    }    
    
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onWeatherChange(WeatherChangeEvent e) {
        boolean rain = e.toWeatherState();
        if(rain) e.setCancelled(true);
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onForm(BlockFormEvent form) {
        form.setCancelled(true);
    }
	
 
    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onThunderChange(ThunderChangeEvent event) {
        boolean storm = event.toThunderState();
        if(storm) event.setCancelled(true);
    } 

   
    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onBlockSpread(BlockSpreadEvent e) {
        e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onBlockGrowth(BlockGrowEvent e) {
        e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void BlockFadeEvent(BlockFadeEvent e) {
        e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void BlockFromToEvent(BlockFromToEvent e) {
        e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void FluidLevelChangeEvent(FluidLevelChangeEvent e) {
        e.setCancelled(true);
    }

    

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onBlockFade(BlockFadeEvent event) {
        if (event.getBlock().getType() == Material.ICE || event.getBlock().getType() == Material.PACKED_ICE || event.getBlock().getType() == Material.SNOW || event.getBlock().getType() == Material.SNOW_BLOCK) {
            event.setCancelled(true);
        }
    }    
    
    
    
    public static class SpectatorMenu implements InventoryProvider {

        @Override
        public void init(final Player p, final InventoryContent contents) {
            p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, .5f, 1);

            Bukkit.getPluginManager().callEvent(new SpectatorMenuEvent(p, contents));
          /*  contents.set(0, ClickableItem.of(mapSelector, e -> {
                if (e.isLeftClick()) {
                    //p.closeInventory();
                    if (p.getGameMode() == GameMode.SPECTATOR) {
                        //
                    } else {
                        p.closeInventory();
                    }
                }
            }));*/

           /* contents.set(2, ClickableItem.of(music, e -> {
                if (e.isLeftClick()) {
                    if (p.getGameMode() == GameMode.SPECTATOR) {
                        //Bukkit.getServer().dispatchCommand(p, "music");   
                    } else {
                        p.closeInventory();
                    }
                }
            }));*/

            contents.set(4, ClickableItem.of(MG.leaveArena.getItem(), e -> {
                if (e.isLeftClick()) {
                    p.closeInventory();
                    if (p.getGameMode() == GameMode.SPECTATOR) {
                        MG.lobbyJoin(p);
                    } else {
                        p.closeInventory();
                    }
                }
            }));

        }
    }

    
    
    
    
}




  /*  @EventHandler(priority = EventPriority.MONITOR)
    public static void SignUpdateEvent(GameInfoUpdateEvent e) {
//System.out.println(" ---- SignUpdateEvent 1 --- "+e.server+" "+e.arena+" this="+SM.this_server_name+" exist?"+AM._ARENAS.containsKey(e.arena));
        if (e.ai.server.equals(Ostrov.MOT_D) && !e.ai.arenaName.isEmpty() && AM.arenas.containsKey(e.ai.arenaName)) {
            final Arena arena = AM.getArena(e.ai.arenaName);
            if (ApiOstrov.isInteger(arena.arenaLobby.getWorld().getName().replaceFirst("map", ""))) {
                final int slot = Integer.valueOf(arena.arenaLobby.getWorld().getName().replaceFirst("map", ""));
                if (mapSelectMenu.getItem(slot) == null) {
                    mapSelectMenu.setItem(slot, new ItemBuilder(Material.GREEN_TERRACOTTA)
                            .name("§e" + arena.getName())
                            .addLore(arena.state.displayColor + arena.state.name())
                            .addLore("мин. игроков: §a" + arena.players.size())
                            //.lore("набить фрагов: §6"+arena.frags_to_win)
                            .build()
                    );
                } else {
                    switch (e.ai.state) {
                        case ОЖИДАНИЕ, РАБОТАЕТ ->
                            mapSelectMenu.getItem(slot).setType(Material.GREEN_TERRACOTTA);
                        case СТАРТ ->
                            mapSelectMenu.getItem(slot).setType(Material.ORANGE_TERRACOTTA);
                        default ->
                            mapSelectMenu.getItem(slot).setType(Material.RED_TERRACOTTA);
                    }
                }

            } else {
                Ostrov.log_err("Мир арены " + arena.getName() + " не имеет формат map+номер, арена не будет отображаться в меню.");
            }
        }
    }*/

    



    /*     
    @EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onWorldChange (final PlayerChangedWorldEvent e) {
//System.out.println("PlayerChangedWorldEvent from="+e.getFrom().getName());
        //final Player p = e.getPlayer();
        new BukkitRunnable() {
            final Player p = e.getPlayer();
            @Override
            public void run() {
                switchLocalGlobal(p, true);
                perWorldTabList(e.getPlayer());
            }
        }.runTaskLater(plugin, 1);
    }
        
        
        
    
    public static void perWorldTabList(final Player player) {
        for (Player other:Bukkit.getOnlinePlayers()) {
            if (player.getWorld().getName().equals(other.getWorld().getName())) {
                player.showPlayer(plugin, other);
                other.showPlayer(plugin, player);
            } else {
                player.hidePlayer(plugin, other);
                other.hidePlayer(plugin, player);
            }
        }

    }
    
    public static void switchLocalGlobal(final Player p, final boolean notify) {
        final Oplayer op = PM.getOplayer(p);
        if (p.getWorld().getName().equalsIgnoreCase("lobby")) { //оказались в лобби, делаем глобальный
            if ( op.isLocalChat()){
                if (notify) p.sendMessage("§8Чат переключен на Общий");
                op.setLocalChat(false);//Ostrov.deluxechatPlugin.setGlobal(p.getUniqueId().toString());
            }
        } else {
            if ( !op.isLocalChat() )  {
                if (notify) p.sendMessage("§8Чат переключен на Арену");
                op.setLocalChat(true);//Ostrov.deluxechatPlugin.setLocal(p.getUniqueId().toString());
            }
        }
    }
    
     */



      //  if (e.getAction() == Action.PHYSICAL || e.getItem() == null) {
       //     return;
       // }

//System.out.println("onInteract item="+e.getItem()+" compareItem?"+ItemUtils.compareItem(e.getItem(), leaveArena, false));
        //if (ItemUtils.compareItem(e.getItem(), leaveArena, false)) {
       //     e.setCancelled(true);
            //AM.GlobalPlayerExit( p);
            //lobbyJoin(p, Bukkit.getWorld("lobby").getSpawnLocation() );
       //     e.getPlayer().performCommand(leaveCommad);
       // } else if (ItemUtils.compareItem(e.getItem(), exit, false)) {
        //    e.setCancelled(true);
       //     ApiOstrov.sendToServer(e.getPlayer(), "lobby0", "");
       // } else if (ItemUtils.compareItem(e.getItem(), mapSelector, false)) {
         //   e.setCancelled(true);
        //    openArenaSelectMenu(e.getPlayer());
        /* else if (ItemUtils.compareItem(e.getItem(), music, false)) {
            e.setCancelled(true);
            e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 0.5F, 1);
            if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) {
                e.getPlayer().performCommand("music switch");
            } else if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                e.getPlayer().performCommand("music");
            }
        }*/
    //}

 /*   @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)  //false = для GM 3
    public void onInventoryClick(InventoryClickEvent e) {
//System.out.println("InventoryClickEvent 1");
        if (e.getSlotType() == InventoryType.SlotType.OUTSIDE || e.getCurrentItem() == null) {
            return;
        }
        final Player p = (Player) e.getWhoClicked();

        if (p.getGameMode() == GameMode.SPECTATOR) {
            if (e.getView().getTitle().equals("§1Меню зрителя")) {
                e.setCancelled(true);
                //System.out.println("Spectator-Menu");
                if (ItemUtils.compareItem(e.getCurrentItem(), teleporter_itemstack, false)) {
                    p.openInventory(getTeleporterInventory(p));
                } else if (ItemUtils.compareItem(e.getCurrentItem(), leaveArena, false)) {
                    e.setCancelled(true);
                    p.performCommand(leaveCommad);
                } else if (ItemUtils.compareItem(e.getCurrentItem(), mapSelector, false)) {
                    e.setCancelled(true);
                    openArenaSelectMenu(p);
                }
            } else if (e.getView().getTitle().equals("§6ТП к игроку")) {
                e.setCancelled(true);
//System.out.println("Spectator1");
                final Player target = Bukkit.getPlayerExact(ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName()));
                if (target == null) {
                    p.sendMessage("§cИгрок не найден");
                    p.closeInventory();
                    return;
                }
                //if (!playerData.arena.players.contains(target.getName()) ) {
                //    p.sendMessage("§cИгрок недоступен для ТП");
                //    p.closeInventory();
                //    return;
                //}
                p.teleport(target.getLocation().add(0.0, 3.0, 0.0));
                return;
            }
        }

        if (e.isCancelled()) {
            return;
        }

        if (ItemUtils.compareItem(e.getCurrentItem(), exit, false)) {
            e.setCancelled(true);
            if (e.getAction() == InventoryAction.PICKUP_ONE || e.getAction() == InventoryAction.PICKUP_ALL) {
                ApiOstrov.sendToServer(p, "lobby0", "");
            }
        } else if (ItemUtils.compareItem(e.getCurrentItem(), mapSelector, false)) {
            e.setCancelled(true);
            if (e.getAction() == InventoryAction.PICKUP_ONE || e.getAction() == InventoryAction.PICKUP_ALL) {
                openArenaSelectMenu(p);
            }
        } else if (ItemUtils.compareItem(e.getCurrentItem(), leaveArena, false)) {
            e.setCancelled(true);
            if (e.getAction() == InventoryAction.PICKUP_ONE || e.getAction() == InventoryAction.PICKUP_ALL) {
                p.performCommand(leaveCommad);
            }
        } else if (ItemUtils.compareItem(e.getCurrentItem(), music, false)) {
            e.setCancelled(true);
            if (e.getAction() == InventoryAction.PICKUP_ONE || e.getAction() == InventoryAction.PICKUP_ALL) {
                p.performCommand("music");
            }
        }

        if (e.getInventory().getType() != InventoryType.CHEST || e.getCurrentItem() == null) {
            return;
        }
        if (e.getView().getTitle().equals("§1Карты")) {
            e.setCancelled(true);
            final ItemStack clicked = e.getCurrentItem();
            if (clicked.getType().name().contains("TERRACOTTA") && clicked.hasItemMeta() && clicked.getItemMeta().hasDisplayName()) {
//System.out.println("getAction="+e.getAction()+" >"+joinCommad+ChatColor.stripColor(clicked.getItemMeta().getDisplayName())+"<");
                if (e.getAction() == InventoryAction.PICKUP_ONE || e.getAction() == InventoryAction.PICKUP_ALL) {
                    //AM.addPlayer( p, ChatColor.stripColor(clicked.getItemMeta().getDisplayName()));
                    p.performCommand(joinCommad + ChatColor.stripColor(clicked.getItemMeta().getDisplayName()));
                }
            }
        }

    }

    private Inventory getTeleporterInventory(final Player p) {
        final Inventory inventory = Bukkit.createInventory(null, 54, "§6ТП к игроку");
        //final Arena arena = AM.getArenaByWorld(p.getWorld().getName());
        //if (arena!=null) {
        //for (final Player player : arena.getPlayers(false)) {
        for (final Player player : p.getWorld().getPlayers()) {
            if (player.getGameMode() == GameMode.SPECTATOR) {
                continue;
            }
            inventory.addItem(new ItemBuilder(Material.PLAYER_HEAD).name("§b" + player.getName()).setSkullOwner(player).build());//plugin.getSkull(player.getName(), ChatColor.AQUA + player.getName()) );
        }
        //}
        return inventory;
    }

    public static void openArenaSelectMenu(final Player p) {
        p.openInventory(mapSelectMenu);
        //plugin.arenaSelector.open(p);
    }
*/