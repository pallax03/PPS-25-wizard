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

find([E|_], E).
find([_|T], E) :- find(T, E).
distinct([], []).
distinct([H | T], O) :- find(T, H), distinct(T, O), !.
distinct([H | T], [H|O]) :- distinct(T, O).



% WIZARD ENGINE.BASIC

% CARD

%% VALIDATOR DO NOT USED (RIPASSO DI PROLOG)
%is_valid_rank(RANK) :- range(1, 13, RANK).
%is_valid_color(COLOR) :- member(COLOR, [red, green, yellow, blue]).
%card(RANK, COLOR) :- is_valid_rank(RANK), is_valid_color(COLOR).
%%validate_cards(+CARDS)
%validate_cards([]).
%validate_cards([wizard | T]) :- validate_cards(T).
%validate_cards([jester | T]) :- validate_cards(T).
%validate_cards([card(RANK, Color) | T]) :- card(RANK, COLOR), !, validate_cards(T).

%special_cards(+Hand, -Specials)
wizard.
jester.
special_cards(HAND, S) :- findall(E, (member(E, HAND), (E = wizard ; E = jester)), S).
%special_cards([wizard, jester, card(13, green), wizard, wizard, jester, jester, card(1, red)], S)
% -> S / [wizard,jester,wizard,wizard,jester,jester]





%extract_colors(+Cards, -Colors) -> return the list of colors in a List of cards (not distincts)
extract_colors(Cards, Colors) :- findall(Color, member(card(_, Color), Cards), Colors).
%extract_colors([card(1, red), card(5, green), card(8, yellow), card(13, red)], Colors)
% -> Colors / [red,green,yellow,red]

%color_frequencies(+Cards, -Frequencies) -> return a mapped list from a List of Cards with DistinctColors and his frequencies
color_frequencies(Cards, Frequencies) :- 
	extract_colors(Cards, Colors), 
	distinct(Colors, DistinctColors), 
	findall(
		freq(Color, N), 
		(member(Color, DistinctColors), count(Colors, Color, N)), 
		Frequencies
	).
%color_frequencies([card(1, red), card(4, red), card(1, yellow), card(13, blue)], Frequencies)
% -> Frequencies / [freq(red,2),freq(yellow,1),freq(blue,1)]

%dominant_color(+Cards, ?Color) -> return the max color present in a List of card (2 red and 2 yellow -> given in next paths)
dominant_color(Cards, Color) :- 
	color_frequencies(Cards, Frequencies), 
	findall(N, member(freq(_, N), Frequencies), Counts),
	min_max(Counts, Max, _),
	member(freq(Color, Max), Frequencies).
% dominant_color([card(1, red), card(4, red), card(1, yellow), card(13, blue)], Color)
% -> Color / red
% dominant_color([card(1, red), card(4, red), card(1, yellow), card(13, yellow)], Color)
% -> Color / red



% WIZARD ENGINE.RULES

% RULES
% following_play_card(?StandardCard, ?FollowingColor).
following_standard_card(card(_, FollowingColor), FollowingColor).

% following_standard_cards(+Hand, +FollowingColor, -LegalStandardCards)
following_standard_cards(Hand, FollowingColor, LegalStandardCards) :- findall(
	Card, (
		member(Card, Hand), 
		following_standard_card(Card, FollowingColor)
	), LegalStandardCards).
%following_standard_cards([card(1, red), card(4, red), card(1, yellow), card(13, blue), wizard, jester], red, L)
% -> L / [card(1,red),card(4,red)]
%following_standard_cards([card(1, red), card(4, red), card(1, yellow), card(13, blue), wizard, jester], jester, L)
%	-> L / []


play_card(wizard, _).
play_card(jester, _).
%playable_cards(HAND, FollowingCard, L) :- 
%playable_cards([card(1, red), card(4, red), card(1, yellow), card(13, blue), wizard, jester], card(3, red), L)



% FINAL API 

%choose_trump(+Hand, -TrumpColor) -> return the best trump to choose based on dominant_color (see dominant_color doc)
choose_trump(Hand, TrumpColor) :- dominant_color(Hand, TrumpColor).

%playable_cards(+Hand, +FollowingColor, -PlayableCards)
playable_cards(Hand, FollowingColor, Hand) :- following_standard_cards(Hand, FollowingColor, []), !.
playable_cards(Hand, FollowingColor, PlayableCards) :- 
	following_standard_cards(Hand, FollowingColor, FollowingStandardCards),
	special_cards(Hand, SpecialCards),
	append(FollowingStandardCards, SpecialCards, PlayableCards).
playable_cards(Hand, Hand). % no Following Card - is necessary?
%playable_cards([card(1, red), card(4, red), card(1, yellow), card(13, blue), wizard, jester], red, L)
% -> L / [card(1,red),card(4,red),wizard,jester]
%playable_cards([card(1, red), card(4, red), card(1, yellow), card(13, blue), wizard, jester], L)
% -> L / [card(1,red),card(4,red),card(1,yellow),card(13,blue),wizard,jester]




%best_playable_card(+Hand, +FollowingColor, +LeaderCard, +Bids, +Tricks, -Card)




