package org.betonquest.betonquest.quest.event.tag;

import org.betonquest.betonquest.api.quest.QuestException;
import org.betonquest.betonquest.database.TagData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

/**
 * Test {@link DeleteTagChanger}.
 */
@ExtendWith(MockitoExtension.class)
class DeleteTagChangerTest extends TagFixture {
    @Test
    void testDeleteTagChangerRemoveNoTags(@Mock final TagData tagData) throws QuestException {
        final DeleteTagChanger changer = new DeleteTagChanger(List.of());

        changer.changeTags(tagData, null);
        verifyNoInteractions(tagData);
    }

    @SuppressWarnings("PMD.UnitTestContainsTooManyAsserts")
    @Test
    void testDeleteTagChangerRemoveMultipleTags(@Mock final TagData tagData) throws QuestException {
        final DeleteTagChanger changer = new DeleteTagChanger(createIdList("tag-1", "tag-2", "tag-3"));

        changer.changeTags(tagData, null);
        verify(tagData).removeTag("tag-1");
        verify(tagData).removeTag("tag-2");
        verify(tagData).removeTag("tag-3");
    }
}
