package org.betoncraft.betonquest.events;

import org.betoncraft.betonquest.BetonQuest;
import org.betoncraft.betonquest.Instruction;
import org.betoncraft.betonquest.api.QuestEvent;
import org.betoncraft.betonquest.config.Config;
import org.betoncraft.betonquest.exceptions.InstructionParseException;
import org.betoncraft.betonquest.exceptions.QuestRuntimeException;

/**
 * Changes player's language.
 */
@SuppressWarnings("PMD.CommentRequired")
public class LanguageEvent extends QuestEvent {

    private final String lang;

    public LanguageEvent(final Instruction instruction) throws InstructionParseException {
        super(instruction, false);
        lang = instruction.next();
        if (!Config.getLanguages().contains(lang)) {
            throw new InstructionParseException("Language " + lang + " does not exists");
        }
    }

    @Override
    protected Void execute(final String playerID) throws QuestRuntimeException {
        BetonQuest.getInstance().getPlayerData(playerID).setLanguage(lang);
        return null;
    }

}
