package sample;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import java.io.IOException;
import java.util.Optional;

/**
 * Type of card: Spell.
 * It contains the spell target, the effect and the magnitude of the action.
 * This class acts as a controller for the spellCard.fxml file, so it contains all information needed for rendering a
 * spell card.
 */
@SuppressWarnings("CanBeFinal")
public class SpellCard extends Card{
    private Pane targetingCardOverlayPane;
    private Button targetHero;
    private Button targetOne;
    private Button targetTwo;
    private Button targetThree;
    private Button targetFive;
    private Button targetFour;
    private Button targetSeven;
    private Button targetSix;
    EffectTarget target;
    SpellEffect effect;
    long magnitude;
    private boolean shouldRenderTargetUI = false;

    private ImageView cardImage;
    private Text spellName;
    private Text spellDescription;
    private Text spellCost;
    private ImageView cardBase;


    @Override
    public boolean doesCardNeedTarget() {
        return this.target == EffectTarget.TARGETED;
    }

    @Override
    public State.Target suggestTarget(State.Hero h) {
        if(this.effect == SpellEffect.SPELL_EFFECT_DAMAGE){
            return new State.Target(h == State.Hero.HILLARY ? State.Hero.TRUMP : State.Hero.HILLARY);
        }else{
            return new State.Target(h);
        }
    }

    /**
     * This function renders a spell type card, setting the image per image URL, and all other characteristics.
     *
     * @param h Hero who owns the card
     * @return the Node to render
     */
    public Node renderCard(State.Hero h){
        this.belongsTo = h;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("spellCard.fxml"));
        loader.setController(this);
        Node root = null;
        try {
            root = loader.load();
            this.cardBase.setImage(new Image("vendor/assets/compressed/spell_card_base.png"));
            this.cardImage.setImage(new Image("vendor/assets/spells/compressed/" + this.imageURL));
            this.spellName.setText(this.name);
            this.spellDescription.setText(this.description);
            this.spellCost.setText(String.valueOf(this.cost));
            this.targetingCardOverlayPane.setOpacity(this.shouldRenderTargetUI ? 1 : 0);

            if(this.shouldRenderTargetUI){
                this.targetHero.setDisable(false);
                switch (this.effect){
                    case SPELL_EFFECT_DAMAGE:
                        switch (this.belongsTo){
                            case HILLARY:
                                int numCreatures = State.getState().getRepublicanFloor().size();
                                for(int i = 1; i < 8; i++){
                                    this.getButton(i).setDisable(i > numCreatures);
                                }
                                break;
                            case TRUMP:
                                numCreatures = State.getState().getDemocratFloor().size();
                                for(int i = 1; i < 8; i++){
                                    this.getButton(i).setDisable(i > numCreatures);
                                }
                                break;
                        }
                        break;
                    case SPELL_EFFECT_HEAL:
                        switch (this.belongsTo){
                            case TRUMP:
                                int numCreatures = State.getState().getRepublicanFloor().size();
                                for(int i = 0; i < 7; i++){
                                    this.getButton(i).setDisable(i > numCreatures);
                                }
                                break;
                            case HILLARY:
                                numCreatures = State.getState().getDemocratFloor().size();
                                for(int i = 0; i < 7; i++){
                                    this.getButton(i).setDisable(i > numCreatures);
                                }
                                break;
                        }
                        break;
                }
            }

            return root;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return root;
    }

    /**
     * This function connects the button clicked to the target creature
     * @param i index of card to target
     * @return the Button related to that target
     */
    private Button getButton(int i) {
        switch (i){
            case 0:
                return this.targetOne;
            case 1:
                return this.targetTwo;
            case 2:
                return this.targetThree;
            case 3:
                return this.targetFour;
            case 4:
                return this.targetFive;
            case 5:
                return this.targetSix;
            case 6:
                return this.targetSeven;
        }
        return null;
    }

    public void cardClicked(){
        System.out.println(this.name + " was clicked");
        if(this.doesCardNeedTarget()){
            this.shouldRenderTargetUI = !this.shouldRenderTargetUI;
            State.getState().tick();
        }else{
            State.getState().playCard(this.belongsTo, this, Optional.empty());
        }
    }


    private void playWithTarget(State.Target t){
        State.getState().playCard(this.belongsTo, this, Optional.of(t));
    }

    /** This function sets the Hero as the target of an action
     *
     * @param mouseEvent click
     */
    public void setTargetHero(MouseEvent mouseEvent) {
        switch (this.effect){
            case SPELL_EFFECT_DAMAGE:
                this.playWithTarget(new State.Target(this.belongsTo == State.Hero.HILLARY ?
                        State.Hero.TRUMP
                        : State.Hero.HILLARY));
                break;
            case SPELL_EFFECT_HEAL:
                this.playWithTarget(new State.Target(this.belongsTo));
                break;
            default:
                break;
        }
    }


    /** This function sets the creature as the target of an action
     * @param i index of the creature to target
     */
    private void setTargetCreature(int i){
        switch(this.effect){
            case SPELL_EFFECT_DAMAGE:
                this.playWithTarget(new State.Target(this.belongsTo == State.Hero.HILLARY ?
                        State.getState().getRepublicanFloor().get(i)
                        : State.getState().getDemocratFloor().get(i)));
                break;
            case SPELL_EFFECT_HEAL:
                this.playWithTarget(new State.Target(this.belongsTo == State.Hero.TRUMP ?
                        State.getState().getRepublicanFloor().get(i)
                        : State.getState().getDemocratFloor().get(i)));
                break;
            default:
                break;
        }
    }

    public void setTargetOne(MouseEvent mouseEvent) {
        this.setTargetCreature(0);
    }

    public void setTargetTwo(MouseEvent mouseEvent) {
        this.setTargetCreature(1);
    }

    public void setTargetThree(MouseEvent mouseEvent) {
        this.setTargetCreature(2);
    }

    public void setTargetFive(MouseEvent mouseEvent) {
        this.setTargetCreature(4);
    }

    public void setTargetFour(MouseEvent mouseEvent) {
        this.setTargetCreature(3);
    }

    public void setTargetSeven(MouseEvent mouseEvent) {
        this.setTargetCreature(6);
    }

    public void setTargetSix(MouseEvent mouseEvent) {
        this.setTargetCreature(5);
    }

}

