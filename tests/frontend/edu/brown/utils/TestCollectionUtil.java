package edu.brown.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

import junit.framework.TestCase;

import org.apache.commons.collections15.set.ListOrderedSet;

import edu.brown.rand.DefaultRandomGenerator;

public class TestCollectionUtil extends TestCase {
    
    private final Random rand = new Random();
    
    /**
     * testIterableEnumeration
     */
    public void testIterableEnumeration() {
        final int size = 10;
        Enumeration<Integer> e = new Enumeration<Integer>() {
            int ctr = 0;
            @Override
            public Integer nextElement() {
                return (ctr++);
            }
            @Override
            public boolean hasMoreElements() {
                return (ctr < size);
            }
        };
        
        List<Integer> found = new ArrayList<Integer>();
        for (Integer i : CollectionUtil.iterable(e))
            found.add(i);
        assertEquals(size, found.size());
    }
    
    /**
     * testAddAll
     */
    public void testAddAll() {
        int cnt = rand.nextInt(50) + 1; 
        List<Integer> l = new ArrayList<Integer>();
        Integer a[] = new Integer[cnt];
        for (int i = 0; i < cnt; i++) {
            int next = rand.nextInt(); 
            l.add(next);
            a[i] = next;
        } // FOR
        
        Collection<Integer> c = CollectionUtil.addAll(new HashSet<Integer>(), l);
        assertEquals(l.size(), c.size());
        assert(c.containsAll(l));
        
        c = CollectionUtil.addAll(new HashSet<Integer>(), a);
        assertEquals(l.size(), c.size());
        assert(c.containsAll(l));
    }
    
    /**
     * testSortByValues
     */
    public void testSortByValues() {
        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("b", 2000);
        map.put("a", 1000);
        map.put("c", 4000);
        map.put("d", 3000);
        
        Map<String, Integer> sorted = CollectionUtil.sortByValues(map);
        assertNotNull(sorted);
        assertEquals(map.size(), sorted.size());
        assert(map.keySet().containsAll(sorted.keySet()));
        assert(map.values().containsAll(sorted.values()));

        List<String> list = new ArrayList<String>(sorted.keySet());
        assertEquals("a", list.get(0));
        Collections.reverse(list);
        assertEquals("c", list.get(0));
    }
    
    /**
     * testGetGreatest
     */
    public void testGetGreatest() {
        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 4);
        map.put("d", 3);
        String key = CollectionUtil.getGreatest(map);
        assertEquals("c", key);
    }
    
    /**
     * testGetFirst
     */
    public void testGetFirst() {
        List<String> list = new ArrayList<String>();
        list.add("a");
        list.add("b");
        list.add("c");
        String key = CollectionUtil.first(list);
        assertEquals("a", key);
    }
    
    /**
     * testPop
     */
    @SuppressWarnings("unchecked")
    public void testPop() {
        String expected[] = new String[11];
        DefaultRandomGenerator rng = new DefaultRandomGenerator();
        for (int i = 0; i < expected.length; i++) {
            expected[i] = rng.astring(1, 32);
        } // FOR
        
        Collection<String> collections[] = new Collection[] {
            CollectionUtil.addAll(new ListOrderedSet<String>(), expected),
            CollectionUtil.addAll(new HashSet<String>(), expected),
            CollectionUtil.addAll(new ArrayList<String>(), expected),
        };
        for (Collection<String> c : collections) {
            assertNotNull(c);
            assertEquals(c.getClass().getSimpleName(), expected.length, c.size());
            String pop = CollectionUtil.pop(c);
            assertNotNull(c.getClass().getSimpleName(), pop);
            assertEquals(c.getClass().getSimpleName(), expected.length-1, c.size());
            assertFalse(c.getClass().getSimpleName(), c.contains(pop));
            
            if (c instanceof List || c instanceof ListOrderedSet) {
                assertEquals(c.getClass().getSimpleName(), expected[0], pop);
            }
        } // FOR
    }
}
