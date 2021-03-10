# LockdownSSL DataStructure

## Description
This is a DataStructure providing ...

* Namespace `LockdownSSL::`
* Templated  with `template<>`

## Inheritance
Inherits from [OtherDataStructure](https://google.com)

## Public Members

### Functions

* `int foo(int* lal, int s)`
[Does some stuff, returns other stuff]

* lol
### Variables
* `m_Member` [Thing type for bla bla]

## Protected Members

### Functions

* `int foo(int* lal, int s)`
[Does some stuff, returns other stuff]

* lol
### Variables
* `m_Member` [Thing type for bla bla]

## Private Members

### Functions

* `int foo(int* lal, int s)`
[Does some stuff, returns other stuff]

* lol
### Variables
* `m_Member` [Thing type for bla bla]

## Usage
If u want to do this, here is example code:
```C++
template<typename T, size_t S>
class FixedSizeSecureAllocator
{
public:
    using value_type = T;
    using size_type = size_t;
    using pointer = value_type*;

public:

    FixedSizeSecureAllocator() = default;

    pointer Allocate(size_type num)
    {
        return m_Arr;
    }

    pointer Reallocate(pointer ptr, size_type oldSize, size_type newSize)
    {
        if (ptr == m_Arr && newSize <= S && newSize < oldSize)
            std::memset(ptr + newSize, 0, (oldSize - newSize) * sizeof(T));

        return ptr;
    }

    void Deallocate(void* ptr, size_type num)
    {
        if(ptr == m_Arr)
            std::memset(ptr, 0, num * sizeof(T));
    }

private:
    T m_Arr[S];
};
```


***

Defined in [SecBlock.h](https://google.com) and [SecBlock.cpp](https://google.com)
