package ninja.leaping.liftplates.util;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextBuilder;
import org.spongepowered.api.text.format.TextColors;

/**
 * Formatting utilities for text output
 */
public class MessageFormatting {
    private MessageFormatting() {}
    public static Text normal(Text input) {
        return normal(input.builder());
    }

    public static Text normal(TextBuilder input) {
        return input.color(TextColors.BLUE).build();
    }

}
