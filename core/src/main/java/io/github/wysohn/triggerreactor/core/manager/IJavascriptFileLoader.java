package io.github.wysohn.triggerreactor.core.manager;

import java.io.File;
import java.io.FileFilter;

public interface IJavascriptFileLoader {
    File[] listFiles(File folder, FileFilter filter);
}
