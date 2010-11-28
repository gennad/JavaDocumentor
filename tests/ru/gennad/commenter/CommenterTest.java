package ru.gennad.commenter;

import java.util.HashMap;

import junit.framework.TestCase;

import ru.gennad.commenter.Commenter;





public class CommenterTest extends TestCase{
	
	public void testArgumentParser() {
		HashMap<String, String> res = Commenter.argumentParser("public void main(String a, int b)");
		assertNotNull(res);
		assertTrue(res.containsKey("a")) ;
		assertTrue(res.containsValue("String")) ;
		
		HashMap<String, String> res1 = Commenter.argumentParser("public void main()");
		//assertNull(res1);
		assertEquals("{}", res1.toString());
		
		
		
		HashMap<String, String> res2 = Commenter.argumentParser("public void main(String a, int b, char c, Boolean d)");
		assertNotNull(res2);
		assertTrue(res2.containsKey("a")) ;
		assertTrue(res2.containsValue("String")) ;
		
		assertTrue(res2.containsKey("b")) ;
		assertTrue(res2.containsValue("int")) ;
		
		assertTrue(res2.containsKey("c")) ;
		assertTrue(res2.containsValue("char")) ;
		
		assertTrue(res2.containsKey("d")) ;
		assertTrue(res2.containsValue("Boolean")) ;
		
		HashMap<String, String> res3 = Commenter.argumentParser("public void main(String s)");
		assertNotNull(res3);
		assertTrue(res3.containsKey("s")) ;
		assertTrue(res3.containsValue("String")) ;
	}
	
	public void testGetReturnType() {
		String param = "public static void main();";
		String res = Commenter.getReturnType(param);
		assertEquals("void", res);
		
		param = "Integer main();";
		res = Commenter.getReturnType(param);
		assertEquals("Integer", res);
		
		param = "public Boolean main();";
		res = Commenter.getReturnType(param);
		assertEquals("Boolean", res);
		
		param = "public Boolean main() throws Exception";
		res = Commenter.getReturnType(param);
		assertEquals("Boolean", res);
	}
	
	public void testGetAllThrows() {
		String param = "public static void main() throws OneException";
		String[] res = Commenter.getAllThrows(param);
		assertNotNull(res);	
		assertEquals("OneException", res[0]);
		
		param = "public static void main() throws OneException, TwoException";
		res = Commenter.getAllThrows(param);
		assertNotNull(res);		
		assertEquals("OneException", res[0]);
		assertEquals("TwoException", res[1]);
		
		param = "public static void main() throws OneException, TwoException, ThreeException";
		res = Commenter.getAllThrows(param);
		assertNotNull(res);		
		assertEquals("OneException", res[0]);
		assertEquals("TwoException", res[1]);
		assertEquals("ThreeException", res[2]);
		
		param = "public static void main()";
		res = Commenter.getAllThrows(param);
		assertNotNull(res);
		assertEquals(0, res.length);
	}
	
	public void testGetFirstSpaces() {
		String param = "     public static void main()";
		String res = Commenter.getFirstSpaces(param);
		assertEquals("     ", res);
	}
	
	public void isLineVariable() {
		String param = "public String a";
		boolean res = Commenter.isLineVariable(param);
		assertTrue(res);
		
		param = "String a";
		res = Commenter.isLineVariable(param);
		assertTrue(res);
		
		param = "String s;";
		res = Commenter.isLineVariable(param);
		assertTrue(res);
		
		param = "String a = null";
		res = Commenter.isLineVariable(param);
		assertTrue(res);
		
		param = "String a = new Int()";
		res = Commenter.isLineVariable(param);
		assertTrue(res);
		
		param = "String a = new Int[]";
		res = Commenter.isLineVariable(param);
		assertTrue(res);				
		
		param = "class Abc";
		res = Commenter.isLineVariable(param);
		assertFalse(res);
		
		param = "import abc.abc";
		res = Commenter.isLineVariable(param);
		assertFalse(res);
	}
	
	
	
}
