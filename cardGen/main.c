#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <time.h>
#include "card.c"

void json_of_card(FILE* handle, CARD card){
  fprintf(handle,
    "{ \"name\"           : \"%s\", "
    "  \"description\"    : \"%s\", "
    "  \"image\"          : \"%s\", "
    "  \"cost\"           : %d,     "
    "  \"type\"           : \"%s\", "
    "  \"family\"         : \"%s\", "
    "  \"creature_props\" : {       "
    "    \"health\"    : %d,        "
    "    \"attack\"    : %d,        "
    "    \"effect\"    : \"%s\",    "
    "    \"target\"    : \"%s\",    "
    "    \"type\"      : \"%s\",    "
    "    \"magnitude\" : %d         "
    "   },                          "
    "  \"spell_props\" : {          "
    "    \"effect\"    : \"%s\",    "
    "    \"target\"    : \"%s\",    "
    "    \"magnitude\" : %d         "
    "   }                           "
    "}                              "
    , card.name, card.description
    , card.image, card.cost, CARD_TYPE_STRING[card.type]
    , CARD_FAMILY_STRING[card.family]
    , card.type == CREATURE ? card.creature_props.health : 0
    , card.type == CREATURE ? card.creature_props.attack : 0
    , card.type == CREATURE ? CREATURE_EFFECT_STRING[card.creature_props.effect] : ""
    , card.type == CREATURE ? EFFECT_TARGET_STRING[card.creature_props.target] : ""
    , card.type == CREATURE ? EFFECT_TYPE_STRING[card.creature_props.type] : ""
    , card.type == CREATURE ? card.creature_props.magnitude : 0
    , card.type == SPELL ? SPELL_EFFECT_STRING[card.spell_props.effect] : ""
    , card.type == SPELL ? EFFECT_TARGET_STRING[card.spell_props.target] : ""
    , card.type == SPELL ? card.spell_props.magnitude : 0);
}

void json_of_deck(FILE* handle, CARD* dem_deck, CARD* rep_deck){
  int i = 0;
  fprintf(handle, "{\"democrat\":[");
  for(i = 0; i < 30; i++){
    json_of_card(handle, dem_deck[i]);
    fprintf(handle, i == 29 ? "]," : ",");
  }
  fprintf(handle, "\"republican\":[");
  for(i = 0; i < 30; i++){
    json_of_card(handle, rep_deck[i]);
    fprintf(handle, i == 29 ? "]}" : ",");
  }
}

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

int main(int argv, char** argc){
  CARD* democrat_deck = generate_deck(DEMOCRATS);
  CARD* republican_deck = generate_deck(REPUBLICANS);
  FILE* file_handle = NULL;

  srand(time(NULL));
  
  file_handle = fopen("deck.json", "w");
  if(file_handle == NULL){
    printf("Failed to open file!\n");
    exit(1);
  }
  
  json_of_deck(file_handle, democrat_deck, republican_deck);
  
  free(democrat_deck);
  free(republican_deck);

  fclose(file_handle);
  return 0;
}

