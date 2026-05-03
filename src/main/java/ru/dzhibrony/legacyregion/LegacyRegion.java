package ru.dzhibrony.legacyregion;

import cn.nukkit.plugin.PluginBase;
import com.mefrreex.jooq.database.IDatabase;
import ru.dzhibrony.legacyregion.command.RegionCommand;
import ru.dzhibrony.legacyregion.config.PluginConfiguration;
import ru.dzhibrony.legacyregion.config.PluginConfigurationLoader;
import ru.dzhibrony.legacyregion.listener.RegionCreationListener;
import ru.dzhibrony.legacyregion.listener.RegionCommandSuggestionListener;
import ru.dzhibrony.legacyregion.listener.RegionProtectionListener;
import ru.dzhibrony.legacyregion.menu.RegionMenu;
import ru.dzhibrony.legacyregion.menu.RegionTextFormatter;
import ru.dzhibrony.legacyregion.model.RegionDefinition;
import ru.dzhibrony.legacyregion.service.InstallModeService;
import ru.dzhibrony.legacyregion.service.MessageService;
import ru.dzhibrony.legacyregion.service.PlayerResolver;
import ru.dzhibrony.legacyregion.service.ProtectionService;
import ru.dzhibrony.legacyregion.service.RegionMemberService;
import ru.dzhibrony.legacyregion.service.RegionService;
import ru.dzhibrony.legacyregion.storage.DatabaseFactory;
import ru.dzhibrony.legacyregion.storage.JooqRegionRepository;
import ru.dzhibrony.legacyregion.storage.RegionRepository;
import ru.dzhibrony.legacyregion.visual.RegionHologramManager;
import ru.dzhibrony.legacyregion.visual.RegionParticleVisualizer;

import java.util.List;

public final class LegacyRegion extends PluginBase {

    private PluginConfiguration configuration;
    private RegionService regionService;
    private RegionHologramManager hologramManager;
    private MessageService messageService;
    private RegionMemberService memberService;
    private RegionMenu menu;
    private ProtectionService protectionService;
    private RegionProtectionListener protectionListener;
    private RegionParticleVisualizer particleVisualizer;

    @Override
    public void onEnable() {
        this.configuration = new PluginConfigurationLoader(this).load();
        this.logRegionDefinitions();
        this.bootstrapServices();
    }

    public void reloadPluginData() {
        this.closeHolograms();
        this.configuration = new PluginConfigurationLoader(this).load();
        this.refreshRuntimeConfiguration();
        this.regionService.load();
        this.regionService.clearListeners();
        this.registerHolograms();
    }

    @Override
    public void onDisable() {
        this.closeHolograms();
    }

    private void bootstrapServices() {
        RegionRepository repository = this.repository();
        MessageService messages = new MessageService(this.configuration.messages());
        InstallModeService installMode = new InstallModeService();
        PlayerResolver players = new PlayerResolver(this.getServer());
        this.regionService = this.regionService(repository);
        this.messageService = messages;
        this.registerHolograms();
        RegionMemberService members = new RegionMemberService(this.regionService, players, this.configuration.transfer());
        this.memberService = members;
        this.registerRuntime(messages, installMode, members);
    }

    private void logRegionDefinitions() {
        List<RegionDefinition> definitions = this.validRegionDefinitions();
        StringBuilder message = new StringBuilder("Подключено ")
                .append(definitions.size())
                .append(" блоков региона:");
        definitions.forEach(definition -> message.append('\n').append(definition.fileName()));
        this.getLogger().info(message.toString());
    }

    private List<RegionDefinition> validRegionDefinitions() {
        return this.configuration.regionDefinitions().stream()
                .filter(this::hasRegisteredBlock)
                .toList();
    }

    private boolean hasRegisteredBlock(RegionDefinition definition) {
        return !definition.namespaceId().isBlank() || definition.legacyId() >= 0;
    }

    private void refreshRuntimeConfiguration() {
        this.messageService.updateMessages(this.configuration.messages());
        this.regionService.updateConfiguration(this.configuration.protection(), this.configuration.regionDefinitions());
        this.memberService.updateTransferSettings(this.configuration.transfer());
        this.protectionService.updateSettings(this.configuration.protection());
        this.protectionListener.updateSettings(this.configuration.protection());
        this.particleVisualizer.updateSettings(this.configuration.particles());
        this.menu.updateText(this.configuration.menu(), this.textFormatter());
    }

    private RegionRepository repository() {
        IDatabase database = DatabaseFactory.create(this.configuration.database(), this.getDataFolder());
        return new JooqRegionRepository(database);
    }

    private RegionService regionService(RegionRepository repository) {
        RegionService service = new RegionService(repository, this.configuration.protection(), this.configuration.regionDefinitions());
        service.load();
        return service;
    }

    private void registerHolograms() {
        this.hologramManager = new RegionHologramManager(this, this.configuration.holograms(), this.regionService);
        this.regionService.addListener(this.hologramManager);
        this.hologramManager.start(this.regionService.allRegions());
    }

    private void closeHolograms() {
        if (this.hologramManager != null) {
            this.hologramManager.close();
            this.hologramManager = null;
        }
    }

    private void registerRuntime(MessageService messages, InstallModeService installMode, RegionMemberService members) {
        this.menu = this.menu(messages, members);
        this.registerCommand(this.menu, members, installMode, messages);
        this.registerListeners(installMode, messages);
    }

    private RegionMenu menu(MessageService messages, RegionMemberService members) {
        return new RegionMenu(this.regionService, members, messages, this.configuration.menu(), this.textFormatter());
    }

    private RegionTextFormatter textFormatter() {
        return new RegionTextFormatter(this.configuration.menu(), this.configuration.messages());
    }

    private void registerCommand(RegionMenu menu, RegionMemberService members, InstallModeService installMode,
                                 MessageService messages) {
        RegionCommand command = new RegionCommand(this.configuration.command(), menu, this.regionService,
                members, installMode, messages, this::reloadPluginData);
        this.getServer().getCommandMap().register(this.getName().toLowerCase(), command);
    }

    private void registerListeners(InstallModeService installMode, MessageService messages) {
        this.protectionService = new ProtectionService(this.regionService, this.configuration.protection());
        this.particleVisualizer = new RegionParticleVisualizer(this, this.configuration.particles());
        this.protectionListener = new RegionProtectionListener(
                this.regionService, this.protectionService, this.configuration.protection(), messages);
        this.getServer().getPluginManager().registerEvents(
                this.protectionListener, this);
        this.getServer().getPluginManager().registerEvents(
                new RegionCreationListener(installMode, this.regionService, messages, this.particleVisualizer), this);
        this.getServer().getPluginManager().registerEvents(
                new RegionCommandSuggestionListener(this, this.regionService), this);
    }
}
