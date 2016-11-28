package sample;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;

/**
 * Reads the Jason file and produce a card out of that input.
 */
@SuppressWarnings("WeakerAccess")
public class DeckFactory {

    public DeckFactory() {
    }

    private CardFamily makeCardFamily(String str){
        switch(str){
            case "DEMOCRATS":
                return CardFamily.DEMOCRATS;
            case "REPUBLICANS":
                return  CardFamily.REPUBLICANS;
            case "COMMON":
                return CardFamily.COMMON;
            default:
                assert false;
        }
        return null;
    }

    private SpellCard makeSpellCard(JSONObject v){
        SpellCard holder = new SpellCard();
        holder.family = this.makeCardFamily((String) v.get("family"));
        holder.name = (String) v.get("name");
        holder.description = (String) v.get("description");
        holder.imageURL = (String) v.get("image");
        holder.cost = (long) v.get("cost");

        JSONObject props = (JSONObject) v.get("spell_props");

        holder.effect = this.makeSpellEffect((String) props.get("effect"));
        holder.target = this.makeTarget((String) props.get("target"));
        holder.magnitude = (long) props.get("magnitude");

        return holder;
    }

    private SpellEffect makeSpellEffect(String effect) {
        switch (effect) {
            case "DRAW_CARD":
                return SpellEffect.DRAW_CARD;
            case "SPELL_EFFECT_DAMAGE":
                return SpellEffect.SPELL_EFFECT_DAMAGE;
            case "SPELL_EFFECT_HEAL":
                return SpellEffect.SPELL_EFFECT_HEAL;
            case "SPELL_EFFECT_SUMMON":
                return SpellEffect.SPELL_EFFECT_SUMMON;
            case "SPELL_EFFECT_NONE":
                return SpellEffect.SPELL_EFFECT_NONE;
            default:
                assert false;
        }
        return null;
    }

    private CreatureCard makeCreatureCard(JSONObject v){
        CreatureCard holder = new CreatureCard();
        holder.family = this.makeCardFamily((String) v.get("family"));
        holder.name = (String) v.get("name");
        holder.description = (String) v.get("description");
        holder.imageURL = (String) v.get("image");
        holder.cost = (long) v.get("cost");

        JSONObject props = (JSONObject) v.get("creature_props");
        holder.health = (long) props.get("health");
        holder.attack = (long) props.get("attack");
        holder.effect = this.makeCreatureEffect((String) props.get("effect"));
        holder.target = this.makeTarget((String) props.get("target"));
        holder.type   = this.makeType((String) props.get("type"));
        holder.magnitude = (long) props.get("magnitude");

        return holder;
    }

    private EffectType makeType(String type) {
        switch (type) {
            case "EFFECT_TYPE_DAMAGE":
                return EffectType.EFFECT_TYPE_DAMAGE;
            case "EFFECT_TYPE_HEAL":
                return EffectType.EFFECT_TYPE_HEAL;
            case "EFFECT_TYPE_NONE":
                return EffectType.EFFECT_TYPE_NONE;
            default:
                assert false;
        }
        return null;
    }

    private EffectTarget makeTarget(String target) {
        switch ( target ){
            case "TARGETED":
                return EffectTarget.TARGETED;
            case "RANDOM":
                return EffectTarget.RANDOM;
            case "ENEMY_AOE":
                return EffectTarget.ENEMY_AOE;
            case "FRIENDLY_AOE":
                return EffectTarget.FRIENDLY_AOE;
            case "ALL_AOE":
                return EffectTarget.ALL_AOE;
            case "ENEMY_HERO":
                return EffectTarget.ENEMY_HERO;
            case "YOUR_HERO":
                return EffectTarget.YOUR_HERO;
            case "EFFECT_TARGET_NONE":
                return EffectTarget.EFFECT_TARGET_NONE;
            default:
                assert false;
        }
        return null;
    }

    private CreatureEffect makeCreatureEffect(String effect) {
        switch (effect) {
            case "DEATHRATTLE":
                return CreatureEffect.DEATHRATTLE;
            case "SUMMON":
                return CreatureEffect.SUMMON;
            case "CREATURE_EFFECT_NONE":
                return CreatureEffect.CREATURE_EFFECT_NONE;
            default:
                assert false;
        }
        return null;
    }

    private Card makeCard(JSONObject v){
        switch ( (String)v.get("type")){
            case "CREATURE":
                return this.makeCreatureCard(v);
            case "SPELL":
                return this.makeSpellCard(v);
            default:
                assert false;
        }
        return null;
    }

    public ArrayList<Card> makeDeck(JSONArray v){
        ArrayList<Card> holder = new ArrayList<>();
        for(Object o : v) {
            holder.add(this.makeCard((JSONObject) o));
        }
        return holder;
    }
}
