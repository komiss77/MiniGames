package ru.ostrov77.minigames;

import org.bukkit.entity.Player;
import ru.komiss77.enums.Game;
import ru.komiss77.enums.GameState;


public interface IArena {
    
    public Game game();
    
    public boolean hasPlayer(final Player p);
    
    public String name();
    
    public String joinCmd();
    
    public String leaveCmd();
    
    public String forceStartCmd();

    public GameState state();
    
}
