#show.
#show starth/3.
#show startv/3.
#show black/2.

h(1..size).
v(1..size).
pos(0..size-1).

runh(1,1,3).
runh(1,2,1).

runh(2,1,2).
runh(2,2,2).

runh(3,1,3).

runh(4,1,1).
runh(4,2,4).

runh(5,1,5).

runh(6,1,2).

runh(7,1,1).
runh(7,2,1).
runh(7,3,1).
runh(7,4,1).


runv(1,1,2).
runv(1,2,1).
runv(1,3,1).

runv(2,1,2).
runv(2,2,2).

runv(3,1,1).
runv(3,2,3).

runv(4,1,1).
runv(4,2,2).

runv(5,1,4).
runv(5,2,1).

runv(6,1,3).

runv(7,1,1).
runv(7,2,2).
runv(7,3,1).

1 { starth(X,Y,R):h(X), X <= size + 1 - N } 1 :- runh(Y,R,N).

paintedh(X+P,Y) :- h(X+P), starth(X,Y,R), runh(Y,R,N), pos(P), P <= N - 1.
black(X,Y) :- paintedh(X,Y).

:- starth(X1,Y,R1), runh(Y,R1,N), starth(X2,Y,R1+1), X1 + N >= X2.
:- black(X,Y), not paintedh(X,Y).

%:- starth(X1,Y,R1), starth(X2,Y,R2), R1 < R2, X1 >= X2.
%:- starth(X,Y,R), black(X-1,Y).
%:- starth(X,Y,R), runh(Y,R,N), black(X+N,Y).



1 { startv(X,Y,R):v(Y), Y <= size + 1 - N } 1 :- runv(X,R,N).

paintedv(X,Y+P) :- v(Y+P), startv(X,Y,R), runv(X,R,N), pos(P), P <= N - 1.
black(X,Y) :- paintedv(X,Y).

:- startv(X,Y1,R1), runv(X,R1,N), startv(X,Y2,R1+1), Y1 + N >= Y2.
%:- startv(X,Y1,R1), startv(X,Y2,R2), R1 < R2, Y1 >= Y2.
%:- startv(X,Y,R), black(X,Y-1).
%:- startv(X,Y,R), runv(X,R,N), black(X,Y+N).

:- black(X,Y), not paintedv(X,Y).
