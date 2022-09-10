package team.unnamed.mappa.bukkit.command;

import me.fixeddev.commandflow.CommandContext;
import me.fixeddev.commandflow.annotated.CommandClass;
import me.fixeddev.commandflow.command.Command;
import me.fixeddev.commandflow.part.CommandPart;
import me.fixeddev.commandflow.part.defaults.SequentialCommandPart;
import me.fixeddev.commandflow.part.defaults.SubCommandPart;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import team.unnamed.mappa.bukkit.util.Texts;
import team.unnamed.mappa.internal.message.MappaTextHandler;
import team.unnamed.mappa.object.TranslationNode;

public class HelpCommand implements CommandClass {
    protected final MappaTextHandler textHandler;

    public HelpCommand(MappaTextHandler handler) {
        this.textHandler = handler;
    }

    public void help(CommandSender sender, CommandContext context) {
        Command rootCommand = context.getRootCommand();
        CommandPart rawPart = rootCommand.getPart();
        SubCommandPart part = null;
        if (rawPart instanceof SubCommandPart) {
            part = (SubCommandPart) rawPart;
        } else if (rawPart instanceof SequentialCommandPart) {
            SequentialCommandPart seq = (SequentialCommandPart) rawPart;
            for (CommandPart seqPart : seq.getParts()) {
                if (!(seqPart instanceof SubCommandPart)) {
                    continue;
                }
                part = (SubCommandPart) seqPart;
                break;
            }
        }

        if (part == null) {
            throw new IllegalArgumentException("No subcommand part found");
        }

        String label = String.join(" ", context.getLabels());

        String rootName = StringUtils.capitalize(
            rootCommand.getName());
        textHandler.send(sender,
            TranslationNode
                .HELP_HEADER
                .with("{name}", rootName));
        for (Command command : part.getSubCommands()) {
            textHandler.send(sender,
                TranslationNode
                    .HELP_ENTRY
                    .with("{label}", label,
                        "{name}", command.getName(),
                        "{description}", Texts.toString(command.getDescription())),
                command);
        }
        textHandler.send(sender,
            TranslationNode
                .HELP_HEADER
                .with("{name}", rootName));
    }
}
