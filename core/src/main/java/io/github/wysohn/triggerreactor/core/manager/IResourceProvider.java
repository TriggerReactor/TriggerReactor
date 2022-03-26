package io.github.wysohn.triggerreactor.core.manager;

import io.github.wysohn.triggerreactor.tools.JarUtil;

import java.io.File;
import java.io.IOException;

public interface IResourceProvider {
    void copyFolderFromJar(String folderName, File destFolder, JarUtil.CopyOption option) throws
            IOException;
}
