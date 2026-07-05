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
is_valid_rank(RANK) :- range(1, 13, RANK).
is_valid_color(COLOR) :- member(COLOR, [red, green, yellow, blue]).
card(RANK, COLOR) :- is_valid_rank(RANK), is_valid_color(COLOR).

%special_cards(+Hand, -Specials)
wizard.
jester.
special_cards(HAND, S) :- findall(E, (member(E, HAND), (E = wizard ; E = jester)), S).
%special_cards([wizard, jester, card(13, green), wizard, wizard, jester, jester, card(1, red)], S)
% -> S / [wizard,jester,wizard,wizard,jester,jester]

%validate_cards(+CARDS)
validate_cards([]).
validate_cards([wizard | T]) :- validate_cards(T).
validate_cards([jester | T]) :- validate_cards(T).
validate_cards([card(RANK, COLOR) | T]) :- card(RANK, COLOR), !, validate_cards(T).

%extract_colors(+Cards, -Colors) -> return the list of colors in a List of cards (not distincts)
extract_colors(CARDS, COLORS) :- findall(COLOR, member(card(_, COLOR), CARDS), COLORS).
%extract_colors([card(1, red), card(5, green), card(8, yellow), card(13, red)], COLORS)
% -> COLORS / [red,green,yellow,red]

%color_frequencies(+Cards, -Frequencies)
color_frequencies(CARDS, FREQUENCIES) :- 
	extract_colors(CARDS, COLORS), 
	distinct(COLORS, DistinctColors), 
	findall(
		freq(COLOR, COUNT), 
		(member(COLOR, DistinctColors), count(COLORS, COLOR, COUNT)), 
		FREQUENCIES
	).
%color_frequencies([card(1, red), card(4, red), card(1, yellow), card(13, blue)], L)

%dominant_color(+Cards, ?Color) -> return the max color present in a List of card
dominant_color(CARDS, COLOR) :- 
	color_frequencies(CARDS, L), 
	findall(N, member(freq(_, N), L), Counts),
	min_max(Counts, Max, _), 
	print(Max),
	member(freq(COLOR, Max), L).
% dominant_color([card(1, red), card(4, red), card(1, yellow), card(13, blue)], COLOR)
% -> COLOR / red
% dominant_color([card(1, red), card(4, red), card(1, yellow), card(13, yellow)], COLOR) % 2 red and 2 yellow -> any of max COLOR is right
% -> COLOR / red



% WIZARD ENGINE.RULES

% RULES
% following_play_card(?StandardCard, ?FollowingColor).
following_standard_card(card(_, COLOR), COLOR).

% following_standard_cards(+Hand, +FollowingColor, -LegalStandardCards)
following_standard_cards(HAND, FollowingColor, LegalStandardCards) :- findall(
	CARD, (
		member(CARD, HAND), 
		following_standard_card(CARD, FollowingColor)
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

%bot_playable_cards(+Hand, +FollowingColor, -PlayableCards)
bot_playable_cards(HAND, FollowingColor, HAND) :- following_standard_cards(HAND, FollowingColor, []), !.
bot_playable_cards(HAND, FollowingColor, PlayableCards) :- 
	following_standard_cards(HAND, FollowingColor, FollowingStandardCards),
	special_cards(HAND, SpecialCards),
	append(FollowingStandardCards, SpecialCards, PlayableCards).
bot_playable_cards(HAND, PlayableCards) :- bot_playable_cards(HAND, null, PlayableCards).	% no FollowingCard
%bot_playable_cards([card(1, red), card(4, red), card(1, yellow), card(13, blue), wizard, jester], red, L)
% -> L / [card(1,red),card(4,red),wizard,jester]
%bot_playable_cards([card(1, red), card(4, red), card(1, yellow), card(13, blue), wizard, jester], L)
% -> L / [card(1,red),card(4,red),card(1,yellow),card(13,blue),wizard,jester]




