package ti4.commands.special;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import ti4.commands.tokens.RemoveToken;
import ti4.commands.units.AddRemoveUnits;
import ti4.generator.Mapper;
import ti4.helpers.AliasHandler;
import ti4.helpers.Constants;
import ti4.helpers.Helper;
import ti4.map.Map;
import ti4.map.Player;
import ti4.map.Tile;
import ti4.message.MessageHelper;

public class MoveCreussWormhole extends SpecialSubcommandData {

    public MoveCreussWormhole() {
        super(Constants.MOVE_CREUSS_WORMHOLE, "Moves a creuss wormhole token.");
        addOptions(new OptionData(OptionType.STRING, Constants.TILE_NAME, "System/Tile name").setRequired(true).setAutoComplete(true));
        addOptions(new OptionData(OptionType.STRING, Constants.TOKEN, "Token Name").setRequired(true).setAutoComplete(true));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Map activeMap = getActiveMap();
        Player player = activeMap.getPlayer(getUser().getId());
        player = Helper.getGamePlayer(activeMap, player, event, null);
        player = Helper.getPlayer(activeMap, player, event);
        if (player == null) {
            sendMessage("Player could not be found");
            return;
        }

        OptionMapping tileOption = event.getOption(Constants.TILE_NAME);
        if (tileOption == null){
            sendMessage("Specify a tile");
            return;
        }
        String tileID = AliasHandler.resolveTile(StringUtils.substringBefore(tileOption.getAsString().toLowerCase(), " "));
        Tile tile = AddRemoveUnits.getTile(event, tileID, activeMap);
        if (tile == null) {
            sendMessage("Could not resolve tileID:  `" + tileID + "`. Tile not found");
            return;
        }

        String tokenName = event.getOption(Constants.TOKEN, null, OptionMapping::getAsString);
        tokenName = AliasHandler.resolveToken(tokenName);
        if (!isValidCreussWormhole(tokenName)) {
            sendMessage("Token Name: " + tokenName + " is not a valid Creuss Wormhole Token.");
            return;
        }

        StringBuilder sb = new StringBuilder(Helper.getPlayerRepresentation(event, player));
        tile.addToken(Mapper.getTokenID(tokenName), Constants.SPACE);
        sb.append(" moved " + Helper.getEmojiFromDiscord(tokenName) + " to " + tile.getRepresentation());
        for (Tile tile_ : activeMap.getTileMap().values()) {
            if (!tile.equals(tile_) && tile_.removeToken(Mapper.getTokenID(tokenName), Constants.SPACE)) {
                sb.append(" (from " + tile_.getRepresentation() + ")");
                break;
            }
        }
        sendMessage(sb.toString());
    }

    private boolean isValidCreussWormhole(String tokenName) {
        List<String> validNames = List.of("creussalpha", "creussbeta", "creussgamma");
        return validNames.contains(tokenName);
    }
    
}