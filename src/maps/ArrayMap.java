package maps;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * @see AbstractIterableMap
 * @see Map
 */
public class ArrayMap<K, V> extends AbstractIterableMap<K, V> {
    // Default initial capacity for the internal array
    private static final int DEFAULT_INITIAL_CAPACITY = 10;
    /*
    Warning:
    You may not rename this field or change its type.
    We will be inspecting it in our secret tests.
     */
    SimpleEntry<K, V>[] entries;

    // The current size of the ArrayMap (number of key-value pairs stored)
    private int size;

    /**
     * Constructs a new ArrayMap with default initial capacity.
     */
    public ArrayMap() {
        this(DEFAULT_INITIAL_CAPACITY);
    }

    /**
     * Constructs a new ArrayMap with the given initial capacity (i.e., the initial
     * size of the internal array).
     *
     * @param initialCapacity the initial capacity of the ArrayMap. Must be > 0.
     */
    public ArrayMap(int initialCapacity) {
        if (initialCapacity <= 0)
        {
            throw new IllegalArgumentException("the initial capacity of the ArrayMap must be > 0 ");
        }
        this.entries = this.createArrayOfEntries(initialCapacity);
    }

    /**
     * This method will return a new, empty array of the given size that can contain
     * {@code Entry<K, V>} objects.
     *
     * Note that each element in the array will initially be null.
     *
     * Note: You do not need to modify this method.
     */
    @SuppressWarnings("unchecked")
    private SimpleEntry<K, V>[] createArrayOfEntries(int arraySize) {
        /*
        It turns out that creating arrays of generic objects in Java is complicated due to something
        known as "type erasure."

        We've given you this helper method to help simplify this part of your assignment. Use this
        helper method as appropriate when implementing the rest of this class.

        You are not required to understand how this method works, what type erasure is, or how
        arrays and generics interact.
        */
        return (SimpleEntry<K, V>[]) (new SimpleEntry[arraySize]);
    }

    // Returns the value to which the specified key is mapped.
    @Override
    public V get(Object key)
    {
        for (int i = 0; i < size; i++) {
            if (entries[i].getKey().equals(key))
            {
                return entries[i].getValue();
            }
        }
        return null;
    }

    // Associates the specified value with the specified key in this map.
    // Returns the previous value associated with key.
    @Override
    public V put(K key, V value)
    {
        // Check if the key already exists; update value if found
        for (int i = 0; i < size; i++) {
            if (entries[i].getKey().equals(key))
            {
                V oldValue = entries[i].getValue();
                entries[i].setValue(value);
                return oldValue;
            }
        }
        // Resize the array if it's full
        if (size == entries.length)
        {
            SimpleEntry<K, V>[] newArray = createArrayOfEntries(entries.length * 2);
            System.arraycopy(entries, 0, newArray, 0, size);
            entries = newArray;
        }
        // Add the new key-value pair
        entries[size++] = new SimpleEntry<>(key, value);
        return null;
    }

    // Removes and returns the mapping for a key from this map if it is present.
    @Override
    public V remove(Object key)
    {
        for (int i = 0; i < size; i++) {
            if (entries[i].getKey().equals(key))
            {
                V oldValue = entries[i].getValue();
                // Replace the removed entry with the last entry
                entries[i] = entries[size - 1];
                entries[size - 1] = null;
                size--;
                return oldValue;
            }
        }
        return null;
    }

    // Removes all of the mappings from this map.
    @Override
    public void clear()
    {
        for (int i = 0; i < size; i++) {
            entries[i] = null;
        }
        size = 0;
    }

    // Returns true if this map contains a mapping for the specified key.
    @Override
    public boolean containsKey(Object key)
    {
        for (int i = 0; i < size; i++) {
            if (entries[i].getKey().equals(key)) {
                return true;
            }
        }
        return false;
    }

    // Returns the number of key-value mappings in this map.
    @Override
    public int size() {
        return size;
    }

    // Returns an iterator that,
    // when used, will yield all key-value mappings contained within this map.
    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
        // Note: You may or may not need to change this method, depending on whether you
        // add any parameters to the ArrayMapIterator constructor.
        return new ArrayMapIterator<>(this.entries);
    }

    // Private static class for the iterator implementation.
    private static class ArrayMapIterator<K, V> implements Iterator<Map.Entry<K, V>> {
        private final SimpleEntry<K, V>[] entries;
        private int index;

        // Constructs an iterator for the given array of entries.
        public ArrayMapIterator(SimpleEntry<K, V>[] entries)
        {
            this.entries = entries;
            this.index = 0;
        }

        // Returns true if the iteration has more elements.
        @Override
        public boolean hasNext()
        {
            while (index < entries.length && entries[index] == null) {
                    index++;
            }
            return index < entries.length;
        }

        // Returns the next element in the iteration.
        @Override
        public Map.Entry<K, V> next()
        {
            // throws NoSuchElementException if the iteration has no more elements.
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return entries[index++];
        }
    }
}
