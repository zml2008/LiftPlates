package com.zachsthings.liftplates.commands;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author zml2008
 */
public class ChildrenCommandExecutorTest {
    @Test
    public void testChooseChild() {
        ChildrenCommandExecutor executor = new ChildrenCommandExecutor() {
            @Override
            public boolean execute(CommandSender sender, Command command, String[] arguments) throws CommandException {
                return false;
            }
        };
        CheckCalledExecutor choiceA = new CheckCalledExecutor();
        CheckCalledExecutor choiceB = new CheckCalledExecutor();
        executor.addChild("first", choiceA);
        executor.addChild("second", choiceB);

        executor.onCommand(null, null, "tada", new String[] {"first", "here", "lala"});
        assertEquals(1, choiceA.getCallCount());
        assertFalse(choiceB.hasBeenCalled());

        executor.onCommand(null, null, "tada", new String[] {"second"});
        assertTrue(choiceB.hasBeenCalled());
        assertEquals(1, choiceB.getCallCount());
    }

    @Test(expected = CommandException.class)
    public void testChildRequired() {
        CheckCalledExecutor executor = new CheckCalledExecutor();
        executor.onCommand(null, null, "child-not-required", ArrayUtils.EMPTY_STRING_ARRAY);
        assertEquals(1, executor.getCallCount());

        executor.addChild("child", new CheckCalledExecutor());
        CommandSender fake = mock(CommandSender.class);
        doThrow(CommandException.class).when(fake).sendMessage(any(String.class));

        executor.onCommand(fake, null, "child-required", ArrayUtils.EMPTY_STRING_ARRAY);

    }

    public static class CheckCalledExecutor extends ChildrenCommandExecutor {
        private int callCount;

        @Override
        public boolean execute(CommandSender sender, Command command, String[] arguments) throws CommandException {
            ++callCount;
            return true;
        }

        public boolean hasBeenCalled() {
            return callCount > 0;
        }

        public int getCallCount() {
            return callCount;
        }
    }
}
