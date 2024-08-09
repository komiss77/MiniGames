package ru.ostrov77.minigames;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import ru.komiss77.enums.GameState;
import ru.komiss77.events.ChatPrepareEvent;
import ru.komiss77.utils.TCUtil;


class ChatLst implements Listener  {
    

    
    @EventHandler(priority = EventPriority.HIGHEST,ignoreCancelled = true)
    public void onChat(ChatPrepareEvent e) {

        final Player p = e.getPlayer();
        
        if ( p.getGameMode() == GameMode.SPECTATOR ) {
            return;
        }
        
        final IArena ia = MG.getArena(p);
        
        Component c;
        
        if (ia == null) {
            
            c = TCUtil.form("§8<игра?> §7")
                    .hoverEvent(HoverEvent.showText(TCUtil.form("§bМиниигры \n§7Игра не выбрана")))
                ;
            

        } else {
            
            c = TCUtil.form( ("<"+ia.name())+"> §7" )
                    .hoverEvent(HoverEvent.showText(TCUtil.form("§bМиниигры \n§7Игра : §e"+ia.name())))
                    ;
            
            if (ia.state() == GameState.ИГРА) {
                e.sendProxy(false);
            }
            
        }
        
        e.setSenderGameInfo(c);
        e.setViewerGameInfo(c);
        

        
    }    
    
    
    
     
    
    
}
