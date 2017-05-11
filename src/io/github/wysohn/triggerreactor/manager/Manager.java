package io.github.wysohn.triggerreactor.manager;

import java.util.ArrayList;
import java.util.List;

import io.github.wysohn.triggerreactor.main.TriggerReactor;

public abstract class Manager {
    private static final List<Manager> managers = new ArrayList<Manager>();
    public static List<Manager> getManagers() {
        return managers;
    }

    protected final TriggerReactor plugin;

    public Manager(TriggerReactor plugin) {
        this.plugin = plugin;

        managers.add(this);
    }

    public abstract void reload();
    public abstract void saveAll();
}
