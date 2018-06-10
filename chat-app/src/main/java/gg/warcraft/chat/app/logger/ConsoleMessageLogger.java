package gg.warcrat.chat.app.logger;

import gg.warcraft.monolith.api.chat.message.Message;

public class ConsoleMessageLogger implements MessageLogger {

    @Override
    public void setGroup(String group) {
        // do nothing
    }

    @Override
    public void log(Message message) {
        String sender = message.getSender().getName();
        String log = String.format("[%s] %s: %s", message.getChannel().getName(), sender, message.getOriginal());
        System.out.println(log);
    }
}
