package org.apache.openjpa.util;

import org.junit.*;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.Arrays;
import java.util.Collection;
import static org.mockito.Mockito.*;

/**
 * Unit test of @link CacheMap
 * Martina Salvati
 */

@RunWith(Enclosed.class)
public class CacheMapTest {

    @RunWith(Parameterized.class)
    public static class CacheMapPutTest<T> {

        @Rule
        public MockitoRule mockitoRule = MockitoJUnit.rule();

        private CacheMap cacheMap ;
        private final Object keytoput;
        private final Object valuetoput;
        private final boolean pinned;
        public  final boolean alreadyput;
        public final boolean soft;
        public final Integer numberofInvocation;

        @Before
        public void setUp() throws Exception {
            this.cacheMap = spy(this.cacheMap);
        }
        public CacheMapPutTest(CacheMap cacheMap, Object keytoput, Object valuetoput, boolean pinned, boolean alreadyput, boolean soft, Integer numberofInvocation) {
            this.cacheMap = cacheMap;
            this.keytoput = keytoput;
            this.valuetoput = valuetoput;
            this.pinned = pinned;
            this.alreadyput = alreadyput;
            this.soft = soft;
            this.numberofInvocation = numberofInvocation;
        }

        @Parameterized.Parameters(name = "{index}: {0}")
        public static Collection<Object[]> getTestParameters() {
            return Arrays.asList(new Object[][]{
                    {new CacheMap(true,1000,1001,1L,1),"key","value1",true,false,false,2},
                    //adequacy
                    {new CacheMap(true,1000,1001,1L,1),"key","value1",true,true,false,3},
                    {new CacheMap(true,1000,1001,1L,1),"key","value1",false,false,false,1},
                    {new CacheMap(true,1000,1001,1L,1),"key","value1",false,true,false,2},
                    {new CacheMap(true,1000,1001,1L,1),"key","value1",false,true,true,2},
                    {new CacheMap(true,0,1,1L,1),"key","value1",false,true,true,3},
            });
        }

        @Test
        public void putTestBase() {
            if(soft) {
                cacheMap.softMap.put(keytoput,valuetoput);
            }
            if(alreadyput) {
                cacheMap.put(keytoput,valuetoput);
            }
            if(pinned) {
                cacheMap.pin(keytoput);
            }
            cacheMap.put(keytoput,valuetoput);
            Assert.assertEquals(cacheMap.get(keytoput),valuetoput);
            verify(cacheMap,times(numberofInvocation)).writeLock();  //adequacy mutation testing
            verify(cacheMap,times(numberofInvocation)).writeUnlock(); //adequacy mutation testing
            Mockito.reset(cacheMap);
        }

    }

    @RunWith(Parameterized.class)
    public static class CacheMapRemoveTest<T> {

        @Rule
        public MockitoRule mockitoRule = MockitoJUnit.rule();

        private CacheMap cacheMap;
        private final Object valuetoremove;
        private final boolean pinned;
        public final Integer numberofInvocation;
        @Before
        public void setUp() throws Exception {
            this.cacheMap = spy(this.cacheMap);
        }

        public CacheMapRemoveTest(CacheMap cacheMap, Object valuetoremove, boolean pinned, Integer numberofInvocation) {
            this.cacheMap = cacheMap;
            this.valuetoremove = valuetoremove;
            this.pinned = pinned;
            this.numberofInvocation = numberofInvocation;
        }

        @Parameterized.Parameters(name = "{index}: {0}")
        public static Collection<Object[]> getTestParameters() {
            return Arrays.asList(new Object[][]{
                    {new CacheMap(true,1000,1001,1L,1),3,false,901},
                    //Adequacy
                    {new CacheMap(true,1000,1001,1L,1),3,true,902},
                    {new CacheMap(true,1000,1001,1L,1),null,false,901},
                    {new CacheMap(true,250,189,1L,1),null,true,152},
            });
        }

        @Test
        public void removeTest() {
            if(pinned) {
                cacheMap.pin(valuetoremove);
            }
            for(int i=0;i<cacheMap.getCacheSize()-100;i++) {
                cacheMap.put(i,i);
            }
            Assert.assertEquals(cacheMap.remove(valuetoremove),valuetoremove);
            verify(cacheMap,times(numberofInvocation)).writeLock();  //adequacy mutation testing
            verify(cacheMap,times(numberofInvocation)).writeUnlock(); //adequacy mutation testing
        }
    }

    @RunWith(Parameterized.class)
    public static class CacheMapPinTest<T> {
        @Rule
        public MockitoRule mockitoRule = MockitoJUnit.rule();

        private CacheMap cacheMap;
        private final Object key;
        private final Object value;
        private final boolean alreadypin;
        private final boolean alreadyput;
        private final boolean soft;
        private final boolean result;
        public final Integer numberofInvocation;

        @Before
        public void setUp() throws Exception {
            this.cacheMap = spy(this.cacheMap);
        }

        public CacheMapPinTest(CacheMap cacheMap, Object key, Object value, boolean alreadypin, boolean alreadyput, boolean soft, boolean result, Integer numberofInvocation) {
            this.cacheMap = cacheMap;
            this.key = key;
            this.value = value;
            this.alreadypin = alreadypin;
            this.alreadyput = alreadyput;
            this.soft = soft;
            this.result = result;
            this.numberofInvocation = numberofInvocation;
        }

        @Parameterized.Parameters(name = "{index}: {0}")
        public static Collection<Object[]> getTestParameters() {
            return Arrays.asList(new Object[][]{
                    {new CacheMap(true,1000,1001,1L,1),"key1","value1",false,false,false,false,1},
                    //adequacy
                    {new CacheMap(true,1000,1001,1L,1),"key2","value2",true,false,false,false,2},
                    {new CacheMap(true,1000,1001,1L,1),"key3","value3",true,true,true,true,3},
                    {new CacheMap(true,1000,1001,1L,1),"key4","value4",false,false,true,true,1},
                    {new CacheMap(true,1000,1001,1L,1),"key5","value5",false,true,false,true,2}
            });
        }

        @Test
        public void pinTest() {
            if(soft)
                cacheMap.softMap.put(key,value);
            if(alreadypin)
                cacheMap.pin(key);
            if(alreadyput)
                cacheMap.put(key,value);
            Assert.assertEquals(cacheMap.pin(key),result);
            verify(cacheMap,times(numberofInvocation)).writeLock();  //adequacy mutation testing
            verify(cacheMap,times(numberofInvocation)).writeUnlock(); //adequacy mutation testing
        }
    }


    @RunWith(Parameterized.class)
    public static class CacheMapUnPinTest<T> {

        @Rule
        public MockitoRule mockitoRule = MockitoJUnit.rule();

        private  CacheMap cacheMap;
        private final Object key;
        private final Object value;
        private final boolean pinned;
        private final boolean result;
        public final Integer numberofInvocation;
        @Before
        public void setUp() throws Exception {
            this.cacheMap = spy(this.cacheMap);
        }
        public CacheMapUnPinTest(CacheMap cacheMap, Object key, Object value, boolean pinned, boolean result, Integer numberofInvocation) {
            this.cacheMap = cacheMap;
            this.key = key;
            this.value = value;
            this.pinned = pinned;
            this.result = result;
            this.numberofInvocation = numberofInvocation;
        }


        @Parameterized.Parameters(name = "{index}: {0}")
        public static Collection<Object[]> getTestParameters() {
            return Arrays.asList(new Object[][]{
                    {new CacheMap(true,1000,1001,1L,1),"key1","value1",false,false,1},
                    //adequacy
                    {new CacheMap(true,1000,1001,1L,1),"key1","value1",true,true,4},
            });
        }

        @Test
        public void unpinTest() {
            if(pinned) {
                cacheMap.pin(key);
                cacheMap.put(key,value);
            }
            Assert.assertEquals(cacheMap.unpin(key),result);
            verify(cacheMap,times(numberofInvocation)).writeLock();  //adequacy mutation testing
            verify(cacheMap,times(numberofInvocation)).writeUnlock(); //adequacy mutation testing
        }
    }

    @RunWith(Parameterized.class)
    public static class CacheMapPutAllTest<T> {

        private final CacheMap cacheMap;
        private final CacheMap putMap;
        private final boolean notEmptyMap;
        private final boolean replaceExisting;

        public CacheMapPutAllTest(CacheMap cacheMap, CacheMap putMap, boolean notEmptyMap, boolean replaceExisting) {
            this.cacheMap = cacheMap;
            this.putMap = putMap;
            this.notEmptyMap = notEmptyMap;
            this.replaceExisting = replaceExisting;
        }


        @Parameterized.Parameters(name = "{index}: {0}")
        public static Collection<Object[]> getTestParameters() {
            return Arrays.asList(new Object[][]{
                    {new CacheMap(true,100,10,1L,1),new CacheMap(true),false,true},
                    //adequacy
                   {new CacheMap(true,100,10,1L,1),new CacheMap(true,10,10,1L,1),true,true},
                   {new CacheMap(true,100,10,1L,1),new CacheMap(true,10,10,1L,1),true,false},
                   {new CacheMap(true,100,10,1L,1),new CacheMap(true,10,10,1L,1),false,false},
            });
        }

        @Test
        public void putAlltest() {
           if(notEmptyMap)  {
               for(int i=0;i<putMap.getCacheSize();i++) {
                   putMap.put(i,i);
                   putMap.softMap.put(i,i);
                   putMap.pinnedMap.put(i,i);
               } }
           cacheMap.putAll(putMap,replaceExisting);
        }
    }

    @RunWith(Parameterized.class)
    public static class CacheMapContainsKeyAndValue<T> {
        @Rule
        public MockitoRule mockitoRule = MockitoJUnit.rule();

        private  CacheMap cacheMap;
        private final boolean pinned;
        private final boolean soft;
        private final Object key;
        private final Object value;
        private final Object result;

        @Before
        public void setUp() throws Exception {
            this.cacheMap = spy(this.cacheMap);
        }

        public CacheMapContainsKeyAndValue(CacheMap cacheMap, boolean pinned, boolean soft, Object key, Object value, Object result) {
            this.cacheMap = cacheMap;
            this.pinned = pinned;
            this.soft = soft;
            this.key = key;
            this.value = value;
            this.result = result;
        }

        @Parameterized.Parameters(name = "{index}: {0}")
        public static Collection<Object[]> getTestParameters() {
            return Arrays.asList(new Object[][]{
                    {new CacheMap(true,10, 11,1L,1),false,false,"key","value",true},
                    //adequacy
                    {new CacheMap(true,0, 1,1L,1),false,false,"key","value",false},
                    {new CacheMap(true,2, 3,1L,1),true,false,"key","value",true},
                    {new CacheMap(true,0, 3,1L,1),true,false,"key","value",true},
                    {new CacheMap(true,0, 3,1L,1),false,true,"key","value",true}
            });
        }

        @Test
        public void containsKeyAndGetValue() {
           for(int i=0;i<cacheMap.getCacheSize();i++) {
               cacheMap.put(key,value);
           }
           if(pinned)
               cacheMap.pinnedMap.put(key,value);
           if(soft)
               cacheMap.softMap.put(key,value);
            Assert.assertEquals(cacheMap.containsKey(key),result);
            Assert.assertEquals(cacheMap.containsValue(value),result);
            verify(cacheMap,times(2)).readLock();  //adequacy mutation testing
        }
    }
}