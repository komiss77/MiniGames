package ru.ostrov77.minigames.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;


public class LobbyJoinEvent extends Event {
    
    private static HandlerList handlers = new HandlerList();
    public final Player p;
    
    public LobbyJoinEvent(final Player p) {
        this.p = p;
    }

    

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
  
}
