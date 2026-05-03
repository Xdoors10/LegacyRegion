package ru.dzhibrony.legacyregion.menu;

import cn.nukkit.Player;
import com.formconstructor.form.CustomForm;
import com.formconstructor.form.SimpleForm;
import com.formconstructor.form.element.SelectableElement;
import com.formconstructor.form.element.custom.Dropdown;
import com.formconstructor.form.element.custom.Input;
import com.formconstructor.form.element.custom.Toggle;
import com.formconstructor.form.element.simple.ImageType;
import ru.dzhibrony.legacyregion.config.MenuText;
import ru.dzhibrony.legacyregion.config.Messages;
import ru.dzhibrony.legacyregion.model.Region;
import ru.dzhibrony.legacyregion.model.RegionMember;
import ru.dzhibrony.legacyregion.service.AddMemberStatus;
import ru.dzhibrony.legacyregion.service.MessageService;
import ru.dzhibrony.legacyregion.service.RegionMemberService;
import ru.dzhibrony.legacyregion.service.RegionService;
import ru.dzhibrony.legacyregion.service.RemoveMemberStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class RegionMenu {

    private static final String ADD_MEMBER_INPUT = "member";
    private static final String REMOVE_MEMBER_DROPDOWN = "member";
    private static final String SETTINGS_BREAK = "members_break";
    private static final String SETTINGS_OWNER = "owner";
    private static final String SETTINGS_HIDE = "hide";
    private static final String SETTINGS_DELETE = "delete";

    private final RegionService regionService;
    private final RegionMemberService memberService;
    private final MessageService messageService;
    private MenuText text;
    private RegionTextFormatter formatter;

    public RegionMenu(RegionService regionService, RegionMemberService memberService,
                      MessageService messageService, MenuText text,
                      RegionTextFormatter formatter) {
        this.regionService = regionService;
        this.memberService = memberService;
        this.messageService = messageService;
        this.text = text;
        this.formatter = formatter;
    }

    public void openRegions(Player player) {
        List<Region> regions = this.regionService.ownedRegions(player);
        if (regions.isEmpty()) {
            this.messageService.send(player, Messages.NO_OWNED_REGIONS, this.messageService.messages().noOwnedRegions());
            return;
        }
        this.regionsForm(player, regions).send(player);
    }

    public void openRegion(Player player, Region region) {
        this.currentOwned(player, region.id()).ifPresent(current -> this.regionForm(player, current).send(player));
    }

    public void openAddMember(Player player, Region region) {
        this.currentOwned(player, region.id()).ifPresent(current -> this.addMemberForm(player, current).send(player));
    }

    public void openRemoveMember(Player player, Region region) {
        this.currentOwned(player, region.id()).ifPresent(current -> this.removeMemberOrWarn(player, current));
    }

    public void openSettings(Player player, Region region) {
        this.currentOwned(player, region.id()).ifPresent(current -> this.settingsForm(player, current).send(player));
    }

    public void updateText(MenuText text, RegionTextFormatter formatter) {
        this.text = text;
        this.formatter = formatter;
    }

    private SimpleForm regionsForm(Player player, List<Region> regions) {
        SimpleForm form = new SimpleForm(this.text.regionsTitle(), this.text.regionsContent());
        regions.forEach(region -> this.addRegionButton(form, player, region));
        return form;
    }

    private void addRegionButton(SimpleForm form, Player player, Region region) {
        String icon = this.regionService.buttonIcon(region);
        if (icon.isBlank()) {
            form.addButton(this.formatter.regionButton(region), (pl, button) -> this.openRegion(player, region));
            return;
        }
        form.addButton(this.formatter.regionButton(region), ImageType.PATH, icon,
                (pl, button) -> this.openRegion(player, region));
    }

    private SimpleForm regionForm(Player player, Region region) {
        SimpleForm form = new SimpleForm(this.formatter.regionTitle(region));
        form.addButton(this.text.addMemberButton(), (pl, button) -> this.openAddMember(player, region));
        form.addButton(this.text.removeMemberButton(), (pl, button) -> this.openRemoveMember(player, region));
        form.addButton(this.text.settingsButton(), (pl, button) -> this.openSettings(player, region));
        return form;
    }

    private CustomForm addMemberForm(Player player, Region region) {
        CustomForm form = new CustomForm(this.text.addMemberTitle());
        form.addElement(this.text.addMemberLabel());
        form.addElement(ADD_MEMBER_INPUT, new Input(this.text.addMemberInput()).setTrim(true));
        form.setSubmitButton(this.text.applyButton());
        form.setHandler((pl, response) -> this.handleAddMember(player, region, response.getInput(ADD_MEMBER_INPUT).getValue()));
        return form;
    }

    private void handleAddMember(Player player, Region region, String memberName) {
        this.currentOwned(player, region.id()).ifPresent(current -> {
            AddMemberStatus status = this.memberService.addMember(current, memberName);
            this.notifyAddMember(player, status);
        });
    }

    private void notifyAddMember(Player player, AddMemberStatus status) {
        switch (status) {
            case ADDED -> {
                this.messageService.send(player, Messages.MEMBER_ADDED, this.messageService.messages().memberAddedToast());
                this.refreshCommandData(player);
            }
            case PLAYER_NEVER_JOINED -> this.messageService.send(player, Messages.PLAYER_NEVER_JOINED, this.messageService.messages().playerNeverJoinedToast());
            case ALREADY_MEMBER -> this.messageService.send(player, Messages.MEMBER_ALREADY_ADDED, this.messageService.messages().memberAlreadyAdded());
            case OWNER -> this.messageService.send(player, Messages.CANNOT_ADD_YOURSELF, this.messageService.messages().cannotAddYourself());
        }
    }

    private void removeMemberOrWarn(Player player, Region region) {
        if (region.members().isEmpty()) {
            this.warnEmptyMembers(player, region);
            return;
        }
        this.removeMemberForm(player, region).send(player);
    }

    private void warnEmptyMembers(Player player, Region region) {
        this.messageService.send(player, Messages.EMPTY_MEMBERS, this.messageService.messages().emptyMembers());
        this.regionForm(player, region).send(player);
    }

    private CustomForm removeMemberForm(Player player, Region region) {
        CustomForm form = new CustomForm(this.text.removeMemberTitle());
        form.addElement(this.text.removeMemberLabel());
        form.addElement(REMOVE_MEMBER_DROPDOWN, this.membersDropdown(region, this.text.removeMemberDropdown()));
        form.setSubmitButton(this.text.applyButton());
        form.setHandler((pl, response) -> this.handleRemoveMember(player, region, response));
        return form;
    }

    private void handleRemoveMember(Player player, Region region, com.formconstructor.form.response.CustomFormResponse response) {
        RegionMember member = response.getDropdown(REMOVE_MEMBER_DROPDOWN).getValue().getValue(RegionMember.class);
        this.currentOwned(player, region.id()).ifPresent(current -> this.removeSelectedMember(player, current, member));
    }

    private void removeSelectedMember(Player player, Region region, RegionMember member) {
        RemoveMemberStatus status = this.memberService.removeMember(region, member.key());
        if (status == RemoveMemberStatus.REMOVED) {
            this.messageService.send(player, Messages.MEMBER_REMOVED, this.messageService.messages().memberRemovedToast());
            this.refreshCommandData(player);
            return;
        }
        this.messageService.send(player, Messages.MEMBER_NOT_FOUND, this.messageService.messages().memberNotFound());
    }

    private CustomForm settingsForm(Player player, Region region) {
        CustomForm form = new CustomForm(this.text.settingsTitle());
        form.addElement(this.text.settingsLabel());
        form.addElement(SETTINGS_BREAK, new Toggle(this.text.allowMembersBreakRegionBlock(), region.membersCanBreakRegionBlock()));
        form.addElement(SETTINGS_OWNER, this.ownerDropdown(region));
        form.addElement(SETTINGS_HIDE, new Toggle(this.text.hideCoordinates(), region.hiddenCoordinates()));
        form.addElement(SETTINGS_DELETE, new Toggle(this.text.deleteRegion(), false));
        form.setSubmitButton(this.text.applyButton());
        form.setHandler((pl, response) -> this.handleSettings(player, region, response));
        return form;
    }

    private void handleSettings(Player player, Region region, com.formconstructor.form.response.CustomFormResponse response) {
        this.currentOwned(player, region.id()).ifPresent(current -> {
            if (response.getToggle(SETTINGS_DELETE).getValue()) {
                this.deleteRegion(player, current);
                return;
            }
            this.applySettings(player, current, response);
        });
    }

    private void applySettings(Player player, Region region, com.formconstructor.form.response.CustomFormResponse response) {
        region.membersCanBreakRegionBlock(response.getToggle(SETTINGS_BREAK).getValue());
        region.hiddenCoordinates(response.getToggle(SETTINGS_HIDE).getValue());
        this.applyOwnerTransfer(region, response);
        this.regionService.save(region);
        this.messageService.send(player, Messages.REGION_UPDATED, this.messageService.messages().regionUpdated());
        this.refreshCommandData(player);
    }

    private void applyOwnerTransfer(Region region, com.formconstructor.form.response.CustomFormResponse response) {
        RegionMember selected = response.getDropdown(SETTINGS_OWNER).getValue().getValue(RegionMember.class);
        if (selected != null) {
            this.memberService.transferOwner(region, selected);
        }
    }

    private void deleteRegion(Player player, Region region) {
        this.regionService.delete(region);
        this.messageService.send(player, Messages.REGION_DELETED, this.messageService.messages().regionDeleted());
        this.refreshCommandData(player);
    }

    private void refreshCommandData(Player player) {
        player.sendCommandData();
    }

    private Dropdown ownerDropdown(Region region) {
        List<SelectableElement> elements = new ArrayList<>();
        elements.add(new SelectableElement(this.text.transferOwnerNoChange(), null));
        region.members().forEach(member -> elements.add(new SelectableElement(member.name(), member)));
        return new Dropdown(this.text.transferOwnerDropdown(), elements);
    }

    private Dropdown membersDropdown(Region region, String name) {
        List<SelectableElement> elements = region.members().stream()
                .map(member -> new SelectableElement(member.name(), member))
                .toList();
        return new Dropdown(name, elements);
    }

    private Optional<Region> currentOwned(Player player, String regionId) {
        Optional<Region> region = this.regionService.byId(regionId);
        if (region.isPresent() && !region.get().isOwner(RegionMember.normalize(player.getName()))) {
            this.messageService.send(player, Messages.ACTION_DENIED, this.messageService.messages().actionDenied());
            return Optional.empty();
        }
        return region;
    }
}
