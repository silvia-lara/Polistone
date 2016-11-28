package sample;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

import java.util.Comparator;

/**Card class:
 * Contains: family it belongs to (rep or dem), name of the card, description, image URL and its cost, contains also
 * a comparator and a function that renders the card's back
 */
@SuppressWarnings("WeakerAccess")
public class Card {

    public CardFamily family;
    public String name;
    public String description;
    public String imageURL;
    public long cost;

    /**
     * This Function compares two instances of cards, considering their cost.
     */
    public static final Comparator<Card> CardComparator = new Comparator<Card>() {
        @Override
        public int compare(Card o1, Card o2) {
            Integer a = (int) o1.cost;
            Integer b = (int) o2.cost;
            return a.compareTo(b);
        }
    };
    public State.Hero belongsTo;

    public boolean doesCardNeedTarget(){
        return false;
    }

    public State.Target suggestTarget(State.Hero h){
        return null;
    }

    /**
     * This function renders the cards' back.
     * @param h
     * @return
     */
    public Node renderCardBack(State.Hero h){
        this.belongsTo = h;
        Rectangle card = new Rectangle();
        card.setHeight(152);
        card.setWidth(106.4);
        ImagePattern p = new ImagePattern(new Image("vendor/assets/card_back.png"));
        card.setFill(p);

        return card;
    }

}
