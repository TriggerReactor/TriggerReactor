package io.github.wysohn.triggerreactor.core.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import io.github.wysohn.triggerreactor.core.manager.IJavascriptFileLoader;
import io.github.wysohn.triggerreactor.core.manager.js.IJSFolderContentCopyHelper;
import io.github.wysohn.triggerreactor.tools.JarUtil;

import java.util.Objects;

public class IOUtilityModule extends AbstractModule {

    @Provides
    public IJSFolderContentCopyHelper copyHelper() {
        return (jarFolder, destFolder) ->
                JarUtil.copyFolderFromJar(jarFolder, destFolder, JarUtil.CopyOption.REPLACE_IF_EXIST);
    }

    @Provides
    public IJavascriptFileLoader javascriptFileLoader() {
        return (folder, filter) -> Objects.requireNonNull(folder.listFiles(filter));
    }
}
