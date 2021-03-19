package io.github.wysohn.triggerreactor.bukkit.tools.migration;

import io.github.wysohn.triggerreactor.core.config.IMigratable;
import io.github.wysohn.triggerreactor.core.config.IMigrationHelper;
import io.github.wysohn.triggerreactor.core.config.source.IConfigSource;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.StringReader;

import static org.mockito.Mockito.mock;

public class NaiveMigrationHelperTest {

    @Test
    public void migrate() {
        IConfigSource mockSource = mock(IConfigSource.class);
        FileConfiguration mockConfig = YamlConfiguration.loadConfiguration(new StringReader("" +
                "Mysql:\n" +
                "  Enable: false\n" +
                "  Address: 127.0.0.1:3306\n" +
                "  DbName: TriggerReactor\n" +
                "  UserName: root\n" +
                "  Password: '1234'\n" +
                "  Deep: \n" +
                "      Value: 5555\n" +
                "      Value2: 52.24\n" +
                "PermissionManager:\n" +
                "  Intercept: true" +
                ""));

        File mockFile = mock(File.class);
        IMigratable mockMigratable = new IMigratable() {
            @Override
            public boolean isMigrationNeeded() {
                return true;
            }

            @Override
            public void migrate(IMigrationHelper migrationHelper) {
                migrationHelper.migrate(mockSource);
            }
        };

        NaiveMigrationHelper helper = new NaiveMigrationHelper(mockConfig, mockFile);

        mockMigratable.migrate(helper);

        Mockito.verify(mockSource).put(Mockito.eq("Mysql.Enable"), Mockito.eq(false));
        Mockito.verify(mockSource).put(Mockito.eq("Mysql.Address"), Mockito.eq("127.0.0.1:3306"));
        Mockito.verify(mockSource).put(Mockito.eq("Mysql.DbName"), Mockito.eq("TriggerReactor"));
        Mockito.verify(mockSource).put(Mockito.eq("Mysql.UserName"), Mockito.eq("root"));
        Mockito.verify(mockSource).put(Mockito.eq("Mysql.Password"), Mockito.eq("1234"));
        Mockito.verify(mockSource).put(Mockito.eq("Mysql.Deep.Value"), Mockito.eq(5555));
        Mockito.verify(mockSource).put(Mockito.eq("Mysql.Deep.Value2"), Mockito.eq(52.24));
        Mockito.verify(mockSource).put(Mockito.eq("PermissionManager.Intercept"), Mockito.eq(true));
    }
}