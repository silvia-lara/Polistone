package sample;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Test the validity of the Jason file and whether DeckFactory produced a meaningful input
 */
public class StateTest {
    @org.junit.Before
    public void setUp() throws Exception {
        State.getState().reset();
    }

    @org.junit.After
    public void tearDown() throws Exception {
        State.getState().reset();
    }

    // Only an internal helper function
    private ArrayList<ArrayList<Card>> getCards(){
        DeckFactory builder = new DeckFactory();
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = null;
        try {
            jsonObject = (JSONObject) jsonParser.parse(new FileReader("../cardGen/deck.json"));
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        DeckFactory deckBuilder = new DeckFactory();
        ArrayList<Card> democrats = deckBuilder.makeDeck((JSONArray) jsonObject.get("democrat"));
        ArrayList<Card> republicans = deckBuilder.makeDeck((JSONArray) jsonObject.get("republican"));
        ArrayList<ArrayList<Card>> holder = new ArrayList<>();
        holder.add(democrats);
        holder.add(republicans);
        return holder;
    }

    @org.junit.Test
    public void testSetupOfState(){
        ArrayList<ArrayList<Card>> decks = getCards();
        assertTrue("Should return true when correct number of cards were given to init", State.getState().init(decks.get(0), decks.get(1)));
        State.getState().reset();
        decks.get(0).remove(0);
        assertFalse("Should return false when wrong number of cards were given to init", State.getState().init(decks.get(0), decks.get(1)));



        State.getState().reset();
        decks = getCards();
        State.getState().init(decks.get(0), decks.get(1));
        assertEquals("Expected your hand to have 4 cards", 4, State.getState().getDemocratHand().size());
        assertEquals("Expected enemy hand to have 4 cards", 4, State.getState().getRepublicanHand().size());
    }


}