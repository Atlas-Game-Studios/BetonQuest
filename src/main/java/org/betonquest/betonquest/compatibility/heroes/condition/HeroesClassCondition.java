package org.betonquest.betonquest.compatibility.heroes.condition;

import com.herocraftonline.heroes.characters.CharacterManager;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.classes.HeroClass;
import com.herocraftonline.heroes.characters.classes.HeroClassManager;
import org.betonquest.betonquest.api.profile.OnlineProfile;
import org.betonquest.betonquest.api.quest.QuestException;
import org.betonquest.betonquest.api.quest.condition.online.OnlineCondition;
import org.betonquest.betonquest.compatibility.heroes.HeroesClassType;
import org.betonquest.betonquest.instruction.variable.VariableNumber;
import org.betonquest.betonquest.instruction.variable.VariableString;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Checks the class of the player and the level.
 */
public class HeroesClassCondition implements OnlineCondition {
    /**
     * The string to match any class.
     */
    private static final String ANY_CLASS = "any";

    /**
     * The {@link CharacterManager} of the Heroes plugin.
     */
    private final CharacterManager characterManager;

    /**
     * The {@link HeroClassManager} of the Heroes plugin.
     */
    private final HeroClassManager classManager;

    /**
     * If the {@link HeroClass} should be the primary class.
     */
    private final HeroesClassType classType;

    /**
     * The {@link VariableString} of the class.
     */
    private final VariableString heroClassVar;

    /**
     * The {@link VariableNumber} of the level of the class.
     */
    @Nullable
    private final VariableNumber levelVar;

    /**
     * Create a new Heroes Class Condition.
     *
     * @param characterManager The {@link CharacterManager} of the Heroes plugin.
     * @param classManager     The {@link HeroClassManager} of the Heroes plugin.
     * @param classType        The {@link HeroesClassType} of the class.
     * @param heroClassVar     The {@link VariableString} of the name of the class.
     * @param levelVar         The optional {@link VariableNumber} of the level of the class.
     */
    public HeroesClassCondition(final CharacterManager characterManager, final HeroClassManager classManager,
                                final HeroesClassType classType, final VariableString heroClassVar,
                                @Nullable final VariableNumber levelVar) {
        this.characterManager = characterManager;
        this.classManager = classManager;
        this.classType = classType;
        this.heroClassVar = heroClassVar;
        this.levelVar = levelVar;
    }

    @Nullable
    private HeroClass getHeroClass(final String heroClassName) throws QuestException {
        if (ANY_CLASS.equalsIgnoreCase(heroClassName)) {
            return null;
        }
        final HeroClass heroClass = classManager.getClass(heroClassName);
        if (heroClass == null) {
            throw new QuestException("Class '" + heroClassName + "' does not exist");
        }
        return heroClass;
    }

    private List<HeroClass> getHeroClasses(final Hero hero) {
        final List<HeroClass> heroClasses = new ArrayList<>();
        if (classType.equals(HeroesClassType.MASTERED)) {
            hero.getMasteredClasses().stream()
                    .map(classManager::getClass)
                    .forEach(heroClasses::add);
        } else {
            heroClasses.add(classType.equals(HeroesClassType.PRIMARY) ? hero.getHeroClass() : hero.getSecondaryClass());
        }
        return heroClasses;
    }

    @Override
    public boolean check(final OnlineProfile profile) throws QuestException {
        boolean any = false;
        final String heroClassName = heroClassVar.getValue(profile);
        final HeroClass heroClass = getHeroClass(heroClassName);
        if (heroClass == null) {
            any = true;
        }

        final Hero hero = characterManager.getHero(profile.getPlayer());
        if (hero == null) {
            return false;
        }

        final List<HeroClass> heroClasses = getHeroClasses(hero);

        final int playerLevel = levelVar == null ? -1 : levelVar.getValue(profile).intValue();
        if (any) {
            return hero.getHeroLevel() >= playerLevel;
        }
        if (playerLevel > 0) {
            return heroClasses.contains(heroClass) && hero.getHeroLevel(heroClass) >= playerLevel;
        }
        return heroClasses.contains(heroClass);
    }
}
