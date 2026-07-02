% BASIC HELPER
range(X, _, X).
range(A, B, X) :- A2 is A+1, A2 =< B, range(A2, B, X).

%rank = 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13.
%color = r, g, y, b.
%card(RANK, COLOR) = rank(RANK), color(COLOR).

% CARD BASIC HELPER
is_valid_rank(RANK) :- range(1, 13, RANK).
is_valid_color(red).
is_valid_color(green).
is_valid_color(yellow).
is_valid_color(blue).
card(RANK, COLOR) :- is_valid_rank(RANK), is_valid_color(COLOR), !.
same_card(card(RANK, COLOR), card(RANK, COLOR)).

standard(RANK, COLOR) :- is_valid_standard_card(card(RANK, COLOR)).

%table(CARDS, CARD).