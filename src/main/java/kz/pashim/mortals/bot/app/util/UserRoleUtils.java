package kz.pashim.mortals.bot.app.util;

import kz.pashim.mortals.bot.app.model.UserEntity;
import kz.pashim.mortals.bot.app.model.UserRole;

import java.util.List;

public class UserRoleUtils {

    public static boolean hasPermission(UserEntity user, UserRole role) {
        if (role == null) {
            return true;
        }
        if (role == UserRole.ADMIN) {
            return role.equals(user.getRole());
        }
        if (role == UserRole.MODERATOR) {
            return List.of(UserRole.ADMIN, UserRole.MODERATOR).contains(role);
        }
        return false;
    }

    public static boolean isAbleToStartGame(UserEntity user) {
        return List.of(UserRole.ADMIN, UserRole.MODERATOR).contains(user.getRole());
    }
}
