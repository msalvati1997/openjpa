package org.apache.openjpa.kernel;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Unit test of @link ResultShape
 * Martina Salvati
 */
@RunWith(Enclosed.class)
public class ResultShapeTest {

    @RunWith(Parameterized.class)
    public static class ResultShapeBasicTest<T> {
        private final Class<T> cls;
        private final Boolean primitive;
        private final FillStrategy<T> strategy;
        private final String alias;
        private final Object res;

        public ResultShapeBasicTest(Class<T> cls, Boolean primitive, FillStrategy<T> strategy, String alias, Object res) {
            this.cls = cls;
            this.primitive = primitive;
            this.strategy = strategy;
            this.alias = alias;
            this.res = res;
        }
        @Parameterized.Parameters(name = "{index}: {0}")
        public static Collection<Object[]> getTestParameters() {
            return Arrays.asList(new Object[][]{
                    {Object.class, true, new FillStrategy.NewInstance<>(Object.class),"alias", null},
                    {Foo2.class, true, null,"", null},
                    {Foo2.class, false, new FillStrategy.NewInstance<>(Foo2.class),null, null},
                    {null, null, null, "", NullPointerException.class},
                    //adequacy
                    {Object[].class,true,new FillStrategy.NewInstance(constructor(Foo2.class, short.class, Foo1.class)),"alias", IllegalArgumentException.class},
                    {Object[].class,false,new FillStrategy.NewInstance(constructor(Foo2.class, short.class, Foo1.class)),"", null}
            });
        }
        @Test
        public void testBasic1() {
            try {
                ResultShape<T> shape = new ResultShape<T>(cls);
                Assert.assertEquals(shape.getType(),cls);
                Assert.assertTrue(!shape.isPrimitive());
                if(alias!=null) {
                    shape.setAlias(alias);
                    Assert.assertEquals(shape.getAlias(),alias);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Assert.assertEquals(e.getClass(), res);
            }
        }

        @Test
        public void testBasic2() {
            try {
                ResultShape<T> shape = new ResultShape<>(cls, primitive);
                Assert.assertEquals(shape.getType(),cls);
                if(primitive)
                   Assert.assertTrue(shape.isPrimitive());
                if(!primitive)
                    Assert.assertTrue(shape.isCompound());
                if(alias!=null) {
                    shape.setAlias(alias);
                    Assert.assertEquals(shape.getAlias(),alias);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Assert.assertEquals(e.getClass(), res);
            }
        }

        @Test
        public void testBasic3() {
            try {
                ResultShape<T> shape = new ResultShape<>(cls, strategy, primitive);
                Assert.assertEquals(shape.getStrategy(),strategy);
                Assert.assertEquals(shape.getType(),cls);
                if(primitive)
                    Assert.assertTrue(shape.isPrimitive());
                if(!primitive)
                    Assert.assertTrue(shape.isCompound());
                if(alias!=null) {
                    shape.setAlias(alias);
                    Assert.assertEquals(shape.getAlias(),alias);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Assert.assertEquals(e.getClass(), res);
            }
        }

        @Test
        public void testBasic4() {
            try {
                ResultShape<T> shape = new ResultShape<>(cls, strategy);
            } catch (Exception e) {
                e.printStackTrace();
                Assert.assertEquals(e.getClass(), res);
            }
        }

    }

    @RunWith(Parameterized.class)
    public static class ResultShapeAddTest{

        final Class<?>[] classes;
        final boolean primitive;
        final Object res;


        public ResultShapeAddTest(Class<?>[] classes, boolean primitive, Object res) {
            this.classes = classes;
            this.primitive = primitive;
            this.res = res;
        }

        @Parameterized.Parameters
        public static Collection<Object[]> getTestParameters() {
            return Arrays.asList(new Object[][]{
                    {new Class[]{Foo2.class, short.class, Foo1.class}, false, null},
                    //Adequacy
                    {new Class[]{Foo2.class, short.class, Foo1.class}, true, UnsupportedOperationException.class}
            });
        }
        @Test
        public void addTest() {
            try {
                    ResultShape<Object> shape = new ResultShape<>(Object.class, primitive);
                    shape.add(classes);
            }catch(Exception e) {
                e.printStackTrace();
                Assert.assertEquals(e.getClass(),res);
            }
        }
    }

    @RunWith(Parameterized.class)
    public static class ResultShapeNestTest {

        public final ResultShape<?> parent_shape;
        public final ResultShape<?> child_shape;
        public final Object res;

        private static ResultShape<?>  myrecursiveshape = new ResultShape<>(Foo1.class,false);

        public ResultShapeNestTest(ResultShape<?> parent_shape, ResultShape<?> child_shape, Object res) {
            this.parent_shape = parent_shape;
            this.child_shape = child_shape;
            this.res = res;
        }

        @Parameterized.Parameters
        public static Collection<Object[]> getTestParameters() {
            return Arrays.asList(new Object[][]{
                    {new ResultShape<>(Object[].class,false), new ResultShape<>(Object.class,false),null},
                    {new ResultShape<>(Foo1[].class,false), new ResultShape<>(Foo1.class,false),null},
                    //adequacy
                    {new ResultShape<>(Object.class,true), new ResultShape<>(Object[].class,false),UnsupportedOperationException.class},// Can not add/nest shape to primitive shape Object
                    {myrecursiveshape, new ResultShape<Foo1>(Foo1.class, new FillStrategy.NewInstance(Foo1.class), false).nest(myrecursiveshape),IllegalArgumentException.class}, //  Foo1 can not nest recursive Foo1{Foo1}
            });
        }

        @Test
        public void testNest() {
            try{
                parent_shape.nest(child_shape);
                Assert.assertTrue(child_shape.isNested());
                Assert.assertTrue(parent_shape.isNesting());
                Assert.assertTrue(parent_shape.isCompound());
                Assert.assertTrue(!parent_shape.getChildren().isEmpty());
            }catch(Exception e) {
                e.printStackTrace();
                Assert.assertEquals(e.getClass(),res);
            }
        }
    }

    @RunWith(Parameterized.class)
    public static class ResultShapeParentTest {

        private static  ResultShape<Foo1> child =  new ResultShape<Foo1>(Foo1.class, true);
        private static  ResultShape<Foo1> parent =  new ResultShape<Foo1>(Foo1.class, false).nest(child);
        private static  ResultShape<Foo2> grandparent =  new ResultShape<Foo2>(Foo2.class, false);

        public final ResultShape<?> parent_shape;
        public final ResultShape<?> child_shape;
        public final Boolean res;

        public ResultShapeParentTest(ResultShape<?> parent_shape, ResultShape<?> child_shape, Boolean res) {
            this.parent_shape = parent_shape;
            this.child_shape = child_shape;
            this.res = res;
        }

        @Parameterized.Parameters
        public static Collection<Object[]> getTestParameters() {
            return Arrays.asList(new Object[][]{
                    {new ResultShape<>(Object[].class,false), new ResultShape<>(Object.class,true),false},
                    //adequacy
                    {new ResultShape<>(Object[].class,false).nest(child),child,true},
                    {grandparent.nest(parent),child,true} //grandparent
            });
        }

        @Test
        public void testParent() {
            Assert.assertEquals(parent_shape.isParent(child_shape),res);
        }
    }

    @RunWith(Parameterized.class)
    public static class ResultShapePackTest<T> {

        public final ResultShape<?> parent_shape;
        public final Object[] values;
        public final Class<?>[] types;
        public final String[] aliases;
        public final T res;

        public ResultShapePackTest(ResultShape<?> parent_shape, Object[] values, Class<?>[] types, String[] aliases, T res) {
            this.parent_shape = parent_shape;
            this.values = values;
            this.types = types;
            this.aliases = aliases;
            this.res = res;
        }
        @Parameterized.Parameters
        public static Collection<Object[]> getTestParameters() {
            return Arrays.asList(new Object[][]{
                    { new ResultShape<Foo2>(Foo2.class, new FillStrategy.NewInstance(Foo2.class),false), new Object[]{(short)2, "isw2"},new Class[]{short.class,String.class},new String[]{"aliases1","aliases2"} ,new Foo2()},
                     //adequacy
                    { new ResultShape<Foo2>(Foo2.class, new FillStrategy.NewInstance(Foo2.class),false),new Object[]{},new Class[]{short.class,String.class,Foo2.class},new String[]{"aliases1","aliases2"} ,IndexOutOfBoundsException.class},
                    { new ResultShape<Foo2>(Foo2.class, new FillStrategy.NewInstance(Foo2.class),false).add(short.class), new Object[]{(short)2, "isw2"},new Class[]{short.class,String.class},new String[]{"aliases1","aliases2"} ,new Foo2()},
            });
    }
        @Test
        public void testPack() {
            try {
                Assert.assertEquals(parent_shape.pack(values,types,aliases).getClass(),res.getClass());
            }catch(Exception e ) {
                e.printStackTrace();
                Assert.assertEquals(e.getClass(),res);
            }
        }
    }

    @RunWith(Parameterized.class)
    public static class ResultShapeGetTest {

        public final ResultShape<?> shape;
        public final List<Class<?>> result1; //with expanding children shape
        public final List<Class<?>> result2; //without expanding children shape

        public ResultShapeGetTest(ResultShape<?> shape, List<Class<?>> result1, List<Class<?>> result2) {
            this.shape = shape;
            this.result1 = result1;
            this.result2 = result2;
        }


        @Parameterized.Parameters
        public static Collection<Object[]> getTestParameters() {
            return Arrays.asList(new Object[][]{
                    {new ResultShape<>(Object.class,true), Arrays.asList(Object.class), Arrays.asList(Object.class)},
                    //adequacy
                    {new ResultShape<>(Object.class,false), Arrays.asList(Object.class), Arrays.asList(Object.class)},
                    {new ResultShape<>(Object.class,false).nest(new ResultShape<>(Foo1.class)).nest(new ResultShape<>(Foo2.class).nest(new ResultShape<>(Integer.class))), Arrays.asList(Foo1.class,Integer.class), Arrays.asList(Foo1.class,Foo2.class)}
            });
        }

        @Test
        public void testgetCompositeType() {
            try {
                Assert.assertEquals(shape.getCompositeTypes(), result1);
            }catch (Exception e ){
                e.printStackTrace();
            }
        }
        @Test
        public void testgetType() {
            try {
                Assert.assertEquals(shape.getTypes(), result2);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
    public static <T> Constructor<T> constructor(Class<T> t, Class<?>... args) {
        try {
            return t.getConstructor(args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static <T> Method method(Class<T> t, String name, Class<?>...args) {
        try {
            return t.getMethod(name, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
