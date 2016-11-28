Java Implementation
===================

The JavaFX framework was used to implement the GUI.

The assets were photoshopped from screenshots of the Hearthstone game.

State
-----

The `State` class serves as the Model in our MVC framework. It is completely
agnostic to the view implementation and is not even aware of any GUI plugging.

It exposes a set of `Observable` variables through which the GUI and hook into
to draw the state as it changes and provides a set of API calls that the UI can
call to move the state of the game along.

Skipping most of the boring data definitions, there are a couple of interesting
functions that allowed for a safer implementation of a class. One popular action
that is called in many contexts is any call that might require mana. This might
be things like playing cards or using the Hero power. The usage pattern is very
similar in that we first have to check if the user has enough mana to perform
the action, then we perform the action and see if the action itself was
successful, and then finally we have to rememeber to deduct the mana if it was.
An abstraction called `withMana` was written to facilitate this. Essentially,
given a hero, the amount of mana the action takes as well as a lambda of the
actual action, we can model the interaction described above like so:

.. code-block:: Java

  public static Optional<String> withMana(Hero h, Integer i, Function<Integer, Optional<String>> f){
        Optional<String> res;
        switch(h){
            case HILLARY:
                if(State.getState().democratMana - i >= 0){
                    res = f.apply((int)State.getState().democratMana);
                    if(!res.isPresent()){
                        State.getState().democratMana -= i;
                    }
                    return res;
                }
                return Optional.of("Not enough mana");
            case TRUMP:
                if(State.getState().republicanMana - i >= 0){
                    res = f.apply((int)State.getState().republicanMana);
                    if(!res.isPresent()){
                        State.getState().republicanMana -= i;
                    }
                    return res;
                }
                return Optional.of("Not enough mana");
        }
        return Optional.of("Fatal error at withMana");
    }

We can now use this function like so:

.. code-block:: Java

  State.withMana(HILLARY, 2, m -> {
    if(rand % 2){
      return Optional.of("This action can fail randomly");
    }
    return Optional.empty();
  })

As long as the lambda returns with an empty optional, ie no error strings, the
`withMana` function will deduct the mana.

Observables
-----------

The main workhorse of the GUI and how we avoided explicit threading for
the GUI was by using observables.

JavaFX properties are often used in conjunction with binding, a powerful
mechanism for expressing direct relationships between variables. When objects
participate in bindings, changes made to one object will automatically be
reflected in another object. This can be useful in a variety of applications.
For example, binding could be used in a bill invoice tracking program, where
the total of all bills would automatically be updated whenever an individual
bill is changed. Or, binding could be used in a graphical user interface (GUI)
that automatically keeps its display synchronized with the application's underlying data.

We use it primarily to keep the game state of the board, the hand and the health
of the different creatures in sync.

