% WIZARD ENGINE.BASIC

is_valid_rank(RANK) :- range(1, 13, RANK).
is_valid_color(COLOR) :- member(COLOR, [red, green, yellow, blue]).
card(RANK, COLOR) :- is_valid_rank(RANK), is_valid_color(COLOR).

% validate_cards(+CARDS)
validate_cards([]).
validate_cards([wizard | T]) :- validate_cards(T).
validate_cards([jester | T]) :- validate_cards(T).
validate_cards([card(RANK, Color) | T]) :- card(RANK, COLOR), !, validate_cards(T).

wizard.
jester.

% card_of_color(?StandardCard, ?Color).
card_of_color(card(Rank, Color), Color) :- card(Rank, Color).

% filter_cards_of_color(+Cards, ?NoColor, -FilteredCards)
filter_cards_of_color(Cards, NoColor, FilteredCards) :- 
	findall(Card, (member(Card, Cards), \+ card_of_color(Card, NoColor)), FilteredCards).

% card_value(+Card, -Rank)
card_value(card(Rank, Color), Rank):- card(Rank, Color).
card_value(jester, 0).
card_value(wizard, 14).