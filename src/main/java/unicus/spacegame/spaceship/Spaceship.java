package unicus.spacegame.spaceship;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * The datastructure representing a spaceship.
 * The spaceship has a length of sections (at least 2).
 * Each section has a number of modules, depending on the SectionType.
 */
public class Spaceship {
    public int length;
    //lists the type of sections currently installed. 0 is near bridge, other end near engineering.
    public SectionType[] sectionTypes;
    public ShipModule[][] modules;
    public ShipWeapon[][] weaponTypes;

    /**
     * Module location inner class.
     * Used to store a location of a module or a section.
     *
     */
    public class ModuleLoc {
        //section, module
        int s, m;
        public boolean isValidSection() {return s < 0 || s > sectionTypes.length;}
        public boolean isValidModule() {
            if (!isValidSection())
                return false;
            if (m < 0)
                return false;
            return  m < sectionTypes[s].getNumModules();
        }
        public ModuleLoc(int s, int m){
            this.s = s; this.m = m;
        }
        public ShipModule getModule() {
            if(isValidModule())
                return modules[s][m];
            return null;
        }
        public SectionType getSection() {
            if (isValidSection())
                return sectionTypes[s];
            return null;
        }
        public ModuleLoc[] getModuleLocList() {
            if(!isValidSection())
                return null;
            int len = getSection().getNumModules();
            ModuleLoc[] ret = new ModuleLoc[len];
            for (int i = 0; i < len; i++) {
                ret[i] = new ModuleLoc(s, i);
            }
            return ret;
        }

        @Override
        public boolean equals(Object obj) {
            if (! (obj instanceof ModuleLoc))
                return false;
            ModuleLoc other = (ModuleLoc)obj;

            return other.s == s && other.m == m;
        }
    }


    /**
     * Creates a length long spaceship, naked down to the framework.
     * Not meant to be used directly. Use one of the Generate functions instead.
     * @param length
     */
    public Spaceship(int length)
    {
        this.length = length;
        sectionTypes = new SectionType[length];
        modules = new ShipModule[length][0];
        for (int i = 0; i < length; i++)
        {
            //None-type sections
            sectionTypes[i] = SectionType.None;
            modules[i] = new ShipModule[0];
        }
    }

    /**
     * Replaces a section of the spaceship.
     * Warning: this WILL replace the existing modules in it with empty modules, without asking!
     * Planned: function to check if replacing a section should be allowed (by gameplay rules)
     *
     * @param index section index of module to replace.
     * @param sectionType The new section type.
     */
    private void forceBuildSection(int index, SectionType sectionType)
    {
        // ( index >= 0 && index < length);
        int sLength = sectionType.getNumModules();
        sectionTypes[index] = sectionType;
        modules[index] = new ShipModule[sLength];
        for(int i = 0; i < sLength; i++){
            modules[index][i] = new ShipModule(sectionType);
        }
    }

    /**
     * Replaces a module of the spaceship.
     * Warning: this WILL replace the existing module, without asking!
     * Planned: function to check if replacing a module should be allowed (by gameplay rules)
     *
     * @param sIndex The section index
     * @param mIndex The module index (of section)
     * @param mType The new module type.
     */
    private void forceBuildModule(int sIndex, int mIndex, ModuleType mType){
        modules[sIndex][mIndex] = new ShipModule(sectionTypes[sIndex], mType);
    }

    /**
     * Generates a new spaceship, with adjustable range of specification.
     * @param rand The instance of Random to use.
     * @param minLength minimal length of the ship
     * @param maxLength maximum length of the ship
     * @param minFull minimal cargo to spawn with (range 0, 1)
     * @param maxFull maximum cargo to spawn with (range 0, 1)
     * @return A Spaceship
     */
    static public Spaceship GenerateStart1(Random rand, int minLength, int maxLength, float minFull, float maxFull){
        int length = rand.nextInt(maxLength - minLength) + minLength;
        float fullRange = maxFull - minFull;
        float full = rand.nextFloat() * fullRange + minFull;
        return GenerateStart1(rand, length, full);
    }

    /**
     * Generates a new spaceship, with some fixed specification
     * @param rand The instance of Random to use.
     * @param length The length of the Spaceship
     * @param full How much of the potential space will be filled with cargo (range 0, 1)
     * @return A Spaceship
     */
    private static Spaceship GenerateStart1(Random rand, int length, float full){
        //length MUST be at least 2.
        if (length < 2)
            length = 2;

        Spaceship ship = new Spaceship(length);
        //center of the wheel section hosts the first hab module
        int habstart = SectionType.Wheel.getNumModules() / 2;
        ship.forceBuildSection(0, SectionType.Wheel);
        ship.forceBuildModule(0, habstart, ModuleType.Habitat);


        int normSize = SectionType.Normal.getNumModules();
        int totCargoSpace = (normSize * (length-1));
        int usedCargoSpace = 0;
        int targetFilled = Math.round( (float)totCargoSpace * full);

        System.out.println("Total cargo space: " + totCargoSpace + ", target cargo: " + targetFilled);

        for(int i = 1; i < length; i++){
            //If none of the modules are used, can targetFilled still be reached?
            boolean canBeEmpty = (usedCargoSpace + normSize * (length - i - 1)) > targetFilled;
            float sectionEmptyChance = rand.nextFloat();
            System.out.println("Chance section is empty: " + sectionEmptyChance + " can be empty: " + canBeEmpty);
            if(canBeEmpty && sectionEmptyChance < 0.3f){
                ship.forceBuildSection(i, SectionType.None);
            } else {
                ship.forceBuildSection(i, SectionType.Normal);
                for(int j = 0; j < normSize; j++)
                {
                    ModuleType type;
                    float moduleEmptyChance = rand.nextFloat();
                    System.out.println("Chance module is empty: " + moduleEmptyChance + " can be empty: " + canBeEmpty);
                    if( usedCargoSpace >= targetFilled || (canBeEmpty && moduleEmptyChance < 0.6f)){
                        type = ModuleType.Empty;
                    } else {
                        type = ModuleType.Cargo;
                        usedCargoSpace++;
                    }
                    ship.forceBuildModule(i, j, type);
                }
            }



        }
        return ship;
    }

    public ArrayList<Integer> GetBuildableModules(Point loc) {
        ArrayList<Integer> list = new ArrayList<Integer>();
        if(loc.y < 0 || loc.x < 0)
            return list;
        else {
            SectionType sectionType = sectionTypes[loc.x];
            ShipModule module = modules[loc.x][loc.y];
            ModuleType[] mTypes = ModuleType.values();

            for (int i = 0; i < mTypes.length; i++)
            {
                if(mTypes[i] == module.moduleType)
                    continue; //Do not include existing type.
                if(mTypes[i].getNeedGravity() && !sectionType.getHasGravity())
                    continue; //Do not include gravity modules for non-gravity section
                list.add(i);
            }
            return list;
        }

    }

    public ArrayList<Integer> GetBuildableSections(Point loc) {
        ArrayList<Integer> list = new ArrayList<Integer>();
        if(loc.x < 0)
            return list;
        SectionType sectionType = sectionTypes[loc.x];
        SectionType[] sTypes    = SectionType.values();
        for (int i = 0; i < sTypes.length; i++){
            if(sTypes[i] == sectionType)
                continue; //Do not include existing type.
            list.add(i);
        }
        return list;
    }

    public boolean canBuildSection(ModuleLoc moduleLoc, SectionType typeToBuild, StringBuffer message) {
        if (!moduleLoc.isValidSection()) {
            message.append("Illegal selection! How did you manage this? HOW!? (this is a bug, please report it)");
            return false;
        }
        if(moduleLoc.getSection() != SectionType.None) {
            message.append("Cannot build section-frame. You need to strip off the old one first.");
            return false;
        }

        //TODO: get material cost of construction.
        ArrayList<CargoPlaceholder> cost = new ArrayList<>();
        if(! CanAfford(cost)) {
            message.append("You cannot afford X resources :-(");
            return false;
        }
        message.append("You can build this section-frame. It will cost X resources.");
        return true;
    }

    public boolean canBuildModule(ModuleLoc moduleLoc, ModuleType typeToBuild, StringBuffer message) {
        if (moduleLoc.isValidModule()){
            message.append("Illegal selection! How did you manage this? HOW!? (this is a bug, please report it)");
            return false;
        }
        if(moduleLoc.getModule().moduleType != ModuleType.Empty) {

            message.insert(0, "The existing module must be removed.\n");
            if (canRemoveModule(moduleLoc, message)) {
                message.append("\n");
            }
            else {
                message.append("Module cannot be built.");
                return false;
            }
        }
        //TODO: check on all compatibility issues
        //If this type of module require gravity, but the section-frame lacks it.
        if(typeToBuild.getNeedGravity() && !moduleLoc.getSection().getHasGravity()) {
            message.append("Unable to build module: This module requires gravity, and this section-frame is weightless.");
            return false;
        }
        //TODO: get material cost of construction.
        ArrayList<CargoPlaceholder> cost = new ArrayList<>();
        if(! CanAfford(cost)) {
            message.append("You cannot afford X resources :-(");
            return false;
        }
        message.append("You can build this module. It will cost X resources.");
        return true;

    }
    public boolean canRemoveModule(ModuleLoc moduleLoc, StringBuffer message) {
        if (moduleLoc.isValidModule()){
            message.append("Illegal selection! How did you manage this? HOW!? (this is a bug, please report it)");
            return false;
        }

        ArrayList<ModuleLoc> lockedModules = getLockedModules();
        if(lockedModules.contains(moduleLoc)) {
            message.append("Your crew is already busy at work here.");
            return false;
        }

        ShipModule module = moduleLoc.getModule();
        if (module.moduleType == ModuleType.Empty) {
            message.append("There is no module to remove.");
            return false;
        }

        //placeholder cargo objects
        ArrayList<CargoPlaceholder> cargoToMove = new ArrayList<>();
        //placeholder crew housing assignment
        ArrayList<HousingPlaceholder> housingToMove = new ArrayList<>();

        if(module instanceof Habitat) {
            Habitat hModule = (Habitat) module;
            Collections.addAll(housingToMove, hModule.getHousingAssignments());
        }
        Collections.addAll(cargoToMove, module.getCargoOnDestruction());

        //Add this location to the locked modules.
        // This is used when checking if cargo,
        // recycled resources and displaced crew can be relocated.
        lockedModules.add(moduleLoc);

        int numCargo = cargoToMove.size(); //STUB - TODO: should report the total cargo units
        int numPeople = housingToMove.size();
        if( !checkCanHouseCrew(housingToMove, lockedModules)) {
            message.append("Cannot remove module. There is not enough crew-quarters to move all ");
            message.append(numPeople + " crewmen. Please construct more habitats.");
            return false;
        }
        if( !checkStoreCargo(cargoToMove, lockedModules)) {
            message.append("Cannot remove module. There is not enough space to store all ");
            message.append(numCargo + " cargo units");
            return false;
        }

        message.append("You can remove this module. You will move and reclaim x resources and displace x crew-members");
        return true;
    }
    public boolean canRemoveSection(ModuleLoc moduleLoc, StringBuffer message) {
        if (!moduleLoc.isValidSection()) {
            message.append("Illegal selection! How did you manage this? HOW!? (this is a bug, please report it)");
            return false;
        }

        ArrayList<ModuleLoc> lockedModules = getLockedModules();
        for (ModuleLoc l : lockedModules)
            if (l.s == moduleLoc.s) {
                message.append("Your crew is already busy working in this section. You cannot remove it.");
                return false;
            }

        if(moduleLoc.getSection() == SectionType.None) {
            message.append("This section is already stripped.");
            return false;
        }

        //placeholder cargo objects
        ArrayList<CargoPlaceholder> cargoToMove = new ArrayList<>();
        //placeholder crew housing assignment
        ArrayList<HousingPlaceholder> housingToMove = new ArrayList<>();

        //TODO: add resources stripped from section-frame to cargo.
        //TODO: add weapon-components dismantled to cargo.

        ModuleLoc[] sModules = moduleLoc.getModuleLocList();
        for (int i = 0, moduleLength = sModules.length; i < moduleLength; i++) {
            ShipModule m = sModules[i].getModule();
            Collections.addAll(cargoToMove, m.getCargoOnDestruction());
            if (m instanceof Habitat) {
                Habitat h = (Habitat) m;
                Collections.addAll(housingToMove, h.getHousingAssignments());
            }
        }

        //Adds this section's modules to the locked modules list.
        // This is used when checking if cargo,
        // recycled resources and displaced crew can be relocated.
        Collections.addAll(lockedModules, moduleLoc.getModuleLocList());

        int numCargo = cargoToMove.size(); //STUB - should report the total cargo units
        int numPeople = housingToMove.size();
        if( !checkCanHouseCrew(housingToMove, lockedModules)) {
            message.append("Cannot strip section. There is not enough crew-quarters to move all ");
            message.append(numPeople + " crewmen. Please construct more habitats.");
            return false;
        }
        if( !checkStoreCargo(cargoToMove, lockedModules)) {
            message.append("Cannot strip section. There is not enough space to store all ");
            message.append(numCargo + " cargo units");
            return false;
        }
        message.append("You can strip this section. This will dismantle all modules and weapons installed on it.");
        message.append("\nYou will strip and move a total of ");
        message.append(numCargo + " units of cargo");
        if (numPeople > 0)
            message.append("/n" + numPeople + " crewmen will have to be moved.");
        return true;
    }

    //region checkStore/re-house shortcuts
    private boolean checkCanHouseCrew(ArrayList<HousingPlaceholder> toMove) {
        return checkCanHouseCrew(toMove, new ArrayList<ModuleLoc>());
    }
    private boolean checkStoreCargo(ArrayList<CargoPlaceholder> toStore) {
        return checkStoreCargo(toStore, new ArrayList<ModuleLoc>());
    }
    //endregion

    // STUB. TODO: check if crew can be housed in available housing space (except in modules in the ignore list).
    private boolean checkCanHouseCrew(ArrayList<HousingPlaceholder> toMove, ArrayList<ModuleLoc> ignoreList) {
        return true;
    }
    // STUB. TODO: check if cargo can be stored in available space (except in modules in the ignore list).
    private boolean checkStoreCargo(ArrayList<CargoPlaceholder> toStore, ArrayList<ModuleLoc> ignoreList) {
        return true;
    }
    //STUB! Todo: check if player can afford the cost.
    private boolean CanAfford(ArrayList<CargoPlaceholder> cost) {
        return true;
    }
    
    public ArrayList<ModuleLoc> getLockedModules() {
        ArrayList<ModuleLoc> ret = new ArrayList<>();
        for (RefitTask task : taskchain) {
            Collections.addAll(ret, task.targets);
        }
        return ret;
    }


    public ArrayList<RefitTask> taskchain;


    //STUB TODO: integrate with the job system
    public void cancelAllRefitTasks(){
        taskchain = new ArrayList<>();
    }


    //TODO: move RefitTaskChain and RefitTask out of the class
    /**
     * The refit-task is a task that may show up for the construction job.
     */
    abstract class RefitTask extends ConstructionTask {
        protected RefitType refitType;
        protected ModuleLoc[] targets;

        public RefitTask(int labourCost, String description, RefitType refitType, ModuleLoc[] targets) {
            super(labourCost, description);
            this.refitType = refitType;
            this.targets = targets;
        }
        public RefitTask(int labourCost, String description, RefitType refitType, ModuleLoc target) {
            super(labourCost, description);
            this.refitType = refitType;
            this.targets = new ModuleLoc[]{target};
        }

        /** TODO: move to bottom-most super-class for tasks.
         * Runs when finishing up the job, the construction job is finished.
         * Some related events could trigger.
         * @return whatever the task was successfully completed.
         */
        abstract boolean onFinish();

        /**
         *
         * @return whatever the task can be removed.
         */
        abstract boolean onRemove();

        public ModuleLoc[] getTargets() {
            return targets;
        }

        public RefitType getRefitType() {
            return refitType;
        }
    }
    enum RefitType{build, remove}










/*    public CanBuildResult canBuildWeapon(int sectionID, int slotID, WeaponType type) {
        CanBuildResult result = new CanBuildResult();
        if (!validateWeaponSlot(sectionID, slotID)) {
            result.possible = false;
            result.message = "Invalid selection.";
            return result;
        }

        //STUB. TODO: check if player can afford to construct this.
        result.possible = true;
        result.message = String.format("A test-weapon will be built on section %1$s's weapon slot number %2$s.", sectionID, slotID);
        return result;
    }
    public CanBuildResult canBuildModule(int sectionID, int moduleID, ModuleType type) {
        CanBuildResult result = new CanBuildResult();
        if (validateModuleSlot(sectionID, moduleID)) {
            result.possible = false;
            result.message = "Invalid selection.";
            return result;
        }
        ShipModule module = modules[sectionID][moduleID];
        //This is a STUB - No care is made for cargo or crew quarters yet

        if (module.moduleType == ModuleType.Empty) {
            if(type == ModuleType.Empty) {
                result.possible = true;
                result.message = "";
                return result;
            }
            //STUB. TODO: check if player can afford to construct this.
            result.possible = true;
            result.message = String.format("A %1$s module will be constructed at section 2$s's module slot number 3$s", type, sectionID, moduleID);

        }


        if(type == ModuleType.Empty) {
            result.possible = true;
            if (module.moduleType == ModuleType.Empty) {
                result.message = "";
            }
            result.message = "Module at ";
        }



    }*/
    /** --- end of refit section ----**/










    /**
     * Test-creates a spaceship, then prints the structure to console.
     * @param args
     */
    public static void main(String[] args) {
        Spaceship ship = Spaceship.GenerateStart1(new Random(0), 2, 10, 0.0f, 1.0f);
        System.out.println(ship.toString());
    }
}


//STUB
class ConstructionTask {
    private int labourCost;
    private String description;

    public ConstructionTask(int labourCost, String description) {

        this.labourCost = labourCost;
        this.description = description;
    }
}


//STUB
enum WeaponType {
    None, TestGun;
    public static WeaponType fromInt(int id){ return values()[id]; }
    public static int toInt(WeaponType type) {return type.toInt(); }
    public int toInt() {return ordinal();}

}

//STUB
class ShipWeapon {

}

//Placeholder classes. TODO: write these classes (duh..)
@SuppressWarnings({"All"})
class CargoPlaceholder{}
@SuppressWarnings({"All"})
class HousingPlaceholder{}
//NOTE: why o' why complain about placeholders, Intellij.
// you are needlessly breaking my workflow.
