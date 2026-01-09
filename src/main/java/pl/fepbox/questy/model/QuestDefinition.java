package pl.fepbox.questy.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class QuestDefinition {
    private final String name;
    private final boolean enabled;
    private final List<String> message;

    public QuestDefinition(String name, boolean enabled, List<String> message) {
        this.name = name;
        this.enabled = enabled;
        this.message = message == null ? new ArrayList<>() : new ArrayList<>(message);
    }

    public String name() {
        return name;
    }

    public boolean enabled() {
        return enabled;
    }

    public List<String> message() {
        return Collections.unmodifiableList(message);
    }
}
