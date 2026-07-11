% UTILS
range(X, _, X).
range(A, B, X) :- A2 is A+1, A2 =< B, range(A2, B, X).

count([], _, 0).
count([H | T], H, N) :- !, count(T, H, N2), N is N2 + 1.
count([H | T], E, N) :- count(T, E, N).

min_max([H], H, H).
min_max([H | T], H, Min) :- min_max(T, Mx, Min), H > Mx, !.
min_max([H | T], Max, H) :- min_max(T, Max, Mn), H < Mn, !.
min_max([H | T], Max, Min) :- min_max(T, Max, Min).

length([], 0).
length([_|T], N) :- length(T, N1), N is N1 + 1.

find([E|_], E).
find([_|T], E) :- find(T, E).
distinct([], []).
distinct([H | T], O) :- find(T, H), distinct(T, O), !.
distinct([H | T], [H|O]) :- distinct(T, O).

% WIZARD ENGINE.BASIC

is_valid_rank(RANK) :- range(1, 13, RANK).
is_valid_color(COLOR) :- member(COLOR, [red, green, yellow, blue]).
card(RANK, COLOR) :- is_valid_rank(RANK), is_valid_color(COLOR).

% validate_cards(+CARDS)
validate_cards([]).
validate_cards([wizard | T]) :- validate_cards(T).
validate_cards([jester | T]) :- validate_cards(T).
validate_cards([card(RANK, Color) | T]) :- card(RANK, Color), !, validate_cards(T).

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

% WIZARD ENGINE.RULES -> Standard Rules of the Game

% following_standard_cards(+Hand, +FollowingColor, -LegalStandardCards)
following_standard_cards(Hand, FollowingColor, LegalStandardCards) :-
	findall(Card, (member(Card, Hand), card_of_color(Card, FollowingColor)), LegalStandardCards).

% following_standard_cards([card(1, red), card(4, red), card(1, yellow), card(13, blue), wizard, jester], red, L) -> L / [card(1,red),card(4,red)]
% following_standard_cards([card(1, red), card(4, red), card(1, yellow), card(13, blue), wizard, jester], jester, L) -> L / []

% special_cards(+Hand, -Specials)
special_cards(HAND, S) :- findall(E, (member(E, HAND), (E = wizard ; E = jester)), S).
% special_cards([wizard, jester, card(13, green), wizard, wizard, jester, jester, card(1, red)], S) -> S / [wizard,jester,wizard,wizard,jester,jester]

% playable_cards(+Hand, ?FollowingColor, -PlayableCards) -> return a List of Playable Cards from Hand.
playable_cards(Hand, FollowingColor, Hand) :- following_standard_cards(Hand, FollowingColor, []), !.
playable_cards(Hand, FollowingColor, PlayableCards) :-
	following_standard_cards(Hand, FollowingColor, FollowingStandardCards),
	special_cards(Hand, SpecialCards),
	append(FollowingStandardCards, SpecialCards, PlayableCards).

% playable_cards([card(1, red), card(4, red), card(1, yellow), card(13, blue), wizard, jester], red, L) -> L / [card(1,red),card(4,red),wizard,jester]
% playable_cards([card(1, red), card(4, red), card(1, yellow), card(13, blue), wizard, jester], none, L) -> L / [card(1,red),card(4,red),card(1,yellow),card(13,blue),wizard,jester]

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
	winning_options(PlayableCards, WinningCard, TrumpColor, WinningCards),
	findall(Card, (member(Card, PlayableCards), \+ member(Card, WinningCards)), LosingCards).

% losing_options([card(1,red),card(4,yellow),wizard,jester], card(10, yellow), red, LosingCards) -> LosingCards / [card(4,yellow),jester]



% WIZARD API

% choose_trump(+Hand, -TrumpColor) -> return the best trump to choose based on dominant_color (see STRATEGY.dominant_color)
choose_trump(Hand, TrumpColor) :- dominant_color(Hand, TrumpColor), is_valid_color(TrumpColor). % not using cut

% choose_trump([card(1, yellow), card(1, red), wizard], TrumpColor). -> TrumpColor / yellow.
% choose_trump([wizard, jester, wizard], TrumpColor). -> no (just pick casually)


% place_bid(+Hand, ?TrumpColor, -Bid) -> return the best Bid based on STRATEGY: cards matching safe_trick OR risky_trick: add a Bid
place_bid(Hand, TrumpColor, Bid) :-
    findall(Card, (member(Card, Hand), (safe_trick(Card, Hand, TrumpColor) ; risky_trick(Card, Hand, TrumpColor))), Cards),
    length(Cards, Bid).

% place_bid([card(1, red), jester, card(4, red), wizard, card(12, yellow), card(13, blue), wizard, jester, wizard], yellow, Bid) -> Bid / 5


% To call when place_bid return an invalid number.
% adjust_bid(+Hand, +RejectedBid, -FinalBid) -> + 1 (fallback) or - 1 (if Hand has jesters) from RejectingBid (avoiding loop)
adjust_bid(Hand, 0, 1) :- !.
adjust_bid(Hand, RejectedBid, FinalBid) :- length(Hand, MaxSize), RejectedBid >= MaxSize, FinalBid is MaxSize - 1, !.
adjust_bid(Hand, RejectedBid, FinalBid) :- member(jester, Hand), FinalBid is RejectedBid - 1, !.
adjust_bid(Hand, RejectedBid, FinalBid) :- FinalBid is RejectedBid + 1, !.

% loop: adjust_bid([card(1, red), card(4, red), wizard, card(12, yellow), card(13, blue), wizard, wizard], 10, Bid) -> Bid / 6
% fallback: adjust_bid([card(1, red), card(4, red), wizard, card(12, yellow), card(13, blue), wizard, wizard], 7, Bid) -> Bid / 6
% jesters: adjust_bid([card(1, red), jester, card(4, red), wizard, card(12, yellow), card(13, blue), wizard, jester, wizard], 9, Bid) -> Bid / 8


% best_playable_card(+Hand, +WinningCard, +FollowingColor, +TrumpColor, +Bids, +Tricks, -BestCard) -> return the best card to play.

% - WinningCard REQUIRED
% 	- want to win with winning cards -> play the lowest to win
best_playable_card(Hand, WinningCard, FollowingColor, TrumpColor, Bids, Tricks, BestCard) :-
	validate_cards([WinningCard]),
	wants_to_win(Bids, Tricks),
	playable_cards(Hand, FollowingColor, PlayableCards),
	winning_options(PlayableCards, WinningCard, TrumpColor, WinningCards),
	WinningCards \= [],
	lowest_card(WinningCards, BestCard), !.

% - WinningCard REQUIRED
% 	- want to win without winning cards -> play the lowest to preserve high cards
best_playable_card(Hand, WinningCard, FollowingColor, TrumpColor, Bids, Tricks, BestCard) :-
	wants_to_win(Bids, Tricks),
	validate_cards([WinningCard]),
	playable_cards(Hand, FollowingColor, PlayableCards),
	winning_options(PlayableCards, WinningCard, TrumpColor, WinningCards),
	WinningCards == [],
	lowest_card(PlayableCards, BestCard), !.

% - WinningCard REQUIRED
% 	- want to lose but every playable card wins -> play the lowest legal card.
best_playable_card(Hand, WinningCard, FollowingColor, TrumpColor, Bids, Tricks, BestCard) :-
	wants_to_lose(Bids, Tricks),
	validate_cards([WinningCard]),
	playable_cards(Hand, FollowingColor, PlayableCards),
	losing_options(PlayableCards, WinningCard, TrumpColor, LosingCards),
	LosingCards == [],
	lowest_card(PlayableCards, BestCard), !.

% - WinningCard REQUIRED
% 	- want to lose -> play the highest to maximaze win on next tricks.
best_playable_card(Hand, WinningCard, FollowingColor, TrumpColor, Bids, Tricks, BestCard) :-
	wants_to_lose(Bids, Tricks),
	validate_cards([WinningCard]),
	playable_cards(Hand, FollowingColor, PlayableCards),
	losing_options(PlayableCards, WinningCard, TrumpColor, LosingCards),
	LosingCards \= [],
	highest_card(LosingCards, BestCard), !.

% - WinningCard NOT REQUIRED
% 	- want to lose:
%			- filter winning cards: removing wizards and trumpColor.
%			- play the lowest card -> highest chance for a higher card than that
best_playable_card(Hand, _, none, TrumpColor, Bids, Tricks, BestCard) :-
	wants_to_lose(Bids, Tricks),
	fallback_filtered_cards(Hand, TrumpColor, FilteredCards),
	lowest_card(FilteredCards, BestCard), !.

% - WinningCard NOT REQUIRED
% 	- want to win:
%			- filter winning cards: removing wizards and trumpColor.
%			- play the lowest card -> highest chance for a higher card than that
best_playable_card(Hand, _, none, TrumpColor, Bids, Tricks, BestCard) :-
	wants_to_win(Bids, Tricks),
	findall(Card, (member(Card, Hand), safe_trick(Card, Hand, TrumpColor); risky_trick(Card, Hand, TrumpColor)), PlayableCards),
	lowest_card(PlayableCards, BestCard), !.


% Cuts keep the final decision API deterministic once a strategy clause matches.
% WANTS TO WIN:
% 	best_playable_card([card(12, red), wizard, card(10, red), wizard], card(10, yellow), yellow, blue, 5, 3, BestCard) -> BestCard / wizard
%		best_playable_card([card(1, blue), card(12, red), wizard, card(10, red), wizard], none, none, red, 5, 3, BestCard) -> BestCard / card(10,red)


% WANTS TO LOSE:
%	 best_playable_card([card(12, red), wizard, card(10, red), wizard], wizard, yellow, red, 2, 3, BestCard) -> BestCard / wizard
%  best_playable_card([card(1, red), card(12, yellow)], card(10, yellow), yellow, blue, 0, 0, BestCard) -> BestCard / card(12,yellow)