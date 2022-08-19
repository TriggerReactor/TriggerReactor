package io.github.wysohn.triggerreactor.core.manager.trigger.location;

import io.github.wysohn.triggerreactor.core.config.InvalidTrgConfigurationException;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.manager.trigger.ITriggerLoader;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.tools.FileUtil;

import java.io.IOException;

public final class WalkTriggerManager extends LocationBasedTriggerManager<LocationBasedTriggerManager.WalkTrigger> {

    public WalkTriggerManager(TriggerReactorCore plugin) {
        super(plugin,
              concatPath(plugin.getDataFolder(), "WalkTrigger"),
              new ITriggerLoader<WalkTrigger>() {
                  @Override
                  public WalkTrigger load(TriggerInfo info) throws InvalidTrgConfigurationException {
                      try {
                          String script = FileUtil.readFromFile(info.getSourceCodeFile());
                          WalkTrigger trigger = new WalkTrigger(info, script);
                          return trigger;
                      } catch (TriggerInitFailedException | IOException e) {
                          e.printStackTrace();
                          return null;
                      }
                  }

                  @Override
                  public void save(WalkTrigger trigger) {
                      try {
                          FileUtil.writeToFile(trigger.getInfo().getSourceCodeFile(), trigger.getScript());
                      } catch (IOException e) {
                          e.printStackTrace();
                      }
                  }
              });
    }

    @Override
    public String getTriggerTypeName() {
        return "Walk";
    }

    @Override
    protected WalkTrigger newTrigger(TriggerInfo info, String script) throws TriggerInitFailedException {
        return new WalkTrigger(info, script);
    }
}
