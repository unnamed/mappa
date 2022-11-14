package team.unnamed.mappa;

import me.fixeddev.commandflow.CommandManager;
import me.fixeddev.commandflow.ErrorHandler;
import me.fixeddev.commandflow.exception.ArgumentParseException;
import me.fixeddev.commandflow.exception.CommandUsage;
import me.fixeddev.commandflow.translator.Translator;
import net.kyori.text.Component;
import team.unnamed.mappa.internal.clipboard.ClipboardHandler;
import team.unnamed.mappa.internal.clipboard.CuboidTransform;
import team.unnamed.mappa.internal.clipboard.VectorTransform;
import team.unnamed.mappa.internal.command.MappaCommandManager;
import team.unnamed.mappa.internal.event.bus.EventBus;
import team.unnamed.mappa.internal.event.bus.PlatformListenerModule;
import team.unnamed.mappa.internal.injector.BasicMappaModule;
import team.unnamed.mappa.internal.injector.MappaInjector;
import team.unnamed.mappa.internal.message.MessageTranslationProvider;
import team.unnamed.mappa.model.MappaPlayer;
import team.unnamed.mappa.model.map.scheme.MapSchemeFactory;
import team.unnamed.mappa.model.region.Cuboid;
import team.unnamed.mappa.object.Vector;
import team.unnamed.mappa.throwable.ArgumentTextParseException;
import team.unnamed.mappa.throwable.InvalidPropertyException;
import team.unnamed.mappa.throwable.ParseException;
import team.unnamed.mappa.util.Texts;

import java.util.concurrent.atomic.AtomicReference;

public final class Mappa {
    private static final AtomicReference<MappaPlatform> PLATFORM = new AtomicReference<>();
    private static final AtomicReference<MappaAPI> API = new AtomicReference<>();

    public static MappaPlatform init(MappaAPI api, MappaPlatformBuilder builder) throws ParseException {
        if (PLATFORM.get() != null) {
            throw new IllegalStateException("A mappa platform is already running!");
        }

        // Add default implementations of components or throw exceptions here
        if (!builder.checkCommandManager()) {
            throw new IllegalArgumentException("Command manager is not initialised!");
        }

        if (!builder.checkDataFolder()) {
            throw new IllegalArgumentException("Data folder is not initialised!");
        }

        if (!builder.checkSchemeMapper()) {
            throw new IllegalArgumentException("Scheme mapper is not initialised!");
        }

        if (!builder.checkMapSchemeFactory()) {
            MapSchemeFactory defaultFactory = MapSchemeFactory.create(
                MappaInjector.newInjector(new BasicMappaModule()));
            builder.mapSchemeFactory(defaultFactory);
        }

        EventBus eventBus = api.getEventBus();
        eventBus.install(new PlatformListenerModule());

        ClipboardHandler clipboardHandler = api.getClipboardHandler();
        clipboardHandler.registerTypeTransform(Vector.class, new VectorTransform());
        clipboardHandler.registerTypeTransform(Cuboid.class, new CuboidTransform());

        API.set(api);
        MappaPlatform platform = builder.build();

        // CommandFlow stuff
        CommandManager internal = platform.getCommandManager().getInternalCommandManager();
        Translator translator = internal.getTranslator();
        translator.setProvider(new MessageTranslationProvider());

        ErrorHandler errorHandler = internal.getErrorHandler();
        errorHandler.addExceptionHandler(ArgumentParseException.class,
            (namespace, throwable) -> {
                MappaPlayer sender = namespace.getObject(
                    MappaPlayer.class,
                    MappaCommandManager.MAPPA_PLAYER);
                String message = throwable.getMessage();
                if (message == null) {
                    return true;
                }

                Component translate = translator.translate(throwable.getMessageComponent(), namespace);
                sender.send(Texts.toString(translate), true);
                return true;
            });
        errorHandler.addExceptionHandler(ArgumentTextParseException.class,
            (namespace, throwable) -> {
                MappaPlayer sender = namespace.getObject(
                    MappaPlayer.class,
                    MappaCommandManager.MAPPA_PLAYER);
                sender.send(throwable.getText(), throwable.getEntities());
                return true;
            });
        errorHandler.addExceptionHandler(InvalidPropertyException.class,
            (namespace, throwable) -> {
                MappaPlayer sender = namespace.getObject(
                    MappaPlayer.class,
                    MappaCommandManager.MAPPA_PLAYER);
                sender.send(throwable.getTextNode());
                return true;
            });
        errorHandler.addExceptionHandler(CommandUsage.class,
            (namespace, throwable) -> {
                MappaPlayer sender = namespace.getObject(
                    MappaPlayer.class,
                    MappaCommandManager.MAPPA_PLAYER);
                sender.send("/" + Texts.toString(throwable), true);
                return true;
            });

        return platform;
    }

    public static MappaPlatform getPlatform() {
        return PLATFORM.get();
    }

    public static MappaAPI getAPI() {
        return API.get();
    }
}
