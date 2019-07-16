/*******************************************************************************
 *     Copyright (C) 2018 wysohn
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
/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package io.github.wysohn.triggerreactor.bukkit.tools;

import com.google.common.collect.ForwardingMultimap;
import com.google.common.collect.Multimap;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.UUID;

public class SkullUtil {

    /**
     * Try to inject the texture value(which is base64 encoded) into the
     * SkullMeta. It silently fails if textureValue is not available for the
     * Skull item(Or perhaps, it does not exist in older minecraft versions)
     *
     * @param SM           the SkullMeta
     * @param textureValue the base64 encoded texture value.
     * @throws Exception just catch this exception and do nothing.
     */
    public static void setTextureValue(SkullMeta SM, String textureValue) throws Exception {
        try {
            Class<?> profileClazz = Class.forName("com.mojang.authlib.GameProfile");
            Class<?> propertyClazz = Class.forName("com.mojang.authlib.properties.Property");

            Object profile = profileClazz.getConstructor(UUID.class, String.class).newInstance(UUID.randomUUID(), null);

            Method getPropertiesMethod = profile.getClass().getMethod("getProperties");
            Object properties = getPropertiesMethod.invoke(profile);

            Method putMethod = properties.getClass().getMethod("put", Object.class, Object.class);
            putMethod.invoke(properties, "textures",
                    propertyClazz.getConstructor(String.class, String.class).newInstance("textures", textureValue));

            Field profileField = SM.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);

            profileField.set(SM, profile);
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * @author meiskam
     */
    public enum CustomSkullType {

        SPIDER("MHF_Spider", "Kelevra_V"), // Thanks Marc Watson
        ENDERMAN("MHF_Enderman", "Violit"), // Thanks Marc Watson
        BLAZE("MHF_Blaze", "Blaze_Head"), // Thanks Marc Watson
        HORSE("gavertoso"), // Thanks Glompalici0us
        SQUID("MHF_Squid", "squidette8"), // Thanks Marc Watson
        SILVERFISH("Xzomag", "AlexVMiner"), // Thanks XlexerX
        ENDER_DRAGON("KingEndermen", "KingEnderman"), // Thanks SethBling
        SLIME("HappyHappyMan", "Ex_PS3Zocker"), // Thanks SethBling
        IRON_GOLEM("MHF_Golem", "zippie007"), // Thanks Marc Watson
        MUSHROOM_COW("MHF_MushroomCow", "Mooshroom_Stew"), // Thanks Marc Watson
        BAT("bozzobrain", "coolwhip101"), // Thanks incraftion.com
        PIG_ZOMBIE("MHF_PigZombie", "ManBearPigZombie", "scraftbrothers5"), // Thanks Marc Watson
        SNOWMAN("Koebasti", "scraftbrothers2"), // Thanks MrLeikermoser
        GHAST("MHF_Ghast", "_QuBra_", "blaiden"), // Thanks Marc Watson
        PIG("MHF_Pig", "XlexerX", "scrafbrothers7"), // Thanks Marc Watson
        VILLAGER("MHF_Villager", "Kuvase", "Villager", "scraftbrothers9"), // Thanks Marc Watson
        SHEEP("MHF_Sheep", "SGT_KICYORASS", "Eagle_Peak"), // Thanks Marc Watson
        COW("MHF_Cow", "VerifiedBernard", "CarlosTheCow"), // Thanks Marc Watson
        CHICKEN("MHF_Chicken", "scraftbrothers1"), // Thanks Marc Watson
        OCELOT("MHF_Ocelot", "scraftbrothers3"), // Thanks Marc Watson
        WITCH("scrafbrothers4"), // Thanks SuperCraftBrothers.com
        MAGMA_CUBE("MHF_LavaSlime"), // Thanks Marc Watson
        WOLF("Pablo_Penguin", "Budwolf"), // I still need an official wolf head if anyone wants to provide one
        CAVE_SPIDER("MHF_CaveSpider"), // Thanks Marc Watson
        RABBIT("rabbit2077"), // Thanks IrParadox
        GUARDIAN("Guardian", "creepypig7", "Creepypig7"), // Thanks lee3kfc
        CREEPER("MHF_Creeper"),
        SKELETON("MHF_Skeleton"),
        ZOMBIE("MHF_Zombie"),
        ARROW_UP("MHF_ArrowUp"),
        ARROW_DOWN("MHF_ArrowDown"),
        ARROW_LEFT("MHF_ArrowLeft"),
        ARROW_RIGHT("MHF_ArrowRight"),
        ;

        private final String owner;

        private static class Holder {
            static HashMap<String, CustomSkullType> map = new HashMap<String, CustomSkullType>();
        }

        CustomSkullType(String owner) {
            this.owner = owner;
            Holder.map.put(owner, this);
        }

        CustomSkullType(String owner, String... toConvert) {
            this(owner);
    /*        for (String key : toConvert) {
                Holder.map.put(key, this);
            }*/
        }

        public String getOwner() {
            return owner;
        }

        public static CustomSkullType get(String owner) {
            return Holder.map.get(owner);
        }
    }

    public static class TestChild {

    }

    public static class Test extends ForwardingMultimap<String, TestChild> {

        @Override
        protected Multimap<String, TestChild> delegate() {
            // TODO Auto-generated method stub
            return null;
        }

    }
}
