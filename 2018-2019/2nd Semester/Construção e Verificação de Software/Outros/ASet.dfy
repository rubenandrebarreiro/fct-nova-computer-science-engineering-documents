/*
    Class ASet 

    Construction and Verification of Software, FCT-UNL, © (uso reservado)

    This class implements a set of integers using an array of integers.
    It illustrates the use of a set to implement the abstract
    state and abstract invariant conditions. There is a mapping
    between the representation state and abstract state that
    guaranties the soundness of the ADT.
 */

class ASet { 
    // Abstract state 
    ghost var s:set<int>;

    // Representation state
    var a:array<int>;
    var size:int;

    // The mapping function between abstract and representation state
    function Sound():bool
        reads this,a
        requires RepInv();
    {
        forall x::(x in s) <==> exists p::(0<=p<size) && (a[p] == x) 
    }

    function RepInv():bool
        reads this,a
    { 
        0 < a.Length && 
        0 <= size <= a.Length && 
        unique(a,0,size)
    }

    function AbsInv():bool
        reads this,a
    {
        RepInv() && Sound()
    }


    // Spec functions

    function unique(b:array<int>, l:int, h:int):bool
        reads b;
        requires 0 <= l <= h <= b.Length ;
    { 
        forall k::(l <= k < h ) ==> forall j::(k<j<h)  ==> b[k] != b[j] 
    }

    function count():int
        reads this,a
        requires RepInv();
    { size }

    function maxsize():int
        reads this,a
        requires RepInv();
    { a.Length }

    // Implementation: Constructor and Methods 

    constructor(SIZE:int)
        requires SIZE > 0;
        ensures AbsInv() && s == {};
    {
        // Init of Representation state
        a := new int[SIZE];
        size := 0;
        // Init of Abstract state
        s := {}; 
    }

    method find(x:int) returns (r:int)
        requires AbsInv()
        ensures  AbsInv()
        ensures -1 <= r < size;
        ensures r < 0 ==> forall j::(0<=j<size) ==> x != a[j];
        ensures r >=0 ==> a[r] == x;
    {
        var i:int := 0;
        while (i<size)
            decreases size-i
            invariant 0 <= i <= size;
            invariant forall j::(0<=j<i) ==> x != a[j];
        {
            if (a[i]==x) { return i; }
            i := i + 1;
        }
        return -1;
    } 

    method add(x:int)
        modifies a, this
        requires AbsInv()
        requires count() < maxsize() 
        ensures  AbsInv() && s == old(s) + {x}
    { 
        var i := find(x);
        if (i < 0) {
            a[size] := x;
            s := s + { x };
            size := size + 1;
            assert a[size-1] == x;
            assert forall i :: (0<=i<size-1) ==> (a[i] == old(a[i]));
            assert forall x::(x in s) <==> exists p::(0<=p<size) && (a[p] == x);
        }
    }
}