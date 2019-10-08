package io.github.wysohn.triggerreactor.sponge.manager.trigger.share.api.nucleus;

import io.github.nucleuspowered.nucleus.api.service.*;
import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.manager.trigger.share.api.APISupportException;
import io.github.wysohn.triggerreactor.sponge.manager.trigger.share.api.APISupport;
import org.spongepowered.api.Sponge;

public class NucleusSupport extends APISupport {
    private NucleusAFKService afk;
    private NucleusBackService back;
    private NucleusHomeService home;
    private NucleusInvulnerabilityService invulnerability;
    private NucleusJailService jail;
    private NucleusKitService kit;
    private NucleusMailService mail;
    private NucleusMuteService mute;
    private NucleusNameBanService ban;
    private NucleusNicknameService nickname;
    private NucleusNoteService note;
    private NucleusPrivateMessagingService privatemessage;
    private NucleusSeenService seen;
    private NucleusServerShopService servershop;
    private NucleusStaffChatService staffchat;
    private NucleusWarningService warning;
    private NucleusWarpService warp;

    public NucleusSupport(TriggerReactor plugin) {
        super(plugin, "nucleus");
    }

    @Override
    public void init() throws APISupportException {
        super.init();

        afk = Sponge.getServiceManager().provide(NucleusAFKService.class).orElse(null);
        back = Sponge.getServiceManager().provide(NucleusBackService.class).orElse(null);
        home = Sponge.getServiceManager().provide(NucleusHomeService.class).orElse(null);
        invulnerability = Sponge.getServiceManager().provide(NucleusInvulnerabilityService.class).orElse(null);
        jail = Sponge.getServiceManager().provide(NucleusJailService.class).orElse(null);
        kit = Sponge.getServiceManager().provide(NucleusKitService.class).orElse(null);
        mail = Sponge.getServiceManager().provide(NucleusMailService.class).orElse(null);
        mute = Sponge.getServiceManager().provide(NucleusMuteService.class).orElse(null);
        ban = Sponge.getServiceManager().provide(NucleusNameBanService.class).orElse(null);
        nickname = Sponge.getServiceManager().provide(NucleusNicknameService.class).orElse(null);
        note = Sponge.getServiceManager().provide(NucleusNoteService.class).orElse(null);
        privatemessage = Sponge.getServiceManager().provide(NucleusPrivateMessagingService.class).orElse(null);
        seen = Sponge.getServiceManager().provide(NucleusSeenService.class).orElse(null);
        servershop = Sponge.getServiceManager().provide(NucleusServerShopService.class).orElse(null);
        staffchat = Sponge.getServiceManager().provide(NucleusStaffChatService.class).orElse(null);
        warning = Sponge.getServiceManager().provide(NucleusWarningService.class).orElse(null);
        warp = Sponge.getServiceManager().provide(NucleusWarpService.class).orElse(null);
    }


}
