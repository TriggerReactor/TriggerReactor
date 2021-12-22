/*
 *     Copyright (C) 2021 wysohn and contributors
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.wysohn.triggerreactor.core.modules;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;
import io.github.wysohn.triggerreactor.core.config.source.EmptyConfigSource;
import io.github.wysohn.triggerreactor.core.config.source.GsonConfigSource;
import io.github.wysohn.triggerreactor.core.config.source.IConfigSourceFactory;

import javax.inject.Named;
import java.io.File;

@Module
public abstract class ConfigSourceFactoryModule {
    private static final String DEFAULT_FACTORY = "gson";

    @Provides
    @Named("DefaultConfigType")
    static String provideDefaultConfigType() {
        return DEFAULT_FACTORY;
    }

    @Provides
    @IntoMap
    @StringKey("none")
    static IConfigSourceFactory provideEmptySource() {
        return (type, folder, fileName) -> new EmptyConfigSource();
    }

    @Provides
    @IntoMap
    @StringKey("gson")
    static IConfigSourceFactory provideGsonSource() {
        return (type, folder, fileName) -> new GsonConfigSource(new File(folder, fileName + ".json"));
    }
}
