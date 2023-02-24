package ti4.commands.game;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import ti4.helpers.Constants;
import ti4.map.Map;
import ti4.map.Player;
import ti4.message.MessageHelper;

public class SetupGameChannels extends GameSubcommandData {
    public SetupGameChannels() {
        super(Constants.GAME_CHANNEL_SETUP, "Setup channels and roles for non standard games");
        addOptions(new OptionData(OptionType.CHANNEL, Constants.MAIN_GAME_CHANNEL, "Specify main game channel").setRequired(true));
    
        addOptions(new OptionData(OptionType.USER, Constants.PLAYER1, "Specify main player for Community/Fog mode").setRequired(false));
        addOptions(new OptionData(OptionType.ROLE, Constants.ROLE1, "Specify role for Community Mode").setRequired(false));
        addOptions(new OptionData(OptionType.CHANNEL, Constants.CHANNEL1, "Specify private channel for player/role").setRequired(false));

        addOptions(new OptionData(OptionType.USER, Constants.PLAYER2, "Specify main player for Community/Fog mode").setRequired(false));
        addOptions(new OptionData(OptionType.ROLE, Constants.ROLE2, "Specify role for Community Mode").setRequired(false));
        addOptions(new OptionData(OptionType.CHANNEL, Constants.CHANNEL2, "Specify private channel for player/role").setRequired(false));

        addOptions(new OptionData(OptionType.USER, Constants.PLAYER3, "Specify main player for Community/Fog mode").setRequired(false));
        addOptions(new OptionData(OptionType.ROLE, Constants.ROLE3, "Specify role for Community Mode").setRequired(false));
        addOptions(new OptionData(OptionType.CHANNEL, Constants.CHANNEL3, "Specify private channel for player/role").setRequired(false));

        addOptions(new OptionData(OptionType.USER, Constants.PLAYER4, "Specify main player for Community/Fog mode").setRequired(false));
        addOptions(new OptionData(OptionType.ROLE, Constants.ROLE4, "Specify role for Community Mode").setRequired(false));
        addOptions(new OptionData(OptionType.CHANNEL, Constants.CHANNEL4, "Specify private channel for player/role").setRequired(false));

        addOptions(new OptionData(OptionType.USER, Constants.PLAYER5, "Specify main player for Community/Fog mode").setRequired(false));
        addOptions(new OptionData(OptionType.ROLE, Constants.ROLE5, "Specify role for Community Mode").setRequired(false));
        addOptions(new OptionData(OptionType.CHANNEL, Constants.CHANNEL5, "Specify private channel for player/role").setRequired(false));

        addOptions(new OptionData(OptionType.USER, Constants.PLAYER6, "Specify main player for Community/Fog mode").setRequired(false));
        addOptions(new OptionData(OptionType.ROLE, Constants.ROLE6, "Specify role for Community Mode").setRequired(false));
        addOptions(new OptionData(OptionType.CHANNEL, Constants.CHANNEL6, "Specify private channel for player/role").setRequired(false));

        addOptions(new OptionData(OptionType.USER, Constants.PLAYER7, "Specify main player for Community/Fog mode").setRequired(false));
        addOptions(new OptionData(OptionType.ROLE, Constants.ROLE7, "Specify role for Community Mode").setRequired(false));
        addOptions(new OptionData(OptionType.CHANNEL, Constants.CHANNEL7, "Specify private channel for player/role").setRequired(false));

        addOptions(new OptionData(OptionType.USER, Constants.PLAYER8, "Specify main player for Community/Fog mode").setRequired(false));
        addOptions(new OptionData(OptionType.ROLE, Constants.ROLE8, "Specify role for Community Mode").setRequired(false));
        addOptions(new OptionData(OptionType.CHANNEL, Constants.CHANNEL8, "Specify private channel for player/role").setRequired(false));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Map activeMap = getActiveMap();

        // Set main channel where SC's get played
        OptionMapping channel = event.getOption(Constants.MAIN_GAME_CHANNEL);
        if (channel == null) {
            MessageHelper.sendMessageToChannel(event.getChannel(), "Must specify main game channel");
            return;
        }
        if (channel.getChannelType() != ChannelType.TEXT) {
            MessageHelper.sendMessageToChannel(event.getChannel(), "Must specify text channel");
            return;
        }

        activeMap.setMainGameChannel(channel.getAsChannel().asTextChannel());

        if (activeMap.isCommunityMode() || activeMap.isFoWMode()) {
            setRoleAndChannel(event, activeMap, Constants.PLAYER1, Constants.ROLE1, Constants.CHANNEL1);
            setRoleAndChannel(event, activeMap, Constants.PLAYER2, Constants.ROLE2, Constants.CHANNEL2);
            setRoleAndChannel(event, activeMap, Constants.PLAYER3, Constants.ROLE3, Constants.CHANNEL3);
            setRoleAndChannel(event, activeMap, Constants.PLAYER4, Constants.ROLE4, Constants.CHANNEL4);
            setRoleAndChannel(event, activeMap, Constants.PLAYER5, Constants.ROLE5, Constants.CHANNEL5);
            setRoleAndChannel(event, activeMap, Constants.PLAYER6, Constants.ROLE6, Constants.CHANNEL6);
            setRoleAndChannel(event, activeMap, Constants.PLAYER7, Constants.ROLE7, Constants.CHANNEL7);
            setRoleAndChannel(event, activeMap, Constants.PLAYER8, Constants.ROLE8, Constants.CHANNEL8);
        }
    }

    private void setRoleAndChannel(SlashCommandInteractionEvent event, Map activeMap, String playerConstant, String roleConstant, String channelConstant) {
        OptionMapping player = event.getOption(playerConstant);
        OptionMapping role = event.getOption(roleConstant);
        OptionMapping channel = event.getOption(channelConstant);

        if (player == null && channel == null) {
            return;
        }
        if (player != null && channel != null) {
            User asUser = player.getAsUser();
            Player player_ = activeMap.getPlayer(asUser.getId());
            if (player_ == null) {
                MessageHelper.sendMessageToChannel(event.getChannel(), "Must specify game player: " + playerConstant + " is invalid.");
                return;
            }
            
            //set community mode data
            if (activeMap.isCommunityMode()) {
                if (role == null) {
                    MessageHelper.sendMessageToChannel(event.getChannel(), "Must specify role for community mode: " + roleConstant + " is missing");
                    return;
                } else {
                    player_.setRoleForCommunity(role.getAsRole());
                }
            }
            
            //set private channel data
            if(channel.getChannelType() != ChannelType.TEXT) {
                MessageHelper.sendMessageToChannel(event.getChannel(), "Must specify text channel for " + channelConstant);
                return;
            }
            player_.setPrivateChannel(channel.getAsChannel().asTextChannel());
        } else {
            MessageHelper.sendMessageToChannel(event.getChannel(), "Must specify player and channel");
            return;
        }
    }
}