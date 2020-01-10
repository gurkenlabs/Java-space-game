package unicus.spacegame.gameevent;

import de.gurkenlabs.litiengine.IUpdateable;
import unicus.spacegame.ui.DebugConsole;
import java.util.Random;
import java.util.function.Function;

import java.lang.invoke.MethodType;
import java.util.ArrayList;

public final class GameEvent implements IUpdateable {
    private static GameEvent INSTANCE;
    private String info = "Magic singleton class";
    ArrayList<RandomEvent> myEvents = new ArrayList<RandomEvent>();
    private GameEvent() {
        INSTANCE = this;
        DebugConsole.getInstance().addGameEventCommands();
        myEvents.add(ScientificDiscovery);
        myEvents.add(MinorAirLeak);
        myEvents.add(MetallicDeposit);
        myEvents.add(AlienMapSellerTrue);
        myEvents.add(AlienMapSellerTrueResult);
        myEvents.add(AlienMapSellerFake);
        myEvents.add(AlienMapSellerFakeResult);
        myEvents.add(GoodGrowingSeason);
        myEvents.add(CrewPlayingGames);
        myEvents.add(WeaponDrillAccident);
    }

    /** Data structure for the event list is in flux. Possibly an external file, possibly a series of constructors.
     */
    public static GameEvent getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new GameEvent();
            //Set up event database.
        }
        return INSTANCE;
    }

    /** Looks over the list of events that are qualified to happen [NB: doesn't actually do this yet, docs ahead of code]
    e.g. "alien runs amok" only if you have >0 aliens aboard
    Calculate appropriate weightings for common and rare events
    Pick one of them and hand off execution to avoid code duplication.
     */
    public int event_Random() {
        //Prerequisite verifier algorithm go here

        //Weighted random selection algorithm. Akin to picking off a D&D random table with entries like 00-22, 22-30, 31-80, 81-99.
        int sum_weights=0;
        for (RandomEvent r: myEvents) {
            sum_weights += r.weight;
        }
        int i = 0;
        int r = new Random().nextInt(sum_weights); //To pick based on relative weight, we have to select from sum of weights, not number of events
        while (r > myEvents.get(i).weight) {
            r -= myEvents.get(i).weight;
            i++;
        }
        execute_event(myEvents.get(i).event_id); //convert index-in-list to index-by-ID

        return event_byID(0);
    }
    /** **obsolete! should merge with execute_event**
     *
     * Event picker, sanity checker
     * Call handling for event chains
     * I forget the name of the pattern but it prevents events from getting into deep stacks of chained calls
     */
    public int event_byID(int eventID) { //Also useful for debugging by console!
        return execute_event(eventID);
        //if (eventID == 0) {
        //    return; //There is no event 0!
        //}
        //int next_ID = 0;
        //do {
        //    next_ID = execute_event(eventID);
        //} while (next_ID != 0);
        //return;
    }

    /** **should merge with event_byID**
     * Returns the ID of a 'follow-up' event for when there is an event chain
    (e.g. an event asks you to make a decision between two feuding crew; you may get a second event where the loser takes matters into his own hands)
    Return 0 to signal there is no further event */
    public int execute_event(int eventID) {
        if(eventID == 0) {
            DebugConsole.getInstance().write("Oopsie: Event 0 was called");
            return 0;
        }

        if(eventIsWaiting()) {
            DebugConsole.getInstance().write("Warning: new event was called while waiting for response to an event.");
            return 0;
        }
        currentEventID = eventID;
        nextEventID = 0;

        //temporary write to debug console
        DebugConsole c = DebugConsole.getInstance();
        c.write("Hello? Yes this is event text...");
        //NOTE: please put the events in an easy to access list. - Lars
        c.write("type event option # to respond:");
        c.write("0 - option 0");
        c.write("1 - option 2");
        c.write("2 - option 3");


        //Pop up a UI dialog box:
        //UI.text = event_text;
        //for i in (0,event_options) : {UI.button = event_choice_text, event_choice_ID}
        //return ID of clicked button
        return currentEventID; //return current event ID to give the console some context on what the f- it just did - Lars
    }
    public int handle_option(int option) {
        if(!eventIsWaiting())
            return 0;

        //do option stuff
        nextEventID = 0; //whatever next id is, if there is one.
        currentEventID = 0;
        return nextEventID;
    }

    private int nextEventID = 0;
    private int currentEventID = 0;
    public boolean eventIsWaiting(){
        return currentEventID != 0;
    }

    //NOTE: if testing standalone without litiengine, have the driver run this update on a loop. - Lars
    @Override
    public void update() {
        if(!eventIsWaiting() && nextEventID != 0)
            execute_event(nextEventID);

    }

    /** Current base class for events.
     * Contains its own ID and text for a dialog box, text on dialog choice options, ID of each dialog choice.
     * If the ID of a dialog choice is nonzero, that will be the followup event triggered.
     * Initial events should be numbered like BASIC: ID 10, 20, 30, 40...
     * so that follow-up events in a chain can be easily inserted at 31,32,33 for the results of event 30.
     * Argument form is e.g. (10, "A thing happened!", new int[]{0}, new String[]{"OK"}.
     * ID Weight makes things happen more or less often. Baseline is 100.
     * Weight is not included in the usual constructor because it should only rarely be altered.
     */
    private class RandomEvent {
        int event_id; //Event IDs should start at 10 and be spaced apart, like BASIC line numbers, for much the same reason.
        String event_text;
        int[] event_choice_ids;
        String[] event_choice_texts;
        int weight=100; //Each random event should have a chance of happening based on (this.weight)/(sum:weights). Most should stay at 100.
        RandomEvent(int id, String text, int[] choice_ids, String[] choice_texts) {
            assert (id != 0);
            this.event_id = id;
            this.event_text = text;
            this.event_choice_ids = choice_ids;
            this.event_choice_texts = choice_texts;
        }
    }

    //Events should be private. External classes call event_by_ID(), not the event object itself.
    private RandomEvent ScientificDiscovery = new RandomEvent(10, "One of our crewmen has made a scientific breakthrough in his spare time! We have gained 5 research points.",
            new int[]{0}, new String[]{"That's good."});
    private RandomEvent MinorAirLeak = new RandomEvent(20, "There was a minor leak in one of the airlocks. We found and patched it, but our oxygen supplies have been depleted slightly.",
            new int[]{0}, new String[]{"Unfortunate."});
    private RandomEvent MetallicDeposit = new RandomEvent(30, "We have stumbled on an asteroid with a high purity metal deposit. The metal was easy to extract and has been added to our stores.",
            new int[]{0}, new String[]{"OK."});
    private RandomEvent AlienMapSellerTrue = new RandomEvent(40, "An independent alien ship is hailing us, offering to trade us knowledge of galactic hyperlanes for some of our shinyum.",
            new int[]{0,41}, new String[]{"No thanks","Pay them 2 Shinium."});
    private RandomEvent AlienMapSellerTrueResult = new RandomEvent(41, "We have integrated the alien coordinates into our own database. We are slightly closer to finding our way back to Earth.",
            new int[]{0}, new String[]{"Onwards!"});
    AlienMapSellerTrueResult.weight = 0; //Why is this a syntax error? Why is it an "Unknown class" syntax error??
    private RandomEvent AlienMapSellerFake = new RandomEvent(45, "An independent alien ship is hailing us, offering to trade us knowledge of galactic hyperlanes for some of our shinyum.",
            new int[]{0,46}, new String[]{"No thanks","Pay them 2 Shinium."});
    private RandomEvent AlienMapSellerFakeResult = new RandomEvent(46, "Sadly the alien coordinates turned out to be gibberish, but after all the time we spent trying to calculate, the scammers have fled.",
            new int[]{0}, new String[]{"Damn them!"});
    AlienMapSellerFakeResult.weight = 0;
    private RandomEvent GoodGrowingSeason = new RandomEvent(50, "Our hydroponic tanks have been flourishing the past week and we are ready to harvest an unusually large crop. +4 food.",
            new int[]{0}, new String[]{"I just hope it's not broccoli."});
    private RandomEvent CrewPlayingGames = new RandomEvent(60, "Your crew has been socializing happily over a lot of the games in the rec room recently. Morale has improved.",
            new int[]{0}, new String[]{"Maybe I should join them."});
    private RandomEvent WeaponDrillAccident = new RandomEvent(70, "One of your marines was injured in training during live weapons practice.",
            new int[]{0}, new String[]{"Medic!"});

    /* private RandomEvent name = new RandomEvent(, "",
                                       new int[]{0}, new String[]{""}); */

    //Event texts can possibly be outsourced to external file for translation later

    //TODO: How to store prerequisites/conditionals?
    //TODO: What's the syntax to look out at ship state variables? (e.g. amount of resources, having a specific module)

}

/*
Data that needs to be associated an event:
-ID
-Dialog text
-whether it is an initial or a follow-up event (i.e. can it happen on its own).
-Option texts
-Option IDs
-Weight (base chance of happening)
-Weight modifiers
-Prerequisites to happening (could be folded into Weight *=0)
-What happens immediately as the event fires
-What happens when an option is selected
 */
//Potentially: options that are only sometimes available?

//Draft
class RandomEvent {
    int e_ID;
    String e_text;
    //prerequisites to fire
    int[] button_IDs;
    String button_texts;
    //button conditionals
    double weight = 100;
    private ArrayList<Object> WeightModifiers;
    boolean isRandom;

    public RandomEvent(int ID, String dialogtext, String optiontext) { //Minimal simple constructor for one-option "info" event
        this.e_ID = ID;
        e_text = dialogtext;
    }
    public RandomEvent(int ID, String dialogtext, int[] option_IDs, String[] option_texts[], double starting_weight, boolean canTriggerRandomly) { //Longer constructor for events with choices
        this.e_ID = ID;
        e_text = dialogtext;
        this.weight = starting_weight;
        this.isRandom = canTriggerRandomly;
        //Install weights
    }
    public double GetWeight() {
        int adjusted_weight = weight;
        for (int i = 0; i<WeightModifiers.size(); i++) {
            adjusted_weight = adjusted_weight * WeightModifiers.get(i)()(); //Please stop thinking that Object() is of type Object, damn syntax
        }
    }
    /* public ¿MatchedPairs? GetOptions() {
        FOption[] result = new FOption[];
        for (FOption o: this.DialogueOptions) {
            if (o.condition) : result.add(o);
        }
   } */

}