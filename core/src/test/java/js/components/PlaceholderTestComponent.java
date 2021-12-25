package js.components;

import dagger.Component;
import io.github.wysohn.triggerreactor.core.manager.AbstractPlaceholderManager;
import js.modules.PlaceholderTestModule;

@Component(modules = PlaceholderTestModule.class)
public interface PlaceholderTestComponent {
    AbstractPlaceholderManager manager();
}
