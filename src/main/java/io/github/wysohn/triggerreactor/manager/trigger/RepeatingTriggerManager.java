/*******************************************************************************
 *     Copyright (C) 2017 wysohn
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
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package io.github.wysohn.triggerreactor.manager.trigger;

import java.io.IOException;

import io.github.wysohn.triggerreactor.core.lexer.LexerException;
import io.github.wysohn.triggerreactor.core.parser.ParserException;
import io.github.wysohn.triggerreactor.main.TriggerReactor;
import io.github.wysohn.triggerreactor.manager.TriggerManager;

public class RepeatingTriggerManager extends TriggerManager {

    public RepeatingTriggerManager(TriggerReactor plugin) {
        super(plugin);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void reload() {
        // TODO Auto-generated method stub

    }

    @Override
    public void saveAll() {
        // TODO Auto-generated method stub

    }

    public class RepeatingTrigger extends Trigger{

        public RepeatingTrigger(String script) throws IOException, LexerException, ParserException {
            super(script);

            init();
        }

        @Override
        public Trigger clone() {
            try {
                return new RepeatingTrigger(this.getScript());
            } catch (IOException | LexerException | ParserException e) {
                e.printStackTrace();
            }

            return null;
        }

    }
}
