package io.github.wysohn.triggerreactor.core.manager.trigger;

import io.github.wysohn.triggerreactor.core.config.source.IConfigSource;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TriggerInfoTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void getSourceCodeFile() throws IOException {
        File file = folder.newFile("test.trg");
        IConfigSource config = mock(IConfigSource.class);

        TriggerInfo info = TriggerInfo.defaultInfo(file, config);

        assertEquals(file, info.getSourceCodeFile());
    }

    @Test
    public void reloadConfig() throws IOException {
        File file = folder.newFile("test.trg");
        IConfigSource config = mock(IConfigSource.class);

        TriggerInfo info = TriggerInfo.defaultInfo(file, config);
        info.reload();

        verify(config).reload();
    }

    @Test
    public void getTriggerName() throws IOException {
        File file = folder.newFile("test.trg");
        IConfigSource config = mock(IConfigSource.class);

        TriggerInfo info = TriggerInfo.defaultInfo(file, config);

        assertEquals("test", info.getTriggerName());
    }

    @Test
    public void put() throws IOException {
        File file = folder.newFile("test.trg");
        IConfigSource config = mock(IConfigSource.class);

        TriggerInfo info = TriggerInfo.defaultInfo(file, config);

        info.put(TriggerConfigKey.KEY_SYNC, true);
        verify(config).put(TriggerConfigKey.KEY_SYNC.getKey(), true);
    }

    @Test
    public void put2() throws IOException {
        File file = folder.newFile("test.trg");
        IConfigSource config = mock(IConfigSource.class);

        TriggerInfo info = TriggerInfo.defaultInfo(file, config);

        info.put(TriggerConfigKey.KEY_SYNC, 3, true);
        verify(config).put(TriggerConfigKey.KEY_SYNC.getKey(3), true);
    }

    @Test
    public void get() throws IOException {
        File file = folder.newFile("test.trg");
        IConfigSource config = mock(IConfigSource.class);

        when(config.get(TriggerConfigKey.KEY_SYNC.getKey(), Boolean.class))
                .thenReturn(Optional.of(true));

        TriggerInfo info = TriggerInfo.defaultInfo(file, config);

        assertTrue(info.get(TriggerConfigKey.KEY_SYNC, Boolean.class).orElse(false));
    }

    @Test
    public void get2() throws IOException {
        File file = folder.newFile("test.trg");
        IConfigSource config = mock(IConfigSource.class);

        when(config.get(TriggerConfigKey.KEY_SYNC.getKey(3), Boolean.class))
                .thenReturn(Optional.of(true));

        TriggerInfo info = TriggerInfo.defaultInfo(file, config);

        assertTrue(info.get(TriggerConfigKey.KEY_SYNC, 3, Boolean.class).orElse(false));
    }

    @Test
    public void getOldKey() throws IOException {
        File file = folder.newFile("test.trg");
        IConfigSource config = mock(IConfigSource.class);

        when(config.get("Sync", Boolean.class))
                .thenReturn(Optional.of(true));

        TriggerInfo info = TriggerInfo.defaultInfo(file, config);

        assertTrue(info.get(TriggerConfigKey.KEY_SYNC, Boolean.class).orElse(false));
    }

    @Test
    public void getOldKey2() throws IOException {
        File file = folder.newFile("test.trg");
        IConfigSource config = mock(IConfigSource.class);

        when(config.get("Sync.3", Boolean.class))
                .thenReturn(Optional.of(true));

        TriggerInfo info = TriggerInfo.defaultInfo(file, config);

        assertTrue(info.get(TriggerConfigKey.KEY_SYNC, 3, Boolean.class).orElse(false));
    }

    @Test
    public void has() throws IOException {
        File file = folder.newFile("test.trg");
        IConfigSource config = mock(IConfigSource.class);

        when(config.has(TriggerConfigKey.KEY_SYNC.getKey()))
                .thenReturn(true);

        TriggerInfo info = TriggerInfo.defaultInfo(file, config);

        assertTrue(info.has(TriggerConfigKey.KEY_SYNC));
    }

    @Test
    public void isSection() throws IOException {
        File file = folder.newFile("test.trg");
        IConfigSource config = mock(IConfigSource.class);

        when(config.isSection(TriggerConfigKey.KEY_SYNC.getKey()))
                .thenReturn(true);

        TriggerInfo info = TriggerInfo.defaultInfo(file, config);

        assertTrue(info.isSection(TriggerConfigKey.KEY_SYNC));
    }

    @Test
    public void delete() throws IOException {
        File file = folder.newFile("test.trg");
        IConfigSource config = mock(IConfigSource.class);

        TriggerInfo info = TriggerInfo.defaultInfo(file, config);
        info.delete();

        verify(config).delete();
        assertFalse(file.exists());
    }

    @Test
    public void isTriggerFile() throws IOException {
        assertTrue(TriggerInfo.isTriggerFile(folder.newFile("test.trg")));
        assertFalse(TriggerInfo.isTriggerFile(folder.newFile("test.txt")));
        assertFalse(TriggerInfo.isTriggerFile(folder.newFile(".trg")));
        assertFalse(TriggerInfo.isTriggerFile(folder.newFolder()));
    }

    @Test
    public void extractName() throws IOException {
        assertEquals("test", TriggerInfo.extractName(folder.newFile("test")));
        assertNull(TriggerInfo.extractName(folder.newFolder()));
        assertEquals("test", TriggerInfo.extractName(folder.newFile("test.trg")));
        assertEquals("test", TriggerInfo.extractName(folder.newFile("test.txt")));
        assertEquals("", TriggerInfo.extractName(folder.newFile(".trg")));
    }

    @Test
    public void hasDuplicate() throws IOException {
        File file = folder.newFile("test.trg");
        IConfigSource config = mock(IConfigSource.class);

        when(config.has(TriggerConfigKey.KEY_SYNC.getKey())).thenReturn(true);
        when(config.has(TriggerConfigKey.KEY_SYNC.getOldKey())).thenReturn(true);

        TriggerInfo info = TriggerInfo.defaultInfo(file, config);

        assertTrue(info.hasDuplicate(TriggerConfigKey.KEY_SYNC));
        verify(config).has(TriggerConfigKey.KEY_SYNC.getKey());
        verify(config).has(TriggerConfigKey.KEY_SYNC.getOldKey());
    }

    @Test
    public void hasDuplicateWithIndex() throws IOException {
        File file = folder.newFile("test.trg");
        IConfigSource config = mock(IConfigSource.class);

        when(config.has(TriggerConfigKey.KEY_TRIGGER_INVENTORY_ITEMS.getKey(3)))
                .thenReturn(true);
        when(config.has(TriggerConfigKey.KEY_TRIGGER_INVENTORY_ITEMS.getOldKey(3)))
                .thenReturn(true);

        TriggerInfo info = TriggerInfo.defaultInfo(file, config);

        assertTrue(info.hasDuplicate(TriggerConfigKey.KEY_TRIGGER_INVENTORY_ITEMS, 3));
        verify(config).has(TriggerConfigKey.KEY_TRIGGER_INVENTORY_ITEMS.getKey(3));
        verify(config).has(TriggerConfigKey.KEY_TRIGGER_INVENTORY_ITEMS.getOldKey(3));
    }
}