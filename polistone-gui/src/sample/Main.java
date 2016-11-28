package sample;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.Scene;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.text.*;
import javafx.scene.layout.Pane;
import javafx.scene.Parent;
import javafx.stage.*;
import javafx.util.Callback;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Main Function.
 */
@SuppressWarnings({"unchecked", "CanBeFinal"})
public class Main extends Application implements Initializable {

    //import variables that connect the fxml files to the main
    private Button demBtn;
    private Button repBtn;
    private Text loadingStatusText;
    public Text textAnchor;
    private ImageView mapBase;
    private ImageView yourHeroPower;
    private ImageView opponentHeroPower;
    private GridPane opponentHand;
    private GridPane yourHand;
    private Button loadDeckBtn;
    public Button exitGameBtn;
    private ImageView opponentHeroPortrait;
    private ImageView yourHeroPortrait;
    private Text yourHeroMana;
    private Text yourHeroHealth;
    private Text opponentHeroMana;
    private Text opponentHeroHealth;
    private GridPane yourFloor;
    private GridPane opponentFloor;
    private Pane opponentHoveringCard;
    private Pane yourHoveringCard;
    private Button targetHero;
    private Button targetTwo;
    private Button targetSix;
    private Button targetFive;
    private Button targetFour;
    private Button targetThree;
    private Button targetOne;
    private Button targetSeven;
    private Button endTurn;
    private Text endGameText;
    private ListView combatLogView;
    private Stage priStage;
    private State.Hero ourHero;
    private Pane targetingCardOverlayPane;
    private DoubleProperty heroPowerTargetingPaneOpacity = new SimpleDoubleProperty(0);
    private ArrayList<Card> demCards;
    private ArrayList<Card> repCards;

    public void start(Stage primaryStage) throws Exception{
        this.priStage = primaryStage;
        this.showLoadingScreen();
    }

    private void loadJSON(){
        try {
            this.loadingStatusText.setText("Attempting to read deck.json");
            FileReader reader = new FileReader("../cardGen/deck.json");

            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);
            DeckFactory deckBuilder = new DeckFactory();
            this.demCards = deckBuilder.makeDeck((JSONArray) jsonObject.get("democrat"));
            this.repCards = deckBuilder.makeDeck((JSONArray) jsonObject.get("republican"));

            this.loadingStatusText.setText("Everything is loaded, please choose a party to start playing!");
            this.demBtn.setDisable(false);
            this.repBtn.setDisable(false);

        } catch (FileNotFoundException ex) {
            this.loadingStatusText.setText("deck.json not found, why not try running make cardGen in the cardGen folder");
            ex.printStackTrace();
        } catch (IOException ex) {
            this.loadingStatusText.setText("Some IO exception, check file permissions?");
            ex.printStackTrace();
        } catch (ParseException ex) {
            this.loadingStatusText.setText("The given deck.json was not valid json, are you sure you didn't edit it? I don't make mistakes in my C code...");
            ex.printStackTrace();
        } catch (NullPointerException ex) {
            this.loadingStatusText.setText("Something's fucky, you should never see this..");
            ex.printStackTrace();
        }
    }

    private void showLoadingScreen() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("loadingScreen.fxml"));
        loader.setController(this);
        Parent root = loader.load();
        this.priStage.setTitle("Polistone");
        this.priStage.setScene(new Scene(root));
        this.priStage.show();
    }

    public void chooseDem(){
        this.ourHero = State.Hero.HILLARY;
        this.initGameBoard();
    }

    public void chooseRep(){
        this.ourHero = State.Hero.TRUMP;
        this.initGameBoard();
    }

    /**
     * This function initializes the gameboard. It acts as a controller for the gameboard.fxml file.
     * The initial stage is rendered. The combat log is created, the End of Game and Tick functions are handled
     */
    private void initGameBoard(){
        FXMLLoader loader = new FXMLLoader(getClass().getResource("gameBoard.fxml"));
        loader.setController(this);
        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.priStage.setScene(new Scene(root));
        this.priStage.show();
        this.priStage.setResizable(true);

        this.mapBase.setImage(new Image("vendor/assets/compressed/map_base.png"));
        this.yourHeroPower.setImage(new Image(
                this.ourHero == State.Hero.HILLARY ?
                "vendor/assets/compressed/hillary_hero_power.png" : "vendor/assets/compressed/trump_hero_power.png"));
        this.opponentHeroPower.setImage(new Image(
                this.ourHero == State.Hero.TRUMP ?
                        "vendor/assets/compressed/hillary_hero_power.png" : "vendor/assets/compressed/trump_hero_power.png"));
        this.yourHeroPortrait.setImage(new Image(
                this.ourHero == State.Hero.HILLARY ?
                        "vendor/assets/compressed/hillary_potrait.png" : "vendor/assets/compressed/trump_potrait.png"
        ));
        this.opponentHeroPortrait.setImage(new Image(
                this.ourHero == State.Hero.TRUMP ?
                        "vendor/assets/compressed/hillary_potrait.png" : "vendor/assets/compressed/trump_potrait.png"
        ));

        double combatLogWrappingWidth = this.combatLogView.getPrefWidth() - 20;
        this.combatLogView.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> list) {
                return new ListCell() {
                    private Text text;

                    @Override
                    public void updateItem(Object item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!isEmpty()) {
                            text = new Text(item.toString());
                            text.setWrappingWidth(combatLogWrappingWidth);
                            setGraphic(text);
                        }
                    }
                };
            }
        });
        this.combatLogView.setItems(State.getState().combatLog);

        State.getState().setTickHandler(s -> {
            this.renderBoard();
            return new Object();
        });

        State.getState().setEndGameHandler( winner -> {
            this.renderEndGame(winner);
            return new Object();
        });

        this.yourHeroHealth.textProperty().bind(
                this.ourHero == State.Hero.HILLARY ?
                        State.getState().democratLives.asString()
                        : State.getState().republicanLives.asString()
        );
        this.opponentHeroHealth.textProperty().bind(
                this.ourHero == State.Hero.HILLARY ?
                        State.getState().republicanLives.asString()
                        : State.getState().democratLives.asString()
        );
        this.yourHeroMana.textProperty().bind(
                this.ourHero == State.Hero.HILLARY ?
                        State.getState().democratMana.asString()
                        : State.getState().republicanMana.asString()
        );
        this.opponentHeroMana.textProperty().bind(
                this.ourHero == State.Hero.HILLARY ?
                        State.getState().republicanMana.asString()
                        : State.getState().democratMana.asString()
        );
        this.targetingCardOverlayPane.opacityProperty().bind(
                this.heroPowerTargetingPaneOpacity
        );

        Main that = this;
        this.heroPowerTargetingPaneOpacity.addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if(newValue.doubleValue() == 1){
                    boolean hasUsedHeroPower = State.getState().getHasUsedHeroPower();
                    that.targetHero.setDisable(hasUsedHeroPower);
                    int numCreatures = State.getState().getDemocratFloor().size();
                    for(int i = 1; i < 8; i++){
                        that.getButton(i).setDisable(hasUsedHeroPower || (i > numCreatures));
                    }
                }
            }
        });

        this.opponentFloor.getChildren().clear();
        this.yourFloor.getChildren().clear();
        State.getState().getDemocratHand().addListener(new ListChangeListener<Card>() {
            @SuppressWarnings("UnusedAssignment")
            @Override
            public void onChanged(Change<? extends Card> c) {
                while(c.next()){
                    for(Card addedCard : c.getAddedSubList()){
                        Node nodeToAdd = addedCard.renderCardBack(that.ourHero);
                        int i = State.getState().getDemocratHand().indexOf(addedCard);
                        if(addedCard instanceof CreatureCard){
                            switch(that.ourHero){
                                case HILLARY:
                                    nodeToAdd = ((CreatureCard) addedCard).renderCard(State.Hero.HILLARY);
                                    break;
                                case TRUMP:
                                    nodeToAdd = addedCard.renderCardBack(State.Hero.HILLARY);
                                    break;
                            }
                        }else{
                            switch(that.ourHero){
                                case HILLARY:
                                    nodeToAdd = ((SpellCard) addedCard).renderCard(State.Hero.HILLARY);
                                    break;
                                case TRUMP:
                                    nodeToAdd = addedCard.renderCardBack(State.Hero.HILLARY);
                                    break;
                            }
                        }
                        switch(that.ourHero){
                            case HILLARY:
                                that.yourHand.getChildren().add(i + 1, nodeToAdd);
                                break;
                            case TRUMP:
                                that.opponentHand.getChildren().add(i + 1, nodeToAdd);
                                break;
                        }
                        GridPane.setColumnIndex(nodeToAdd, i + 1);
                    }
                    if(c.wasRemoved()){
                        int i = 1;
                        switch(that.ourHero){
                            case HILLARY:
                                that.yourHand.getChildren().remove(1, that.yourHand.getChildren().size());
                                i = 1;
                                for (Card card : State.getState().getDemocratHand()) {
                                    Node nodeToAdd;
                                    if(card instanceof CreatureCard){
                                        nodeToAdd = ((CreatureCard) card).renderCard(State.Hero.HILLARY);
                                    }else{
                                        nodeToAdd = ((SpellCard) card).renderCard(State.Hero.HILLARY);
                                    }
                                    that.yourHand.getChildren().add(i, nodeToAdd);
                                    GridPane.setColumnIndex(nodeToAdd, i++);
                                }
                                break;
                            case TRUMP:
                                that.opponentHand.getChildren().remove(1, that.opponentHand.getChildren().size());
                                i = 1;
                                for (Card card : State.getState().getDemocratHand()) {
                                    Node nodeToAdd;
                                    nodeToAdd = card.renderCardBack(State.Hero.HILLARY);
                                    that.opponentHand.getChildren().add(i, nodeToAdd);
                                    GridPane.setColumnIndex(nodeToAdd, i++);
                                }
                                break;
                        }
                    }
                }
            }
        });
        State.getState().getRepublicanHand().addListener(new ListChangeListener<Card>() {
            @Override
            public void onChanged(Change<? extends Card> c) {
                while(c.next()){
                    for(Card addedCard : c.getAddedSubList()){
                        Node nodeToAdd = addedCard.renderCardBack(that.ourHero);
                        int i = State.getState().getRepublicanHand().indexOf(addedCard);
                        if(addedCard instanceof CreatureCard){
                            switch(that.ourHero){
                                case TRUMP:
                                    nodeToAdd = ((CreatureCard) addedCard).renderCard(State.Hero.TRUMP);
                                    break;
                                case HILLARY:
                                    nodeToAdd = addedCard.renderCardBack(State.Hero.TRUMP);
                                    break;
                            }
                        }else{
                            switch(that.ourHero){
                                case TRUMP:
                                    nodeToAdd = ((SpellCard) addedCard).renderCard(State.Hero.TRUMP);
                                    break;
                                case HILLARY:
                                    nodeToAdd = addedCard.renderCardBack(State.Hero.TRUMP);
                                    break;
                            }
                        }
                        switch(that.ourHero){
                            case TRUMP:
                                that.yourHand.getChildren().add(i + 1, nodeToAdd);
                                break;
                            case HILLARY:
                                that.opponentHand.getChildren().add(i + 1, nodeToAdd);
                                break;
                        }
                        GridPane.setColumnIndex(nodeToAdd, i + 1);
                    }
                    if(c.wasRemoved()){
                        int i;
                        switch(that.ourHero){
                            case TRUMP:
                                that.yourHand.getChildren().remove(1, that.yourHand.getChildren().size());
                                i = 1;
                                for (Card card : State.getState().getRepublicanHand()) {
                                    Node nodeToAdd;
                                    if(card instanceof CreatureCard){
                                        nodeToAdd = ((CreatureCard) card).renderCard(State.Hero.TRUMP);
                                    }else{
                                        nodeToAdd = ((SpellCard) card).renderCard(State.Hero.TRUMP);
                                    }
                                    that.yourHand.getChildren().add(i, nodeToAdd);
                                    GridPane.setColumnIndex(nodeToAdd, i++);
                                }
                                break;
                            case HILLARY:
                                that.opponentHand.getChildren().remove(1, that.opponentHand.getChildren().size());
                                i = 1;
                                for (Card card : State.getState().getRepublicanHand()) {
                                    Node nodeToAdd;
                                    nodeToAdd = card.renderCardBack(State.Hero.TRUMP);
                                    that.opponentHand.getChildren().add(i, nodeToAdd);
                                    GridPane.setColumnIndex(nodeToAdd, i++);
                                }
                                break;
                        }
                    }
                }
            }
        });
        State.getState().getDemocratFloor().addListener(new ListChangeListener<State.PlayedCreature>() {
            @Override
            public void onChanged(Change<? extends State.PlayedCreature> c) {
                while(c.next()){
                    for(State.PlayedCreature p : c.getAddedSubList()){
                        switch(that.ourHero){
                            case HILLARY:
                                that.yourFloor.getChildren().add(p.creature.selfNode);
                                GridPane.setColumnIndex(p.creature.selfNode, State.getState().getDemocratFloor().size() - 1);
                                break;
                            case TRUMP:
                                that.opponentFloor.getChildren().add(p.creature.selfNode);
                                GridPane.setColumnIndex(p.creature.selfNode, State.getState().getDemocratFloor().size() - 1);
                                break;
                        }
                    }
                    if(c.wasRemoved()){
                        int i = 0;
                        switch(that.ourHero){
                            case HILLARY:
                                that.yourFloor.getChildren().clear();
                                for(State.PlayedCreature p : State.getState().getDemocratFloor()){
                                    that.yourFloor.getChildren().add(i, p.creature.selfNode);
                                    GridPane.setColumnIndex(p.creature.selfNode, i++);
                                }
                                break;
                            case TRUMP:
                                that.opponentFloor.getChildren().clear();
                                i = 0;
                                for(State.PlayedCreature p : State.getState().getDemocratFloor()){
                                    that.opponentFloor.getChildren().add(i, p.creature.selfNode);
                                    GridPane.setColumnIndex(p.creature.selfNode, i++);
                                }
                                break;
                        }
                    }
                }
            }
        });
        State.getState().getRepublicanFloor().addListener(new ListChangeListener<State.PlayedCreature>() {
            @Override
            public void onChanged(Change<? extends State.PlayedCreature> c) {
                while(c.next()){
                    for(State.PlayedCreature p : c.getAddedSubList()){
                        switch(that.ourHero){
                            case TRUMP:
                                that.yourFloor.getChildren().add(p.creature.selfNode);
                                GridPane.setColumnIndex(p.creature.selfNode, State.getState().getRepublicanFloor().size() - 1);
                                break;
                            case HILLARY:
                                that.opponentFloor.getChildren().add(p.creature.selfNode);
                                GridPane.setColumnIndex(p.creature.selfNode, State.getState().getRepublicanFloor().size() - 1);
                                break;
                        }
                    }
                    if(c.wasRemoved()){
                        int i = 0;
                        switch(that.ourHero){
                            case TRUMP:
                                that.yourFloor.getChildren().clear();
                                for(State.PlayedCreature p : State.getState().getRepublicanFloor()){
                                    that.yourFloor.getChildren().add(i, p.creature.selfNode);
                                    GridPane.setColumnIndex(p.creature.selfNode, i++);
                                }
                                break;
                            case HILLARY:
                                that.opponentFloor.getChildren().clear();
                                i = 0;
                                for(State.PlayedCreature p : State.getState().getRepublicanFloor()){
                                    that.opponentFloor.getChildren().add(i, p.creature.selfNode);
                                    GridPane.setColumnIndex(p.creature.selfNode, i++);
                                }
                                break;
                        }
                    }
                }
            }
        });


        State.getState().init(this.demCards, this.repCards);
        State.getState().startTurn(this.ourHero);
    }

    /**
     * This function renders the end of the game. It acts as a controller for the endGame.fxml file.
     * It saves the combat log to a gameLog.log file.
     * @param winner The hero which has the most HP
     */
    private void renderEndGame(State.Hero winner){
        FXMLLoader loader = new FXMLLoader(getClass().getResource("endGame.fxml"));
        loader.setController(this);
        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.priStage.setScene(new Scene(root));
        this.priStage.show();
        this.priStage.setResizable(false);

        this.endGameText.setText(winner == this.ourHero ? "You Won!" : "You lost");

        PrintWriter printWriter = null;
        try {
            printWriter = new PrintWriter("gameLog.log", "UTF-8");
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        for(String log : State.getState().combatLog){
            printWriter.println(log);
        }
        printWriter.close();
    }

    private void renderBoard(){
        System.out.println("Starting render");
        this.renderHoveringCards();
        System.out.println("Hovering cards done");
        this.renderEndTurnButton();
    }

    private void renderEndTurnButton(){
        if(State.getState().getWhoseTurn() == this.ourHero){
            this.endTurn.setDisable(false);
            this.endTurn.setText("End Turn");
        }else{
            this.endTurn.setDisable(true);
            this.endTurn.setText("Opponent Turn");
        }
    }

    private Button getButton(int i){
        switch (i){
            case 1:
                return this.targetOne;
            case 2:
                return this.targetTwo;
            case 3:
                return this.targetThree;
            case 4:
                return this.targetFour;
            case 5:
                return this.targetFive;
            case 6:
                return this.targetSix;
            case 7:
                return this.targetSeven;
        }
        return null;
    }



    private void renderHoveringCards() {
        //Update your Hovering card
        Node nodeToAdd;
        Optional<State.PlayedSpell> cardToRender = (
                this.ourHero == State.Hero.HILLARY ?
                        State.getState().getDemocratHoveringCard()
                        : State.getState().getRepublicanHoveringCard()
        );
        if (cardToRender.isPresent()) {
            nodeToAdd = cardToRender.get().spell.renderCard(this.ourHero);
            this.yourHoveringCard.getChildren().add(nodeToAdd);
        }

        //Update opponent Hovering card
        cardToRender = (
                this.ourHero == State.Hero.TRUMP ?
                        State.getState().getDemocratHoveringCard()
                        : State.getState().getRepublicanHoveringCard()
        );
        if (cardToRender.isPresent()) {
            nodeToAdd = cardToRender.get().spell.renderCard(
                    this.ourHero == State.Hero.HILLARY ?
                            State.Hero.TRUMP
                            : State.Hero.HILLARY
            );
            this.opponentHoveringCard.getChildren().add(nodeToAdd);
        }

    }


    public static void main(String[] args) {
        Application.launch(Main.class, null);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void loadDeck(MouseEvent mouseEvent) {
        this.loadDeckBtn.setDisable(true);
        this.loadJSON();
    }

    public void exitGame(MouseEvent mouseEvent) {
        System.exit(0);
    }


    public void setTargetHero(MouseEvent mouseEvent) {
        State.getState().useHeroPower(this.ourHero, new State.Target(State.Hero.HILLARY));
    }

    public void setTargetOne(MouseEvent mouseEvent) {
        State.getState().useHeroPower(this.ourHero, new State.Target(State.getState().getDemocratFloor().get(0)));
    }
    public void setTargetTwo(MouseEvent mouseEvent) {
        State.getState().useHeroPower(this.ourHero, new State.Target(State.getState().getDemocratFloor().get(1)));
    }
    public void setTargetThree(MouseEvent mouseEvent) {
        State.getState().useHeroPower(this.ourHero, new State.Target(State.getState().getDemocratFloor().get(2)));
    }
    public void setTargetFour(MouseEvent mouseEvent) {
        State.getState().useHeroPower(this.ourHero, new State.Target(State.getState().getDemocratFloor().get(3)));
    }
    public void setTargetFive(MouseEvent mouseEvent) {
        State.getState().useHeroPower(this.ourHero, new State.Target(State.getState().getDemocratFloor().get(4)));
    }
    public void setTargetSix(MouseEvent mouseEvent) {
        State.getState().useHeroPower(this.ourHero, new State.Target(State.getState().getDemocratFloor().get(5)));
    }
    public void setTargetSeven(MouseEvent mouseEvent) {
        State.getState().useHeroPower(this.ourHero, new State.Target(State.getState().getDemocratFloor().get(6)));
    }

    public void heroPowerBtnClickHandler(MouseEvent mouseEvent) {
        this.heroPowerTargetingPaneOpacity.setValue(
                this.heroPowerTargetingPaneOpacity.get() == 1 ? 0 : 1
        );
    }

    public void endTurnFunction(MouseEvent mouseEvent) {
        this.endTurn.setDisable(true);
        this.endTurn.setText("Opponent Turn");
        State.getState().shouldTick = false;
        State.getState().endTurn(this.ourHero, false);
        this.heroPowerTargetingPaneOpacity.setValue(0);
        State.getState().shouldTick = true;
        State.getState().tick();
    }
}
