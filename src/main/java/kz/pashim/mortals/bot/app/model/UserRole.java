package kz.pashim.mortals.bot.app.model;

public enum UserRole {
    ADMIN("Администратор"),
    MODERATOR("Модератор");

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String displayName;
}
