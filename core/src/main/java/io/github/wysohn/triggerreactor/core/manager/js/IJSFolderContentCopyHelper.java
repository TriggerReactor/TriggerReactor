package io.github.wysohn.triggerreactor.core.manager.js;

import java.io.File;
import java.io.IOException;

public interface IJSFolderContentCopyHelper {
    void copyFolderFromJar(String jarFolderLocation, File destFolder) throws IOException;
}
