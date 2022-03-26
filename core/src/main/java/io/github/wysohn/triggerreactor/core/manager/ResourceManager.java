package io.github.wysohn.triggerreactor.core.manager;

import io.github.wysohn.triggerreactor.tools.JarUtil;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;

public class ResourceManager extends Manager implements IResourceProvider {
    @Inject
    ResourceManager() {
    }

    @Override
    public void onDisable() {

    }

    @Override
    public void onEnable() throws Exception {

    }

    @Override
    public void onReload() throws RuntimeException {

    }

    @Override
    public void copyFolderFromJar(String folderName, File destFolder, JarUtil.CopyOption option) throws
            IOException {
        JarUtil.copyFolderFromJar(folderName, destFolder, option);
    }
}