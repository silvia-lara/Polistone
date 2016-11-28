#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include "json.h"

#define KNRM  "\x1B[0m"
#define KRED  "\x1B[31m"
#define KGRN  "\x1B[32m"
#define KYEL  "\x1B[33m"
#define KBLU  "\x1B[34m"
#define KMAG  "\x1B[35m"
#define KCYN  "\x1B[36m"
#define KWHT  "\x1B[37m"

int main(){
  FILE* file_handle = NULL;
  long file_length = 0;
  char* buffer = {0};
  json_value* res = NULL;
  int passed = 1;
 
  if(access("deck.json", F_OK) != -1){
    file_handle = fopen("deck.json", "r");
    fseek(file_handle, 0, SEEK_END);
    file_length = ftell(file_handle);
    fseek(file_handle, 0, SEEK_SET);
    buffer = malloc(file_length);
    fread(buffer, 1, file_length, file_handle);

    res = json_parse(buffer, file_length);
    if(res == NULL){
      passed = 0;
      printf(KRED "deck.json was not a valid json file ✗ \n" KNRM);
    }else{
      printf(KGRN "deck.json is a valid json file ✓ \n" KNRM); 
    }

    if(res->type != json_object){
      passed = 0;
      printf(KRED "Expected a top level object ✗ \n" KNRM);
    }else{
      printf(KGRN "Found a top level object ✓ \n" KNRM); 
    }

    if(res->u.object.length != 2){
      passed = 0;
      printf(KRED "Expected 2 top level objects ✗ \n" KNRM);
    }else{
      printf(KGRN "Found 2 object fields ✓ \n" KNRM); 
    }

    if( strncmp(res->u.object.values[0].name, "democrat", 8) != 0){
      passed = 0;
      printf(KRED "Expected democrats first ✗ \n" KNRM);
    }else{
     printf(KGRN "Found the democrat field ✓ \n" KNRM); 
    }

    if(res->u.object.values[0].value->u.array.length != 30){
      passed = 0;
      printf(KRED "Expected 30 cards for democrats ✗ \n" KNRM);
    }else{
     printf(KGRN "Found 30 cards for democrats ✓ \n" KNRM); 
    }

    if( strncmp(res->u.object.values[1].name, "republican", 10) != 0){
      passed = 0;
      printf(KRED "Expected republicans second ✗ \n" KNRM);
    }else{
     printf(KGRN "Found the republican field ✓ \n" KNRM); 
    }

    if(res->u.object.values[1].value->u.array.length != 30){
      passed = 0;
      printf(KRED "Expected 30 cards for republicans ✗ \n" KNRM);
    }else{
     printf(KGRN "Found 30 cards for republicans ✓ \n" KNRM); 
    }



  }else{
    printf( KRED "I expect a deck.json to be present... \n" KNRM);
    exit(1);
  }
  
  if(passed){
    printf(KGRN "All tests passed! 🎉 \n" KNRM);
  }else{
    printf(KRED "Tests were a failure, just like you 🙀 \n"KNRM);
  }
  


  return 0;
}

