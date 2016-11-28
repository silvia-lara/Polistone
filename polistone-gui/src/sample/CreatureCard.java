package sample;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Card type: Creature.
 * It contains the health and attack of the creature. It also acts as a controller for the creatureCard.fxml file
 * and so it contains all the features needed to render the card.
 */
@SuppressWarnings("CanBeFinal")
public class CreatureCard extends Card implements Initializable {


    public long health;
    public long attack;

    public CreatureEffect effect;
    public EffectTarget target;
    public EffectType type;
    public long magnitude;

    private Pane targetingCardOverlayPane;
    private Button targetHero;
    private Button targetOne;
    private Button targetTwo;
    private Button targetThree;
    private Button targetFive;
    private Button targetFour;
    private Button targetSeven;
    private Button targetSix;


    private ImageView cardImage;
    private Text creatureName;
    private Text creatureDescription;
    private Text creatureAttack;
    private Text creatureHealth;
    private Text creatureCost;
    private ImageView cardBase;
    @FXML
    private Text sleepingIndicator;
    private DoubleProperty targetOverlayOpacity = new SimpleDoubleProperty(0);
    public Node selfNode;

    @Override
    public boolean doesCardNeedTarget() {
        return this.target == EffectTarget.TARGETED;
    }

    public void isBeingAddedToBoard(State.PlayedCreature toBePlayed){
        CreatureCard that = this;
        if(this.sleepingIndicator == null){
            this.renderCard(this.belongsTo);
        }
        toBePlayed.isSleeping.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                that.sleepingIndicator.setOpacity(newValue ? 1 : 0);
            }
        });
        this.sleepingIndicator.setOpacity(1);
    }
    
    /**
     * This function renders a creature type card, setting the image per image URL, and all other characteristics.
     *
     * @param h Hero who owns the card
     * @return the Node to render
     */
    public Node renderCard(State.Hero h) {
        this.belongsTo = h;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("creatureCard.fxml"));
        loader.setController(this);
        Node root = null;
        try {
            root = loader.load();
            this.cardBase.setImage(new Image("vendor/assets/compressed/creature_card_base.png"));
            this.cardImage.setImage(new Image("vendor/assets/creatures/compressed/" + this.imageURL));
            this.creatureName.setText(this.name);
            this.creatureDescription.setText(this.description);
            this.creatureCost.setText(String.valueOf(this.cost));
            this.belongsTo = h;
            this.sleepingIndicator.setOpacity(0);
            this.targetingCardOverlayPane.setOpacity(0);
            CreatureCard that = this;
            this.targetOverlayOpacity.addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    Optional<State.PlayedCreature> isOnBoard = State.getState().isCreatureOnBoard(that);
                    if(isOnBoard.isPresent()){
                        if(isOnBoard.get().isSleeping.get()){
                            that.targetingCardOverlayPane.setOpacity(0);
                        }else{
                            that.targetingCardOverlayPane.setOpacity(newValue.doubleValue());
                        }
                    }
                    if(newValue.doubleValue() == 1){
                        State.PlayedCreature playedInstance = isOnBoard.get();
                        if(playedInstance.hasAttackedThisTurn){
                            that.targetHero.setDisable(true);
                            for(int i = 0; i < 7; i++){
                                that.getButton(i).setDisable(true);
                            }
                        }else{
                            that.targetHero.setDisable(false);
                            int count;
                            switch (that.belongsTo){
                                case HILLARY:
                                    count = State.getState().getRepublicanFloor().size();
                                    for(int i = 0; i < 7; i++){
                                        that.getButton(i).setDisable(i >= count);
                                    }
                                    break;
                                case TRUMP:
                                    count = State.getState().getDemocratFloor().size();
                                    for(int i = 0; i < 7; i++){
                                        that.getButton(i).setDisable(i >= count);
                                    }
                                    break;
                            }
                        }
                    }
                }
            });
            this.targetOverlayOpacity.setValue(0);
            this.reRenderCard();

            this.selfNode = root;
            return root;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return root;
    }

    public void reRenderCard(){
        this.creatureAttack.setText(String.valueOf(this.attack));
        this.creatureHealth.setText(String.valueOf(this.health));
    }

    public void cardClicked() {
        System.out.println(this.name + " was clicked");
        Optional<State.PlayedCreature> isOnBoard = State.getState().isCreatureOnBoard(this);
        if(isOnBoard.isPresent()){
            this.targetOverlayOpacity.setValue(this.targetOverlayOpacity.get() == 1 ? 0 : 1);
        }else{
            State.getState().playCard(this.belongsTo, this, Optional.empty());
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    /** This function sets the creature as the target of an action
     * @param i index of the creature to target
     */
    private void setTargetCreature(int i){
        Optional<State.PlayedCreature> isOnBoard = State.getState().isCreatureOnBoard(this);
        if(isOnBoard.isPresent()){
            State.PlayedCreature playedInstance = isOnBoard.get();
            playedInstance.attack(new State.Target(
                    this.belongsTo == State.Hero.HILLARY ?
                            State.getState().getRepublicanFloor().get(i)
                            : State.getState().getDemocratFloor().get(i)
            ));
            this.targetOverlayOpacity.setValue(0);
        }
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

    /** This function sets the Hero as the target of an action
     *
     * @param mouseEvent click
     */
    public void setTargetHero(MouseEvent mouseEvent) {
        Optional<State.PlayedCreature> isOnBoard = State.getState().isCreatureOnBoard(this);
        if(isOnBoard.isPresent()){
            State.PlayedCreature playedInstance = isOnBoard.get();
            playedInstance.attack(new State.Target(
                    this.belongsTo == State.Hero.HILLARY ?
                            State.Hero.TRUMP : State.Hero.HILLARY
            ));
            this.targetOverlayOpacity.setValue(0);
        }
    }
}
