C Implementation
================

There are three main functions implemented in C, that together generate the deck.json.

**To compile, run make ./cardGenMain**

Card.c Features
---------------

In Card.c we create the cards we are going to play with. Each card can either be a spell or a creature card. Spell cards and creature cards hold different properties, and therefore, different fields. 
What they hold in common is modelled in the Card struct:

.. code-block:: C

	typedef struct CARD {
	  CARD_TYPE type;
	  CARD_FAMILY family;
	  char name[NAME_SIZE];
	  char description[DESCRIPTION_SIZE];
	  char image[NAME_SIZE];
	  unsigned short int cost;


This is the structure of any Spell Card:

.. code-block:: C

	typedef struct SPELL_PROPS{  
	  EFFECT_TARGET target;
	  SPELL_EFFECT effect;
	  short int magnitude;
	} SPELL_PROPS;


This is the structure of any Creature Card:

.. code-block:: C

    typedef struct CREATURE_PROPS { 
	  short int health;
	  short int attack;
	  
	  CREATURE_EFFECT effect;
	  EFFECT_TARGET target;
	  EFFECT_TYPE type;
	  short int magnitude;
	} CREATURE_PROPS;

Once the struct for each card is created, we generate each class of cards: democrat spells, republican spells, common spells, spell cards, democrat creatures, republican creatures, common creatures, creature cards.

Spells and Creatures are devided into Low, Medium and High cost ones. Each card is generated in its own group. The following is an exerpt of code generating a low cost republican spell:

.. code-block:: C

	void generate_republican_spell(CARD* holder, CARD_STRENGTH strn, int cardCost){
	int budget = (cardCost * 2) + 1; //budget to distribute betweeh Health and //Attack points
	  int toss = 0; //random number
	  char buffer[DESCRIPTION_SIZE] = {0};
	  holder->family = REPUBLICANS;
	  switch(strn){
		Case LOW:
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
		        

A similar creation code is used to generate the other cards.

Instead, to generate a general creature card, the following general structure is used: 

.. code-block:: C

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


Main.c : 
--------

Main.c takes the cards that card.c generated and returns complete decks encoded in json. The following function generates the deck deciding the composition of each deck, considering the cost category of each card, the family and whether the card is a spell or a creature card.

.. code-block:: C

	CARD* generate_deck(CARD_FAMILY family){
	  CARD* cards = calloc(30, sizeof (CARD));
	  int i = 0;
	  int j = 0;
	  /* Allocation strategy, 10 spells, 20 creatures */
	  
	  /* Allocate cards */
	  for(i = 0; i < 30; i++){
	    /* allocate spells */
	    if(i < 10){
	      /* 3 spells common, 7 family specific */
	      /* of the 7 family spells, make 3 low power, 2 med power and 2 high power */ 
	      if(i < 3){
	        generate_spell_card(cards + i, COMMON,
	            i < 2 ? LOW : MED);
	      }else{
	        generate_spell_card(cards + i, family,
	            i < 6 ? LOW : (i < 8 ? MED : HIGH));
	      }
	    }else{
	      /* of 20 creatures, make 5 common, 15 of the family type */
	      if(i < 15){
	        /* of the 5 common, make 3 low and 2 med */
	        generate_creature_card(cards + i, COMMON, i < 13 ? LOW : MED);
	      }else{
	        /* of the 15 family cards, make 7 common, 5 med, 3 high */
	        generate_creature_card(cards + i, family,
	            (i < 22 ? LOW : (i < 27 ? MED : HIGH)));
	      }
	    }
	  }

	  shuffle_deck(cards); 
	  return cards;
	}

The cards are then shuffled:

.. code-block:: C

	void shuffle_deck(CARD* deck){
	  /* fisher yates shuffle */
	  int i = 0;
	  int j = 0;
	  CARD temp = {0};

	  for(i = 29; i > 0; i--){
	    j = rand_int(i + 1);
	    temp = deck[j];
	    deck[j] = deck[i];
	    deck[i] = temp;
	  }
	}

The deck is encoded a json file by other helper functions included in this file.


Json.c :
--------	
This file contains a json parser found on GitHub under the user UDP. It's an open source, ansi complient that we use in test.c to make sure the file we generate is a valid json file.

Test.c :
--------
This is a testing script that checks for:

1. Validity of the json file
2. Order of insertion
3. Correct number of cards inserted in each deck


