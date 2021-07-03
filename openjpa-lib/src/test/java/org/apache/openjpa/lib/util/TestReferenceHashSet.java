package org.apache.openjpa.lib.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import org.apache.openjpa.lib.util.collections.AbstractReferenceMap.ReferenceStrength;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.junit.Assert.*;


@RunWith(value = Parameterized.class)
public class TestReferenceHashSet {

	private ReferenceHashSet coll;
	//A set whose values may be stored as weak or soft references
	public TestReferenceHashSet(ReferenceHashSet coll) {
		super();
		this.coll = coll;
	}
	
	@Parameterized.Parameters( name = "{index}: {0}),{1},{2}" )
	public static Collection<Object[]> getTestParameters() {
		return Arrays.asList(new Object[][] { 
			    { new ReferenceHashSet(ReferenceStrength.WEAK) },
				{ new ReferenceHashSet(ReferenceStrength.SOFT) },
				{ new ReferenceHashSet(ReferenceStrength.HARD)},
				 });
	}
	@Test 
	public void testRetain() {
		Collection<Integer> collection2 = new ArrayList<>();
		for(int i=0;i<10;i++) {
			collection2.add(i);
		}
		assertTrue(coll.addAll(collection2));
		coll.clear();
		assertEquals(0, coll.size());
		assertTrue(coll.addAll(collection2));

		Collection<Integer> collection3 = new ArrayList<>();
		for(int i=5;i<10;i++) {
			collection3.add(i);
		}
		coll.retainAll(collection3);
		assertEquals(5, coll.size());
	}
	@Test
	public void testSize() {
		coll.clear();
		assertEquals(0, coll.size());
		coll.add(1);
		assertEquals(1, coll.size());
		coll.add(2);
		assertEquals(2, coll.size());
		coll.remove(1);
		assertEquals(1, coll.size());
		coll.clear();
		assertEquals(0, coll.size());
		
	}
	@Test
	public void testIsEmpty() {
		coll.clear();
		assertTrue(coll.isEmpty());
		coll.add(2);
		assertFalse(coll.isEmpty());
		coll.clear();
		assertTrue(coll.isEmpty());
	}

	@Test
	public void testRemoveAll() {
		coll.clear();
		Collection<Integer> collection = new ArrayList<>();
		coll.add(1);
		assertEquals(1, coll.iterator().next());
		collection.add(2);
		collection.add(3);
		collection.add(4);
		coll.add(2);
		coll.add(3);
		coll.add(4);
		assertTrue(!coll.remove(new Byte[4]));
		assertTrue(coll.remove(1));
		assertTrue(coll.removeAll(collection));
		assertEquals(0, coll.size());
		assertTrue(coll.isEmpty());
	}

	@Test
	public void testContainsAll() {
		coll.clear();
		coll.add(1);
		Collection<Integer> collection = new ArrayList<>();
		assertEquals(1, coll.iterator().next());
		collection.add(2);
		collection.add(3);
		collection.add(4);
		coll.add(2);
		coll.add(3);
		coll.add(4);
		assertTrue(coll.containsAll(collection));
	}
	@Test
	public void testAddAndContains() {
		coll.clear();
		assertEquals(0, coll.size());
		assertTrue(!coll.contains(1));
		assertTrue(coll.add(1));
		assertEquals(1, coll.size());
		assertTrue(coll.contains(1));
	}
	@Test
	public void testEquals() {
		coll.clear();
		assertTrue(coll.equals(coll));

		ReferenceHashSet coll2 = new ReferenceHashSet(ReferenceStrength.WEAK);
		assertTrue(coll.equals(coll2));
		
		coll.add(1);
		coll2.add(1);
		assertTrue(coll.equals(coll2));
		
		coll.clear();
		assertFalse(coll.equals(coll2));
	}
	
	@Test
	public void testArrayFunction() {
		coll.clear();
		Object[] na = coll.toArray();
		assertEquals(coll.size(),na.length);
		}
	}
