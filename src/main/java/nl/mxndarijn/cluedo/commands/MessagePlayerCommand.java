package nl.mxndarijn.cluedo.commands;

import nl.mxndarijn.api.mxcommand.MxCommand;
import nl.mxndarijn.cluedo.data.Permissions;
import nl.mxndarijn.cluedo.game.Game;
import nl.mxndarijn.cluedo.managers.language.LanguageManager;
import nl.mxndarijn.cluedo.managers.language.LanguageText;
import nl.mxndarijn.cluedo.managers.world.GameWorldManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

public class MessagePlayerCommand extends MxCommand {


    public MessagePlayerCommand(Permissions permissions, boolean b, boolean b1) {
        super(permissions, b, b1);
    }


    @Override
    public void execute(CommandSender sender, Command command, String label, String[] args) {

        Player p = (Player) sender;
        Optional<Game> optionalGame = GameWorldManager.getInstance().getGameByWorldUID(p.getWorld().getUID());
        if (optionalGame.isEmpty()) {
            p.sendMessage(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_MESSAGE_FROM_HOST_NOT_HOST));
            return;
        }
        Game game = optionalGame.get();

        if (!game.getHosts().contains(p.getUniqueId())) {
            p.sendMessage(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_MESSAGE_FROM_HOST_NOT_HOST));
            return;
        }

        if (args.length < 2) {
            p.sendMessage(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_MESSAGE_FROM_HOST_SYNTAX_ERROR));
            return;
        }

        Player player = Bukkit.getPlayer(args[0]);
        if (player == null) {
            p.sendMessage(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_MESSAGE_FROM_HOST_PLAYER_NOT_FOUND));
            return;
        }

        if (game.getGamePlayerOfPlayer(player.getUniqueId()).isEmpty()) {
            p.sendMessage(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_MESSAGE_FROM_HOST_NOT_IN_GAME));
            return;
        }

        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        player.sendMessage(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_MESSAGE_FROM_HOST, Collections.singletonList(message)));
        game.sendMessageToHosts(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_MESSAGE_FROM_HOST_TO_ALL_HOSTS, Arrays.asList(player.getName(), message)));
        p.sendMessage(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_MESSAGE_FROM_HOST_SEND));
    }
}
