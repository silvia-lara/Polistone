package sample;

import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * This class represents the State of the game at each moment:
 * what cards are on the board, what cards are on each hand,
 * how many attack points, health points and lives each creature/hero has, which cards are sleeping, which cards have
 * attacked this turn, which Hero is playing... Moreover, it contains the actions that are admissible by the rules of
 * the game: attack, heal, damage, die, start turn, end turn... It contains a game initializer.
 * Only the state can modify itself, since everything depends on it. That's why the getState
 * function is implemented as a singleton.
 */
@SuppressWarnings("CanBeFinal")
public class State {
    private static final int MAX_HERO_LIFE = 30;

    /**
     * The Hero is either Hillary Clinton or Donald Trump
     */
    public enum Hero {
        HILLARY, TRUMP
    }

    /**
     * This class defines the type of cards that have been played and are now on the floor.
     */
    @SuppressWarnings("CanBeFinal")
    public class PlayedCreature {
        public boolean hasAttackedThisTurn;
        public BooleanProperty isSleeping;
        public int maxHealth;
        public CreatureCard creature;
        public Hero h;


        /**
         * This function transforms a creature into a played creature, considering the possibilities of Summoning
         * effects. If the creature played has a summoning effect, the summoned creature is also played. This function
         * also initialises the played creature to an initial state of sleep, and it records it maximum health and the
         * hero it belongs to, in such a way that attack and healing actions are performed accordingly.
         *
         * @param h Hero that played the card
         * @param creature creature that is going to get played
         */
        public PlayedCreature(Hero h,CreatureCard creature) {
            this.creature = creature;
            this.isSleeping = new SimpleBooleanProperty(true);
            this.isSleeping.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    System.out.println("Is sleeping was set to " + String.valueOf(newValue));
                }
            });
            this.hasAttackedThisTurn = false;
            this.maxHealth = (int) creature.health;
            this.h = h;

            if(creature.effect == CreatureEffect.SUMMON){
                CreatureCard holder = new CreatureCard();
                switch (creature.name){
                    case "Minority Voter":
                        holder.family = CardFamily.COMMON;
                        holder.name = "Another Minority Voter";
                        holder.description = "Si se puede!";
                        holder.imageURL = "minority_voter.png";
                        holder.cost = 0;
                        holder.health = 2;
                        holder.attack = 2;
                        holder.effect = CreatureEffect.CREATURE_EFFECT_NONE;
                        holder.target = EffectTarget.EFFECT_TARGET_NONE;
                        holder.type = EffectType.EFFECT_TYPE_NONE;
                        holder.magnitude = 0;
                        break;
                    case "Armed Militia":
                        holder.family = CardFamily.COMMON;
                        holder.name = "Armed Posse";
                        holder.description = "Arpaio 2020";
                        holder.imageURL = "armed_militia.png";
                        holder.cost = 0;
                        holder.health = 1;
                        holder.attack = 4;
                        holder.effect = CreatureEffect.CREATURE_EFFECT_NONE;
                        holder.target = EffectTarget.EFFECT_TARGET_NONE;
                        holder.type = EffectType.EFFECT_TYPE_NONE;
                        holder.magnitude = 0;
                        break;
                    case "Putin":
                        holder.family = CardFamily.COMMON;
                        holder.name = "Kremlin Hacker";
                        holder.description = "Priviet prostituski";
                        holder.imageURL = "kremlin_hacker.png";
                        holder.cost = 0;
                        holder.health = 4;
                        holder.attack = 3;
                        holder.effect = CreatureEffect.CREATURE_EFFECT_NONE;
                        holder.target = EffectTarget.EFFECT_TARGET_NONE;
                        holder.type = EffectType.EFFECT_TYPE_NONE;
                        holder.magnitude = 0;
                        break;
                }
                switch (h){
                    case HILLARY:
                        State.getState().democratHand.add(holder);
                        break;
                    case TRUMP:
                        State.getState().republicanHand.add(holder);
                        break;
                }
                State.getState().playCard(h, holder, Optional.empty());
            }
        }

        /**
         * This function performs damage of a certain amount. If the damage bring the HP to zero or less the card is
         * killed.
         *
         * @param amount of damage to be issued
         */
        public void damage(int amount) {
            State.getState().addToCombatLog(this.creature.name + " took " + String.valueOf(this.creature.health - amount) + " damage");
            if(this.creature.health - amount <= 0){
                this.die();
            }else {
                this.creature.health -= amount;
            }
            this.creature.reRenderCard();
        }

        /**
         * This function handles the case in which the creature dies, removing
         * it from its floor. Since some of the cards have Deathrattles,
         * it performs a check to see whether the dying card is one of those. If it is, it performs the deathrattle.
         */
        private void die() {
            if(this.creature.effect == CreatureEffect.DEATHRATTLE){
                switch(this.creature.target){
                    case ENEMY_AOE:
                        switch(this.h){
                            case HILLARY:
                                State.getState().damageHero(Hero.TRUMP, (int) this.creature.magnitude);
                                State.getState().republicanFloor.forEach(c -> c.damage((int) this.creature.magnitude));
                                break;
                            case TRUMP:
                                State.getState().damageHero(Hero.HILLARY, (int) this.creature.magnitude);
                                State.getState().democratFloor.forEach(c -> c.damage((int) this.creature.magnitude));
                                break;
                        }
                        break;
                    case FRIENDLY_AOE:
                        switch(this.h){
                            case TRUMP:
                                State.getState().healHero(Hero.TRUMP, (int) this.creature.magnitude);
                                State.getState().republicanFloor.forEach(c -> c.heal((int) this.creature.magnitude));
                                break;
                            case HILLARY:
                                State.getState().healHero(Hero.HILLARY, (int) this.creature.magnitude);
                                State.getState().democratFloor.forEach(c -> c.heal((int) this.creature.magnitude));
                                break;
                        }
                        break;
                    case ENEMY_HERO:
                        switch(this.h){
                            case HILLARY:
                                State.getState().damageHero(Hero.TRUMP, (int) this.creature.magnitude);
                                break;
                            case TRUMP:
                                State.getState().damageHero(Hero.HILLARY, (int) this.creature.magnitude);
                                break;
                        }
                        break;
                    case YOUR_HERO:
                        switch (creature.name){
                            case "Bernie Sanders":
                                State.getState().damageHero(Hero.HILLARY, (int) this.creature.magnitude);
                                break;
                            case "Barack Obama":
                                State.getState().healHero(Hero.HILLARY, (int) this.creature.magnitude);
                                break;
                        }
                        break;
                }
            }
            switch(this.creature.belongsTo){
                case HILLARY:
                    State.getState().democratFloor.remove(this);
                    break;
                case TRUMP:
                    State.getState().republicanFloor.remove(this);
                    break;
            }
            State.getState().addToCombatLog(this.creature.name + " is dead");
        }

        /**
         * This function heals a creature of amount healing points, un to a maximum of its original health.
         * @param amount of healing points to be delivered
         */
        public void heal(int amount) {
            this.creature.health = Math.min(this.maxHealth, (this.creature.health + amount));
            this.creature.reRenderCard();
        }

        /**
         * This function attacks the target given as an input, if the card has not attacked this turn yet and if
         * it's not sleeping.
         * After the attack is completed, the card ability to attack is disabled and the state gets re-rendered.
         *
         * @param t Target of the attack
         * @return an empty string if the attack is successful, or the relative error message if it is not.
         */
        public Optional<String> attack(Target t){
            if(this.hasAttackedThisTurn){
                Optional.of("You have already attacked this turn");
            }
            if(this.isSleeping.get()){
                Optional.of("You are sleeping right now. Sweet drams and come back next time.");
            }
            State.withTarget(t, hero -> {
                State.getState().damageHero(hero,(int) this.creature.attack); return new Object();
            }, creature -> {
                creature.damage((int) this.creature.attack);
                this.damage((int) creature.creature.attack);
                return new Object();
            });
            this.hasAttackedThisTurn = true;
            State.getState().tick();
            return Optional.empty();
        }
    }

    /**
     * This class identifies the general idea of a Target. A target can be either a Hero or a Played creature, so
     * the constructor for this class accepts either one or the other. The helper functions return whether the
     * Hero or a Creature is targeted. These are only used by the withTarget function, as a safety measure.
     */

    public static class Target{
        Hero h;
        PlayedCreature p;

        public Target(Hero h) {
            this.h = h;
        }

        public Target(PlayedCreature p) {
            this.p = p;
        }

        public Optional<Hero> isHeroTargeted(){
            if(this.h == null){
                return Optional.empty();
            }
            return Optional.of(this.h);
        }

        public Optional<PlayedCreature> isCreatureTargeted(){
            if(this.p == null){
                return Optional.empty();
            }
            return Optional.of(this.p);
        }
    }

    /** This function defines the scope of the healing or attack.
     * It first determines whether we need to attack a hero or a creature. Then it applies the action on the target.
     *
     * @param t Target
     * @param h Hero action
     * @param c Creature action
     * @return Object: unit equivalent of returning void
     */
    private static Object withTarget(Target t, Function<Hero, Object> h, Function<PlayedCreature, Object> c){
        if(t.isCreatureTargeted().isPresent()){
            return c.apply(t.isCreatureTargeted().get());
        }
        if(t.isHeroTargeted().isPresent()){
            return h.apply(t.isHeroTargeted().get());
        }
        return new Object();
    }

    /**Deals with any situation that requires Mana exchange.
     * It makes sure that the mana are deducted correctly,
     * considering whether the hero has enough mana to perform a the action.
     * The input function is then applied on the number of lives available and it deducts the cost by those.
     *
     * @param h Hero performing the action
     * @param i how many mana it costs
     * @param f function of integer(how many manas are available) and optional
     * @return empty optional if it succeeded, the message error otherwise
     */
    private static Optional<String> withMana(Hero h, Integer i, Function<Integer, Optional<String>> f){
        Optional<String> res;
        switch(h){
            case HILLARY:
                if(State.getState().democratMana.get() - i >= 0){
                    res = f.apply((int)State.getState().democratMana.get());
                    if(!res.isPresent()){
                        State.getState().democratMana.setValue(State.getState().democratMana.get() - i);
                    }
                    return res;
                }
                return Optional.of("Not enough mana");
            case TRUMP:
                if(State.getState().republicanMana.get() - i >= 0){
                    res = f.apply((int)State.getState().republicanMana.get());
                    if(!res.isPresent()){
                        State.getState().republicanMana.setValue(State.getState().republicanMana.get() - i);
                    }
                    return res;
                }
                return Optional.of("Not enough mana");
        }
        return Optional.of("Fatal error at withMana");
    }

    /**
     * This class represents the category of played spells.
     *
     * The constructor takes the Hero that is trying to play the
     * spell, the spell card and the optional target that the Hero has chosen, turning a spelled card into a played
     * spell. A played spell can either damage or heal and its target(s) can be random targets, all friendly minions,
     * all enemy minions, enemy hero, or your hero
     *
     * The case in which the effect of the spell has a random target is dealt with in the getRandomTarget
     * function. A random number from zero to the number of played cards + 2 heroes is drawn. This number is the index
     * of the card/hero that will be set as a target.
     *
     */
    @SuppressWarnings("CanBeFinal")
    public class PlayedSpell {
        public SpellCard spell;
        public Hero h;

        private Target getRandomTarget(){
            int numPossibleTargets = (State.getState().democratFloor.size() + State.getState().republicanFloor.size() + 2);
            int toss = ThreadLocalRandom.current().nextInt(0, numPossibleTargets );
            switch (toss){
                case 0:
                    return new Target(Hero.HILLARY);
                case 1:
                    return new Target(Hero.TRUMP);
                default:
                    if(toss <= (State.getState().democratFloor.size() + 1)){
                        toss -= 2;
                        return new Target(State.getState().democratFloor.get(toss));
                    } else {
                        toss -= 2 + State.getState().democratFloor.size();
                        return new Target(State.getState().republicanFloor.get(toss));
                    }
            }
        }

        public PlayedSpell(Hero h, SpellCard spell, Optional<Target> target) {
            this.spell = spell;
            switch (spell.effect){
                case DRAW_CARD:
                    System.out.println("Starting draw");
                    for(int i = 0; i < spell.magnitude; i++){
                        State.getState().drawCard(h);
                    }
                    System.out.println("Ending draw");
                    break;
                case SPELL_EFFECT_DAMAGE:
                    switch (spell.target){
                        case TARGETED:
                            if(!target.isPresent()){
                                // return Optional.of("Targeted spell called with no target :(");
                            }

                            State.withTarget(target.get(), (hero -> {
                                State.getState().damageHero(hero, (int) spell.magnitude); return new Object();
                            }), (c -> {
                                c.damage((int) spell.magnitude); return new Object();
                            }));

                            break;
                        case RANDOM:
                            State.withTarget(this.getRandomTarget(), hero -> {
                                State.getState().damageHero(hero,(int)spell.magnitude);
                                return new Object();
                            }, playedCreature -> {
                                playedCreature.damage((int) spell.magnitude);
                                return new Object();
                            } );
                            break;
                        case ENEMY_AOE:
                            switch (h){
                                case HILLARY:
                                    State.getState().damageHero(Hero.TRUMP, (int) spell.magnitude);
                                    State.getState().republicanFloor.forEach(c -> c.damage((int) spell.magnitude));
                                    break;
                                case TRUMP:
                                    State.getState().damageHero(Hero.HILLARY, (int) spell.magnitude);
                                    State.getState().democratFloor.forEach(c -> c.damage((int) spell.magnitude));
                                    break;
                            }
                            break;
                        case FRIENDLY_AOE:
                            switch (h){
                                case TRUMP:
                                    State.getState().damageHero(Hero.TRUMP, (int) spell.magnitude);
                                    State.getState().republicanFloor.forEach(c -> c.damage((int) spell.magnitude));
                                    break;
                                case HILLARY:
                                    State.getState().damageHero(Hero.HILLARY, (int) spell.magnitude);
                                    State.getState().democratFloor.forEach(c -> c.damage((int) spell.magnitude));
                                    break;
                            }
                            break;
                        case ALL_AOE:
                            State.getState().damageHero(Hero.TRUMP, (int) spell.magnitude);
                            State.getState().republicanFloor.forEach(c -> c.damage((int) spell.magnitude));
                            State.getState().damageHero(Hero.HILLARY, (int) spell.magnitude);
                            State.getState().democratFloor.forEach(c -> c.damage((int) spell.magnitude));
                            break;
                        case ENEMY_HERO:
                            switch (h){
                                case HILLARY:
                                    State.getState().damageHero(Hero.TRUMP,(int) spell.magnitude);
                                    break;
                                case TRUMP:
                                    State.getState().damageHero(Hero.HILLARY,(int) spell.magnitude);
                                    break;
                            }
                            break;
                        case YOUR_HERO:
                            State.getState().damageHero(h,(int) spell.magnitude);
                            break;
                        case EFFECT_TARGET_NONE:
                            break;
                    }
                    break;
                case SPELL_EFFECT_HEAL:
                    switch (spell.target) {
                        case TARGETED:
                            if(!target.isPresent()){
                                // return Optional.of("Targeted spell called with no target :(");
                            }

                            State.withTarget(target.get(), (hero -> {
                                State.getState().healHero(hero, (int) spell.magnitude); return new Object();
                            }), (c -> {
                                c.heal((int) spell.magnitude); return new Object();
                            }));

                            break;
                        case RANDOM:
                            State.withTarget(this.getRandomTarget(), hero -> {
                                State.getState().healHero(hero,(int)spell.magnitude);
                                return new Object();
                            }, playedCreature -> {
                                playedCreature.heal((int) spell.magnitude);
                                return new Object();
                            } );
                            break;
                        case ENEMY_AOE:
                            switch (h){
                                case HILLARY:
                                    State.getState().healHero(Hero.TRUMP, (int) spell.magnitude);
                                    State.getState().republicanFloor.forEach(c -> c.heal((int) spell.magnitude));
                                    break;
                                case TRUMP:
                                    State.getState().healHero(Hero.HILLARY, (int) spell.magnitude);
                                    State.getState().democratFloor.forEach(c -> c.heal((int) spell.magnitude));
                                    break;
                            }
                            break;
                        case FRIENDLY_AOE:
                            switch (h){
                                case TRUMP:
                                    State.getState().healHero(Hero.TRUMP, (int) spell.magnitude);
                                    State.getState().republicanFloor.forEach(c -> c.heal((int) spell.magnitude));
                                    break;
                                case HILLARY:
                                    State.getState().healHero(Hero.HILLARY, (int) spell.magnitude);
                                    State.getState().democratFloor.forEach(c -> c.heal((int) spell.magnitude));
                                    break;
                            }
                            break;
                        case ALL_AOE:
                            State.getState().healHero(Hero.TRUMP, (int) spell.magnitude);
                            State.getState().republicanFloor.forEach(c -> c.heal((int) spell.magnitude));
                            State.getState().healHero(Hero.HILLARY, (int) spell.magnitude);
                            State.getState().democratFloor.forEach(c -> c.heal((int) spell.magnitude));
                            break;
                        case ENEMY_HERO:
                            switch (h){
                                case HILLARY:
                                    State.getState().healHero(Hero.TRUMP,(int) spell.magnitude);
                                    break;
                                case TRUMP:
                                    State.getState().healHero(Hero.HILLARY,(int) spell.magnitude);
                                    break;
                            }
                            break;
                        case YOUR_HERO:
                            State.getState().healHero(h,(int) spell.magnitude);
                            break;
                        case EFFECT_TARGET_NONE:
                            break;
                    }
                    break;
                case SPELL_EFFECT_SUMMON:
                    CreatureCard holder = new CreatureCard();
                    holder.family = CardFamily.COMMON;
                    holder.name = "Random Voter";
                    holder.description = "Why am I here?";
                    holder.imageURL = "random_voter.png";
                    holder.cost = 0;
                    holder.health = 2;
                    holder.attack = 1;
                    holder.effect = CreatureEffect.CREATURE_EFFECT_NONE;
                    holder.target = EffectTarget.EFFECT_TARGET_NONE;
                    holder.type = EffectType.EFFECT_TYPE_NONE;
                    holder.magnitude = 0;
                    switch (h){
                        case HILLARY:
                            State.getState().democratHand.add(holder);
                            break;
                        case TRUMP:
                            State.getState().republicanHand.add(holder);
                            break;
                    }
                    State.getState().playCard(h, holder, Optional.empty());
                    break;
                case SPELL_EFFECT_NONE:
                    break;
            }
        }
    }

    public void setTickHandler(Function<State, Object> tickHandler) {
        this.tickHandler = tickHandler;
    }

    private Function<State,Object> tickHandler;
    private Function<Hero,Object> endGameHandler;

    public void setEndGameHandler(Function<Hero, Object> endGameHandler) {
        this.endGameHandler = endGameHandler;
    }

    /**
     * This holds the only instance of the state class. Makes this a singleton
     */
    private static State stateInstance;
    //Who's playing right now?
    public Hero getWhoseTurn() {
        return whoseTurn;
    }
    private Hero whoseTurn;
    // A count of the number of turns taken by both players. Eg. If shillary
    // starts first, turncount is 1. When she ends and trump begins, turn count
    // is 2. Use min(ceil(turnCount / 2.0), 10) to get mana available in turn
    private int turnCount;
    // Records the available manas for each party
    public LongProperty democratMana;
    public LongProperty republicanMana;
    
    //getters for Mana and lives
    public long getDemocratMana() {
        return democratMana.get();
    }
    public long getRepublicanMana() {
        return republicanMana.get();
    }
    public int getDemocratLives() {
        return democratLives.get()  ;
    }
    public int getRepublicanLives() {
        return republicanLives.get() ;
    }
    //Records the number of lives available to each hero
    public IntegerProperty democratLives;
    public IntegerProperty republicanLives;
    //Indicates whether the Hero has used its power on this turn. If it has, then
    //the power cannot be used again.
    private boolean hasUsedHeroPower;
    // Shows the list of cards in the hand of a certain player
    private ObservableList<Card> democratHand;
    private ObservableList<Card> republicanHand;

    public ObservableList<PlayedCreature> getDemocratFloor() { return democratFloor; }
    public ObservableList<PlayedCreature> getRepublicanFloor() { return republicanFloor; }

    // Shows the list of cards that are currently on the floor of each player.
    //All of these cards are Creature cards, since Spells get discarded instead.
    private ObservableList<PlayedCreature> democratFloor;
    private ObservableList<PlayedCreature> republicanFloor;
    //Shows the list of cards currently on the deck of each party
    private List<Card> democratDeck;
    private List<Card> republicanDeck;

    public Optional<PlayedSpell> getDemocratHoveringCard() {
        return democratHoveringCard;
    }

    public Optional<PlayedSpell> getRepublicanHoveringCard() {
        return republicanHoveringCard;
    }

    /**Shows the last played Spell card: this feature is useful when one
     * player plays a spell card: so that the adversary knows what's going
     * to happen
     */
    private Optional<PlayedSpell> democratHoveringCard;
    /**Shows the last played Spell card: this feature is useful when one
     * player plays a spell card: so that the adversary knows what's going
     * to happen
     */
    private Optional<PlayedSpell> republicanHoveringCard;

    public ObservableList<String> combatLog = FXCollections.observableArrayList();

    private void addToCombatLog(String log){
        this.combatLog.add(0, log);
    }

    public boolean shouldTick = true;


    /**
     * this method initializes the state to some default value to avoid null pointer exceptions in the future
     */
    private State() {
        this.whoseTurn = Hero.HILLARY;
        this.turnCount = 0;
        this.democratMana = new SimpleLongProperty(0);
        this.republicanMana = new SimpleLongProperty(0);
        this.democratLives = new SimpleIntegerProperty(0);
        this.republicanLives = new SimpleIntegerProperty(0);
        this.hasUsedHeroPower = false;
        this.democratHand = FXCollections.observableArrayList();
        this.republicanHand = FXCollections.observableArrayList();
        this.democratFloor = FXCollections.observableArrayList();
        this.republicanFloor = FXCollections.observableArrayList();
        this.democratDeck = new ArrayList<>();
        this.republicanDeck = new ArrayList<>();
        this.democratHoveringCard = Optional.empty();
        this.republicanHoveringCard = Optional.empty();

    }

    //Singleton structure of the State
    static public State getState() {
        if (State.stateInstance == null) {
            State.stateInstance = new State();
        }
        return State.stateInstance;
    }


    /**
     * Match initializer: it assigns the decks, it checks whether there are actually
     * 30 cards and it draws the initial 4 cards assigning them to each player
     *
     * @param democrats Democrat deck
     * @param republicans Republican deck
     * @return True if the init succeeded
     */
   public boolean init(ArrayList<Card> democrats, ArrayList<Card> republicans) {
        if (democrats.size() != 30 || republicans.size() != 30) {
            return false;
        }
        this.republicanDeck = republicans;
        this.democratDeck = democrats;

        IntStream.range(0, 4).forEach(i -> {
            this.drawCard(Hero.HILLARY);
            this.drawCard(Hero.TRUMP);
        });

        this.democratLives.setValue(MAX_HERO_LIFE);
        this.republicanLives.setValue(MAX_HERO_LIFE);

        return true;
    }

    /**
     * Function called to start each turn. It sets up initial condition: it draws a card, updates the status of the turn
     * and whether the hero has already used its power. Then it wakes up every creature.
     * @param h Hero
     */
    public void startTurn(Hero h){
        this.drawCard(h);
        this.hasUsedHeroPower = false;
        this.turnCount++;
        int turnMana = (int) Math.min(Math.ceil(this.turnCount / 2.0), 10.0);
        switch (h){
            case HILLARY:
                this.democratMana.setValue(turnMana);
                for(PlayedCreature p : this.democratFloor){
                    p.isSleeping.setValue(false);
                    p.hasAttackedThisTurn = false;
                }
                break;
            case TRUMP:
                this.republicanMana.setValue(turnMana);
                for(PlayedCreature p : this.republicanFloor){
                    p.isSleeping.setValue(false);
                    p.hasAttackedThisTurn = false;
                }
                break;
        }
        this.whoseTurn = h;
        this.tick();
    }

    /**
     * Changes the state to an pre-initial state. Used for testing purposes
     */
    public void reset() {
        this.democratHand = FXCollections.emptyObservableList();
        this.republicanHand = FXCollections.emptyObservableList();
        this.democratDeck = new ArrayList<>();
        this.republicanDeck = new ArrayList<>();

    }

    //The following two functions show the list display the list of cards in each
    //player's hand
    public ObservableList<Card> getDemocratHand() {
        return this.democratHand;
    }
    public ObservableList<Card> getRepublicanHand() {
        return this.republicanHand;
    }

    /**
     * This function assesses who the winner is when one player has no more cards to draw
     */
    private void noMoreCardsScenerio(){
        if(this.democratLives.get() > this.republicanLives.get()){
            this.endGame(Hero.HILLARY);
        }else{
            this.endGame(Hero.TRUMP);
        }
    }

    /**
     * This function checks whether the deck is empty and returns a card, assigning it to the hand of the hero inputted
     * @param h Hero
     * @return empty optional if the drawing succeeded or the error message otherwise
     */
    private Optional<String> drawCard(Hero h) {
        switch (h) {
            case HILLARY:
                if (this.democratDeck.isEmpty()) {
                    this.noMoreCardsScenerio();
                    return Optional.of("No more cards to draw");
                } else if(this.democratDeck.size() == 10) {
                    this.democratDeck.remove(this.democratDeck.size() - 1);
                    return Optional.of("Enough cards on your hand. You can't draw more than this.");
                }else{
                    Card newCard = this.democratDeck.remove(this.democratDeck.size() - 1);
                    this.democratHand.add(newCard);
                    return Optional.empty();
                }
            case TRUMP:
                if (this.republicanDeck.isEmpty()) {
                    this.noMoreCardsScenerio();
                    return Optional.of("No more cards to draw");
                } else if(this.republicanDeck.size() == 10) {
                    this.republicanDeck.remove(this.democratDeck.size() - 1);
                    return Optional.of("Enough cards on your hand. You can't draw more than this.");
                }else{
                    Card newCard = this.republicanDeck.remove(this.republicanDeck.size() - 1);
                    this.republicanHand.add(newCard);
                    return Optional.empty();
                }
        }
        return null;
    }

    //These two functions are callers of the above function, which is protected.
    //They provide the hero call for the able function.
    public Optional<String> drawDemocratCard() {
        return this.drawCard(Hero.HILLARY);
    }
    public Optional<String> drawRepublicanCard() {
        return this.drawCard(Hero.TRUMP);
    }

    /**This function plays a card. It first checks whether the card is a spell card.
     * If it is, it put is in its relative hovering position. If instead it's a creature card, it puts it on its
     * relative floor, after checking whether there is space on the floor. It then adds the action to the combat log
     *
     * @param h Hero
     * @param playedCard
     * @param t target
     * @return empty optional if the action succeeded, the error message if it didn't
     */
    protected Optional<String> playCard(Hero h, Card playedCard, Optional<Target> t) {
        Optional<String> holder = State.withMana(h, (int) playedCard.cost, m -> {
            switch (h) {
                case HILLARY:
                    if(!this.democratHand.contains(playedCard)){
                        return Optional.of("Played a card that does not exist in hand");
                    }else{
                        if (playedCard instanceof CreatureCard) {
                            if (this.democratFloor.size() < 8) {
                                PlayedCreature toBePlayed = new PlayedCreature(h, (CreatureCard) playedCard);
                                ((CreatureCard) playedCard).isBeingAddedToBoard(toBePlayed);
                                this.democratFloor.add(toBePlayed);
                                this.democratHand.remove(playedCard);
                            } else {
                                return Optional.of("You can't play this card. Your floor is full");
                            }
                        } else if (playedCard instanceof SpellCard) {
                            this.democratHoveringCard = Optional.of(new PlayedSpell(h, (SpellCard) playedCard, t));
                            this.democratHand.remove(playedCard);
                        }
                    }
                    break;
                case TRUMP:
                    if(!this.republicanHand.contains(playedCard)){
                        return Optional.of("Played a card that does not exist in hand");
                    }else{
                        if (playedCard instanceof CreatureCard) {
                            if (this.republicanFloor.size() < 8) {
                                PlayedCreature toBePlayed = new PlayedCreature(h, (CreatureCard) playedCard);
                                ((CreatureCard) playedCard).isBeingAddedToBoard(toBePlayed);
                                this.republicanFloor.add(toBePlayed);
                                this.republicanHand.remove(playedCard);
                            } else {
                                return Optional.of("You can't play this card. Your floor is full");
                            }
                        } else if (playedCard instanceof SpellCard) {
                            this.republicanHoveringCard = Optional.of(new PlayedSpell(h, (SpellCard) playedCard, t));
                            this.republicanHand.remove(playedCard);
                        }
                    }
                    break;
            }
            return Optional.empty();
        });
        if(!holder.isPresent()){
            this.addToCombatLog(playedCard.name + " was played");
        }
        this.tickHandler.apply(State.getState());
        return holder;
    }

    public void tick(){
        if(this.shouldTick){
            this.tickHandler.apply(State.getState());
        }
    }

    /** This function simulates using the Hero power to attack a certain target.
     * Using the withMana function, to not make mistakes on the mana detraction, it heals the target if the player hero
     * is Hillary or it damages the target if the player Hero is Trump. It then renders the state.
     *
     * @param h Hero
     * @param t Target
     * @return empty optional if the action succeeded, the error message otherwise
     */
    public Optional<String> useHeroPower(Hero h,Target t){
        Optional<String> holder = State.withMana(h, 2, (m -> {
            if(this.hasUsedHeroPower){
                return Optional.of("You have already used the hero power in this turn!");
            }
            switch(h){
                case HILLARY:
                    State.withTarget(t, (hero -> {
                        State.getState().healHero(hero, 2);
                        return new Object();
                    }), (creature -> {
                        creature.heal(2);
                        return new Object();
                    }));
                    break;
                case TRUMP:
                    State.withTarget(t, (hero -> {
                        State.getState().damageHero(hero, 2);
                        return new Object();
                    }), (creature -> {
                        creature.damage(2);
                        return new Object();
                    }));
                    break;
            }
            this.hasUsedHeroPower = true;
            return Optional.empty();
        }));
        this.tick();
        return holder;
    }


    /** This function damages the Hero sets as a target with a certain magnitude of attack points, decreasing the HP
     * of the target. It then adds the attack to the command log.
     * @param hero player
     * @param magnitude of attack
     */
    private void damageHero(Hero hero, int magnitude) {
        this.addToCombatLog(hero.toString() + " took " + String.valueOf(magnitude) + " damage");
        switch (hero){
            case HILLARY:
                if(this.democratLives.get() - magnitude <= 0){
                    this.endGame(Hero.TRUMP);
                }else{
                    this.democratLives.setValue(this.democratLives.get() - magnitude);
                }
                break;
            case TRUMP:
                if(this.republicanLives.get() - magnitude <= 0){
                    this.endGame(Hero.HILLARY);
                }else{
                    this.republicanLives.setValue(this.republicanLives.get() - magnitude);
                }
                break;
        }
        this.tick();
    }

    /** This function heals the hero set as a target of a certain magnitude of Health points, up to a maximum of its
     * original HP (30).
     *
     * @param hero Player
     * @param magnitude of healing
     */
    private void healHero(Hero hero, int magnitude) {
        this.addToCombatLog(hero.toString() + " healed for " + String.valueOf(magnitude));
        switch (hero){
            case HILLARY:
                this.democratLives.setValue(Math.min(MAX_HERO_LIFE, this.democratLives.get() + magnitude));
                break;
            case TRUMP:
                this.republicanLives.setValue(Math.min(MAX_HERO_LIFE, this.republicanLives.get() + magnitude));
                break;
        }
    }

    /** This function ends the current turn. If it's called by the AI it assigns the floor to the player,
     * if it isn't it assigns the floor to the AI.
     *
     * @param h Hero that is calling the function
     * @param isAI Whether the hero that is calling the function is currently handled by the AI or not
     */
    public void endTurn(Hero h, boolean isAI) {
        this.addToCombatLog(h.toString() + " has ended the turn");
        switch (h){
            case HILLARY:
                this.startTurn(Hero.TRUMP);
                if(!isAI){
                    this.autoPlayTurn(Hero.TRUMP);
                    this.tick();
                }
                break;
            case TRUMP:
                this.startTurn(Hero.HILLARY);
                if(!isAI){
                    this.autoPlayTurn(Hero.HILLARY);
                    this.tick();
                }
                break;
        }
    }

    private void endGame(Hero winner){
        this.endGameHandler.apply(winner);
    }

    public Optional<String> democratPlayCard(Card playedCard, Optional<Target> t){
        return playCard(Hero.HILLARY,playedCard,t);
    }
    public Optional<String> republicanPlayCard(Card playedCard, Optional<Target> t) {
            return playCard(Hero.TRUMP,playedCard, t);
    }

    //this boolean keeps track of whether the Hero has already used its power since it can only use it once per turn
    public boolean getHasUsedHeroPower() {
        return hasUsedHeroPower;
    }

    /**This function serves to your satisfaction when you are feeling particularly dumb.
     * It always plays the most expensive card it can get: it first sorts the hand in decreasing cost order, then it
     * chooses the first one it can afford. Loops until it has mana available.
     * Then it attacks the Hero with each card on the floor and
     * with its hero power. When it can't do anything anymore it calls endTurn, declaring that it is an AI.
     *
     * @param h Hero that is being handled by the AI
     */
    private void autoPlayTurn(Hero h){
        int manaAvailable = (int) (h == Hero.HILLARY ? this.democratMana.get() : this.republicanMana.get());
        ArrayList<Card> holder = new ArrayList<>(h == Hero.HILLARY ? this.democratHand : this.republicanHand);
        holder.sort(Card.CardComparator);
        while(!holder.isEmpty() && manaAvailable != 0) {
            Card topCard = holder.get(holder.size() - 1);
            if (topCard.cost <= manaAvailable) {
                State.getState().playCard(
                        h,
                        topCard,
                        topCard.doesCardNeedTarget() ? Optional.of(topCard.suggestTarget(h)) : Optional.empty()
                );
                manaAvailable -= topCard.cost;
            } else {
                holder.remove(holder.size() - 1);
            }
        }
        if (manaAvailable > 1){
            State.getState().useHeroPower(h, new Target(Hero.HILLARY));
        }

        (h == Hero.HILLARY ? this.democratFloor : this.republicanFloor).stream()
                .filter(c -> !c.hasAttackedThisTurn && !c.isSleeping.get())
                .forEach(c -> c.attack(new Target(h == Hero.HILLARY ? Hero.TRUMP : Hero.HILLARY)));
        this.endTurn(h, true);
    }

    /**
     * This function determines whether the Creature is on the board or not.
     *
     * @param c Creature card
     * @return empty optional if the card isn't on the board, returns the card otherwise
     */
    public Optional<PlayedCreature> isCreatureOnBoard(CreatureCard c){
        for(PlayedCreature p : this.getDemocratFloor()){
            if(p.creature.equals(c)){
                return Optional.of(p);
            }
        }

        for(PlayedCreature p : this.getRepublicanFloor()){
            if(p.creature.equals(c)){
                return Optional.of(p);
            }
        }

        return Optional.empty();
    }
}

