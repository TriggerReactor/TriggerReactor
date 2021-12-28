package js.components;

import dagger.Component;
import io.github.wysohn.triggerreactor.core.manager.PlaceholderManager;
import js.modules.PlaceholderTestModule;
import js.scope.JsTestScope;

@Component(modules = {PlaceholderTestModule.class},
           dependencies = {ScriptEngineComponent.class})
@JsTestScope
public interface PlaceholderTestComponent {
    PlaceholderManager manager();
}
