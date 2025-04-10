package maps;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * @see AbstractIterableMap
 * @see Map
 */
public class ChainedHashMap<K, V> extends AbstractIterableMap<K, V> {

    private static final double DEFAULT_RESIZING_LOAD_FACTOR_THRESHOLD = 0.75;
    private static final int DEFAULT_INITIAL_CHAIN_COUNT = 100;
    private static final int DEFAULT_INITIAL_CHAIN_CAPACITY = 16;

    /*
    Warning:
    You may not rename this field or change its type.
    We will be inspecting it in our secret tests.
     */
    AbstractIterableMap<K, V>[] chains;

    // You're encouraged to add extra fields (and helper methods) though!
    // Add thresholds for load factors
    private final double loadFactor;
    // Add the initial capacity of each chain
    private final int initialCapacity;
    // Add the number of elements in the current hash table
    private int size;
    /**
     * Constructs a new ChainedHashMap with default resizing load factor threshold,
     * default initial chain count, and default initial chain capacity.
     */
    public ChainedHashMap() {
        this(DEFAULT_RESIZING_LOAD_FACTOR_THRESHOLD, DEFAULT_INITIAL_CHAIN_COUNT, DEFAULT_INITIAL_CHAIN_CAPACITY);
    }

    /**
     * Constructs a new ChainedHashMap with the given parameters.
     *
     * @param resizingLoadFactorThreshold the load factor threshold for resizing. When the load factor
     *                                    exceeds this value, the hash table resizes. Must be > 0.
     * @param initialChainCount the initial number of chains for your hash table. Must be > 0.
     * @param chainInitialCapacity the initial capacity of each ArrayMap chain created by the map.
     *                             Must be > 0.
     */
    public ChainedHashMap(double resizingLoadFactorThreshold, int initialChainCount, int chainInitialCapacity) {
        if (resizingLoadFactorThreshold <= 0 || initialChainCount <= 0 || chainInitialCapacity <= 0) {
            throw new IllegalArgumentException("must be > 0");
        }
        // Initialize
        this.loadFactor = resizingLoadFactorThreshold;
        this.initialCapacity = chainInitialCapacity;
        this.chains = createArrayOfChains(initialChainCount);
        this.size = 0;
    }

    /**
     * This method will return a new, empty array of the given size that can contain
     * {@code AbstractIterableMap<K, V>} objects.
     * Note that each element in the array will initially be null.
     * Note: You do not need to modify this method.
     * @see ArrayMap createArrayOfEntries method for more background on why we need this method
     */
    @SuppressWarnings("unchecked")
    private AbstractIterableMap<K, V>[] createArrayOfChains(int arraySize) {
        return (AbstractIterableMap<K, V>[]) new AbstractIterableMap[arraySize];
    }

    /**
     * Returns a new chain.
     * This method will be overridden by the grader so that your ChainedHashMap implementation
     * is graded using our solution ArrayMaps.
     * Note: You do not need to modify this method.
     */
    protected AbstractIterableMap<K, V> createChain(int initialSize) {
        return new ArrayMap<>(initialSize);
    }

    @Override
    public V get(Object key) {
        // Calculate the hash code of the key
        int hashCode = key == null ? 0 : key.hashCode();
        // Maps the hash code to the index range of the chains array
        int index = Math.floorMod(hashCode, chains.length);
        // Check the value of chains[index]
        if (chains[index] != null) {
            return chains[index].get(key);
        }
        return null;
    }

    @Override
    public V put(K key, V value) {
        // Calculate the current load factor
        int currentSize = size+ 1;
        int chainsLength = chains.length;
        double currentLF = (double) currentSize / chainsLength;
        // Check if it needs to resize
        if (currentLF > loadFactor) {
            resize();
        }
        // Calculates the hash of the key
        int hashCode = key == null ? 0 : key.hashCode();
        // Maps the hash code to the index range of the chains array
        int index = Math.floorMod(hashCode, chains.length);
        // Check if chains[index] is null
        if (chains[index] == null) {
            chains[index] = createChain(initialCapacity);
        }
        // Get the past value
        V past = chains[index].put(key, value);
        // If the past value is null, increase size
        if (past == null) {
            size++;
        }
        return past;
    }

    private void resize() {
        // Calculate the new capacity to ensure that
        // it is a minimum prime number greater than or equal to twice the current capacity
        // to avoid collisions
        int newCapacity = findNextPrime(chains.length * 2);
        // Create a new chain array
        AbstractIterableMap<K, V>[] newChains = createArrayOfChains(newCapacity);
        // Traverse all chains
        for (AbstractIterableMap<K, V> chain : chains) {
            if (chain == null) {
                continue; //
            }
            // Iterate through each entry in the chain
            for (Map.Entry<K, V> entry : chain) {
                K key = entry.getKey();
                // Calculates the hash value of the key
                int hashCode = Objects.hashCode(key);
                // Maps the hash code to the index range of the chains array
                int newIndex = Math.floorMod(hashCode, newCapacity);
                // If the index position of the new chain is empty, a new chain is created
                if (newChains[newIndex] == null) {
                    newChains[newIndex] = createChain(initialCapacity);
                }
                // Put the entry in a new chain
                newChains[newIndex].put(key, entry.getValue());
            }
        }
        // Update the chains
        chains = newChains;
    }
    // Find the smallest prime number greater than or equal to n
    private int findNextPrime(int n) {
        if (n <= 1) {
            return 2;
        }
        while (true) {
            if (isPrime(n)) {
                return n;
            }
            n++;
        }
    }
    // Determine whether a number is prime
    private boolean isPrime(int n) {
        if (n <= 1) {
            return false;
        }
        if (n == 2) {
            return true;
        }
        if (n % 2 == 0) {
            return false;
        }
        for (int i = 3; i * i <= n; i += 2) {
            if (n % i == 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public V remove(Object key) {
        // Calculate the hash value of the key
        int hashCode = key == null ? 0 : key.hashCode();
        // Maps the hash code to the index range of the chains array
        int index = Math.floorMod(hashCode, chains.length);
        // Check whether a linked list already exists at this index location
        if (chains[index] != null) {
            V usedvalue = chains[index].remove(key);
            // If the used value is not null, reduce size
            if (usedvalue != null) {
                size--;
            }
            return usedvalue;
        }
        return null;
    }

    @Override
    public void clear() {
        // Reinitialize the linked array
        this.chains = createArrayOfChains(chains.length);
        this.size = 0;
    }

    @Override
    public boolean containsKey(Object key) {
        // Calculate the hash code of the key
        int hashCode = (key == null) ? 0 : key.hashCode();
        // Maps the hash code to the index range of the chains array
        int index = Math.floorMod(hashCode, chains.length);
        // Check the chain at the index is empty
        if (chains[index] == null) {
            return false;
        }
        // Check that the chain contains the specified key
        return chains[index].containsKey(key);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
        // Note: you won't need to change this method (unless you add more constructor parameters)
        return new ChainedHashMapIterator<>(this.chains);
    }


    /*
    See the assignment webpage for tips and restrictions on implementing this iterator.
     */
    private static class ChainedHashMapIterator<K, V> implements Iterator<Map.Entry<K, V>> {
        private AbstractIterableMap<K, V>[] chains;
        // You may add more fields and constructor parameters
        private int chainIndex;
        private Iterator<Map.Entry<K, V>> iterator;

        public ChainedHashMapIterator(AbstractIterableMap<K, V>[] chains) {
            this.chains = chains;
            this.chainIndex = 0;
            this.iterator = getNextChainIterator();
        }
        // Get the iterator of the next non-empty linked list
        private Iterator<Map.Entry<K, V>> getNextChainIterator() {
            // Iterate through the chains array
            for (; chainIndex < chains.length; chainIndex++) {
                //Get chain of the current index location
                AbstractIterableMap<K, V> chain = chains[chainIndex];
                // Check whether the linked list exists and is not empty
                if (chain != null && !chain.isEmpty()) {
                    return chain.iterator();
                }
            }
            return null;
        }

        @Override
        public boolean hasNext() {
            // Check if the iterator of the current chain has the next element
            if (iterator != null && iterator.hasNext()) {
                return true;
            }
            // Switch to the next chain
            chainIndex++;
            iterator = getNextChainIterator();
            // Check if the iterator of the new chain has the next element
            return iterator != null && iterator.hasNext();
        }

        @Override
        public Map.Entry<K, V> next() {
            // Check if there is another element
            if (hasNext()) {
                return iterator.next();
            } else {
                throw new NoSuchElementException();
            }
        }
    }
}
