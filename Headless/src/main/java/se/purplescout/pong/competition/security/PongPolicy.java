package se.purplescout.pong.competition.security;

import se.purplescout.pong.competition.compiler.DynaCompTest;

import java.io.FilePermission;
import java.lang.reflect.ReflectPermission;
import java.net.SocketPermission;
import java.security.*;
import java.util.*;

public class PongPolicy extends Policy {

    @Override
    public PermissionCollection getPermissions(CodeSource codesource) {
        PermissionCollection permissions = new MyPermissionCollection();
        if (!codesource.getLocation().getHost().equals(DynaCompTest.PADDLES_CODESOURCE)) {
            addAllPermissions(permissions);
            addPermissionsForCompilation(permissions);
        }
        permissions.setReadOnly();
        return permissions;
    }

    private void addAllPermissions(PermissionCollection permissions) {
        permissions.add(new AllPermission());
    }

    private void addPermissionsForCompilation(PermissionCollection permissions) {
        permissions.add(new PropertyPermission("sun.*", "read"));
        permissions.add(new PropertyPermission("java.*", "read"));
        permissions.add(new PropertyPermission("nonBatchMode", "read"));
        permissions.add(new PropertyPermission("os.name", "read"));
        permissions.add(new PropertyPermission("line.separator", "read"));
        permissions.add(new PropertyPermission("env.class.path", "read"));
        permissions.add(new PropertyPermission("application.home", "read"));

        permissions.add(new FilePermission("<<ALL FILES>>", "read"));

        permissions.add(new RuntimePermission("createClassLoader"));

        permissions.add(new ReflectPermission("suppressAccessChecks"));

        permissions.add(new SocketPermission(DynaCompTest.PADDLES_CODESOURCE, "resolve"));
    }

    class MyPermissionCollection extends PermissionCollection {

        private static final long serialVersionUID = 614300921365729272L;
        private final ArrayList<Permission> perms = new ArrayList<>();

        public MyPermissionCollection() {
        }

        public MyPermissionCollection(PermissionCollection c) {
            Enumeration<Permission> elements = c.elements();
            while (elements.hasMoreElements()) {
                add(elements.nextElement());
            }
        }

        @Override
        public void add(Permission p) {
            perms.add(p);
        }

        @Override
        public boolean implies(Permission p) {
            for (Iterator<Permission> i = perms.iterator(); i.hasNext();) {
                if (i.next().implies(p)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public Enumeration<Permission> elements() {
            return Collections.enumeration(perms);
        }
    }
}
