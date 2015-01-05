package se.purplescout.pong.competition.security;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.security.AccessControlException;
import java.security.Policy;

public class FileSystemAccessTest extends SecurityTest {

    @Test
    public void testReadProtectedFile() {
        java.io.File f = new java.io.File("/etc/shadow");
        if (!f.exists()) {
            Assert.fail("File to read doesn't exist");
        }
        String violation = "try {\n" +
                "            java.io.File f = new java.io.File(\"/etc/shadow\");\n" +
                "            java.util.List<String> lines = java.nio.file.Files.lines(f.toPath()).collect(java.util.stream.Collectors.toList());\n" +
                "            System.out.println(lines);\n" +
                "        } catch (Exception e) {\n" +
                "            throw new RuntimeException(e);\n" +
                "        }";

        assertThrowables(doSecurityTest(violation), AccessControlException.class);
    }

    @Test
    public void testCreateFile() {
        java.io.File f = new java.io.File("hej");
        if (f.exists() && !f.delete()) {
            Assert.fail("File to create already exists and cannot be removed");
        }
        String violation = "try {\n" +
                "            java.io.File f = new java.io.File(\"hej\");\n" +
                "            f.createNewFile();" +
                "        } catch (Exception e) {\n" +
                "            throw new RuntimeException(e);\n" +
                "        }";

        assertThrowables(doSecurityTest(violation), AccessControlException.class);
    }

    @Test
    public void testCreateTempFile() {
        String violation = "try {\n" +
                "            java.nio.file.Files.createTempFile(\"foo\", \"bar\");" +
                "        } catch (Exception e) {\n" +
                "            throw new RuntimeException(e);\n" +
                "        }";

        assertThrowables(doSecurityTest(violation), SecurityException.class);
    }

    @Test
    public void testReadExistingFile() throws IOException {
        new File("foo").createNewFile();
        String violation = "try {\n" +
                "            java.io.File f = new java.io.File(\"foo\");\n" +
                "            java.util.List<String> lines = java.nio.file.Files.lines(f.toPath()).collect(java.util.stream.Collectors.toList());\n" +
                "            System.out.println(lines);\n" +
                "        } catch (Exception e) {\n" +
                "            throw new RuntimeException(e);\n" +
                "        }";

        assertThrowables(doSecurityTest(violation), AccessControlException.class);
    }
}
