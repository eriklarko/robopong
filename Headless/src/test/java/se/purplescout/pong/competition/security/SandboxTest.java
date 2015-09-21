package se.purplescout.pong.competition.security;

import org.apache.commons.io.IOUtils;
import org.apache.tools.ant.util.JavaEnvUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.security.AccessControlException;
import java.sql.Time;
import java.util.concurrent.TimeUnit;

public class SandboxTest extends SecurityTest {
    public static void main(String[] args) {
        System.out.println("Hejsan!!");
        System.exit(13337);
    }

    //@Ignore
    @Test
    public void testHuuuugeAmountsOfMemory() {
        //doSecurityTest("byte[] lol = new byte[Integer.MAX_VALUE];" +
        //        "byte[] lol2 = new byte[Integer.MAX_VALUE];" +
        //        "byte[] lol3 = new byte[Integer.MAX_VALUE];");
    }



    @Test
    public void testSockets()  {
        assertThrowables(doSecurityTest("try {" +
                "java.net.Socket socket = new java.net.Socket(\"localhost\", 8080);" +
                "java.io.BufferedOutputStream bufferedOutputStream = new java.io.BufferedOutputStream(socket.getOutputStream());" +
                "java.io.OutputStreamWriter outputStreamWriter = new java.io.OutputStreamWriter(bufferedOutputStream);" +
                "outputStreamWriter.write(\"foo\");" +
                "outputStreamWriter.flush();" +
                "} catch(java.io.IOException ex) {}"), AccessControlException.class);
    }

    @Test
    public void testSystemExit() {
        assertThrowables(doSecurityTest("System.exit(0);"), AccessControlException.class);
    }

    @Test
    public void testShellScripts() {
        assertThrowables(doSecurityTest("try {" +
                "   new ProcessBuilder(\"dir\").start().waitFor();" +
                "} catch (InterruptedException e) {" +
                "   e.printStackTrace();" +
                "} catch (java.io.IOException e) {" +
                "   e.printStackTrace();" +
                "}"), AccessControlException.class);
    }

    @Test
    public void testStartingThreads() {
        assertThrowables(doSecurityTest("new Thread(() -> { System.out.println(\"foo\"); }).start();"), AccessControlException.class);
    }

    @Test
    public void testThreadPool() {
        assertThrowables(doSecurityTest("new java.util.concurrent.ScheduledThreadPoolExecutor(1).execute(() -> System.out.println(\"foo\"));"), AccessControlException.class);
    }

    @Test
    public void testNativeCode() {

    }
}
