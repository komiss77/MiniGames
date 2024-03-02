package ru.ostrov77.minigames;

import org.bukkit.entity.Player;
import ru.komiss77.enums.Game;


public interface IArena {
    
    public Game game();
    
    public boolean hasPlayer(final Player p);
    
    public String joinCmd();
    
    public String leaveCmd();
    
    public String forceStartCmd();
    
}
