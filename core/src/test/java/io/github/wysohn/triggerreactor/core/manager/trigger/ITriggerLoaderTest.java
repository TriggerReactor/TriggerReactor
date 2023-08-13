package io.github.wysohn.triggerreactor.core.manager.trigger;

import io.github.wysohn.triggerreactor.core.config.InvalidTrgConfigurationException;
import io.github.wysohn.triggerreactor.core.config.source.IConfigSource;
import io.github.wysohn.triggerreactor.core.config.source.IConfigSourceFactory;
import io.github.wysohn.triggerreactor.core.config.source.SaveWorker;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.CommandTrigger;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class ITriggerLoaderTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private ITriggerLoader<CommandTrigger> loader = new TestLoader();

    @Test
    public void listTriggers() throws Exception {
        // arrange
        SaveWorker saveWorker = mock(SaveWorker.class);

        File file1 = folder.newFile("test1.trg");
        File folder1 = folder.newFolder("folder1");
        File file2 = new File(folder1, "test2.trg");
        File file3 = new File(folder1, "test3.trg");
        File folder2 = folder.newFolder("folder2");
        File file4 = new File(folder2, "test4.trg");
        File file5 = new File(folder2, "test5.trg");

        file1.createNewFile();
        file2.createNewFile();
        file3.createNewFile();
        file4.createNewFile();
        file5.createNewFile();

        IConfigSourceFactory factory = mock(IConfigSourceFactory.class);

        // act
        TriggerInfo[] result = loader.listTriggers(saveWorker, folder.getRoot(), factory);

        // assert
        verify(factory, times(5)).create(any(), any(File.class), any());
        assertEquals(5, result.length);
    }

    private static class TestLoader implements ITriggerLoader<CommandTrigger> {
        @Override
        public CommandTrigger load(TriggerInfo info) throws InvalidTrgConfigurationException {
            return null;
        }

        @Override
        public void save(CommandTrigger trigger) {

        }

        @Override
        public TriggerInfo toTriggerInfo(File sourceCodeFile, IConfigSource configSource) {
            return mock(TriggerInfo.class);
        }
    }
}
