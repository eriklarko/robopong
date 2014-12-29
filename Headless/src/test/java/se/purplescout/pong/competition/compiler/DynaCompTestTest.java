package se.purplescout.pong.competition.compiler;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Objects;
import org.junit.Ignore;

@Ignore
public class DynaCompTestTest {

    @Test
    public void compileMinimalClass() throws Exception {
        String code = "package foo;" +
                      "public class Bar extends Object {}";
        String output = compileAndGetOutput(code);
        Assert.assertEquals("", output);
    }

    private String compileAndGetOutput(String code) throws ClassNotFoundException, JDKNotFoundException, InstantiationException, InvalidSourceStringException, IllegalAccessException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos);
        DynaCompTest.compile(code, pw);
        return new String(baos.toByteArray());
    }

    @Test
    public void compileClassWithoutPackage() throws Exception {
        String code = "public class Bar extends Object {}";
        try {
            compileAndGetOutput(code);
            Assert.fail("Expected exception not thrown");
        } catch (InvalidSourceStringException ex) {
            Assert.assertTrue("Exception message did not contain information about the error", ex.getMessage().toLowerCase().contains("package"));
        }
    }

    /*@Test(expected = InstantiationException.class)
    public void compileClassWithoutEmptyOrDefaultConstructor() throws Exception {
        String code = "package foo;" +
                "public class Bar extends Object {" +
                "   public Bar(String s) {}" +
                "}";
        compileAndGetOutput(code);
    }

    @Test(expected = IllegalAccessException.class)
    public void compileClassWithNonPublicEmptyConstructor() throws Exception {
        String code = "package foo;" +
                "public class Bar extends Object {" +
                "   Bar() {}" +
                "}";
        compileAndGetOutput(code);
    }*/

    @Test(expected = IllegalAccessException.class)
    public void compileNonPublicClass() throws Exception {
        String code = "package foo;" +
                "class Bar extends Object {" +
                "   public Bar() {}" +
                "}";
        compileAndGetOutput(code);
    }

    @Test
    public void compileClassWithMissingBracket() throws Exception {
        String code = "package foo;" +
                "public class Bar extends Object {" +
                "   public Bar() }" +
                "}";
        String output = compileAndGetOutput(code);
        Assert.assertTrue(output.contains("java:1: error: ';' expected"));
    }

    @Test
    public void compileClassWithComments() throws Exception {
        String code = "package foo;" +
                "public class Bar extends Object {" +
                "   // HERE BE COMMENTS\n" +
                "}";
        String output = compileAndGetOutput(code);
        Assert.assertEquals("", output);
    }

    @Test
    public void compileChangedClass() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos);

        Class<?> firstObjClass = DynaCompTest.compile("package foo;" +
                "public class Bar extends Object {" +
                "    public String toString() {" +
                "        return \"a\";" +
                "    }" +
                "}", pw);
        Class<?> secondObjClass = DynaCompTest.compile("package foo;" +
                "public class Bar extends Object {" +
                "    public String toString() {" +
                "        return \"b\";" +
                "    }" +
                "}", pw);

        Object firstObj = firstObjClass.newInstance();
        Object secondObj = secondObjClass.newInstance();

        Assert.assertFalse(Objects.equals(firstObj.toString(), secondObj.toString()));
    }

    @Test(expected = JDKNotFoundException.class)
    public void compileWithoutJdk() throws Exception {
        String oldHome = System.getProperty("java.home");
        System.clearProperty("java.home");
        compileAndGetOutput("package foo;" +
                "public class Bar extends Object {}");

        System.setProperty("java.home", oldHome);
    }

    @Test
    public void compileWithUnderlineInClassName() throws ClassNotFoundException, JDKNotFoundException, InstantiationException, InvalidSourceStringException, IllegalAccessException {
        String code = "package foo;" +
                "public class Bar_ extends Object {}";
        String output = compileAndGetOutput(code);
        Assert.assertEquals("", output);
    }

    @Test
    public void compileWithÅÄÖInClassName() throws ClassNotFoundException, JDKNotFoundException, InstantiationException, InvalidSourceStringException, IllegalAccessException {
        String code = "package foo;" +
                "public class FooÅÄÖåäöBar extends Object {}";
        String output = compileAndGetOutput(code);
        Assert.assertEquals("", output);
    }
}