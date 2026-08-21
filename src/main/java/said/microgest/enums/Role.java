package said.microgest.enums;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum Role {
    ADMIN,
    AGENT,
    SUPERVISEUR;

    public Set<Permissions> getPermissions() {
        Set<Permissions> permissions = new HashSet<>();

        switch (this) {
            case ADMIN:
                permissions.addAll(Arrays.asList(Permissions.values()));
                break;

            case AGENT:
                permissions.add(Permissions.VIEW_DASHBOARD);
                permissions.add(Permissions.VIEW_STATISTICS);
                permissions.add(Permissions.MANAGE_ADHERENTS);
                permissions.add(Permissions.MANAGE_OPERATIONS);
                permissions.add(Permissions.MANAGE_PRETS);
                permissions.add(Permissions.MANAGE_REMBOURSEMENTS);
                permissions.add(Permissions.VIEW_EPARGNE);
                break;

            case SUPERVISEUR:
                permissions.add(Permissions.VIEW_DASHBOARD);
                permissions.add(Permissions.VIEW_STATISTICS);
                permissions.add(Permissions.VALIDATE_PRETS);
                permissions.add(Permissions.EXPORT_REPORTS);
                permissions.add(Permissions.VIEW_EPARGNE);
                break;

            default:
                break;
        }

        return permissions;
    }

    public boolean hasPermission(Permissions permission) {
        return getPermissions().contains(permission);
    }

    public boolean hasAnyPermission(Permissions... permissions) {
        return getPermissions().stream().anyMatch(p -> Arrays.asList(permissions).contains(p));
    }

    public boolean hasAllPermissions(Permissions... permissions) {
        return getPermissions().containsAll(Arrays.asList(permissions));
    }
}