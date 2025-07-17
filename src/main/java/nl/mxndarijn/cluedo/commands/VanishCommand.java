package nl.mxndarijn.cluedo.commands;

import nl.mxndarijn.api.mxcommand.MxCommand;
import nl.mxndarijn.api.util.MxWorldFilter;
import nl.mxndarijn.cluedo.data.ChatPrefix;
import nl.mxndarijn.cluedo.data.Permissions;
import nl.mxndarijn.cluedo.managers.VanishManager;
import nl.mxndarijn.cluedo.managers.language.LanguageManager;
import nl.mxndarijn.cluedo.managers.language.LanguageText;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VanishCommand extends MxCommand {

    public VanishCommand(Permissions permission, boolean onlyPlayersCanExecute, boolean canBeExecutedInGame, MxWorldFilter worldFilter) {
        super(permission, onlyPlayersCanExecute, canBeExecutedInGame, worldFilter);
    }

    public VanishCommand(Permissions permission, boolean onlyPlayersCanExecute, boolean canBeExecutedInGame) {
        super(permission, onlyPlayersCanExecute, canBeExecutedInGame);
    }

    @Override
    public void execute(CommandSender sender, Command command, String label, String[] args) throws Exception {
        Player p = (Player) sender;
        VanishManager.getInstance().toggleVanish(p);
        if (VanishManager.getInstance().isPlayerHidden(p)) {
            p.sendMessage(LanguageManager.getInstance().getLanguageString(LanguageText.VANISH_ON, ChatPrefix.WIDM));
        } else {
            p.sendMessage(LanguageManager.getInstance().getLanguageString(LanguageText.VANISH_OFF, ChatPrefix.WIDM));
        }
    }
}
