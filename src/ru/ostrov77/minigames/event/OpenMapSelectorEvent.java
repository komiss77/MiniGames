package ru.ostrov77.minigames.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;


public class OpenMapSelectorEvent extends Event {
    
    private static HandlerList handlers = new HandlerList();
    private final Player p;
    public boolean canceled;
    
    public OpenMapSelectorEvent(final Player p) {
        this.p = p;
    }

    public Player getPlayer() {
        return p;
    }
    
    public void cancel() {
        this.canceled = true;
    }
    

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

  
}
