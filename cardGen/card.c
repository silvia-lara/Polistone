#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <math.h>
#include <time.h>
#include <assert.h>

#define NAME_SIZE 40
#define DESCRIPTION_SIZE 200

typedef enum CARD_TYPE { CREATURE, SPELL } CARD_TYPE;
typedef enum CARD_FAMILY { DEMOCRATS, REPUBLICANS, COMMON } CARD_FAMILY;
typedef enum CREATURE_EFFECT { DEATHRATTLE, SUMMON, CREATURE_EFFECT_NONE } CREATURE_EFFECT;
typedef enum SPELL_EFFECT { DRAW_CARD, SPELL_EFFECT_DAMAGE, SPELL_EFFECT_HEAL, SPELL_EFFECT_SUMMON, SPELL_EFFECT_NONE } SPELL_EFFECT;
typedef enum EFFECT_TARGET { TARGETED, RANDOM, ENEMY_AOE, FRIENDLY_AOE, ALL_AOE, ENEMY_HERO, YOUR_HERO, EFFECT_TARGET_NONE } EFFECT_TARGET;
typedef enum EFFECT_TYPE { EFFECT_TYPE_DAMAGE, EFFECT_TYPE_HEAL, EFFECT_TYPE_NONE } EFFECT_TYPE;
typedef enum CARD_STRENGTH { LOW, MED, HIGH } CARD_STRENGTH;

static char* CARD_TYPE_STRING[] = {"CREATURE", "SPELL"};
static char* CARD_FAMILY_STRING[] = {"DEMOCRATS", "REPUBLICANS", "COMMON"};
static char* CREATURE_EFFECT_STRING[] = {"DEATHRATTLE", "SUMMON", "CREATURE_EFFECT_NONE"};
static char* SPELL_EFFECT_STRING[] = {"DRAW_CARD", "SPELL_EFFECT_DAMAGE", "SPELL_EFFECT_HEAL", "SPELL_EFFECT_SUMMON", "SPELL_EFFECT_NONE"};
static char* EFFECT_TARGET_STRING[] = {"TARGETED", "RANDOM", "ENEMY_AOE", "FRIENDLY_AOE", "ALL_AOE", "ENEMY_HERO", "YOUR_HERO", "EFFECT_TARGET_NONE"};
static char* EFFECT_TYPE_STRING[] = {"EFFECT_TYPE_DAMAGE", "EFFECT_TYPE_HEAL", "EFFECT_TYPE_NONE"};
static char* CARD_STRENGTH_STRING[] = {"LOW", "MED", "HIGH"};


typedef struct CREATURE_PROPS { 
  short int health;
  short int attack;
  
  CREATURE_EFFECT effect;
  EFFECT_TARGET target;
  EFFECT_TYPE type;
  short int magnitude;
} CREATURE_PROPS;

static CREATURE_PROPS emptyCreatureProp = {0,0,CREATURE_EFFECT_NONE,EFFECT_TARGET_NONE,EFFECT_TYPE_NONE,0};

typedef struct SPELL_PROPS{  
  EFFECT_TARGET target;
  SPELL_EFFECT effect;
  short int magnitude;
} SPELL_PROPS;

static SPELL_PROPS emptySpellProp = { EFFECT_TARGET_NONE,SPELL_EFFECT_NONE,0 };

typedef struct CARD {
  CARD_TYPE type;
  CARD_FAMILY family;
  char name[NAME_SIZE];
  char description[DESCRIPTION_SIZE];
  char image[NAME_SIZE];
  unsigned short int cost;

  CREATURE_PROPS creature_props;
  SPELL_PROPS spell_props;
} CARD;

int rand_int(int n) {
  int limit = RAND_MAX - RAND_MAX % n;
  int rnd;

  srand(time(NULL));
  do {
    rnd = rand();
  } while (rnd >= limit);
  return rnd % n;
}

int rand_range(int n, int m){
  srand(time(NULL));
  /* bad rng when range is narrow */
  if(m == n + 1){
    return (rand() % 2) ? m : n;
  }
  return n + rand_int((m + 1) - n);
}

void generate_democrat_spell(CARD* holder, CARD_STRENGTH strn, int cardCost){
  int budget = (cardCost * 2) + 1;
  int toss = 0;
  char buffer[DESCRIPTION_SIZE] = {0};
  holder->family = DEMOCRATS;
  switch(strn){
    case LOW:
        toss = rand_range(1,2);
        if(toss == 1){
          stpncpy(holder->name, "Public Infrastructure", NAME_SIZE);
          sprintf(
              buffer,
              "Spend money on public works."
              "Boosts morale, healing all "
              "friendly creatures for %d health",
              (short int)ceil(budget/3.0));
          stpncpy(holder->description, buffer, DESCRIPTION_SIZE);
          stpncpy(holder->image, "public_infra.png", NAME_SIZE);
          holder->spell_props.target = FRIENDLY_AOE;
          holder->spell_props.effect = SPELL_EFFECT_HEAL;
          holder->spell_props.magnitude = (short int)ceil(budget/3.0);
        }else{
          stpncpy(holder->name, "Racial Equality", NAME_SIZE);
          sprintf(
              buffer,
              "Promote racial equality, "
              "healing a targeted friendly minion "
              "for %d health",
              budget - 1);
          stpncpy(holder->description, buffer, DESCRIPTION_SIZE);
          stpncpy(holder->image, "racial_equality.png", NAME_SIZE);
          holder->spell_props.target = TARGETED;
          holder->spell_props.effect = SPELL_EFFECT_HEAL;
          holder->spell_props.magnitude = budget - 1;
        }
      break;
    case MED:
      stpncpy(holder->name, "Free College", NAME_SIZE);
      sprintf(
          buffer,
          "Forgive student debts, increasing "
          "opportunities for everyone! Draw "
          "%d cards",
          (short int)floor(budget / 4.0));
      stpncpy(holder->description, buffer, DESCRIPTION_SIZE);
      stpncpy(holder->image, "college_education.png", NAME_SIZE);
      holder->spell_props.target = YOUR_HERO;
      holder->spell_props.effect = DRAW_CARD;
      holder->spell_props.magnitude = (short int)floor(budget / 4.0);
      break;
    case HIGH:
      stpncpy(holder->name, "Sexual Scandal", NAME_SIZE);
      sprintf(
          buffer,
          "You can't unsee what you have seen. "
          "Deal %d damage to all enemy characters ",
          (short int)floor(budget / 5.0));
      stpncpy(holder->description, buffer, DESCRIPTION_SIZE);
      stpncpy(holder->image, "sexual_scandal.png", NAME_SIZE);
      holder->spell_props.target = ENEMY_AOE;
      holder->spell_props.effect = SPELL_EFFECT_DAMAGE;
      holder->spell_props.magnitude = (short int)floor(budget/5.0);
      break;
  }
}

void generate_republican_spell(CARD* holder, CARD_STRENGTH strn, int cardCost){
  int budget = (cardCost * 2) + 1;
  int toss = 0;
  char buffer[DESCRIPTION_SIZE] = {0};
  holder->family = REPUBLICANS;
  switch(strn){
    case LOW:
        toss = rand_range(1,2);
        if(toss == 1){
          stpncpy(holder->name, "Fox News Interview", NAME_SIZE);
          sprintf(
              buffer,
              "Leaves enemy speechless, "
              "attacking the Hero for  "
              "%d damage"               ,
              (short int)ceil(budget/2.0));
          stpncpy(holder->description, buffer, DESCRIPTION_SIZE);
          stpncpy(holder->image, "fox_news.png", NAME_SIZE);
          holder->spell_props.target = ENEMY_HERO;
          holder->spell_props.effect = SPELL_EFFECT_DAMAGE;
          holder->spell_props.magnitude = (short int)ceil(budget/2.0);
        }else{
          stpncpy(holder->name, "Tweet Storm", NAME_SIZE);
          sprintf(
              buffer,
              "The hero tweets at 2 am, inficting "
              "%d damage to a random target.",
              budget + 1);
          stpncpy(holder->description, buffer, DESCRIPTION_SIZE);
          stpncpy(holder->image, "tweet_storm.png", NAME_SIZE);
          holder->spell_props.target = RANDOM;
          holder->spell_props.effect = SPELL_EFFECT_DAMAGE;
          holder->spell_props.magnitude = budget + 1;
        }
      break;
    case MED:
      stpncpy(holder->name, "Less Taxes", NAME_SIZE);
      sprintf(
          buffer,
          "Decreases tax rates, making "
          "republicans happy, deal "
          "%d health to friendly minions",
          (short int)ceil(budget / 4.0));
      stpncpy(holder->description, buffer, DESCRIPTION_SIZE);
      stpncpy(holder->image, "less_tax.png", NAME_SIZE);
      holder->spell_props.target = YOUR_HERO;
      holder->spell_props.effect = DRAW_CARD;
      holder->spell_props.magnitude = (short int)ceil(budget / 4.0);
      break;
    case HIGH:
      stpncpy(holder->name, "Grab Kitty", NAME_SIZE);
      sprintf(
          buffer,
          "Everyone is shocked. The nation suffers. "
          "Deal %d damage to all characters",
          (short int)floor(budget / 5.0));
      stpncpy(holder->description, buffer, DESCRIPTION_SIZE);
      stpncpy(holder->image, "sexual_scandal.png", NAME_SIZE);
      holder->spell_props.target = ALL_AOE;
      holder->spell_props.effect = SPELL_EFFECT_DAMAGE;
      holder->spell_props.magnitude = (short int)floor(budget/5.0);
      break;
  }
}

void generate_common_spell(CARD* holder, CARD_STRENGTH strn, int cardCost){
  int budget = (cardCost * 2) + 1;
  int toss = 0;
  char buffer[DESCRIPTION_SIZE] = {0};
  holder->family = COMMON;
  switch(strn){
    case LOW:
      toss = rand_range(1,2);
      if(toss == 1){
        stpncpy(holder->name, "Undecided Voter", NAME_SIZE);
        sprintf(
            buffer,
            "Is that you Ken Bone? "
            "Summon a random voter");
        stpncpy(holder->description, buffer, DESCRIPTION_SIZE);
        stpncpy(holder->image, "undecided_voter.png", NAME_SIZE);
        holder->spell_props.target = EFFECT_TARGET_NONE;
        holder->spell_props.effect = SPELL_EFFECT_SUMMON;
        holder->spell_props.magnitude = 1;
      }else{
        stpncpy(holder->name, "Fake News", NAME_SIZE);
        sprintf(
            buffer,
            "Publish fake news, reducing opponent team "
            "morale, dealing %d damage all of them.",
            (short int)floor(budget / 3.0));
        stpncpy(holder->description, buffer, DESCRIPTION_SIZE);
        stpncpy(holder->image, "fake_news.png", NAME_SIZE);
        holder->spell_props.target = ENEMY_AOE;
        holder->spell_props.effect = SPELL_EFFECT_DAMAGE;
        holder->spell_props.magnitude = (short int)floor(budget / 3.0);
      }
      break;
    case MED:
      stpncpy(holder->name, "Poll Results", NAME_SIZE);
      sprintf(
          buffer,
          "Release poll results, hurting enemy hero"
          " for %d health",
          (short int)ceil(budget / 2.0));
      stpncpy(holder->description, buffer, DESCRIPTION_SIZE);
      stpncpy(holder->image, "poll_results.png", NAME_SIZE);
      holder->spell_props.target = ENEMY_HERO;
      holder->spell_props.effect = SPELL_EFFECT_DAMAGE;
      holder->spell_props.magnitude = (short int)ceil(budget / 2.0);
      break;
    /* Never called */
    case HIGH:
      assert(0);
      break;
  }
}

void generate_spell_card(CARD* holder, CARD_FAMILY family, CARD_STRENGTH strn){
  int cardCost = 0;
  switch(strn){
    case LOW  : cardCost = rand_range(1,3); break;
    case MED  : cardCost = rand_range(4,7); break;
    case HIGH : cardCost = rand_range(8,10); break;
  };
  holder->type = SPELL;
  holder->cost = cardCost;
  holder->creature_props = emptyCreatureProp;
  switch(family){
    case DEMOCRATS   : generate_democrat_spell(holder, strn, cardCost); break;
    case REPUBLICANS : generate_republican_spell(holder, strn, cardCost); break;
    case COMMON      : generate_common_spell(holder, strn, cardCost); break;
  };
}

void generate_democrat_creature(CARD* holder,CARD_STRENGTH strn, int cardCost){
  int toss = 0;
  char buffer[DESCRIPTION_SIZE] = {0};
  holder->family = DEMOCRATS;
  switch(strn){
    case LOW:
      toss = rand_range(1,4);
      switch(toss){
        case 1:
            stpncpy(holder->name, "Rastafarian", NAME_SIZE);
            sprintf(
                buffer,
                "Bob Marley did not die in vain");
            stpncpy(holder->description, buffer, DESCRIPTION_SIZE);
            stpncpy(holder->image, "rastafarian.png", NAME_SIZE);
            holder->creature_props.health = rand_range(1,2);
            holder->creature_props.attack = rand_range(1,2);
            holder->creature_props.effect = CREATURE_EFFECT_NONE;
            holder->creature_props.target = EFFECT_TARGET_NONE;
            holder->creature_props.magnitude = 0;        
          break;
        case 2:
           stpncpy(holder->name, "Soc. Justice Warrior", NAME_SIZE);
            sprintf(
                buffer,
                "M'Lady");
            stpncpy(holder->description, buffer, DESCRIPTION_SIZE);
            stpncpy(holder->image, "SJW.png", NAME_SIZE);
            holder->creature_props.health = rand_range(1,2);
            holder->creature_props.attack = rand_range(1,2);
            holder->creature_props.effect = CREATURE_EFFECT_NONE;
            holder->creature_props.target = EFFECT_TARGET_NONE;
            holder->creature_props.magnitude = 0;
          break;
        case 3:
           stpncpy(holder->name, "Tree Hugger", NAME_SIZE);
            sprintf(
                buffer,
                "If you hug the tree long enough, the tree hugs you back");
            stpncpy(holder->description, buffer, DESCRIPTION_SIZE);
            stpncpy(holder->image, "tree_hugger.png", NAME_SIZE);
            holder->creature_props.health = rand_range(2,3);
            holder->creature_props.attack = 1;
            holder->creature_props.effect = CREATURE_EFFECT_NONE;
            holder->creature_props.target = EFFECT_TARGET_NONE;
            holder->creature_props.magnitude = 0;

          break;
        case 4:
           stpncpy(holder->name, "Pro-Choicer", NAME_SIZE);
            sprintf(
                buffer,
                "If you are aganist abortion, then don't have one.");
            stpncpy(holder->description, buffer, DESCRIPTION_SIZE);
            stpncpy(holder->image, "pro_choice.png", NAME_SIZE);
            holder->creature_props.health = 1;
            holder->creature_props.attack = rand_range(2,3);
            holder->creature_props.effect = CREATURE_EFFECT_NONE;
            holder->creature_props.target = EFFECT_TARGET_NONE;
            holder->creature_props.magnitude = 0;
          break;
      }
      break;
    case MED:
      toss = rand_range(1,2);
      if(toss == 1){
        stpncpy(holder->name, "Union Worker", NAME_SIZE);
        sprintf(
            buffer,
            "'Management will fight for my interests' said no one ever");
        stpncpy(holder->description, buffer, DESCRIPTION_SIZE);
        stpncpy(holder->image, "union_worker.png", NAME_SIZE);
        holder->creature_props.health = rand_range(4,6);
        holder->creature_props.attack = rand_range(3,4);
        holder->creature_props.effect = CREATURE_EFFECT_NONE;
        holder->creature_props.target = EFFECT_TARGET_NONE;
        holder->creature_props.magnitude = 0;

      }else{
        stpncpy(holder->name, "Minority Voter", NAME_SIZE);
        sprintf(
            buffer,
            "Not quite a minority anymore, are we? "
            "Battlecry: Summon another minority voter");
        stpncpy(holder->description, buffer, DESCRIPTION_SIZE);
        stpncpy(holder->image, "minority_voter.png", NAME_SIZE);
        holder->creature_props.health = rand_range(2,3);
        holder->creature_props.attack = rand_range(2,3);
        holder->creature_props.effect = SUMMON;
        holder->creature_props.target = EFFECT_TARGET_NONE;
        holder->creature_props.magnitude = 1;

      }
      break;
    case HIGH:
      toss = rand_range(1,2);
      if(toss == 1){
        stpncpy(holder->name, "Bernie Sanders", NAME_SIZE);
        sprintf(
            buffer,
            "Feel the Bern. "
            "Deathrattle: Mobilise the youth, dealing 5 damage to Hillary Clinton");
        stpncpy(holder->description, buffer, DESCRIPTION_SIZE);
        stpncpy(holder->image, "bernie_sanders.png", NAME_SIZE);
        holder->creature_props.health = rand_range(6,8);
        holder->creature_props.attack = rand_range(6,8);
        holder->creature_props.effect = DEATHRATTLE;
        holder->creature_props.target = YOUR_HERO;
        holder->creature_props.magnitude = 5;
      }else{
        stpncpy(holder->name, "Barack Obama", NAME_SIZE);
        sprintf(
            buffer,
            "Obama out. "
            "Deathrattle: Attempt to preserve legacy, healing Hillary Clinton "
            "for 5 health");
        stpncpy(holder->description, buffer, DESCRIPTION_SIZE);
        stpncpy(holder->image, "obama.png", NAME_SIZE);
        holder->creature_props.health = rand_range(5,6);
        holder->creature_props.attack = rand_range(5,6);
        holder->creature_props.effect = DEATHRATTLE;
        holder->creature_props.target = YOUR_HERO;
        holder->creature_props.magnitude = 5;
      }
      break;
  }

}

void generate_republican_creature(CARD* holder,CARD_STRENGTH strn,int cardCost){
  int toss = 0;
  char buffer[DESCRIPTION_SIZE] = {0};
  holder->family = REPUBLICANS;
  switch(strn){
    case LOW:
      toss = rand_range(1,4);
      switch(toss){
        case 1:
            stpncpy(holder->name, "Pro-Lifer", NAME_SIZE);
            sprintf(
                buffer,
                "God is pro-life y'know");
            stpncpy(holder->description, buffer, DESCRIPTION_SIZE);
            stpncpy(holder->image, "pro_lifer.png", NAME_SIZE);
            holder->creature_props.health = rand_range(1,2);
            holder->creature_props.attack = rand_range(1,2);
            holder->creature_props.effect = CREATURE_EFFECT_NONE;
            holder->creature_props.target = EFFECT_TARGET_NONE;
            holder->creature_props.magnitude = 0;
          break;
        case 2:
            stpncpy(holder->name, "Gun Advocate", NAME_SIZE);
            sprintf(
                buffer,
                "And on the thrid day, god created the Remington"
                " bolt action rifle so man could fight the dinosaurs.");
            stpncpy(holder->description, buffer, DESCRIPTION_SIZE);
            stpncpy(holder->image, "gun_advocate.png", NAME_SIZE);
            holder->creature_props.health = rand_range(1,2);
            holder->creature_props.attack = rand_range(1,2);
            holder->creature_props.effect = CREATURE_EFFECT_NONE;
            holder->creature_props.target = EFFECT_TARGET_NONE;
            holder->creature_props.magnitude = 0;
          break;
        case 3:
            stpncpy(holder->name, "Klan Member", NAME_SIZE);
            sprintf(
                buffer,
                "I met the grand wizard once you know.");
            stpncpy(holder->description, buffer, DESCRIPTION_SIZE);
            stpncpy(holder->image, "klan_member.png", NAME_SIZE);
            holder->creature_props.health = rand_range(2,3);
            holder->creature_props.attack = 1;
            holder->creature_props.effect = CREATURE_EFFECT_NONE;
            holder->creature_props.target = EFFECT_TARGET_NONE;
            holder->creature_props.magnitude = 0;
          break;
        case 4:
            stpncpy(holder->name, "Bible Thumper", NAME_SIZE);
            sprintf(
                buffer,
                "If you can't say something nice, don't say nothin at all");
            stpncpy(holder->description, buffer, DESCRIPTION_SIZE);
            stpncpy(holder->image, "bible_thumper.png", NAME_SIZE);
            holder->creature_props.health = 1;
            holder->creature_props.attack = rand_range(2,3);
            holder->creature_props.effect = CREATURE_EFFECT_NONE;
            holder->creature_props.target = EFFECT_TARGET_NONE;
            holder->creature_props.magnitude = 0;
          break;
      }
      break;
    case MED:
      toss = rand_range(1,2);
      if(toss == 1){
        stpncpy(holder->name, "The 1%", NAME_SIZE);
        sprintf(
            buffer,
            "Quite..");
        stpncpy(holder->description, buffer, DESCRIPTION_SIZE);
        stpncpy(holder->image, "one_percent.png", NAME_SIZE);
        holder->creature_props.health = rand_range(4,6);
        holder->creature_props.attack = rand_range(3,4);
        holder->creature_props.effect = CREATURE_EFFECT_NONE;
        holder->creature_props.target = EFFECT_TARGET_NONE;
        holder->creature_props.magnitude = 0;
      }else{
        stpncpy(holder->name, "Armed Militia", NAME_SIZE);
        sprintf(
            buffer,
            "Forget gold, buy lead! "
            "Battlecry: Summon another militia member");
        stpncpy(holder->description, buffer, DESCRIPTION_SIZE);
        stpncpy(holder->image, "armed_militia.png", NAME_SIZE);
        holder->creature_props.health = rand_range(2,3);
        holder->creature_props.attack = rand_range(2,3);
        holder->creature_props.effect = SUMMON;
        holder->creature_props.target = EFFECT_TARGET_NONE;
        holder->creature_props.magnitude = 1;
      }
      break;
    case HIGH:
      toss = rand_range(1,2);
      if(toss == 1){
        stpncpy(holder->name, "Pence", NAME_SIZE);
        sprintf(
            buffer,
            "Hooiser Daddy. "
            "Deathrattle: Restrict rights, dealing 2 damage to all enemy units");
        stpncpy(holder->description, buffer, DESCRIPTION_SIZE);
        stpncpy(holder->image, "pence.png", NAME_SIZE);
        holder->creature_props.health = rand_range(5,7);
        holder->creature_props.attack = rand_range(5,7);
        holder->creature_props.effect = DEATHRATTLE;
        holder->creature_props.target = ENEMY_AOE;
        holder->creature_props.magnitude = 2;
      }else{
        stpncpy(holder->name, "Putin", NAME_SIZE);
        sprintf(
            buffer,
            "I came here to wrestle bears and interfere in elections. "
            "Battlecry: Summon a kremlin hacker");
        stpncpy(holder->description, buffer, DESCRIPTION_SIZE);
        stpncpy(holder->image, "putin.png", NAME_SIZE);
        holder->creature_props.health = rand_range(5,6);
        holder->creature_props.attack = rand_range(5,6);
        holder->creature_props.effect = SUMMON;
        holder->creature_props.target = EFFECT_TARGET_NONE;
        holder->creature_props.magnitude = 1;
      }
      break;
  }
}

void generate_common_creature(CARD* holder,CARD_STRENGTH strn,int cardCost){
  int toss = 0;
  char buffer[DESCRIPTION_SIZE] = {0};
  holder->family = COMMON;
  switch(strn){
    case LOW:
      toss = rand_range(1,2);
      if(toss == 1){
        stpncpy(holder->name, "Protestor", NAME_SIZE);
        sprintf(
            buffer,
            "Thats like, your opinion man");
        stpncpy(holder->description, buffer, DESCRIPTION_SIZE);
        stpncpy(holder->image, "protestor.png", NAME_SIZE);
        holder->creature_props.health = rand_range(1,2);
        holder->creature_props.attack = rand_range(1,2);
        holder->creature_props.effect = CREATURE_EFFECT_NONE;
        holder->creature_props.target = EFFECT_TARGET_NONE;
        holder->creature_props.magnitude = 0;
      }else{
        toss = rand_range(1,2);
        stpncpy(holder->name, "Pollster", NAME_SIZE);
        sprintf(
            buffer,
            "I have a poster of Nate Silver on my wall. "
            "Deathrattle: Make a mistake in the math, dealing "
            "%d damage to the enemy hero", toss);
        stpncpy(holder->description, buffer, DESCRIPTION_SIZE);
        stpncpy(holder->image, "pollster.png", NAME_SIZE);
        holder->creature_props.health = rand_range(1,2);
        holder->creature_props.attack = rand_range(1,2);
        holder->creature_props.effect = DEATHRATTLE;
        holder->creature_props.target = ENEMY_HERO;
        holder->creature_props.magnitude = toss;
      }
      break;
    case MED:
      toss = rand_range(1,2);
      if(toss == 1){
        stpncpy(holder->name, "Banker", NAME_SIZE);
        sprintf(
            buffer,
            "Oh the Wells Fargo van is coming to town");
        stpncpy(holder->description, buffer, DESCRIPTION_SIZE);
        stpncpy(holder->image, "banker.png", NAME_SIZE);
        holder->creature_props.health = rand_range(3,5);
        holder->creature_props.attack = rand_range(3,5);
        holder->creature_props.effect = CREATURE_EFFECT_NONE;
        holder->creature_props.target = EFFECT_TARGET_NONE;
        holder->creature_props.magnitude = 0;
      }else{
        toss = rand_range(2,3);
        stpncpy(holder->name, "Journalist", NAME_SIZE);
        sprintf(
            buffer,
            "I am the fourth estate! "
            "Deathrattle: Publish editorial, boosting morale and "
            "healing friendly units for %d health", toss);
        stpncpy(holder->description, buffer, DESCRIPTION_SIZE);
        stpncpy(holder->image, "journalist.png", NAME_SIZE);
        holder->creature_props.health = rand_range(3,4);
        holder->creature_props.attack = rand_range(3,4);
        holder->creature_props.effect = DEATHRATTLE;
        holder->creature_props.target = FRIENDLY_AOE;
        holder->creature_props.magnitude = toss;
      }
      break;
    /* Never called */
    case HIGH:
      assert(0);
      break;
  }

}

void generate_creature_card(CARD* holder, CARD_FAMILY family, CARD_STRENGTH strn){
  int cardCost = 0;
  switch(strn){
    case LOW  : { cardCost = rand_range(1,3); assert (cardCost > 0 && cardCost < 4); } break;
    case MED  : { cardCost = rand_range(4,7); assert (cardCost > 3 && cardCost < 8); } break;
    case HIGH : { cardCost = rand_range(8,10); assert (cardCost > 7 && cardCost < 11); } break;
  };
  holder->type = CREATURE;
  holder->cost = cardCost;
  holder->spell_props = emptySpellProp;
  switch(family){
    case DEMOCRATS   : generate_democrat_creature(holder, strn, cardCost); break;
    case REPUBLICANS : generate_republican_creature(holder, strn, cardCost); break;
    case COMMON      : generate_common_creature(holder, strn, cardCost); break;
  }
}

