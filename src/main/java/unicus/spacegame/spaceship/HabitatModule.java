package unicus.spacegame.spaceship;

import unicus.spacegame.CargoCollection;
import unicus.spacegame.CargoContainer;
import unicus.spacegame.crew.AbstractHousing;
import unicus.spacegame.crew.HousingPlace;
import unicus.spacegame.crew.SpaceCrew;

import java.util.Collection;

/*
 * Idea:
 * Split habitat housing into multiple. Maybe of different types.
 * This to make a bigger point of social dynamics.
 *
 * Remote guardian penalty for childcare jobs should in that case take the module into account, and not the housing.
 *
 */

public class HabitatModule extends AbstractShipModule implements HousingPlace {
    public HabitatModule(ShipLoc loc) {
        super(loc);
        housing = new HabitatHousing(SpaceCrew.SC().getHousingKeys().yieldKey());
        SpaceCrew.SC().addHousing(housing);
    }
    HabitatHousing housing;

    @Override
    public int[] getHousings() {
        return new int[]{housing.getKeyID()};
    }

    class HabitatHousing extends AbstractHousing {

        HabitatModule this0;
        public HabitatHousing(int keyID) {
            super(keyID, 5);
            this0 = HabitatModule.this;
        }

        /**
         * Things happening at the end of a month.
         * May trigger events related to living situations.
         */
        @Override
        public void endOfMonth() {

        }
    }

    @Override
    public int getNumComponents() {
        return 0;
    }

    @Override
    public abstractShipComponent[] getComponents() {
        return new abstractShipComponent[0];
    }

    /**
     * For modules:
     * Whatever this module requires gravity to be constructed.
     *
     * @return true
     */
    @Override
    public boolean useGravity() {
        return true;
    }

    @Override
    public ModuleType getModuleType() {
        return ModuleType.Habitat;
    }

    @Override
    public String GetName() {
        return "Habitat module at " + loc.toString();
    }

    @Override
    public Collection<CargoCollection> getCargoOnDestruction() {
        return CargoContainer.Null.getCollection();
    }

    /**
     * This function runs when the ship-part is removed or dismantled from the ship.
     * This function only deals with this object itself, any ship-part depended on this,
     * will be taken care of from HomeShip. .
     */
    @Override
    public void onDestroy() {
        SpaceCrew.SC().removeHousing(housing.getKeyID());
    }
}
