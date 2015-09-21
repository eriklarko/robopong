    package se.purplescout.pong.competition.jvm;

import org.apache.commons.io.IOUtils;
import org.apache.tools.ant.util.JavaEnvUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.util.concurrent.TimeUnit;

public class Jvm {

    private final Class<?> clazz;
    private final long millisToWait;
    private final NewJvmListener listener;
    private final String[] arguments;

    public Jvm(Class<?> clazz, long millisToWait, NewJvmListener listener, String... arguments) {
        this.clazz = clazz;
        this.millisToWait = millisToWait;
        this.listener = listener;
        this.arguments = arguments;
    }

    public void run() throws InterruptedException, IOException {
        String javaPath = JavaEnvUtils.getJreExecutable("java");
        String classpath = System.getProperty("java.class.path");

        String[] processArguments = {javaPath, "-cp", classpath, clazz.getName()}; // These are needed to run the specified class
        processArguments = combine(processArguments, arguments); // Add the arguments that the specified class should have

        ProcessBuilder processBuilder = new ProcessBuilder(processArguments);

        Process process = processBuilder.start();
        boolean processFinishedInTime = process.waitFor(millisToWait, TimeUnit.MILLISECONDS);
        if (processFinishedInTime) {
            StringWriter stdout = new StringWriter();
            StringWriter stderr = new StringWriter();

            IOUtils.copy(process.getInputStream(), stdout);
            IOUtils.copy(process.getErrorStream(), stderr);
            // This writer contains the error output stream as well. This means that any exceptions thrown will muddled in with the other output

            if (listener != null) {
                listener.acceptOutputFromOtherJvm(new ExecutionResult(stdout.toString(), stderr.toString(), process.exitValue()));
            }
        } else {
            process.destroyForcibly();
            if (listener != null) {
                listener.otherJvmTimedOut();
            }
        }
    }

        private String[] combine(String[] a, String[] b) {
            String[] c = new String[a.length + b.length];

            int i = 0;
            for (; i < a.length; i++) {
                c[i] = a[i];
            }
            for (int j = 0; j < b.length; j++) {
                c[i + j] = b[j];
            }
            return c;
        }
}
