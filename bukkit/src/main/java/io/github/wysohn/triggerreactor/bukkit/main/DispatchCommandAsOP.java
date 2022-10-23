package io.github.wysohn.triggerreactor.bukkit.main;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.Callable;

class DispatchCommandAsOP implements Callable<Void> {
        private final Player player;
        private final String cmd;

        private boolean wasOp;

        DispatchCommandAsOP(Player player, String cmd) {
            super();
            this.player = player;
            this.cmd = cmd;
        }

        void deOpIfWasNotOp() {
            if (!wasOp)
                player.setOp(false);
        }

        @Override
        public Void call() throws Exception {
            wasOp = player.isOp();

            try {
                player.setOp(true);

                Bukkit.dispatchCommand(player, cmd); // now we register CommandTrigger to commandMap
            } catch (Exception e) {

            } finally {
                deOpIfWasNotOp();
            }
            return null;
        }

    }