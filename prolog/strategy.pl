% WIZARD STRATEGY -> Strategies for API

% fallback_filtered_cards(+Cards, +NoColor, -FilteredCards) -> FilteredCards is filter_cards_of_color, but if empty return Cards.
fallback_filtered_cards(Cards, NoColor, Cards) :- filter_cards_of_color(Cards, NoColor, []), !.
fallback_filtered_cards(Cards, NoColor, FilteredCards) :- filter_cards_of_color(Cards, NoColor, FilteredCards).

% dominant_color(+Cards, -Color) -> return the max color present in a List of card (2 red and 2 yellow -> given in next paths)
dominant_color(Cards, Color) :-
	color_frequencies(Cards, Frequencies),
	findall(N, member(freq(_, N), Frequencies), Counts),
	min_max(Counts, Max, _),
	member(freq(Color, Max), Frequencies).

% dominant_color([card(1, red), card(4, red), card(1, yellow), card(13, blue)], Color) -> Color / red
% dominant_color([card(1, red), card(4, red), card(1, yellow), card(13, yellow)], Color) -> Color / red

% safe_trick(+Card, +Hand, +TrumpColor) -> evaluate if card can be a secure trick
% 	- wizards
safe_trick(wizard, _, _).
% 	- Trump Cards, Rank in range 10 - 13.
safe_trick(card(Rank, TrumpColor), _, TrumpColor) :- range(10, 13, Rank).
% 	- no Trump Cards, 13 Rank (highest)
safe_trick(card(13, Color), _, TrumpColor) :- Color \= TrumpColor.

% ricky_trick(+Card, +Hand, +TrumpColor) -> evaluate if card can be a risky trick (exclude safe_trick: (Rank 13 is already included in safe_trick))
% 	- no Trump Cards, Hand contains >= 5 of the same color, Rank in range 10 - 12.
risky_trick(card(Rank, Color), Hand, TrumpColor) :-
	range(10, 12, Rank),
	Color \= TrumpColor,
	count_color(Hand, Color, Count),
	Count >= 5.
% 	- no Trump Cards, Hand contains only 2 cards of a color, Rank in range 11 - 12.
risky_trick(card(Rank, Color), Hand, TrumpColor) :-
    range(11, 12, Rank),
    Color \= TrumpColor,
    count_color(Hand, Color, Count),
    Count =< 2.


% beats(+MyCard, +WinningCard, +TrumpColor) -> evaluate TrickWinner using MyCard
%		- any cards wins over jester, but not another jester.
beats(NotJester, jester, _) :- !, NotJester \= jester.
%		- wizard wins over all, but only the first one.
beats(wizard, NotWizard, _) :- !, NotWizard \= wizard.
%		- a Trump always beat a not Trump
beats(card(_, TrumpColor), card(_, Color), TrumpColor) :- Color \= TrumpColor.
%		- highest Rank
beats(card(MyCardRank, Color), card(WinningCardRank, Color), _) :- MyCardRank > WinningCardRank.


% winning_options(+PlayableCards, +WinningCard, +TrumpColor, -WinningCards) -> given a Legit List of PlayableCards (playable_cards doc) -> return a List of WinningCards
winning_options(PlayableCards, WinningCard, TrumpColor, WinningCards) :-
	findall(Card, (member(Card, PlayableCards), beats(Card, WinningCard, TrumpColor)), WinningCards).

% winning_options([card(1,red),card(4,yellow),wizard,jester], card(10, yellow), red, WinningCards) -> WinningCards / [card(1,red),wizard]

% losing_options(+PlayableCards, +WinningCard, +TrumpColor, -LosingCards) -> given a Legit List of PlayableCards (playable_cards doc) -> return a List of LosingCards
losing_options(PlayableCards, WinningCard, TrumpColor, LosingCards) :-
	findall(Card, (member(Card, PlayableCards), \+ beats(Card, WinningCard, TrumpColor)), LosingCards).

% losing_options([card(1,red),card(4,yellow),wizard,jester], card(10, yellow), red, LosingCards) -> LosingCards / [card(4,yellow),jester]