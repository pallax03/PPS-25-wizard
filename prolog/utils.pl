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