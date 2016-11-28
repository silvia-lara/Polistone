CFLAGS=-ansi -Wall -Werror -pedantic -Wno-overlength-strings -Wno-unused-variable -g

json.o : json.c
	gcc $(CFLAGS) -c json.c -o json.o

cardGenMain:
	gcc $(CFLAGS) -c card.c main.c
	gcc main.c -o cardGenMain

deck.json: cardGenMain
	./cardGenMain

test: json.o deck.json
	gcc test.c json.o $(CFLAGS) -o test && ./test

clean:
	rm -rf *.o cardGenMain deck.json test
