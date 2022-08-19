package io.github.wysohn.triggerreactor.core.manager.trigger.location;

import io.github.wysohn.triggerreactor.core.config.InvalidTrgConfigurationException;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.manager.trigger.ITriggerLoader;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.tools.FileUtil;

import java.io.IOException;

public final class ClickTriggerManager extends LocationBasedTriggerManager<LocationBasedTriggerManager.ClickTrigger> {

    public ClickTriggerManager(TriggerReactorCore plugin) {
        super(plugin,
              concatPath(plugin.getDataFolder(), "ClickTrigger"),
              new ITriggerLoader<ClickTrigger>() {
                  @Override
                  public ClickTrigger load(TriggerInfo info) throws InvalidTrgConfigurationException {
                      try {
                          String script = FileUtil.readFromFile(info.getSourceCodeFile());
                          return newInstance(info, script);
                      } catch (TriggerInitFailedException | IOException e) {
                          e.printStackTrace();
                          return null;
                      }
                  }

                  @Override
                  public void save(ClickTrigger trigger) {
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
        return "Click";
    }

    @Override
    protected ClickTrigger newTrigger(TriggerInfo info, String script) throws TriggerInitFailedException {
        return newInstance(info, script);
    }

    private static ClickTrigger newInstance(TriggerInfo info, String script) throws TriggerInitFailedException {
        return new ClickTrigger(info, script, activity ->
                activity == Activity.LEFT_CLICK_BLOCK || activity == Activity.RIGHT_CLICK_BLOCK);
    }
}
