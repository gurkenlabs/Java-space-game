package unicus.spacegame.crew;

public class ChildCrewman extends AbleCrewman {
    public ChildCrewman(int keyID, int birthDate, long randomSeed, int[] parents) {
        super(keyID, CrewmanState.child, birthDate, randomSeed, parents);
    }

    public ChildCrewman(int keyID, int birthDate, CrewSelfID selfID, CrewmanGeneData geneData, int[] skillValues, double base_intelligence, double base_morale) {
        super(keyID, CrewmanState.child, birthDate, selfID, geneData, skillValues, base_intelligence, base_morale);
    }

    protected ChildCrewman(AbleCrewman crewman) {
        super(crewman, CrewmanState.child);
    }

    protected ChildCrewman(AbstractCrewman crewman) {
        super(crewman, CrewmanState.child);
    }
}
