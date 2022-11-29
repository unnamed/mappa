package team.unnamed.mappa.model;

import me.fixeddev.commandflow.annotated.part.PartInjector;
import me.fixeddev.commandflow.part.CommandPart;
import net.kyori.text.Component;
import org.jetbrains.annotations.Nullable;
import team.unnamed.mappa.MappaAPI;
import team.unnamed.mappa.internal.clipboard.ClipboardHandler;
import team.unnamed.mappa.internal.command.CommandSchemeNodeBuilder;
import team.unnamed.mappa.internal.command.Commands;
import team.unnamed.mappa.internal.event.MappaPropertySetEvent;
import team.unnamed.mappa.internal.event.MappaSetupStepEvent;
import team.unnamed.mappa.internal.event.bus.EventBus;
import team.unnamed.mappa.internal.message.MappaTextHandler;
import team.unnamed.mappa.internal.region.RegionRegistry;
import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.model.map.property.MapCollectionProperty;
import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.model.region.RegionSelection;
import team.unnamed.mappa.model.visualizer.PropertyVisual;
import team.unnamed.mappa.model.visualizer.Visualizer;
import team.unnamed.mappa.object.Vector;
import team.unnamed.mappa.object.*;
import team.unnamed.mappa.object.config.LineDeserializableList;
import team.unnamed.mappa.throwable.ParseException;
import team.unnamed.mappa.util.BlockFace;
import team.unnamed.mappa.util.Texts;

import java.lang.reflect.Type;
import java.util.*;

import static team.unnamed.mappa.util.Texts.toPrettifyString;

public abstract class AbstractMappaPlayer<T> implements MappaPlayer {
    protected T entity;

    protected MappaTextHandler textHandler;
    protected MappaAPI api;

    protected MapSession session;

    public AbstractMappaPlayer(T entity, MappaAPI api) {
        this.entity = entity;
        this.textHandler = api.getPlatform().getTextHandler();
        this.api = api;
    }

    @Override
    public void send(String message, boolean formal) {
        if (formal) {
            String prefix = textHandler.getPrefix(entity);
            message = prefix + message;
        }
        send(message);
    }

    @Override
    public void send(Text text) {
        textHandler.send(entity, text);
    }

    @Override
    public void send(Text text, Object... entities) {
        textHandler.send(entity, text, entities);
    }

    @Override
    public String format(String node) {
        return textHandler.format(entity, node);
    }

    @Override
    public String format(Text text) {
        return textHandler.format(entity, text);
    }

    @Override
    public String format(Text text, Object... entities) {
        return textHandler.format(entity, text, entities);
    }

    @Override
    public void selectMapSession(MapSession session) {
        send(TranslationNode
                .SELECTED_SESSION
                .formalText(),
            session);
        this.session = session;
        clearVisuals();
    }

    @Override
    public void deselectMapSession() {
        send(TranslationNode
                .DESELECTED_SESSION
                .formalText(),
            session);
        this.session = null;
        clearVisuals();
    }

    public void clearVisuals() {
        Visualizer visualizer = api.getVisualizer();
        if (visualizer != null && visualizer.hasVisuals(this)) {
            visualizer.clearVisualsOf(this);

            send(TranslationNode
                .CLEAR_VISUAL
                .formalText());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R> void selectRegion(R object, RegionSelection.Order order) {
        RegionRegistry registry = api.getRegionRegistry();
        if (!checkComponent(RegionRegistry.class, registry)) {
            return;
        }

        Class<R> clazz = (Class<R>) object.getClass();
        RegionSelection<R> selection = registry.getOrNewSelection(
            getUniqueId().toString(), clazz);
        if (order == RegionSelection.Order.FIRST) {
            selection.setFirstPoint(object);
        } else {
            selection.setSecondPoint(object);
        }
    }

    @Override
    public void showVisual(String path, boolean notify) {
        if (!checkSession()) {
            return;
        }

        Visualizer visualizer = api.getVisualizer();
        if (!checkComponent(Visualizer.class, visualizer)) {
            return;
        }

        if (!session.containsProperty(path)) {
            return;
        }

        PropertyVisual visual = visualizer.getPropertyVisualOf(session, path);
        if (visual == null) {
            send(TranslationNode
                .NO_VISUAL
                .withFormal("{property}", path));
            return;
        }
        Set<PropertyVisual> visuals = visualizer.getVisualsOf(this);
        visual.show(this);
        visuals.add(visual);
        if (visuals.size() > visualizer.getMaxVisuals()) {
            Iterator<PropertyVisual> it = visuals.iterator();
            PropertyVisual next = it.next();
            next.hide(this);
            it.remove();
        }
        send(TranslationNode
            .SHOW_VISUAL
            .withFormal("{property}", path));
    }

    @Override
    public void hideVisual(String path, boolean notify) {
        if (!checkSession()) {
            return;
        }

        Visualizer visualizer = api.getVisualizer();
        if (!checkComponent(Visualizer.class, visualizer)) {
            return;
        }

        if (!session.containsProperty(path)) {
            return;
        }

        Set<PropertyVisual> visuals = visualizer.getVisualsOf(this);
        Map<String, PropertyVisual> mapVisuals = visualizer.getVisualsOfSession(session);
        PropertyVisual visual = mapVisuals.get(path);
        if (visual == null) {
            send(TranslationNode
                .NO_VISUAL
                .withFormal("{property}", path));
            return;
        }
        visual.hide(this);
        visuals.remove(visual);
        send(TranslationNode
            .HIDE_VISUAL
            .withFormal("{property}", path));
        if (visuals.isEmpty()) {
            visualizer.clearVisualsOf(this);
        }
    }

    @Override
    public boolean hasVisual(String path) {
        if (session == null) {
            return false;
        }

        Visualizer visualizer = api.getVisualizer();
        if (visualizer == null) {
            return false;
        }

        PropertyVisual visual = visualizer.getPropertyVisualOf(session, path);
        return visual != null && visual.containsPlayer(this);
    }

    @Override
    public void setProperty(String path, Object value) throws ParseException {
        MapProperty property = getProperty(path);
        if (property == null) {
            return;
        }

        if (property instanceof MapCollectionProperty) {
            session.property(path, value);
            String valueString = toPrettifyString(value);
            send(TranslationNode
                .PROPERTY_LIST_ADDED
                .withFormal("{type}", getTypeName(property.getType()),
                    "{name}", path,
                    "{value}", valueString
                ));
        } else {
            property.parseValue(value);
            if (value instanceof LineDeserializableList) {
                send(TranslationNode
                    .PROPERTY_CHANGE_TO
                    .withFormal("{name}", path,
                        "{value}", ""));
                LineDeserializableList list = (LineDeserializableList) value;
                for (String listValue : list.deserialize()) {
                    send(TranslationNode
                        .PROPERTY_LIST_ADDED_ENTRY
                        .with("{value}", listValue));
                }
            } else {
                String valueString = toPrettifyString(value);
                send(TranslationNode
                    .PROPERTY_CHANGE_TO
                    .withFormal("{name}", path,
                        "{value}", valueString));
            }
        }

        showVisual(path, false);
    }

    @Override
    public void removePropertyValue(String path, Object value) throws ParseException {
        if (!checkSession()) {
            return;
        }

        boolean found = session.removePropertyValue(path, value);
        String valueString = toPrettifyString(value);
        if (found) {
            MapProperty property = session.getProperty(path);
            send(TranslationNode
                .PROPERTY_LIST_REMOVED
                .withFormal("{type}", getTypeName(property.getType()),
                    "{name}", path,
                    "{value}", valueString));
        } else {
            send(TranslationNode
                .PROPERTY_LIST_VALUE_NOT_FOUND
                .withFormal("{name}", path,
                    "{value}", valueString));
        }
    }

    @Override
    public void clearProperty(String path) {
        if (!checkSession()) {
            return;
        }

        MapProperty property = getProperty(path);
        if (property == null) {
            return;
        }

        clearProperty(path, property);
    }

    @Override
    public void clearProperty(String path, MapProperty property) {
        property.clearValue();
        send(TranslationNode
            .PROPERTY_CLEAR
            .withFormal("{name}", path));
    }

    @Override
    public void showSessionInfo(@Nullable MapSession session) {
        if (session == null) {
            if (!checkSession()) {
                return;
            }

            session = this.session;
        }

        send(TranslationNode.SESSION_INFO_HEADER, session);
        send(TranslationNode.SESSION_MAP_NAME, session);
        send(TranslationNode.SESSION_DATE, session);
        send(TranslationNode.SESSION_WORLD_NAME, session);
        send(TranslationNode.SESSION_VERSION, session);

        Collection<String> authors = session.getAuthors();
        TranslationNode node = TranslationNode.SESSION_AUTHOR;
        Text text;
        if (authors.isEmpty()) {
            text = node.with("{author}", "");
        } else if (authors.size() == 1) {
            text = node.with("{author}", authors.iterator().next());
        } else {
            text = node.with("{author}", "");
            send(text);
            authors.forEach(author ->
                send(TranslationNode
                    .SESSION_AUTHOR_ENTRY
                    .with("{author}", author)));
            return;
        }
        send(text);
        send(TranslationNode.SESSION_INFO_HEADER, session);
    }

    @Override
    public void showSessionList() {
        Collection<MapSession> sessions = api.getPlatform()
            .getMapRegistry()
            .getMapSessions();
        int size = sessions.size();
        if (size == 0) {
            send(
                BukkitTranslationNode
                    .SESSION_LIST_EMPTY
                    .formalText());
            return;
        }
        send(
            BukkitTranslationNode
                .SESSION_LIST_HEADER
                .withFormal(
                    "{number}", size
                ));

        for (MapSession session : sessions) {
            String line = format(
                BukkitTranslationNode
                    .SESSION_LIST_ENTRY
                    .text(),
                session);
            sendActionSessionEntry(session, line);
        }
    }

    protected abstract void sendActionSessionEntry(MapSession session, String line);

    @Override
    public void showPropertyInfo(String path) {
        MapProperty property = getProperty(path);
        if (property == null) {
            return;
        }

        showPropertyInfo(path, property);
    }

    @Override
    public void showPropertyInfo(String path, MapProperty property) {
        TextNode header = TranslationNode
            .PROPERTY_INFO_HEADER
            .with("{name}", property.getName());
        send(header);
        send(TranslationNode
            .PROPERTY_INFO_PATH
            .with("{path}", path));
        String typeName = getTypeName(property.getType());
        String propertyType = getPropertyTypeName(property);
        if (propertyType != null) {
            typeName += " (" + propertyType + ")";
        }
        send(TranslationNode
            .PROPERTY_INFO_TYPE
            .with("{type}", typeName));
        Object value = property.getValue();
        if (property instanceof MapCollectionProperty) {
            Collection<Object> collection = Objects.requireNonNull((Collection<Object>) value);
            send(TranslationNode
                .PROPERTY_INFO_VALUE
                .with("{value}", collection.isEmpty() ? "null" : ""));
            for (Object entry : collection) {
                send(TranslationNode
                    .PROPERTY_INFO_VALUE_LIST
                    .with("{value}", toPrettifyString(entry)));
            }
        } else {
            send(TranslationNode
                .PROPERTY_INFO_VALUE
                .with("{value}", toPrettifyString(value, " -> ")));
        }
        send(header);
    }

    @Override
    public void showSetup() throws ParseException {
        if (!checkSession()) {
            return;
        }

        if (!session.setup()) {
            send(BukkitTranslationNode.NO_SETUP.formalText());
            return;
        }

        String setupStep = session.currentSetup();
        String sessionId = session.getId();
        String line = session.getSchemeName()
            + " "
            + setupStep.replace(".", " ")
            + " "
            + sessionId;

        Text header = BukkitTranslationNode
            .SETUP_HEADER
            .with(
                "{session_id}", sessionId
            );
        send(header);
        Text defineText = BukkitTranslationNode
            .DEFINE_PROPERTY
            .with("{property}", setupStep);
        MapProperty property = session.getProperty(setupStep);
        Text typeText = BukkitTranslationNode
            .TYPE_PROPERTY
            .with("{type}", Texts.getTypeName(property.getType()));

        CommandSchemeNodeBuilder builder = api.getPlatform().getCommandBuilder();
        PartInjector injector = builder.getInjector();
        CommandPart part = Commands.ofPart(injector, property.getType());
        Component component = part.getLineRepresentation();
        String arg = component == null
            ? ""
            : Texts.toString(component);
        sendActionSetup(defineText, typeText, line, arg, property);

        Map<String, Text> errors = session.checkWithScheme(true);
        if (errors.isEmpty()) {
            send(BukkitTranslationNode.SETUP_READY.text());
        }
        send(header);
        api.getEventBus().callEvent(new MappaSetupStepEvent(this, session));
    }

    @Override
    public void showTreeProperty(Map<String, Object> section) {
        if (!checkSession()) {
            return;
        }

        String header = format(TranslationNode.TREE_START.text(), session);
        send(header);
        printMapSection(section);
        send(header);
    }

    protected void printMapSection(Map<?, ?> map) {
        String spacer = Texts.spacer(3);
        if (map == null || map.isEmpty()) {
            return;
        }

        for (Map.Entry<?, ?> entry : map.entrySet()) {
            Object key = entry.getKey();
            String path = toPrettifyString(key);
            Object value = entry.getValue();
            if (value instanceof Map) {
                Map<String, ?> section = (Map<String, ?>) value;
                sendHoverTreeSection(
                    TranslationNode
                        .TREE_SECTION
                        .withFormal(
                            "{indent}", spacer,
                            "{name}", path),
                    TranslationNode
                        .TREE_SECTION_HOVER
                        .with("{name}", path),
                    path,
                    section.keySet());
                continue;
            }

            if (!(value instanceof MapProperty)) {
                throw new IllegalArgumentException(
                    "Trying to show tree properties found unknown object (" + value + ")");
            }
            MapProperty property = (MapProperty) value;

            Object propertyValue = property.getValue();
            if (propertyValue instanceof Collection) {
                Collection<?> collection = (Collection<?>) propertyValue;
                send(TranslationNode.TREE_PROPERTY
                    .with("{indent}", spacer,
                        "{name}", path,
                        "{value}", ""));
                for (Object object : collection) {
                    send(TranslationNode
                        .TREE_COLLECTION_VALUE
                        .with("{indent}", spacer,
                            "{value}", Texts.toPrettifyString(object, " -> ")));
                }
            } else {
                send(TranslationNode.TREE_PROPERTY
                    .with("{indent}", spacer,
                        "{name}", path,
                        "{value}", Texts.toPrettifyString(propertyValue, " -> ")));
            }
        }
    }

    protected abstract void sendHoverTreeSection(Text message, Text hoverText, String path, Collection<String> properties);

    protected abstract void sendActionSetup(Text defineText, Text typeText, String argLine, String arg, MapProperty property);

    @Override
    public void verifyMapSession(boolean showAll) throws ParseException {
        if (!checkSession()) {
            return;
        }

        Map<String, Text> errorMessages = session.checkWithScheme(false);
        if (!errorMessages.isEmpty()) {
            send(
                TranslationNode
                    .VERIFY_SESSION_FAIL
                    .withFormal("{number}", errorMessages.size()));
            int i = 0;
            for (Map.Entry<String, Text> entry : errorMessages.entrySet()) {
                Text errorText = entry.getValue();
                send(
                    TranslationNode
                        .VERIFY_SESSION_FAIL_ENTRY
                        .with("{property}", entry.getKey(),
                            "{error}", format(errorText)));
                ++i;
                if (i == 15 && !showAll) {
                    send(
                        TranslationNode
                            .VERIFY_SESSION_FAIL_SHORTCUT
                            .with("{number}", errorMessages.size() - i));
                    break;
                }
            }
        } else {
            send(
                TranslationNode
                    .VERIFY_SESSION_SUCCESS
                    .formalText()
            );
        }
    }

    protected MapProperty getProperty(String path) {
        if (!checkSession()) {
            return null;
        }

        MapProperty property = session.getProperty(path);
        if (property == null) {
            send(TranslationNode
                .INVALID_PROPERTY
                .withFormal("{property}", path));
        }
        return property;
    }

    protected String getTypeName(Type type) {
        return type instanceof Class
            ? ((Class<?>) type).getSimpleName()
            : type.getTypeName();
    }

    protected String getPropertyTypeName(MapProperty property) {
        if (!(property instanceof MapCollectionProperty)) {
            return null;
        }

        MapCollectionProperty list = (MapCollectionProperty) property;
        return getTypeName(list.getCollectionType());
    }

    protected boolean checkSession() {
        if (session == null) {
            send(TranslationNode.SESSION_NOT_SELECTED.formalText());
            return false;
        }

        return true;
    }

    protected boolean checkComponent(Class<?> clazz, @Nullable Object object) {
        if (object == null) {
            send(TranslationNode
                .COMPONENT_NOT_FOUND
                .withFormal("{name}", getTypeName(clazz)));
            return false;
        }

        return true;
    }

    @Override
    public Clipboard copy(Map<String, MapProperty> properties) {
        ClipboardHandler clipboardHandler = api.getClipboardHandler();
        return clipboardHandler.newCopyOfProperties(this, properties);
    }

    @Override
    public void clearClipboard() {
        ClipboardHandler clipboardHandler = api.getClipboardHandler();
        clipboardHandler.clearClipboardOf(this);
        send(TranslationNode.CLIPBOARD_CLEAR.formalText());
    }

    @Override
    public void paste(boolean mirror) throws ParseException {
        Clipboard clipboard = getClipboard();
        if (clipboard == null) {
            send(TranslationNode.NO_CLIPBOARD.formalText());
            return;
        }

        Vector position = getPosition();
        EventBus eventBus = api.getEventBus();
        clipboard.paste(BlockFace.yawToFace(position.getYaw()),
            position,
            mirror,
            session,
            (path, property) -> eventBus.callEvent(
                new MappaPropertySetEvent(this,
                    session,
                    path,
                    property,
                    true)));
        TranslationNode node = mirror
            ? TranslationNode.CLIPBOARD_MIRROR_PASTED
            : TranslationNode.CLIPBOARD_PASTED;
        send(node.text());
    }

    @Override
    public void castPaste(String pathCast, boolean mirror) throws ParseException {
        Clipboard clipboard = getClipboard();
        if (clipboard == null) {
            send(TranslationNode.NO_CLIPBOARD);
            return;
        }

        Vector position = getPosition();
        EventBus eventBus = api.getEventBus();
        clipboard.castPaste(BlockFace.yawToFace(position.getYaw()),
            position,
            mirror,
            session,
            pathCast,
            (path, property) -> eventBus.callEvent(
                new MappaPropertySetEvent(this,
                    session,
                    path,
                    property,
                    true)));
        send(
            TranslationNode
                .CLIPBOARD_CAST_PASTED
                .with("{new-path}", pathCast));
    }

    @Override
    public Clipboard getClipboard() {
        ClipboardHandler clipboardHandler = api.getClipboardHandler();
        return clipboardHandler.getClipboardOf(this);
    }

    @Override
    public MapSession getMapSession() {
        return session;
    }

    @Override
    public T asEntity() {
        return entity;
    }

    @Override
    public void flush() {
        this.entity = null;
        this.api = null;
        this.textHandler = null;
        this.session = null;
    }

    @Override
    public String toString() {
        UUID id = getUniqueId();
        return id == null ? null : id.toString();
    }
}
