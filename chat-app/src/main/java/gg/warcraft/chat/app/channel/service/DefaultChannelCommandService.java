package gg.warcrat.chat.app.channel.service;

import com.google.inject.Inject;
import gg.warcraft.monolith.api.chat.channel.Channel;
import gg.warcraft.monolith.api.chat.channel.ChannelCommandService;
import gg.warcraft.monolith.api.chat.channel.ChannelRepository;
import gg.warcraft.monolith.api.command.CommandHandler;
import gg.warcraft.monolith.api.command.service.CommandCommandService;
import gg.warcraft.monolith.api.event.EventService;
import gg.warcraft.monolith.api.util.ColorCode;
import gg.warcraft.monolith.app.chat.channel.GlobalChannel;
import gg.warcraft.monolith.app.chat.channel.LocalChannel;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public class DefaultChannelCommandService implements ChannelCommandService {
    private static final String NAME_NULL_OR_EMPTY = "Failed to create channel with null or empty name.";
    private static final String NAME_ALREADY_EXISTS = "Failed to create channel '%s', name already exists";
    private static final String ALIAS_NULL_OR_EMPTY = "Failed to create channel '%s' with null or empty alias.";
    private static final String ALIAS_ALREADY_EXISTS = "Failed to create channel '%s', alias '%s' already exists";
    private static final String SHORTCUT_EMPTY = "Failed to create channel '%s' with empty shortcut.";
    private static final String SHORTCUT_ALREADY_EXISTS = "Failed to create channel '%s', shortcut '%s' already exists";

    private final ChannelRepository repository;
    private final ChannelCommandHandlerFactory commandHandlerFactory;
    private final CommandCommandService commandCommandService;
    private final EventService eventService;

    @Inject
    public DefaultChannelCommandService(ChannelRepository repository,
                                        ChannelCommandHandlerFactory commandHandlerFactory,
                                        CommandCommandService commandCommandService, EventService eventService) {
        this.repository = repository;
        this.commandHandlerFactory = commandHandlerFactory;
        this.commandCommandService = commandCommandService;
        this.eventService = eventService;
    }

    private void registerChannel(Channel channel, CommandHandler commandHandler) {
        String name = channel.getName();
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException(NAME_NULL_OR_EMPTY);
        } else if (repository.getByAlias(name) != null) {
            String nameAlreadyExists = String.format(NAME_ALREADY_EXISTS, name);
            throw new IllegalArgumentException(nameAlreadyExists);
        }

        List<String> aliases = channel.getAliases();
        aliases.forEach(alias -> {
            if (alias == null || alias.isEmpty()) {
                String aliasNullOrEmpty = String.format(ALIAS_NULL_OR_EMPTY, name);
                throw new IllegalArgumentException(aliasNullOrEmpty);
            } else if (repository.getByAlias(alias) != null) {
                String aliasAlreadyExists = String.format(ALIAS_ALREADY_EXISTS, name, alias);
                throw new IllegalArgumentException(aliasAlreadyExists);
            }
        });

        String shortcut = channel.getShortcut();
        if (shortcut != null) {
            if (shortcut.isEmpty()) {
                String shortcutEmpty = String.format(SHORTCUT_EMPTY, name);
                throw new IllegalArgumentException(shortcutEmpty);
            } else if (repository.getByShortcut(shortcut) != null) {
                String shortcutAlreadyExists = String.format(SHORTCUT_ALREADY_EXISTS, name, shortcut);
                throw new IllegalArgumentException(shortcutAlreadyExists);
            }
        }

        commandCommandService.createCommand(name, aliases, commandHandler);
        repository.save(channel);
    }

    @Override
    public Channel createGlobalChannel(String name, List<String> aliases, String shortcut, ColorCode color,
                                       String formattingString, Predicate<UUID> joinCondition) {
        GlobalChannel channel = new GlobalChannel(name, aliases, shortcut, color, formattingString, joinCondition);
        CommandHandler commandHandler = commandHandlerFactory.createGlobalChannelCommandExecutor(channel);
        registerChannel(channel, commandHandler);
        eventService.subscribe(channel);
        return channel;
    }

    @Override
    public Channel createLocalChannel(String name, List<String> aliases, String shortcut, ColorCode color,
                                      String formattingString, float radius) {
        LocalChannel channel = new LocalChannel(name, aliases, shortcut, color, formattingString, radius);
        CommandHandler commandHandler = commandHandlerFactory.createLocalChannelCommandExecutor(channel);
        registerChannel(channel, commandHandler);
        return channel;
    }

    @Override
    public void joinChannel(Channel channel, UUID playerId) {
        // TODO add GlobalChannel and LocalChannel interfaces where global exposes the joinCondition
        // TODO test for join condition
        // TODO if true create new global channel with added playerId and save
    }

    @Override
    public void leaveChannel(Channel channel, UUID playerId) {
        // TODO see above
    }
}
