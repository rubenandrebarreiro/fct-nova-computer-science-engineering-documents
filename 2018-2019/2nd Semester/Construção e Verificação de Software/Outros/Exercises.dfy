// Exercise 9

method Reverse(a:array<int>, n:int) returns (b:array<int>)
    requires 0 <= n <= a.Length
    ensures b.Length == a.Length
    ensures forall j :: (0<= j < n) ==> b[j] == a[n-1-j]
    ensures forall j :: (n<= j < b.Length) ==> b[j] == a[j]
{
    b := new int[a.Length];
    var i := 0;
    while i < n
        decreases n-i
        invariant 0 <= i <= n
        invariant forall j :: (0<= j < i) ==> b[j] == a[n-1-j]
    {
        b[i] := a[n-1-i];
        i := i + 1;
    }
    assert forall j :: (0<= j < n) ==> b[j] == a[n-1-j];
    while i < a.Length
        decreases a.Length - i
        invariant n <= i <= a.Length
        invariant forall j :: (0<= j < n) ==> b[j] == a[n-1-j];
        invariant forall j :: (n <= j < i) ==> b[j] == a[j];
    {
        b[i] := a[i];
        i := i + 1;
    }
    assert i == a.Length;
}

// Exercise 10

// method IndexOf checks if b appears within a, and returns the 
// position if that is the case, or -1 if not
// in this example, a and b are c strings (null terminated)

function isequalfromN(a:array<char>, offset:int, b:array<char>, n:int):bool
    reads a,b
    requires 0 <= n <= b.Length
    requires 0 <= offset <= a.Length - n
{
    forall j :: 0 <= j < n ==> b[j] == a[offset+j]
}

method Equal(a:array<char>, offset:int, b:array<char>) returns (r:bool)
    requires 0 <= offset <= a.Length - b.Length
    ensures r <==> isequalfromN(a,offset, b, b.Length)
{
    var i := 0;
    r := true;
    while i < b.Length
        decreases b.Length - i
        invariant 0 <= i <= b.Length
        invariant r <==> isequalfromN(a,offset, b, i)
    {
        if a[offset+i] != b[i]
        {
            r := false; 
            break;
        }
        i := i + 1;
    }
}

method IndexOf(a:array<char>, b:array<char>) returns (pos:int)
    requires b.Length <= a.Length 
    ensures 0 <= pos ==> pos < a.Length-b.Length && isequalfromN(a,pos,b, b.Length)
    ensures pos == -1 ==> forall j :: 0 <= j < a.Length-b.Length ==> !isequalfromN(a,j,b,b.Length)
{
    var i := 0;
    var n := a.Length - b.Length;
    while i < n
        decreases n - i
        invariant 0 <= i <= n
        invariant forall j :: 0 <= j < i ==> !isequalfromN(a,j,b,b.Length)
    {
        var r := Equal(a,i,b);
        if r 
        { return i; }
        i := i + 1;
    }
    return -1;
}
