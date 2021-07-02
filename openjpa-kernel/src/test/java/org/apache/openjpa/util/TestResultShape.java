package org.apache.openjpa.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.openjpa.kernel.FillStrategy;
import org.apache.openjpa.kernel.ResultShape;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Enclosed.class)
public class TestResultShape {

	@RunWith(Parameterized.class)
    public static class TestPrimitiveShape {
		
		private ResultShape<Object> shape;

		   public TestPrimitiveShape(ResultShape<Object> shape) {
				super();
				this.shape = shape;
			}

		   @Parameterized.Parameters( name = "{index}: {0}" )
		   public static Collection<Object[]> getTestParameters() {
		 	return Arrays.asList(new Object[][] {  
				{new ResultShape<>(Object.class, true)},
				{new ResultShape<>(Foo2.class, true)}
		     }); 
		      }
		     //A primitive shape can not have child shapes e.g. Foo2 or float.
		     @Test(expected = UnsupportedOperationException.class)
		      public void testPrimitiveShapeChild() {
		        shape.add(Foo2.class);
		      }
		     //A primitive category shape is declared during construction and immutable. 
			  @Test
			  public void testCategoryOfPrimitiveShape() {
				assertTrue(shape.isPrimitive());
			  }

	}
	@RunWith(Parameterized.class)
    public static class TestCompoundShape {
		
		private ResultShape<Object> shape;

		   public TestCompoundShape(ResultShape<Object> shape) {
				super();
				this.shape = shape;
			}

		   @Parameterized.Parameters( name = "{index}: {0}" )
		   public static Collection<Object[]> getTestParameters() {
		 	return Arrays.asList(new Object[][] {  
				{new ResultShape<>(Object.class, false)} 
		     }); 
		      }
		   
		      @Test
			  public void testCategoryOfCompoundShape() {
				assertTrue(shape.isCompound());
			  }
		      @Test
		      public void testCompundChild() {
		    	  shape.add(Foo2.class);
		    	  shape.add(int.class, double.class);
			      assertTrue(!shape.isPrimitive());
			      assertTrue(shape.isCompound());
		   }
		      @Test
			  public void testMutableCategory() {
				assertTrue(shape.isCompound());
				shape.add(int.class, double.class);
			    ResultShape<Object[]> nonPrimitiveShape = new ResultShape<>(Object[].class);
			    shape.nest(nonPrimitiveShape);
				assertTrue(shape.isNesting());
				assertTrue(nonPrimitiveShape.isNested());

			  }
	}
	@RunWith(Parameterized.class)
    public static class TestNestingShape {
		   private ResultShape<Object> shape;
		   @Before 
		   public void SetUp() {
				assertTrue(shape.isCompound());
				shape.add(int.class, double.class);
			    ResultShape<Object[]> nonPrimitiveShape = new ResultShape<>(Object[].class);
			    shape.nest(nonPrimitiveShape);
				assertTrue(shape.isNesting());
		   }
		   public TestNestingShape(ResultShape<Object> shape) {
				super();
				this.shape = shape;
			}

		   @Parameterized.Parameters( name = "{index}: {0}" )
		   public static Collection<Object[]> getTestParameters() {
		 	return Arrays.asList(new Object[][] {  
				{new ResultShape<>(Object.class, false)},
				{new ResultShape<>(Foo2.class, false)} 
		     }); 
		   }
		   @Test
		   public void TestNestingIfShapesArePrimitive() {
		        ResultShape<Object[]> shape2 = new ResultShape<>(Object[].class);
		        shape2.add(int.class, double.class);
		        ResultShape<Object> primitiveShape = new ResultShape<>(Object.class, true);
		        shape2.nest(primitiveShape);
		        List<ResultShape<?>> list_ch = shape2.getChildren();
		        assertTrue(list_ch.get(0).isPrimitive());
		        assertTrue(list_ch.get(1).isPrimitive());
		        assertTrue(list_ch.get(2).isPrimitive());
		        assertFalse(shape2.isParent(list_ch.get(0)));
		        assertFalse(shape2.isParent(list_ch.get(1)));
		        assertTrue(shape2.isParent(list_ch.get(2))); 
			    assertTrue(shape2.isCompound());
			    assertFalse(shape2.isNesting());
		   }
		   //Notice that all nested shapes are compound shapes but not all compound shapes are nesting.
		   @Test
		   public void TestCompundShapesNesting() {
		        ResultShape<Object[]> shape2 = new ResultShape<>(Object[].class);
		        shape2.add(int.class, double.class);
		        ResultShape<Object> primitiveShape = new ResultShape<>(Object.class, true);
		        shape2.nest(primitiveShape);
			    assertTrue(shape2.isCompound()); //compund shapes
			    assertTrue(primitiveShape.isNested());
			    assertFalse(shape2.isNesting());		//case 1:  compound shapes are not nesting
			    
			    ResultShape<Object[]> nonPrimitiveShape = new ResultShape<>(Object[].class);
		        nonPrimitiveShape.add(int.class, double.class);
		        shape.nest(nonPrimitiveShape);
		        assertTrue(nonPrimitiveShape.isCompound()); //nested shapes are compound
		        assertTrue(nonPrimitiveShape.isNested());
    
			    shape.add(int.class,double.class);     //case 2 : compound shapes are nesting
			    assertTrue(shape.isCompound());
			    assertTrue(shape.isNesting());
		   
		   }
		   
	}
	 public static class OtherTests {
	  @Test
	    public void testRecursiveNestingIsNotAllowed() {
	        ResultShape<Object[]> root = new ResultShape<>(Object[].class);
	        ResultShape<Foo1> bar1 = new ResultShape<Foo1>(Foo1.class, new FillStrategy.NewInstance(Foo1.class), false);
	        bar1.add(int.class);
	        ResultShape<Foo2> fooBarConstructor = new ResultShape<Foo2>(Foo2.class,
	                new FillStrategy.NewInstance(constructor(Foo2.class, short.class, Foo1.class)));	  
	        fooBarConstructor.add(short.class);
	        fooBarConstructor.nest(bar1);
	        root.add(Foo2.class, Object.class);
	        root.nest(fooBarConstructor);
	        ResultShape<Foo1> bar2 = new ResultShape<Foo1>(Foo1.class, new FillStrategy.NewInstance(Foo1.class), false);
	        root.nest(bar2);
	        try {
	            bar1.nest(fooBarConstructor);
	            fail();
	        } catch (IllegalArgumentException e) {
	        }
	    }
	  @Test
	  public void TestGetTypes() {
	      ResultShape<Object[]> shape = new ResultShape<>(Object[].class);
		  shape.add(int.class, double.class);
		  List<Class<?>> types = shape.getTypes();
    	  List<Class<?>> added = new ArrayList<>();
    	  added.add(int.class);
    	  added.add(double.class);
    	  assertEquals(types,added);
	  }
	  @Test 
	  public void TestGetCompositeTypes() {
		    ResultShape<Object[]> root  = new ResultShape<>(Object[].class);
	        FillStrategy<Foo1> strategy1 = new FillStrategy.NewInstance<>(Foo1.class);
	        ResultShape<Foo1> foo1 = new ResultShape<>(Foo1.class, strategy1, false);
	        foo1.add(int.class);
	        FillStrategy<Foo2> strategy2 = new FillStrategy.NewInstance<>(constructor(Foo2.class, short.class, Foo1.class));
	        ResultShape<Foo2> fooBarConstructor = new ResultShape<>(Foo2.class, strategy2);
	        fooBarConstructor.add(short.class);
	        fooBarConstructor.nest(foo1);
	        root.add(Foo2.class, Object.class);
	        root.nest(fooBarConstructor);
	        ResultShape<Foo1> foo2 = new ResultShape<Foo1>(Foo1.class, new FillStrategy.NewInstance(Foo1.class), false);
	        root.nest(foo2);
	        assertEquals(Arrays.asList(Foo2.class, Object.class, short.class, int.class, Foo1.class),
	                root.getCompositeTypes());
	        assertEquals(Arrays.asList(Foo2.class, Object.class, Foo2.class, Foo1.class), root.getTypes());
	        assertEquals(5, root.argLength());
	        assertEquals(4, root.length());  
	  }
//	  The purpose of a shape is to populate an instance of T from an array of input values where each array element is further specified with a type and an alias. 
//	  FillStrategy determines how a shape populates an instance of T by consuming the input array element values. The input data is presented as an Object[] with a parallel array of types because the primitive types (short, float etc.) are not preserved in the input array. 
//	  For certain FillStrategy such as MAP or BEAN, the alias of the input array element are used to identify the Map key or setter methods respectively.
	 
	  @Test
	  public void TestPurposeShape() {
	        ResultShape<Foo2> foo = new ResultShape<Foo2>(Foo2.class, new FillStrategy.NewInstance(Foo2.class), false);
	        //array of input values 
	        foo.add(short.class,String.class);
		    Object[] values = {(short)2007, "isw2"};
	        Class[]  types  = {short.class,String.class};
	        String[]  aliases  = {"aliases1","aliases2"};
	        Foo2 result = foo.pack(values, types, aliases);
	        assertEquals(2007, result.shrt);
	        assertEquals("isw2",result.string);
	  }


	  <T> Constructor<T> constructor(Class<T> t, Class<?>...args) {
	        try {
	            return t.getConstructor(args);
	        } catch (Exception e) {
	            throw new RuntimeException(e);
	        }
	    }
	    <T> Method method(Class<T> t, String name, Class<?>...args) {
	        try {
	            return t.getMethod(name, args);
	        } catch (Exception e) {
	            throw new RuntimeException(e);
	        }
	    }
	 }
}


