package se.purplescout.pong.competition.compiler;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DynaCompTest {

    public static final String CLASS_NAME_PATTERN = "class\\s+([\\wåäöÅÄÖ]+)\\s+extends";

    public static Class<?> compile(String code, PrintWriter errorWriter) throws InvalidSourceStringException, ClassNotFoundException, InstantiationException, IllegalAccessException, JDKNotFoundException {
        // Full name of the class that will be compiled.
        // If class should be in some package,
        // fullName should contain it too
        // (ex. "testpackage.DynaClass")
        Pattern classPattern = Pattern.compile(CLASS_NAME_PATTERN);
        Matcher m = classPattern.matcher(code);
        Pattern packagePattern = Pattern.compile("package (.*?);");
        Matcher pm = packagePattern.matcher(code);

        String className = null;
        String packageName = null;
        if (m.find()) {
            String oldClassName = m.group(1);
            className = oldClassName + UUID.randomUUID().toString().replace("-", "");
            code = code.replace(oldClassName, className);

            if (pm.find()) {
                packageName = pm.group(1);
                //System.out.println("Class name: " + className + ", package name: " + packageName);
            } else {
                throw new InvalidSourceStringException("Package name not found");
            }
        } else {
            throw new InvalidSourceStringException("Class name not found");
        }
        String fullName = packageName + "." + className;

        // We get an instance of JavaCompiler. Then we create a file manager
        // (our custom implementation of it)
        // System.out.println("========================= " + System.getProperty("java.home"));
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        JavaFileManager fileManager = null;
        try {
            fileManager = new ClassFileManager(compiler.getStandardFileManager(null, null, null));
        } catch (NullPointerException ex) {
            throw new JDKNotFoundException();
        }

        code = clean(code); // Epic name

        // Dynamic compiling requires specifying
        // a list of "files" to compile. In our case
        // this is a list containing one "file" which is in our case
        // our own implementation (see details below)
        List<JavaFileObject> jfiles = new ArrayList<>();
        jfiles.add(new CharSequenceJavaFileObject(fullName, code));

        // We specify a task to the compiler. Compiler should use our file
        // manager and our list of "files".
        // Then we run the compilation with call()
        compiler.getTask(errorWriter, fileManager, null, null, null, jfiles).call();

        // Creating an instance of our compiled class and
        // running its toString() method

        Class<?> clazz = null;
        try {
            ClassLoader classLoader = fileManager.getClassLoader(null);
            clazz = classLoader.loadClass(fullName);
        } catch (NullPointerException ex) {
            // This happens if there is something wrong with the source code.
            // The errors are already logged in errorWriter.
            return null;
        }

        if (clazz == null) {
            throw new ClassNotFoundException("Could not find " + fullName);
        }

        return clazz;
    }

    /**
     * Removes illegal or dangerous code
     * @param code
     * @return
     */
    private static String clean(String code) {
        // TODO: reflection is not removed.. getClass().getDeclared... is still allowed as long as you do everything in one go.
        return code.replaceAll("System\\.exit", "")
                   .replaceAll("java\\.lang\\.reflect", "")
                   .replaceAll("java\\.net", "")
                   .replaceAll("java\\.nio", "");
    }
}
