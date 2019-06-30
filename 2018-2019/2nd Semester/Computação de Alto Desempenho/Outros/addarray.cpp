#include <array>
#include <algorithm>
#include <iostream>

using namespace std;


template <typename T, size_t Size>
ostream& operator<<(ostream& out, const array<T, Size>& a) {

    out << "[ ";
    copy(a.begin(),
         a.end(),
         ostream_iterator<T>(out, " "));
    out << "]";

    return out;
}

int main() {

    constexpr size_t size = 100;

    array<int, size> a;
    fill(a.begin(), a.end(), 1);

    array<int, size> b;
    fill(b.begin(), b.end(), 2);

    array<int, size> c;
    for (int i = 0; i < size; i++)
        c[i] = a[i] + b[i];

    cout << c << "\n";

    return 0;
}