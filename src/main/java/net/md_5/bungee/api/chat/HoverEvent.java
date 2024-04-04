package net.md_5.bungee.api.chat;

import java.beans.ConstructorProperties;
import java.util.Arrays;

public final class HoverEvent {
    private final Action action;

    private final BaseComponent[] value;

    public String toString() {
        return "HoverEvent(action=" + getAction() + ", value=" + Arrays.deepToString((Object[])getValue()) + ")";
    }

    @ConstructorProperties({"action", "value"})
    public HoverEvent(Action action, BaseComponent[] value) {
        this.action = action;
        this.value = value;
    }

    public Action getAction() {
        return this.action;
    }

    public BaseComponent[] getValue() {
        return this.value;
    }

    public enum Action {
        SHOW_TEXT, SHOW_ACHIEVEMENT, SHOW_ITEM, SHOW_ENTITY;
    }
}
