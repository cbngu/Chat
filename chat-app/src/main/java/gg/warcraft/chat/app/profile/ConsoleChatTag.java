package gg.warcraft.chat.app.profile;

import gg.warcraft.chat.api.profile.ChatTag;
import gg.warcraft.monolith.api.util.ColorCode;

public class ConsoleChatTag implements ChatTag {
    private static final String CONSOLE_TAG = "Server";

    @Override
    public String getName() {
        return CONSOLE_TAG;
    }

    @Override
    public ColorCode getColor() {
        return ColorCode.YELLOW;
    }
}
