package ru.ostrov77.minigames.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import ru.komiss77.utils.inventory.InventoryContent;


public class SpectatorMenuEvent extends Event {
    
    private static HandlerList handlers = new HandlerList();
    public final Player p;
    public final InventoryContent contents;
    
    public SpectatorMenuEvent(final Player p, final InventoryContent contents) {
        this.p = p;
        this.contents = contents;
    }

    

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
  
}
