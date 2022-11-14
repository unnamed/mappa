package team.unnamed.mappa.command;

import me.fixeddev.commandflow.CommandContext;
import me.fixeddev.commandflow.annotated.CommandClass;
import me.fixeddev.commandflow.command.Command;
import me.fixeddev.commandflow.part.CommandPart;
import me.fixeddev.commandflow.part.defaults.SequentialCommandPart;
import me.fixeddev.commandflow.part.defaults.SubCommandPart;
import team.unnamed.mappa.model.MappaPlayer;
import team.unnamed.mappa.object.Text;
import team.unnamed.mappa.object.TranslationNode;
import team.unnamed.mappa.util.Texts;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class HelpCommand implements CommandClass {
    public static final int MAX_ENTRIES = 8;

    public void help(MappaPlayer sender, int page, CommandContext context) {
        List<Command> executionPath = context.getExecutionPath();
        int commandPosition = Math.max(0, executionPath.size() - 2);
        // Get command with subcommands or root of all commands
        Command command = executionPath.get(commandPosition);
        CommandPart rawPart = command.getPart();
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

        List<String> labels = context.getLabels();
        // remove "help" label
        labels.remove(labels.size() - 1);
        String label = String.join(" ", labels);

        String rootName = Texts.capitalize(command.getName());
        List<Command> subCommands = part.getSubCommands()
            .stream()
            .filter(c -> !c.getName().equals("help"))
            .sorted(Comparator.comparing(Command::getName))
            .collect(Collectors.toList());
        int size = subCommands.size();
        if (size > MAX_ENTRIES) {
            int maxPage = (int) Math.ceil(size / (double) MAX_ENTRIES);
            page = range(1, maxPage, page);

            int min;
            int max;
            if (page == 1) {
                min = 0;
                max = MAX_ENTRIES;
            } else {
                int entry = MAX_ENTRIES * page;
                min = entry - MAX_ENTRIES;
                max = Math.min(size, entry);
            }

            subCommands = subCommands.subList(min, max);
            rootName += " (" + page + "/" + maxPage + ")";
        }

        Text header = TranslationNode
            .HELP_HEADER
            .with("{name}", rootName);
        sender.send(header);
        for (Command subCommand : subCommands) {
            // don't index it
            if (subCommand.getName().equals("help")) {
                continue;
            }
            sender.send(
                TranslationNode
                    .HELP_ENTRY
                    .with("{label}", label,
                        "{name}", subCommand.getName(),
                        "{description}", Texts.toString(subCommand.getDescription())));
        }
        sender.send(header);
    }

    public int range(int min, int max, int value) {
        if (value < min) {
            return min;
        } else if (value > max) {
            return max;
        }
        return value;
    }
}
