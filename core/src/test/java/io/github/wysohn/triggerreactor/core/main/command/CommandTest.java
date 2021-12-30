package io.github.wysohn.triggerreactor.core.main.command;

import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CommandTest {
    @Test
    public void test() {
        ITriggerCommand command = CommandBuilder.begin("--- TriggerReactor ---")
                .composite("cmd", "/trg cmd ddd <name> [script]", ddd -> ddd.leaf("ddd",
                        "/trg cmd ddd <name> " + "[script]", (sender, args) -> {
                            String name = args.poll();
                            if (name == null)
                                return false;

                            StringBuilder script = new StringBuilder();
                            while (!args.isEmpty()) {
                                script.append(args.poll());
                                script.append(' ');
                            }

                            sender.sendMessage(script.toString());
                            return true;
                        }))
                .build();

        ICommandSender sender = mock(ICommandSender.class);
        Queue<String> args = ITriggerCommand.toQueue("cmd ddd myCommand #MESSAGE 1 + 3 + 4;");

        assertTrue(command.onCommand(sender, args));
        verify(sender).sendMessage("#MESSAGE 1 + 3 + 4; ");
    }

    @Test
    public void testAlias() {
        ITriggerCommand command = CommandBuilder.begin("--- TriggerReactor ---")
                .composite(new String[]{"cmd", "c"}, "/trg cmd ddd <name> [script]", ddd -> ddd.leaf("ddd",
                        "/trg cmd ddd " + "<name> [script]", (sender, args) -> {
                            String name = args.poll();
                            if (name == null)
                                return false;

                            StringBuilder script = new StringBuilder();
                            while (!args.isEmpty()) {
                                script.append(args.poll());
                                script.append(' ');
                            }

                            sender.sendMessage(script.toString());
                            return true;
                        }))
                .build();

        ICommandSender sender = mock(ICommandSender.class);

        Queue<String> args = ITriggerCommand.toQueue("cmd ddd myCommand #MESSAGE 1 + 3 + 4;");
        assertTrue(command.onCommand(sender, args));
        args = ITriggerCommand.toQueue("c ddd myCommand #MESSAGE 1 + 3 + 4;");
        assertTrue(command.onCommand(sender, args));

        verify(sender, times(2)).sendMessage("#MESSAGE 1 + 3 + 4; ");
    }

    @Test
    public void testTab() {
        ITriggerCommand command = CommandBuilder.begin("--- TriggerReactor ---")
                .composite(new String[]{"cmd", "c"}, "/trg cmd", ddd -> {
                    ddd.leaf(new String[]{"create", "c"}, "/trg cmd create " + "<name> [script]", (sender, args) -> {
                        return true;
                    }).leaf("ccc", "/trg cmd ccc <name> [script]", (sender, args) -> {
                        return true;
                    }).leaf("cdcd", "/trg cdcd create <name> [script]", (sender, args) -> {
                        return true;
                    }).leaf(new String[]{"delete", "d"}, "/trg cmd delete <name> [script]", (sender, args) -> {
                        return true;
                    });
                })
                .build();

        List<String> args = ITriggerCommand.toList("c");
        List<String> expected = ITriggerCommand.toList("c", "cmd");

        assertEquals(expected, command.onTab(args.listIterator()));
    }

    @Test
    public void testTab2() {
        ITriggerCommand command = CommandBuilder.begin("--- TriggerReactor ---")
                .composite(new String[]{"cmd", "c"}, "/trg cmd", ddd -> {
                    ddd.leaf(new String[]{"create", "c"}, "/trg cmd create " + "<name> [script]", (sender, args) -> {
                        return true;
                    }).leaf("ccc", "/trg cmd ccc <name> [script]", (sender, args) -> {
                        return true;
                    }).leaf("cdcd", "/trg cdcd create <name> [script]", (sender, args) -> {
                        return true;
                    }).leaf(new String[]{"delete", "d"}, "/trg cmd delete <name> [script]", (sender, args) -> {
                        return true;
                    });
                })
                .build();

        List<String> args = ITriggerCommand.toList("c", "c");
        List<String> expected = ITriggerCommand.toList("c", "create", "ccc", "cdcd");
        assertEquals(expected, command.onTab(args.listIterator()));
    }

    @Test
    public void testTab3() {
        ITriggerCommand command = CommandBuilder.begin("--- TriggerReactor ---")
                .composite(new String[]{"cmd", "c"}, "/trg cmd", ddd -> {
                    ddd.leaf(new String[]{"create", "c"}, "/trg cmd create " + "<name> [script]", (sender, args) -> {
                        return true;
                    }).leaf("ccc", "/trg cmd ccc <name> [script]", (sender, args) -> {
                        return true;
                    }).leaf("cdcd", "/trg cdcd create <name> [script]", (sender, args) -> {
                        return true;
                    }).leaf(new String[]{"delete", "d"}, "/trg cmd delete <name> [script]", (sender, args) -> {
                        return true;
                    });
                })
                .build();

        List<String> args = ITriggerCommand.toList("cmd", "d");
        List<String> expected = ITriggerCommand.toList("d", "delete");
        assertEquals(expected, command.onTab(args.listIterator()));
    }

    @Test
    public void testTab4() {
        ITriggerCommand command = CommandBuilder.begin("--- TriggerReactor ---")
                .composite("item", "/trg item", builder -> {
                    builder.composite("lore", "/trg item lore", inner -> {
                        inner.leaf("add", "/trg cmd create " + "<name> [script]", (sender, args) -> {
                            return true;
                        }).leaf("abab", "/trg cmd create " + "<name> [script]", (sender, args) -> {
                            return true;
                        }).leaf("abac", "/trg cmd create " + "<name> [script]", (sender, args) -> {
                            return true;
                        }).leaf("delete", "/trg cmd create " + "<name> [script]", (sender, args) -> {
                            return true;
                        }).leaf("edit", "/trg cmd create " + "<name> [script]", (sender, args) -> {
                            return true;
                        });
                    });
                })
                .build();

        List<String> args = ITriggerCommand.toList("i");
        List<String> expected = ITriggerCommand.toList("item");
        assertEquals(expected, command.onTab(args.listIterator()));
    }

    @Test
    public void testTab5() {
        ITriggerCommand command = CommandBuilder.begin("--- TriggerReactor ---")
                .composite("item", "/trg item", builder -> {
                    builder.composite("lore", "/trg item lore", inner -> {
                        inner.leaf("add", "/trg cmd create " + "<name> [script]", (sender, args) -> {
                            return true;
                        }).leaf("abab", "/trg cmd create " + "<name> [script]", (sender, args) -> {
                            return true;
                        }).leaf("abac", "/trg cmd create " + "<name> [script]", (sender, args) -> {
                            return true;
                        }).leaf("delete", "/trg cmd create " + "<name> [script]", (sender, args) -> {
                            return true;
                        }).leaf("edit", "/trg cmd create " + "<name> [script]", (sender, args) -> {
                            return true;
                        });
                    });
                })
                .build();

        List<String> args = ITriggerCommand.toList("item");
        List<String> expected = ITriggerCommand.toList("item");
        assertEquals(expected, command.onTab(args.listIterator()));
    }

    @Test
    public void testTab6() {
        ITriggerCommand command = CommandBuilder.begin("--- TriggerReactor ---")
                .composite("item", "/trg item", builder -> {
                    builder.composite("lore", "/trg item lore", inner -> {
                        inner.leaf("add", "/trg cmd create " + "<name> [script]", (sender, args) -> {
                            return true;
                        }).leaf("abab", "/trg cmd create " + "<name> [script]", (sender, args) -> {
                            return true;
                        }).leaf("abac", "/trg cmd create " + "<name> [script]", (sender, args) -> {
                            return true;
                        }).leaf("delete", "/trg cmd create " + "<name> [script]", (sender, args) -> {
                            return true;
                        }).leaf("edit", "/trg cmd create " + "<name> [script]", (sender, args) -> {
                            return true;
                        });
                    });
                })
                .build();

        List<String> args = ITriggerCommand.toList("item", "l");
        List<String> expected = ITriggerCommand.toList("lore");
        assertEquals(expected, command.onTab(args.listIterator()));
    }

    @Test
    public void testTab7() {
        ITriggerCommand command = CommandBuilder.begin("--- TriggerReactor ---")
                .composite("item", "/trg item", builder -> {
                    builder.composite("lore", "/trg item lore", inner -> {
                        inner.leaf("add", "/trg cmd create " + "<name> [script]", (sender, args) -> {
                            return true;
                        }).leaf("abab", "/trg cmd create " + "<name> [script]", (sender, args) -> {
                            return true;
                        }).leaf("abac", "/trg cmd create " + "<name> [script]", (sender, args) -> {
                            return true;
                        }).leaf("delete", "/trg cmd create " + "<name> [script]", (sender, args) -> {
                            return true;
                        }).leaf("edit", "/trg cmd create " + "<name> [script]", (sender, args) -> {
                            return true;
                        });
                    });
                })
                .leaf("bah", "", (sender, args) -> {
                    return true;
                })
                .build();

        List<String> args = ITriggerCommand.toList("item", "lore");
        List<String> expected = ITriggerCommand.toList("lore");
        assertEquals(expected, command.onTab(args.listIterator()));
    }

    @Test
    public void testTab7_2() {
        ITriggerCommand command = CommandBuilder.begin("--- TriggerReactor ---")
                .composite("item", "/trg item", builder -> {
                    builder.composite("lore", "/trg item lore", inner -> {
                        inner.leaf("add", "/trg cmd create " + "<name> [script]", (sender, args) -> {
                            return true;
                        }).leaf("abab", "/trg cmd create " + "<name> [script]", (sender, args) -> {
                            return true;
                        }).leaf("abac", "/trg cmd create " + "<name> [script]", (sender, args) -> {
                            return true;
                        }).leaf("delete", "/trg cmd create " + "<name> [script]", (sender, args) -> {
                            return true;
                        }).leaf("edit", "/trg cmd create " + "<name> [script]", (sender, args) -> {
                            return true;
                        });
                    });
                })
                .composite("bah", "", builder -> {

                })
                .build();

        List<String> args = ITriggerCommand.toList("item");
        List<String> expected = ITriggerCommand.toList("item");
        assertEquals(expected, command.onTab(args.listIterator()));
    }

    @Test
    public void testTab7_3() {
        ITriggerCommand command = CommandBuilder.begin("--- TriggerReactor ---")
                .composite("item", "/trg item", builder -> {
                    builder.composite("lore", "/trg item lore", inner -> {
                        inner.leaf("add", "/trg cmd create " + "<name> [script]", (sender, args) -> {
                            return true;
                        }).leaf("abab", "/trg cmd create " + "<name> [script]", (sender, args) -> {
                            return true;
                        }).leaf("abac", "/trg cmd create " + "<name> [script]", (sender, args) -> {
                            return true;
                        }).leaf("delete", "/trg cmd create " + "<name> [script]", (sender, args) -> {
                            return true;
                        }).leaf("edit", "/trg cmd create " + "<name> [script]", (sender, args) -> {
                            return true;
                        });
                    });
                })
                .composite("bah", "", builder -> {

                })
                .build();

        List<String> args = ITriggerCommand.toList("bah", "foo", "raa");
        List<String> expected = new ArrayList<>();
        assertEquals(expected, command.onTab(args.listIterator()));
    }

    @Test
    public void testTab8() {
        ITriggerCommand command = CommandBuilder.begin("--- TriggerReactor ---")
                .composite("item", "/trg item", builder -> {
                    builder.composite("lore", "/trg item lore", inner -> {
                        inner.leaf("add", "/trg cmd create " + "<name> [script]", (sender, args) -> {
                            return true;
                        }).leaf("abab", "/trg cmd create " + "<name> [script]", (sender, args) -> {
                            return true;
                        }).leaf("abac", "/trg cmd create " + "<name> [script]", (sender, args) -> {
                            return true;
                        }).leaf("delete", "/trg cmd create " + "<name> [script]", (sender, args) -> {
                            return true;
                        }).leaf("edit", "/trg cmd create " + "<name> [script]", (sender, args) -> {
                            return true;
                        });
                    });
                })
                .build();

        List<String> args = ITriggerCommand.toList("item", "lore", "a");
        List<String> expected = ITriggerCommand.toList("add", "abab", "abac");
        assertEquals(expected, command.onTab(args.listIterator()));
    }

    @Test
    public void testTab9() {
        ITriggerCommand command = CommandBuilder.begin("--- TriggerReactor ---")
                .composite("item", "/trg item", builder -> {
                    builder.composite("lore", "/trg item lore", inner -> {
                        inner.leaf("add", "/trg cmd create " + "<name> [script]", (sender, args) -> {
                            return true;
                        }).leaf("abab", "/trg cmd create " + "<name> [script]", (sender, args) -> {
                            return true;
                        }).leaf("abac", "/trg cmd create " + "<name> [script]", (sender, args) -> {
                            return true;
                        }).leaf("delete", "/trg cmd create " + "<name> [script]", (sender, args) -> {
                            return true;
                        }).leaf("edit", "/trg cmd create " + "<name> [script]", (sender, args) -> {
                            return true;
                        });
                    });
                })
                .build();

        List<String> args = ITriggerCommand.toList("item", "lore", "ab");
        List<String> expected = ITriggerCommand.toList("abab", "abac");
        assertEquals(expected, command.onTab(args.listIterator()));
    }

    @Test
    public void testTab10() {
        ITriggerCommand command = CommandBuilder.begin("--- TriggerReactor ---")
                .composite("item", "/trg item", builder -> {
                    builder.composite("lore", "/trg item lore", inner -> {
                        inner.leaf("add", "/trg cmd create " + "<name> [script]", (sender, args) -> {
                            return true;
                        }).leaf("abab", "/trg cmd create " + "<name> [script]", (sender, args) -> {
                            return true;
                        }).leaf("abac", "/trg cmd create " + "<name> [script]", (sender, args) -> {
                            return true;
                        }).leaf("delete", "/trg cmd create " + "<name> [script]", (sender, args) -> {
                            return true;
                        }).leaf("edit", "/trg cmd create " + "<name> [script]", (sender, args) -> {
                            return true;
                        });
                    }).leaf("lore2", "", (sender, args) -> true);
                })
                .build();

        List<String> args = ITriggerCommand.toList("item", "lore2", "a", "b", "c");
        List<String> expected = new LinkedList<>();
        assertEquals(expected, command.onTab(args.listIterator()));
    }

    @Test
    public void testTab11() {
        ITriggerCommand command = CommandBuilder.begin("--- TriggerReactor ---")
                .composite("item", "/trg item", builder -> {
                    builder.composite("lore", "/trg item lore", inner -> {
                        inner.leaf("add", "/trg cmd create " + "<name> [script]", (sender, args) -> {
                            return true;
                        }).leaf("abab", "/trg cmd create " + "<name> [script]", (sender, args) -> {
                            return true;
                        }).leaf("abac", "/trg cmd create " + "<name> [script]", (sender, args) -> {
                            return true;
                        }).leaf("delete", "/trg cmd create " + "<name> [script]", (sender, args) -> {
                            return true;
                        }).leaf("edit", "/trg cmd create " + "<name> [script]", (sender, args) -> {
                            return true;
                        });
                    }).leaf("lore2", "", (sender, args) -> {
                        return true;
                    }).leaf("ggg", "", (sender, args) -> {
                        return true;
                    });
                })
                .build();

        List<String> args = ITriggerCommand.toList("item", "lo");
        List<String> expected = ITriggerCommand.toList("lore", "lore2");
        assertEquals(expected, command.onTab(args.listIterator()));
    }

    @Test
    public void testTab12() {
        ITriggerCommand command = CommandBuilder.begin("--- TriggerReactor ---")
                .composite("item", "/trg item", builder -> {
                    builder.composite("lore", "/trg item lore", inner -> {
                        inner.leaf("add", "/trg cmd create " + "<name> [script]", (sender, args) -> {
                            return true;
                        }).leaf("abab", "/trg cmd create " + "<name> [script]", (sender, args) -> {
                            return true;
                        }).leaf("abac", "/trg cmd create " + "<name> [script]", (sender, args) -> {
                            return true;
                        }).leaf("delete", "/trg cmd create " + "<name> [script]", (sender, args) -> {
                            return true;
                        }).leaf("edit", "/trg cmd create " + "<name> [script]", (sender, args) -> {
                            return true;
                        });
                    }).leaf("lore2", "", (sender, args) -> {
                        return true;
                    }).leaf("ggg", "", (sender, args) -> {
                        return true;
                    });
                })
                .build();

        List<String> args = new LinkedList<>();
        List<String> expected = new LinkedList<>();
        assertEquals(expected, command.onTab(args.listIterator()));
    }

    @Test
    public void testInvalidCommand1() {
        ITriggerCommand command = CommandBuilder.begin("--- TriggerReactor ---")
                .composite("cmd", "/trg cmd", ddd -> ddd.leaf("ddd", "/trg cmd ddd <name> [script]", (sender, args) -> {
                    String name = args.poll();
                    if (name == null)
                        return false;

                    StringBuilder script = new StringBuilder();
                    while (!args.isEmpty()) {
                        script.append(args.poll());
                        script.append(' ');
                    }

                    sender.sendMessage(script.toString());
                    return true;
                }))
                .leaf("test", "/trg test", (sender, strings) -> true)
                .build();

        ICommandSender sender = mock(ICommandSender.class);
        Queue<String> args = ITriggerCommand.toQueue("cmdd myCommand #MESSAGE 1 + 3 + 4;");

        assertFalse(command.onCommand(sender, args));
        verify(sender).sendMessage("--- TriggerReactor ---");
        verify(sender).sendMessage("  /trg cmd");
        verify(sender).sendMessage("  /trg test");
    }

    @Test
    public void testInvalidCommand2() {
        ITriggerCommand command = CommandBuilder.begin("--- TriggerReactor ---").composite("cmd", "/trg cmd", cmd -> {
            cmd.leaf("ddd", "/trg cmd ddd <name> [script]", (sender, args) -> {
                String name = args.poll();
                if (name == null)
                    return false;

                StringBuilder script = new StringBuilder();
                while (!args.isEmpty()) {
                    script.append(args.poll());
                    script.append(' ');
                }

                sender.sendMessage(script.toString());
                return true;
            }).leaf("eee", "/trg cmd eee hi usage", (sender, strings) -> true);
        }).build();

        ICommandSender sender = mock(ICommandSender.class);
        Queue<String> args = ITriggerCommand.toQueue("cmd fff myCommand #MESSAGE 1 + 3 + 4;");

        assertFalse(command.onCommand(sender, args));
        verify(sender).sendMessage("/trg cmd");
        verify(sender).sendMessage("  /trg cmd ddd <name> [script]");
        verify(sender).sendMessage("  /trg cmd eee hi usage");
    }
}