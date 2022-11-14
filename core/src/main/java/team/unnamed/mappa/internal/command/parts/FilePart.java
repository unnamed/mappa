package team.unnamed.mappa.internal.command.parts;

import me.fixeddev.commandflow.CommandContext;
import me.fixeddev.commandflow.exception.ArgumentParseException;
import me.fixeddev.commandflow.part.CommandPart;
import me.fixeddev.commandflow.stack.ArgumentStack;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FilePart implements CommandPart {
    private final String name;
    private final File folder;

    public FilePart(String name, File folder) {
        this.name = name;
        this.folder = folder;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void parse(CommandContext context,
                      ArgumentStack stack,
                      @Nullable CommandPart part)
        throws ArgumentParseException {
        String fileName = stack.next();
        File file = new File(folder, fileName);
        if (!file.exists()) {
            throw new ArgumentParseException();
        }

        context.setValue(this, file);
    }

    @Override
    public @Nullable List<String> getSuggestions(CommandContext context, ArgumentStack stack) {
        String string = stack.next();
        String[] files = folder.list();
        if (files == null || files.length == 0) {
            return null;
        }

        List<String> list = new ArrayList<>();
        for (String fileName : files) {
            if (!fileName.startsWith(string)) {
                continue;
            }

            list.add(fileName);
        }

        return list;
    }
}
