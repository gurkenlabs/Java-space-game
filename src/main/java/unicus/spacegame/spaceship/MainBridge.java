package unicus.spacegame.spaceship;

import unicus.spacegame.crew.*;

public class MainBridge extends SpecialModule implements Workplace {
    public static final int CAPTAIN_JOB_KEY = 100;
    public static final int BRIDGE_JOB_KEY  = 101;

    public CaptainJob captainJob;
    public BridgeDuty bridgeDuty;

    protected MainBridge() {
        super(new ShipLoc(0,1));
        captainJob = new CaptainJob();
        bridgeDuty = new BridgeDuty();
    }

    @Override
    public String GetName() {
        return "(STUB) Main bridge";
    }

    /**
     * Returns a list of all jobs that depends on this object being active.
     * Should this object be removed, these jobs must also be removed.
     *
     * @return KeyID of the job(s) dependent on this object.
     */
    @Override
    public int[] getDependentJobs() {
        return new int[]{CAPTAIN_JOB_KEY, BRIDGE_JOB_KEY};
    }

    /**
     * Returns a list the job(s) associated with this object.
     * This is meant as a tool for player interface.
     * Should include all dependent jobs.
     *
     * @return
     */
    @Override
    public int[] getAllJobs() {
        return new int[]{CAPTAIN_JOB_KEY, BRIDGE_JOB_KEY};
    }

    class CaptainJob extends AbstractJob{
        protected CaptainJob() {
            super(CAPTAIN_JOB_KEY, 1);
        }

        /**
         * Gets the amount of workload to be put on assigned crewmembers this month.
         *
         * @return A value of workload pressure.
         */
        @Override
        public double getMonthlyWorkload() {
            //TODO: scale workload with number of living crewmen
            return 1000;
        }

        /**
         * Calculates a base efficiency for how well a crewman will do this job.
         * Used in UI to show percentage efficiency.
         * Note: implementation should include the result from {@link AbleCrewman#getGeneralWorkModifier()},
         * unless implementation has an alternative.
         *
         * @param crewID The ID of the crewman
         * @return The base efficiency of the crewman, where 1.0 equals 100%.
         */
        @Override
        public double getWorkModifierOfCrewman(int crewID) {
            return 1.0;
        }

        @Override
        public void endOfMonth() {
            super.endOfMonth();
            //TODO: Calculate consequences for captain job
        }

    }

    class BridgeDuty extends AbstractJob{
        protected BridgeDuty() {
            super(BRIDGE_JOB_KEY, 12);
        }

        /**
         * Gets the amount of workload to be put on assigned crewmembers this month.
         *
         * @return A value of workload pressure.
         */
        @Override
        public double getMonthlyWorkload() {
            int numShifts = getNumShifts();

            switch (numShifts) {
                case 1:
                    return 6000;
                case 2:
                    return 8000;
                //case 3:
                //    return 10000;
                //case 4:
                //    return 10000;
                default:
                    return 10000;
            }
        }

        /**
         * Calculates a base efficiency for how well a crewman will do this job.
         * Used in UI to show percentage efficiency.
         * Note: implementation should include the result from {@link AbleCrewman#getGeneralWorkModifier()},
         * unless implementation has an alternative.
         *
         * @param crewID The ID of the crewman
         * @return The base efficiency of the crewman, where 1.0 equals 100%.
         */
        @Override
        public double getWorkModifierOfCrewman(int crewID) {
            return 1.0;
        }
    }

    private int getNumShifts() {
        int numAssignments =  SpaceCrew.SC().getJobAssignmentsByJob(BRIDGE_JOB_KEY).length;

        if (numAssignments < 3)
            return 1;
        if(numAssignments < 9)
            return 2;
        if(numAssignments < 12)
            return 3;
        else
            return 4;
    }
}
