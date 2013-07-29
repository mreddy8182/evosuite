package org.evosuite.utils;

import static org.junit.Assert.*;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

import org.junit.Assert;
import org.junit.Test;

import com.googlecode.gentyref.TypeToken;

public class TestGenericClass {

	@Test
	public void testWildcardClassloader() {
		GenericClass clazz = new GenericClass(Class.class).getWithWildcardTypes();
		assertEquals("java.lang.Class<?>", clazz.getTypeName());
		clazz.changeClassLoader(TestGenericClass.class.getClassLoader());
		assertEquals("java.lang.Class<?>", clazz.getTypeName());
	}
	
	@Test
	public void testAssignablePrimitives() {
		GenericClass clazz1 = new GenericClass(int.class);
		GenericClass clazz2 = new GenericClass(int.class);
		Assert.assertTrue(clazz1.isAssignableTo(clazz2));
		Assert.assertTrue(clazz1.isAssignableFrom(clazz2));
	}
	
	@Test
	public void testAssignableObject() {
		GenericClass clazz1 = new GenericClass(Object.class);
		GenericClass clazz2 = new GenericClass(Object.class);
		Assert.assertTrue(clazz1.isAssignableTo(clazz2));
	}
	
	@Test
	public void testAssignableIntegerObject() {
		GenericClass clazz1 = new GenericClass(Integer.class);
		GenericClass clazz2 = new GenericClass(Object.class);
		Assert.assertTrue(clazz1.isAssignableTo(clazz2));
		Assert.assertFalse(clazz1.isAssignableFrom(clazz2));
	}
	
	@Test
	public void testAssignableIntegerNumber() {
		GenericClass clazz1 = new GenericClass(Integer.class);
		GenericClass clazz2 = new GenericClass(Number.class);
		Assert.assertTrue(clazz1.isAssignableTo(clazz2));
		Assert.assertFalse(clazz1.isAssignableFrom(clazz2));
	}

	@Test
	public void testAssignableIntInteger() {
		GenericClass clazz1 = new GenericClass(Integer.class);
		GenericClass clazz2 = new GenericClass(int.class);
		Assert.assertTrue(clazz1.isAssignableTo(clazz2));
		Assert.assertTrue(clazz1.isAssignableFrom(clazz2));
	}

	@Test
	public void testAssignableClass() {
		GenericClass clazzTypeVar = new GenericClass(Class.class);
		GenericClass clazzWildcard = clazzTypeVar.getWithWildcardTypes();
		
		ParameterizedType type = new ParameterizedTypeImpl(Class.class, new Type[] {Integer.class}, null);
		GenericClass clazzConcrete = new GenericClass(type);
		
		Assert.assertFalse(clazzWildcard.isAssignableTo(clazzConcrete));
		Assert.assertFalse(clazzWildcard.isAssignableTo(clazzTypeVar));
		Assert.assertTrue(clazzWildcard.isAssignableTo(clazzWildcard));
		
		Assert.assertFalse(clazzTypeVar.isAssignableTo(clazzConcrete));
		Assert.assertTrue(clazzTypeVar.isAssignableTo(clazzTypeVar));
		Assert.assertTrue(clazzTypeVar.isAssignableTo(clazzWildcard));

		Assert.assertTrue(clazzConcrete.isAssignableTo(clazzConcrete));
		Assert.assertFalse(clazzConcrete.isAssignableTo(clazzTypeVar));
		Assert.assertTrue(clazzConcrete.isAssignableTo(clazzWildcard));
	}


	private static class A {
	}

	@SuppressWarnings({ "rawtypes", "unused", "unchecked" })
	@Test
	public void test01() throws Throwable {

		/*
		 * This test case come from compilation issue found during SBST'13 competition:
		 * 
		 * String string0 = vM0.run(vM0.stack);
		 * 
		 * SUT at:  http://www.massapi.com/source/jabref-2.6/src/java/net/sf/jabref/bst/VM.java.html
		 * 
		 * Snippet of interest:
		 * 
		 * 1) Stack<Object> stack = new Stack<Object>();
		 * 2)  public String run(Collection<BibtexEntry> bibtex) {
		 */

		Collection<?> col0 = new Stack<Object>();
		Collection<A> col1 = new Stack();
		Collection col2 = new Stack();
		Collection col3 = new Stack<Object>();

		/*
		 *  following does not compile
		 *  
		 *  Collection<A> col = new Stack<Object>();
		 *  
		 *  but it can be generated by EvoSuite
		 */

		GenericClass stack = new GenericClass(Stack.class).getWithWildcardTypes();
		GenericClass collection = new GenericClass(Collection.class).getWithWildcardTypes();
		Assert.assertTrue(stack.isAssignableTo(collection));

		GenericClass objectStack = new GenericClass(col0.getClass());
		Assert.assertTrue(objectStack.isAssignableTo(collection));

		Type typeColA = new TypeToken<Collection<A>>() {
		}.getType();
		Type typeStack = new TypeToken<Stack>() {
		}.getType();
		Type typeObjectStack = new TypeToken<Stack<Object>>() {
		}.getType();

		GenericClass classColA = new GenericClass(typeColA);
		GenericClass classStack = new GenericClass(typeStack).getWithWildcardTypes();
		GenericClass classObjectStack = new GenericClass(typeObjectStack);

		Assert.assertFalse(classStack.isAssignableTo(classColA));
		Assert.assertFalse(classObjectStack.isAssignableTo(classColA));
		Assert.assertFalse(classColA.isAssignableFrom(classObjectStack));
	}

	@Test
	public void test1() {
		Type listOfString = new TypeToken<List<String>>() {
		}.getType();
		Type listOfInteger = new TypeToken<List<Integer>>() {
		}.getType();

		GenericClass listOfStringClass = new GenericClass(listOfString);
		GenericClass listOfIntegerClass = new GenericClass(listOfInteger);

		Assert.assertFalse(listOfStringClass.isAssignableFrom(listOfIntegerClass));
		Assert.assertFalse(listOfStringClass.isAssignableTo(listOfIntegerClass));
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void test2() {
		Type listOfString = new TypeToken<List<String>>() {
		}.getType();
		Type plainList = new TypeToken<List>() {
		}.getType();
		Type objectList = new TypeToken<List<Object>>() {
		}.getType();

		GenericClass listOfStringClass = new GenericClass(listOfString);
		GenericClass plainListClass = new GenericClass(plainList).getWithWildcardTypes();
		GenericClass objectListClass = new GenericClass(objectList);

		/*
		 * Note:
		 * 
		 * 		List<String> l = new LinkedList<Object>();
		 * 
		 *  does not compile
		 */

		Assert.assertFalse(listOfStringClass.isAssignableTo(objectListClass));

		Assert.assertFalse(listOfStringClass.isAssignableFrom(plainListClass));
		Assert.assertTrue(listOfStringClass.isAssignableTo(plainListClass));
	}

	@Test
	public void test3() {
		Type listOfInteger = new TypeToken<List<Integer>>() {
		}.getType();
		Type listOfSerializable = new TypeToken<List<Serializable>>() {
		}.getType();

		GenericClass listOfIntegerClass = new GenericClass(listOfInteger);
		GenericClass listOfSerializableClass = new GenericClass(listOfSerializable);

		Assert.assertFalse(listOfIntegerClass.isAssignableFrom(listOfSerializableClass));
		Assert.assertFalse(listOfSerializableClass.isAssignableFrom(listOfIntegerClass));

		Assert.assertTrue(listOfIntegerClass.isAssignableFrom(listOfIntegerClass));
		Assert.assertTrue(listOfSerializableClass.isAssignableFrom(listOfSerializableClass));
	}
	

	private class NumberBoundary<T extends Number> {}
	private class ComparableBoundary<T extends Comparable<T>> {}
	private class RefinedComparableBoundary<T extends java.util.Date> extends ComparableBoundary<java.util.Date> {}


	@Test
	public void testTypeVariableBoundariesNumber() {
		TypeVariable<?> numberTypeVariable = NumberBoundary.class.getTypeParameters()[0];
		
		GenericClass listOfIntegerClass = new GenericClass(Integer.class);
		GenericClass listOfSerializableClass = new GenericClass(Serializable.class);

		Assert.assertTrue(listOfIntegerClass.satisfiesBoundaries(numberTypeVariable));
		Assert.assertFalse(listOfSerializableClass.satisfiesBoundaries(numberTypeVariable));
	}
	
	@Test
	public void testTypeVariableBoundariesComparable() {
		TypeVariable<?> comparableTypeVariable = ComparableBoundary.class.getTypeParameters()[0];
		
		GenericClass listOfIntegerClass = new GenericClass(Integer.class);
		GenericClass listOfSerializableClass = new GenericClass(Serializable.class);

		Assert.assertTrue(listOfIntegerClass.satisfiesBoundaries(comparableTypeVariable));
		Assert.assertFalse(listOfSerializableClass.satisfiesBoundaries(comparableTypeVariable));
	}

	@Test
	public void testTypeVariableBoundariesRefined() {
		TypeVariable<?> dateTypeVariable = RefinedComparableBoundary.class.getTypeParameters()[0];
		TypeVariable<?> comparableTypeVariable = ComparableBoundary.class.getTypeParameters()[0];

		GenericClass listOfIntegerClass = new GenericClass(Integer.class);
		GenericClass listOfComparableClass = new GenericClass(Comparable.class);
		GenericClass listOfDateClass = new GenericClass(java.util.Date.class);
		GenericClass listOfSqlDateClass = new GenericClass(java.sql.Date.class);

		Assert.assertFalse(listOfIntegerClass.satisfiesBoundaries(dateTypeVariable));
		Assert.assertFalse(listOfComparableClass.satisfiesBoundaries(dateTypeVariable));
		Assert.assertTrue(listOfDateClass.satisfiesBoundaries(dateTypeVariable));
		Assert.assertTrue(listOfSqlDateClass.satisfiesBoundaries(dateTypeVariable));

		Assert.assertTrue(listOfIntegerClass.satisfiesBoundaries(comparableTypeVariable));
//		Assert.assertTrue(listOfComparableClass.satisfiesBoundaries(comparableTypeVariable));
		Assert.assertTrue(listOfDateClass.satisfiesBoundaries(comparableTypeVariable));
		// Assert.assertTrue(listOfSqlDateClass.satisfiesBoundaries(comparableTypeVariable));
	}
	
	@Test
	public void testWildcardObjectBoundaries() {
		
		WildcardType objectType = new WildcardTypeImpl(new Type[] { Object.class }, new Type[] {});
		
		GenericClass integerClass = new GenericClass(Integer.class);
		GenericClass comparableClass = new GenericClass(Comparable.class);
		GenericClass dateClass = new GenericClass(java.util.Date.class);
		GenericClass sqlDateClass = new GenericClass(java.sql.Date.class);

		Assert.assertTrue(integerClass.satisfiesBoundaries(objectType));
		Assert.assertTrue(comparableClass.satisfiesBoundaries(objectType));
		Assert.assertTrue(dateClass.satisfiesBoundaries(objectType));
		Assert.assertTrue(sqlDateClass.satisfiesBoundaries(objectType));
	}
	
	@Test
	public void testWildcardNumberBoundaries() {
		
		WildcardType objectType = new WildcardTypeImpl(new Type[] { Number.class }, new Type[] {});
		
		GenericClass integerClass = new GenericClass(Integer.class);
		GenericClass comparableClass = new GenericClass(Comparable.class);
		GenericClass dateClass = new GenericClass(java.util.Date.class);
		GenericClass sqlDateClass = new GenericClass(java.sql.Date.class);

		Assert.assertTrue(integerClass.satisfiesBoundaries(objectType));
		Assert.assertFalse(comparableClass.satisfiesBoundaries(objectType));
		Assert.assertFalse(dateClass.satisfiesBoundaries(objectType));
		Assert.assertFalse(sqlDateClass.satisfiesBoundaries(objectType));
	}
	
	@Test
	public void testWildcardIntegerBoundaries() {
		
		WildcardType objectType = new WildcardTypeImpl(new Type[] { Integer.class }, new Type[] {});
		
		GenericClass integerClass = new GenericClass(Integer.class);
		GenericClass comparableClass = new GenericClass(Comparable.class);
		GenericClass dateClass = new GenericClass(java.util.Date.class);
		GenericClass sqlDateClass = new GenericClass(java.sql.Date.class);

		Assert.assertTrue(integerClass.satisfiesBoundaries(objectType));
		Assert.assertFalse(comparableClass.satisfiesBoundaries(objectType));
		Assert.assertFalse(dateClass.satisfiesBoundaries(objectType));
		Assert.assertFalse(sqlDateClass.satisfiesBoundaries(objectType));
	}
	
	@Test
	public void testWildcardComparableBoundaries() {
		
		WildcardType objectType = new WildcardTypeImpl(new Type[] { Comparable.class }, new Type[] {});
		
		GenericClass integerClass = new GenericClass(Integer.class);
		GenericClass comparableClass = new GenericClass(Comparable.class);
		GenericClass dateClass = new GenericClass(java.util.Date.class);
		GenericClass sqlDateClass = new GenericClass(java.sql.Date.class);

		Assert.assertTrue(integerClass.satisfiesBoundaries(objectType));
		Assert.assertTrue(comparableClass.satisfiesBoundaries(objectType));
		Assert.assertTrue(dateClass.satisfiesBoundaries(objectType));
		Assert.assertTrue(sqlDateClass.satisfiesBoundaries(objectType));
	}
	
	@Test
	public void testWildcardDateBoundaries() {
		
		WildcardType objectType = new WildcardTypeImpl(new Type[] { java.util.Date.class }, new Type[] {});
		
		GenericClass integerClass = new GenericClass(Integer.class);
		GenericClass comparableClass = new GenericClass(Comparable.class);
		GenericClass dateClass = new GenericClass(java.util.Date.class);
		GenericClass sqlDateClass = new GenericClass(java.sql.Date.class);

		Assert.assertFalse(integerClass.satisfiesBoundaries(objectType));
		Assert.assertFalse(comparableClass.satisfiesBoundaries(objectType));
		Assert.assertTrue(dateClass.satisfiesBoundaries(objectType));
		Assert.assertTrue(sqlDateClass.satisfiesBoundaries(objectType));
	}
	
	@Test
	public void testWildcardSqlDateBoundaries() {
		
		WildcardType objectType = new WildcardTypeImpl(new Type[] { java.sql.Date.class }, new Type[] {});
		
		GenericClass integerClass = new GenericClass(Integer.class);
		GenericClass comparableClass = new GenericClass(Comparable.class);
		GenericClass dateClass = new GenericClass(java.util.Date.class);
		GenericClass sqlDateClass = new GenericClass(java.sql.Date.class);

		Assert.assertFalse(integerClass.satisfiesBoundaries(objectType));
		Assert.assertFalse(comparableClass.satisfiesBoundaries(objectType));
		Assert.assertFalse(dateClass.satisfiesBoundaries(objectType));
		Assert.assertTrue(sqlDateClass.satisfiesBoundaries(objectType));
	}
	
	@Test
	public void testWildcardDateSuperBoundaries() {
		
		WildcardType objectType = new WildcardTypeImpl(new Type[] { Object.class }, new Type[] {java.util.Date.class });
		
		GenericClass integerClass = new GenericClass(Integer.class);
		GenericClass comparableClass = new GenericClass(Comparable.class);
		GenericClass dateClass = new GenericClass(java.util.Date.class);
		GenericClass sqlDateClass = new GenericClass(java.sql.Date.class);

		Assert.assertFalse(integerClass.satisfiesBoundaries(objectType));
		Assert.assertFalse(comparableClass.satisfiesBoundaries(objectType));
		Assert.assertTrue(dateClass.satisfiesBoundaries(objectType));
		Assert.assertTrue(sqlDateClass.satisfiesBoundaries(objectType));
	}
	
	@Test
	public void testWildcardDateBothBoundaries() {
		
		WildcardType objectType = new WildcardTypeImpl(new Type[] { java.util.Date.class }, new Type[] {java.util.Date.class });
		
		GenericClass integerClass = new GenericClass(Integer.class);
		GenericClass comparableClass = new GenericClass(Comparable.class);
		GenericClass dateClass = new GenericClass(java.util.Date.class);
		GenericClass sqlDateClass = new GenericClass(java.sql.Date.class);

		Assert.assertFalse(integerClass.satisfiesBoundaries(objectType));
		Assert.assertFalse(comparableClass.satisfiesBoundaries(objectType));
		Assert.assertTrue(dateClass.satisfiesBoundaries(objectType));
		Assert.assertTrue(sqlDateClass.satisfiesBoundaries(objectType));
	}
	
	@Test
	public void testWildcardDateBothBoundaries2() {
		
		WildcardType objectType = new WildcardTypeImpl(new Type[] { Comparable.class }, new Type[] {java.util.Date.class });
		
		GenericClass integerClass = new GenericClass(Integer.class);
		GenericClass comparableClass = new GenericClass(Comparable.class);
		GenericClass dateClass = new GenericClass(java.util.Date.class);
		GenericClass sqlDateClass = new GenericClass(java.sql.Date.class);

		Assert.assertFalse(integerClass.satisfiesBoundaries(objectType));
		Assert.assertFalse(comparableClass.satisfiesBoundaries(objectType));
		Assert.assertTrue(dateClass.satisfiesBoundaries(objectType));
		Assert.assertTrue(sqlDateClass.satisfiesBoundaries(objectType));
	}
	
	@Test
	public void testWildcardInvalidBoundaries() {
		
		WildcardType objectType = new WildcardTypeImpl(new Type[] { Number.class }, new Type[] {java.util.Date.class });
		
		GenericClass integerClass = new GenericClass(Integer.class);
		GenericClass comparableClass = new GenericClass(Comparable.class);
		GenericClass dateClass = new GenericClass(java.util.Date.class);
		GenericClass sqlDateClass = new GenericClass(java.sql.Date.class);

		Assert.assertFalse(integerClass.satisfiesBoundaries(objectType));
		Assert.assertFalse(comparableClass.satisfiesBoundaries(objectType));
		Assert.assertFalse(dateClass.satisfiesBoundaries(objectType));
		Assert.assertFalse(sqlDateClass.satisfiesBoundaries(objectType));
	}
	
}
