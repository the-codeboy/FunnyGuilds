package net.dzikoysk.funnyguilds.feature.command.admin;

import net.dzikoysk.funnycommands.resources.ValidationException;
import net.dzikoysk.funnycommands.stereotypes.FunnyCommand;
import net.dzikoysk.funnyguilds.event.SimpleEventHandler;
import net.dzikoysk.funnyguilds.event.rank.DeathsChangeEvent;
import net.dzikoysk.funnyguilds.feature.command.AbstractFunnyCommand;
import net.dzikoysk.funnyguilds.feature.command.UserValidation;
import net.dzikoysk.funnyguilds.shared.FunnyFormatter;
import net.dzikoysk.funnyguilds.user.User;
import net.dzikoysk.funnyguilds.user.UserRank;
import org.bukkit.command.CommandSender;
import panda.std.Option;

import static net.dzikoysk.funnyguilds.feature.command.DefaultValidation.when;

public final class DeathsCommand extends AbstractFunnyCommand {

    @FunnyCommand(
            name = "${admin.deaths.name}",
            permission = "funnyguilds.admin",
            completer = "online-players:3",
            acceptsExceeded = true
    )
    public void execute(CommandSender sender, String[] args) {
        when(args.length < 1, this.messages.generalNoNickGiven);
        when(args.length < 2, this.messages.adminNoDeathsGiven);

        int deaths = Option.attempt(NumberFormatException.class, () -> Integer.parseInt(args[1])).orThrow(() -> {
            return new ValidationException(FunnyFormatter.format(this.messages.adminErrorInNumber, "{ERROR}", args[1]));
        });

        User admin = AdminUtils.getAdminUser(sender);
        User user = UserValidation.requireUserByName(args[0]);

        UserRank userRank = user.getRank();
        int change = deaths - userRank.getDeaths();

        DeathsChangeEvent deathsChangeEvent = new DeathsChangeEvent(AdminUtils.getCause(admin), admin, user, change);
        if (!SimpleEventHandler.handle(deathsChangeEvent)) {
            return;
        }

        int finalDeaths = user.getRank().getDeaths() + deathsChangeEvent.getDeathsChange();
        user.getRank().setDeaths(finalDeaths);

        FunnyFormatter formatter = new FunnyFormatter()
                .register("{PLAYER}", user.getName())
                .register("{DEATHS}", finalDeaths);

        this.sendMessage(sender, formatter.format(this.messages.adminDeathsChanged));
    }

}
