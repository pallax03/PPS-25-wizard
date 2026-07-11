% WIZARD STRATEGY.HELPER -> Feature extracted from Rules and Basic, for Strategies

% extract_colors(+Cards, -Colors) -> return the list of colors in a List of cards (not distincts)
extract_colors(Cards, Colors) :- findall(Color, member(card(_, Color), Cards), Colors).

% extract_colors([card(1, red), card(5, green), card(8, yellow), card(13, red)], Colors) -> Colors / [red,green,yellow,red]

% color_frequencies(+Cards, -Frequencies) -> return a mapped list from a List of Cards with DistinctColors and his frequencies
color_frequencies(Cards, Frequencies) :-
	extract_colors(Cards, Colors),
	distinct(Colors, DistinctColors),
	findall(
		freq(Color, N),
		(member(Color, DistinctColors), count(Colors, Color, N)),
		Frequencies
	).

% color_frequencies([card(1, red), card(4, red), card(1, yellow), card(13, blue)], Frequencies) -> Frequencies / [freq(red,2),freq(yellow,1),freq(blue,1)]

% count_trumps(+Hand, +TrumpColor, -Count) -> return the number of trumps in Hand
count_trumps(Hand, TrumpColor, Count) :- following_standard_cards(Hand, TrumpColor, TrumpCards), length(TrumpCards, Count).
% count_wizards(+Hand, -Count) -> return the number of wizards in Hand
count_wizards(Hand, Count) :- findall(wizard, member(wizard, Hand), Wizards), length(Wizards, Count).
% count_jesters(+Hand, -Count) -> return the number of jesters in Hand
count_jesters(Hand, Count) :- findall(jester, member(jester, Hand), Jesters), length(Jesters, Count).

cards_ranks_of_color(Cards, Color, Ranks) :- findall(Rank, member(card(Rank, Color), Cards), Ranks).
count_color(Cards, Color, Count) :- cards_ranks_of_color(Cards, Color, Ranks), length(Ranks, Count).


% wants_to_win/lose(+Bids, +Tricks) -> checkers.
wants_to_win(Bids, Tricks) :- Bids > Tricks.
wants_to_lose(Bids, Tricks) :- Bids =< Tricks.

% lowest_card(+Cards, -LowestCard) -> lowest based on card_value.
lowest_card(Cards, LowestCard) :-
	findall(Rank, (member(Card, Cards), card_value(Card, Rank)), Ranks),
	min_max(Ranks, _, MinRank),
	member(LowestCard, Cards),
	card_value(LowestCard, MinRank), !.

% highest_card(+Cards, -HighestCard) -> highest based on card_value.
highest_card(Cards, HighestCard) :-
	findall(Rank, (member(Card, Cards), card_value(Card, Rank)), Ranks),
	min_max(Ranks, MaxRank, _),
	member(HighestCard, Cards),
	card_value(HighestCard, MaxRank), !.