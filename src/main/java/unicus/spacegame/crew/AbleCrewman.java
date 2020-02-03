package unicus.spacegame.crew;
/*
Note that some features of the AdultCrewman may be moved to a new superclass
 or interfaces as other implementations of AbstractCrewman are written.
 */

import java.util.Random;

/**
 * A typical crewman, able to take on jobs onboard the ship.
 */
abstract public class AbleCrewman extends AbstractCrewman {

    //Maximum obtainable level in a skill.
    private static final int SKILL_CAP = 100;

    //Randomizer constants for crewman skills
    private static final int MIN_SKILL = 5;
    private static final int MAX_SKILL = 60;
    private static final int RAND_SKILL = MAX_SKILL - MIN_SKILL;

    //Maximum obtainable base intelligence (traits may increase final intelligence)
    private static final double BASE_INT_CAP = 100;

    //randomizer constants for crewman intelligence
    private static final int MIN_BASE_INT = 5;
    private static final int MAX_BASE_INT = 90;
    private static final int RAND_INT = MAX_BASE_INT - MIN_BASE_INT;

    //Maximum obtainable base morale
    private static final double BASE_MORALE_CAP = 10000; //10'000

    //randomizer obtainable base morale
    private static final int MIN_BASE_MORALE = -1000; //-1'000
    private static final int MAX_BASE_MORALE = 5000;  //5'000
    private static final int RAND_MORALE = MAX_BASE_MORALE - MIN_BASE_MORALE;
    //Note: yes, base morale can be as bad as -1000. This represents a rather indolent crewman (might replace with trait)
    //Note: current plan is for UI to display morale divided by 1'000

    //much much monthly rest or overwork will add or reduce stress.
    private static final double MORALE_TO_STRESS_RATIO = 0.01;


    //Stress-level where a stress-related crisis event will trigger.
    private static final double CRISIS_TRIGGER_STRESS = 100.0;

    //Skill values for the crewman.
    private int[] skillValues;

    //The accumulated stress of this crewman.
    double stress;

    //Base value intelligence for the crewman.
    protected double base_intelligence;

    //base value morale of a crewman.
    public double base_morale;

    /**
     * Gets the intelligence of the crewman, including traits modifying it.
     * Intelligence is used to calculate the chance of gaining a bonus skill-points when learning.
     * An intelligence level above 100 is guaranteed at least one bonus skill point when learning.
     * Intelligence is also used to calculate a chance for a workplace accident.
     * A crewman with intelligence above 100 is unlikely to have an accident.
    */
    public double getIntelligence(){
        return base_intelligence; //todo: add or subtract value according to traits
    }

    /**
     * Inspired by the morale system in Oxygen Not Included, crewmen have a system of morale and stress.
     * A crewman with a negative morale will accumulate stress over time.
     * A crewman with positive morale will lose stress over time.
     * @return
     */
    public double getMorale() {return base_morale;} //todo: add and subtract morale based on workload, traits and amenities.

    public int getSkill(SkillTypes skillType) {
        int skillIndex = SkillTypes.GetIndexByType(skillType);
        if(skillIndex < 0 || skillIndex >= skillValues.length)
            throw new IllegalArgumentException("Skill " + skillType + " is not a valid skill. This is a bug. If you loaded from a save-file, please check the game version and the change logs for a change to skill-types.");
        return getSkill(skillIndex);
    }
    public int getSkill(int skillIndex) {
        if(skillIndex < 0 || skillIndex >= skillValues.length)
            throw new IllegalArgumentException("index " + skillIndex + " is not a valid skill index.");
        return skillValues[skillIndex]; //todo: add or subtract value according to traits
    }

    //Trains skill at index skillIndex by amount
    public void trainSkill(int skillIndex, int amount) {
        this.skillValues[skillIndex] += amount;
        if (this.skillValues[skillIndex] > SKILL_CAP) {
            this.skillValues[skillIndex] = SKILL_CAP;
        }
    }
    //Trains type skill by amount
    public void trainSkill(SkillTypes type, int amount) {
        trainSkill(SkillTypes.GetIndexByType(type), amount);
    }
    //Trains type skill by 1
    public void trainSkill(SkillTypes type) {
        trainSkill(type, 1);
    }

    public AbleCrewman(int keyID, CrewmanState state, int birthDate, long randomSeed, int[] parents) {
        //Note: see onRandomize.
        super(keyID, state, birthDate, randomSeed, parents);
        assert (state.isWorkAble());
    }

    public AbleCrewman(int keyID, CrewmanState state, int birthDate, CrewSelfID selfID, CrewmanGeneData geneData, int[] skillValues, double base_intelligence, double base_morale) {
        super(keyID, state, birthDate, selfID, geneData);
        assert (state.isWorkAble());
        this.skillValues = skillValues;
        this.base_intelligence = base_intelligence;
        this.base_morale = base_morale;
    }

    protected AbleCrewman(AbleCrewman crewman, CrewmanState state) {
        super(crewman, state);
        assert (state.isWorkAble());
        this.skillValues = crewman.skillValues;
        this.base_morale = crewman.base_morale;
        this.base_intelligence = crewman.base_morale;
    }
    protected AbleCrewman(AbstractCrewman crewman, CrewmanState state) {
        super(crewman, state);
        assert (state.isWorkAble());
        //TODO: calculate skills, morale and intelligence based on previous state.
        this.skillValues = new int[SkillTypes.values().length];
        this.base_morale = 1000;
        this.base_intelligence = 50;

    }

    @Override
    protected void onRandomize(Random r) {
        super.onRandomize(r);

        //randomizing crewman stats
        base_intelligence = r.nextInt(RAND_INT) + MIN_BASE_INT;
        base_morale = r.nextInt(RAND_MORALE) + MIN_BASE_MORALE;

        //Randomize each skill with a minimum of 5 and a maximum of 60.
        this.skillValues = new int[SkillTypes.values().length];
        for (int i = 0; i < skillValues.length; i++) {
            skillValues[i] = r.nextInt(RAND_SKILL) + MIN_SKILL;
        }
    }

    /**
     * (STUB) Gets modifiers to amount of work done by this crewman,
     * according to general traits and situation affecting most jobs.
     *
     * @return An added modifier of efficiency for the crewman's work.
     */
    public double getGeneralWorkModifier() {
        return 0.0;
    }

    //stress gained or lost at the end of month
    protected double monthStressChange = 0;
    protected double monthWorkload = 0;
    protected double monthRest = 0;
    //jobs assigned to the crewman (including month result)
    protected JobAssignment[] monthJobAssignments = new JobAssignment[0];

    /**
     * Called last at the end of month cycle.
     * planned feature:
     *         1. Consume resources based on crewman needs (rations?).
     *         2. Apply change to stress
     *             1. Applied according to morale-workload bonus and traits
     *         3. If stress is high, or experiences lack of resources
     *             1. Chance to trigger crewman illness or crewman stress event
     *                 1. These events may result in negative traits, or damage to other crew or the homeship.
     *         4. Chance to trigger crewman aging-event.
     *             1. This is a minor event, where a crewman advances from one state to another.
     *             2. In case of senior crewman, this typically mean death by old age.
     */
    @Override
    protected void endOfMonth() {
        monthRest = 0;
        monthWorkload = 0;
        monthStressChange = 0;
        monthJobAssignments = SpaceCrew.getInstance().getJobAssignmentsByCrewman(keyID);
        for (JobAssignment ja: monthJobAssignments ) {
            monthWorkload += ja.getMonthWorkloadShare();
        }

        //TODO: add ship amenities to monthRest.
        //TODO: add effects from traits to monthStressChange and monthRest.

        monthRest = base_morale;
        monthStressChange = (monthWorkload - monthRest) * MORALE_TO_STRESS_RATIO;

        stress += monthStressChange;
        if(false) { //TODO: if age/criteria in range of advancing CrewmanState (eg. adult crewman to senior crewman)
            //TODO: chance to trigger crewman aging event - force trigger event if at far end of age-range.
        }
        if(stress >= CRISIS_TRIGGER_STRESS) {
            //TODO: trigger crewman stress crisis event
        }
    }

    @Override
    public StringBuffer toString(StringBuffer text) {
        text = super.toString(text);
        text.append("Intelligence: ").append(getIntelligence()).append("\n");
        text.append("Stress: ").append(stress).append("\n");

        for (SkillTypes s : SkillTypes.values()) {
            text.append(s.toString()).append(": ").append(getSkill(s)).append("\n");
        }
        text.append("Status update last month:\n");
        text.append("Number of jobs: ").append(monthJobAssignments.length);
        text.append("Stress-change: ").append(monthStressChange);

        return text;
    }
}

