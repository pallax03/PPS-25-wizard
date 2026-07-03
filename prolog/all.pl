% UTILS
range(X, _, X).
range(A, B, X) :- A2 is A+1, A2 =< B, range(A2, B, X).

count([], _, 0).
count([H | T], H, N) :- count(T, H, N2), N is N2 + 1.
count([H | T], E, N) :- count(T, E, N), H \= E.

max([H], H, H).
max([H | T], H, Min) :- max(T, Mx, Min), H > Mx, !.
max([H | T], Max, H) :- max(T, Max, Mn), H < Mn, !.
max([H | T], Max, Min) :- max(T, Max, Min).

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

% HAND

% TABLE

% HELPER FUNCTIONS


extract_colors(CARDS, COLORS) :- findall(COLOR, member(card(_, COLOR), CARDS), COLORS).
color_frequencies(CARDS, L) :- 
	extract_colors(CARDS, COLORS), 
	distinct(COLORS, DistinctColors), 
	findall(
		freq(COLOR, COUNT), 
		(member(COLOR, DistinctColors), count(COLORS, COLOR, COUNT)), 
		L
	).
%color_frequencies([card(1, red), card(4, red), card(1, yellow), card(13, blue)], L)


%dominant_color(HAND, COLOR).
%dominant_color(CARDS, COLOR) :- extract_colors(CARDS, COLORS), count(COLORS, member(COLOR, COLORS), N), max().



% WIZARD ENGINE.RULES

% RULES
% following_play_card(?PLAYCARD, ?TOPCARD).
following_play_card(card(RANK1, COLOR), card(RANK2, COLOR)) :- card(RANK1, COLOR), card(RANK2, COLOR).

% following_playable_cards(+Hand, +TopCard, -LegalCards)
following_playable_cards(HAND, TOPCARD, L) :- findall(CARD, (member(CARD, HAND), following_play_card(CARD, TOPCARD)), L).



% FINAL API 

%bot_playable_cards(+Hand, +TopCard, -FinalPlayableCards)
bot_playable_cards(HAND, TOPCARD, HAND) :- following_playable_cards(HAND, TOPCARD, []), !.
bot_playable_cards(HAND, TOPCARD, PLAYABLECARDS) :- following_playable_cards(HAND, TOPCARD, PLAYABLECARDS).


% DELETE?
% OR WITH IF-ELSE (MORE PERFORMANCE)
bot_playable_cards(HAND, TOPCARD, FINAL) :- 
	following_playable_cards(HAND, TOPCARD, TEMP_LIST), 
	( TEMP_LIST == [] -> FINAL = HAND ; FINAL = TEMP_LIST).





