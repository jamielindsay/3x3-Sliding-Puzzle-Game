package puzzle;

/**
 * Author: drs
 * Based on Haskell's standard list functions
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class List<E> {

    /*
     * We begin with some static methods. These represent functions that generate list
     * instances. In some cases these have specialised types
     */

    /**
     * @param x			the head of the new list
     * @param xs		the tail of the new list
     * @return			the new list (x:xs)
     */
    public static <E> List<E> cons(E x, List<E> xs) {
        return xs.addFront(x);
    }

    /**
     * Create a new empty list
     * @return		[]
     */
    public static <E> List<E> emptyList() {
        return new List<E>();
    }

    /**
     * Create a singleton list
     * @param x		the only value in the list
     * @return		[x]
     */
    public static <E> List<E> single(E x) {
        return cons(x, emptyList());
    }

    /**
     * Concatenate a list of lists into a single list
     * @param xss	The list of lists to be flattened
     */
    public static <E> List<E> concat(List<List<E>> xss) {
        return xss.foldr(List::append, emptyList());
    }

    /**
     * Generate a list from multiple copies of the same element
     * @param n		the number of copies
     * @param x		the item to be replicated
     * @return		the list [x,x,x,x...] (n times)
     */
    public static <E> List<E> repeat(int n, E x) {
        if (n<=0)
            return emptyList();
        else
            return cons(x, repeat(n-1, x));
    }

    /**
     * Generate successive values while a condition is true. If p(startingValue) is false then an
     * empty list is returned
     * @param p					the condition to be satisfied by each value
     * @param f					how to generate the successor value
     * @param startingValue		the initial value
     * @return					the list [startingValue, f(startingValue), f(f(startingValue))...]
     * 							for all 0&le;k such that p(f^k(startingValue)) is true
     */
    public static <E> List<E> iterateWhile(Predicate<E> p, UnaryOperator<E> f, E startingValue) {
        if (p.negate().test(startingValue))
            return emptyList();
        else
            return cons(startingValue, iterateWhile(p, f, f.apply(startingValue)));
    }

    /**
     * Convert an array into a list
     * @param array	the array to be converted [x0,x1,x2...]
     * @return		the corresponding list x0:x1:x2...
     */
    public static <E> List<E> arrayToList(E[] array) {
        List<E> xs = new List<E>();
        for(int i = array.length - 1; i>=0; i--)
            xs = cons(array[i], xs);
        return xs;
    }

    /**
     * Convert an arraylist into a list
     * @param arrayList		the arraylist to be converted [x0,x1,x2...]
     * @return				the corresponding list x0:x1:x2...
     */
    public static <E> List<E> arrayListToList(ArrayList<E> arrayList) {
        List<E> xs = new List<E>();
        for(E x : arrayList)
            xs = cons(x, xs);
        return xs.reverse();
    }

    /**
     * Convert a stream into a list
     * @param stream	the stream to be converted x0,x1,x2...
     * @return			the corresponding list x0:x1:x2...
     */
    public static <E> List<E> streamToList(Stream<E> stream) {
        return arrayListToList((ArrayList<E>)(stream.collect(Collectors.toList())));
    }

    /**
     * Turn rows into columns and columns into rows.
     * This algorithm translated from Haskell's Data.List.transpose implementation
     * @param list		a two-dimensional list  (list of lists)
     * @return 			a list of lists for which p.transpose(i,j) = p(j,i)
     */
    public static <E> List<List<E>> transpose(List<List<E>> list) {
        if (list.isEmpty())
            return emptyList();
        else
        if (list.head().isEmpty())
            return transpose(list.tail());
        else {
            E x = list.head().head();
            List<E> xs = list.head().tail();
            List<List<E>> xss = list.tail().filter(l -> !l.isEmpty());
            return cons(cons(x, xss.map(List::head)), transpose(cons(xs,xss.map(List::tail))));
        }
    }

    /**
     * Return an integer subrange
     * @param a		the start value
     * @param b		the end value (inclusive)
     * @return		[a, a+1, a+2, ..., b]
     */
    public static List<Integer> rangeClosed(int a, int b) {
        return arrayToList(IntStream.rangeClosed(a, b).boxed().toArray(Integer[]::new));
    }

    /**
     * Return an integer subrange
     * @param a		the start value
     * @param b		the end value (exclusive)
     * @return		[a, a+1, a+2, ..., b-1]
     */
    public static List<Integer> range(int a, int b) {
        return arrayToList(IntStream.range(a, b).boxed().toArray(Integer[]::new));
    }

    /**
     * Return an integer subrange
     * @param a		the start value
     * @param b		the end value (possibly inclusive)
     * @param c		the step value
     * @return		[a, a+c, a+2c, ...] last value is &le;b
     */
    public static List<Integer> rangeStep(int a, int b, int c) {
        int k = (b - a) / c;
        return arrayToList(Stream.iterate(a, x -> x+c).limit(k+1).toArray(Integer[]::new));
    }


    /**
     * Convert a list of characters to a string
     */
    public static String implode(List<Character> list) {
        return list	.map(x -> x.toString())
                .foldr((a,b) -> a+b, "");
    }

    /**
     * Convert a string to a list of characters
     */
    public static List<Character> explode(String s) {
        return streamToList(s.chars().mapToObj(i -> (char)i));
    }

    /**
     * Any instance represents an empty list.  All instance methods act upon empty lists.
     * Overridden versions for non-empty lists are handled in an anonymous subclass that
     * is defined within the addFront() method.
     */

    /**
     * Perform a structural equality check (same elements in same order).
     * Two empty lists are equal.
     */
    @SuppressWarnings("unchecked")
    @Override public boolean equals(Object that) {
        return (that != null && this.getClass() == that.getClass() && ((List<E>)that).isEmpty());

    }

    /**
     * Group a list into sublists of size n
     * Note that this method is inherited by subclasses, and not overridden
     * @param n			the size of each sublist (except possibly the last which may be shorter)
     * @return			the list of n-sized sublists
     */
    public List<List<E>> group(int n) {
        if (n<1)
            throw new IllegalArgumentException("group(n): n must be > 0");
        else
            return iterateWhile(xs -> !xs.isEmpty(), xs -> xs.drop(n), this).map(xs -> xs.take(n));
    }

    /**
     * Convert a list to a stream
     * Note that this method is inherited by subclasses, and not overridden
     * @return		a stream with the values drawn from the list
     */
    public Stream<E> listToStream() {
        return this.toArrayList().stream();
    }

    /**
     * Convert a list to an array list
     * @return		an array list with the values drawn from the list
     */
    public ArrayList<E> toArrayList() {
        return new ArrayList<E>();
    }

    /**
     * Convert a list to a string for, e.g., printing
     */
    @Override public String toString() {
        return "[]";
    }

    /**
     * Test if a list is empty.
     * @return		true if empty, false otherwise
     */
    public boolean isEmpty() {
        return true;
    }

    /**
     * Return the length of a list.
     * @return		0 if empty, 1 + length of tail otherwise
     */
    public int length() {
        return 0;
    }

    /**
     * Look up item in list at index (first item is at index zero)
     * @param k		the index
     * @return		error if empty or index out of range, otherwise item at index
     */
    public E at(int k) {
        throw new NoSuchElementException("at");
    }

    /**
     * Return the first item in the list
     * @return		nothing it empty, otherwise the first item in the list
     */
    public E head() {
        throw new NoSuchElementException("head");
    }

    /**
     * Return the sublist starting from the second element
     * @return		nothing if empty, otherwise the list without the first item
     */
    public List<E> tail() {
        throw new NoSuchElementException("tail");
    }

    /**
     * Append two lists.  This is O(N) where N is the length of this list
     * @return		the two lists joined together
     */
    public List<E> append(List<E> that) {
        return that;
    }

    /**
     * Reverse a list
     * @return		the list with the items in the reverse order
     */
    public List<E> reverse() {
        return emptyList();
    }

    /**
     * Take n elements from the front of a list
     * @param n		the number of elements to take
     * @return		the initial subsequence of length &le; n
     */
    public List<E> take(int n) {
        return emptyList();
    }

    /**
     * Drop n elements from the front of a list
     * @param n		the number of elements to drop
     * @return		the final subsequence after the first n elements have been removed
     */
    public List<E> drop(int n) {
        return emptyList();
    }

    /**
     * Initial subsequence while the condition is true
     * @param p		the condition each value must pass
     * @return		the initial subsequence of elements that all satisfy p
     */
    public List<E> takeWhile(Predicate<E> p) {
        return emptyList();
    }

    /**
     * Final subsequence with all initial elements satisfying the condition dropped
     * @param p		the condition each element in the dropped initial sequence must pass
     * @return		the final subsequence of elements once the initial subsequence of elements satisfying p are removed
     */
    public List<E> dropWhile(Predicate<E> p) {
        return emptyList();
    }

    /**
     * The items from the list that satisfy the condition in the original order
     * @param p		the condition each kept element must satisfy
     * @return		the sequence generated from all elements satisfying p in the original order
     */
    public List<E> filter(Predicate<E> p) {
        return emptyList();
    }

    /**
     * Returns true if all elements in the list satisfy the condition
     * @param p		the condition each element must satisfy
     * @return		true if all elements satisfy p, else false
     */
    public boolean all(Predicate<E> p) {
        return true;
    }
    /**
     * Returns true if any elements in the list satisfy the condition
     * @param p		the condition each element must satisfy
     * @return		true if any element satisfies p, else false
     */
    public boolean any(Predicate<E> p) {
        return false;
    }

    /**
     * Apply a function to each item in the list
     * @param f		the function to apply to each element
     * @return		the transformed list
     */
    public <F> List<F> map(Function<E,F> f) {
        return emptyList();
    }

    /**
     * Reduce a list by applying a binary operator between all elements bracketing to the left
     * foldl op accumulator [x1, x2,... , xk] = (((accumulator op x1) op x2) ... op xk)
     * @param op				the operator to apply between elements
     * @param accumulator		the accumulator value (and the initial left value)
     * @return					the value generated by reducing the list
     */
    public <F> F foldl(BiFunction<F,E,F> op, F accumulator) {
        return accumulator;
    }

    /**
     * Reduce a list by applying a binary operator between all elements bracketing to the left
     * foldl1 op 				[x1, x2,... , xk] = ((x1 op x2) ... op xk)
     * @param op				the operator to apply between elements
     * @return					the value generated by reducing the list
     */
    public E foldl1(BinaryOperator<E> op) {
        throw new UnsupportedOperationException("foldl1 emptylist");
    }

    /**
     * Reduce a list by applying a binary operator between all elements bracketing to the right
     * foldr op lastElement [x1, x2,... , xk] = x1 op (x2 op (... (xk op lastElement)...))
     * @param op				the operator to apply between elements
     * @param lastElement		the last element in the reduction
     * @return					the value generated by reducing the list
     */
    public <F> F foldr(BiFunction<E,F,F> op, F lastElement) {
        return lastElement;
    }

    /**
     * Reduce a list by applying a binary operator between all elements bracketing to the right
     * foldr1 op 				[x1, x2,... , xk] = x1 op (x2 op (...(op[k-1] op xk)...)
     * @param op				the operator to apply between elements
     * @return					the value generated by reducing the list
     */
    public E foldr1(BinaryOperator<E> op) {
        throw new UnsupportedOperationException("foldr1 emptylist");
    }

    /**
     * Apply a binary operator to corresponging elements in two lists
     * zipWith op [x1, x2, ...] [y1, y2, ...] = [x1 op y1, x2 op y2, ...]
     * The length of the result is the length of the shortest input list
     * @param that		the list to zip with this
     * @param op		the operator to apply to corresponding items
     * @return			the new list of items formed from the operator applied to corresponding pairs
     */
    public <F,G> List<G> zipWith(List<F> that, BiFunction<E,F,G> op) {
        return emptyList();
    }

    /**
     * Place an item in between elements in a list
     * e.g. [x1, x2, x3, ...].intersperse(sep) = [x1, sep, x2, sep, x3, sep, ...]
     * @param sep		the separator item
     * @return			the list with the separator interspersed between items
     */
    public List<E> intersperse(E sep) {
        return emptyList();
    }

    /**
     * Add an item to the front of a list  xs.addFront(x)  =  x:xs
     * @param x		the item to be added
     * @return		the new list with x added to the front
     */
    public List<E> addFront(E x){
        return new List<E>() {
            @SuppressWarnings("unchecked")
            @Override
            public boolean equals(Object that) {
                if (that == null || this.getClass() != that.getClass())
                    return false;
                else {
                    List<E> other = (List<E>)that;
                    return (this.head().equals(other.head()) && this.tail().equals(other.tail()));
                }
            }

            @Override
            public ArrayList<E> toArrayList() {
                ArrayList<E> l = List.this.toArrayList();
                l.add(0, x);
                return l;
            }

            @Override
            public String toString() {
                StringBuffer sb = new StringBuffer(); // localised mutable state
                this.map(Object::toString).intersperse(",").listToStream().forEachOrdered(s -> sb.append(s));
                return "[" + sb.toString() + "]";
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public int length() {
                return List.this.length() + 1;
            }

            @Override
            public E at(int k) {
                if (k<0)
                    throw new IndexOutOfBoundsException("at");
                else if (k==0)
                    return x;
                else
                    return List.this.at(k-1);
            }

            @Override
            public E head() {
                return x;
            }

            @Override
            public List<E> tail() {
                return List.this;
            }

            @Override
            public List<E> append(List<E> that) {
                return cons(x, List.this.append(that));
            }

            private List<E> rev(List<E> acc, List<E> remain) {
                if (remain.isEmpty())
                    return acc;
                else
                    return rev(cons(remain.head(), acc), remain.tail());
            }

            @Override
            public List<E> reverse() {
                return rev(emptyList(),this);
            }

            @Override
            public List<E> take(int n) {
                if (n<=0)
                    return emptyList();
                else
                    return cons(x, List.this.take(n-1));
            }

            @Override
            public List<E> drop(int n) {
                if (n<=0)
                    return this;
                else
                    return List.this.drop(n-1);
            }

            @Override
            public List<E> takeWhile(Predicate<E> p) {
                if(p.test(x))
                    return cons(x, List.this.takeWhile(p));
                else
                    return emptyList();
            }

            @Override
            public List<E> dropWhile(Predicate<E> p) {
                if(p.test(x))
                    return List.this.dropWhile(p);
                else
                    return this;
            }

            @Override
            public List<E> filter(Predicate<E> p) {
                if(p.test(x))
                    return cons(x, List.this.filter(p));
                else
                    return List.this.filter(p);
            }

            @Override
            public boolean all(Predicate<E> p) {
                if (p.test(x))
                    return List.this.all(p);
                else
                    return false;
            }

            @Override
            public boolean any(Predicate<E> p) {
                if (p.test(x))
                    return true;
                else
                    return List.this.any(p);
            }

            @Override
            public <F> List<F> map(Function<E,F> f) {
                return cons(f.apply(x), List.this.map(f));
            }

            //@Override
            public <F> F foldl(BiFunction<F,E,F> op, F accumulator) {
                return  List.this.foldl(op, op.apply(accumulator,x));
            }

            @Override
            public E foldl1(BinaryOperator<E> op) {
                return List.this.foldl(op, x);
            }

            @Override
            public <F> F foldr(BiFunction<E,F,F> op, F lastElement) {
                return op.apply(x, List.this.foldr(op, lastElement));
            }

            @Override
            public E foldr1(BinaryOperator<E> op) {
                return List.this.foldr(op, x);
            }

            @Override
            public <F,G> List<G> zipWith(List<F> that, BiFunction<E,F,G> f) {
                if (that.isEmpty())				// NB this cannot be empty
                    return new List<G>();
                else
                    return cons(f.apply(x, that.head()), List.this.zipWith(that.tail(), f));
            }

            @Override
            public List<E> intersperse(E sep) {
                if (List.this.isEmpty())
                    return single(x);
                else
                    return cons(x, cons(sep, List.this.intersperse(sep)));
            }
        };
    }
}