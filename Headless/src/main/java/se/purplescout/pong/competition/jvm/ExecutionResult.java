package se.purplescout.pong.competition.jvm;

public class ExecutionResult {

    private final String stdout, stderr;
    private final int exitCode;

    public ExecutionResult(String stdout, String stderr, int exitCode) {
        this.stdout = stdout;
        this.stderr = stderr;
        this.exitCode = exitCode;
    }

    public String getStdout() {
        return stdout;
    }

    public String getStderr() {
        return stderr;
    }

    public int getExitCode() {
        return exitCode;
    }

    @Override
    public String toString() {
        return "ExecutionResult{" +
                "stdout='" + stdout + '\'' +
                ", stderr='" + stderr + '\'' +
                ", exitCode=" + exitCode +
                '}';
    }
}
