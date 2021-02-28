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

package io.github.wysohn.triggerreactor.core.config;

import io.github.wysohn.triggerreactor.core.config.source.ITypeValidator;

import java.io.File;

public interface IConfigSourceFactory {
    IConfigSource none(File folder, String fileName);

    IConfigSource none(File file);

    IConfigSource gson(File folder, String fileName, ITypeValidator... validators);

    IConfigSource gson(File file, ITypeValidator... validators);
}
