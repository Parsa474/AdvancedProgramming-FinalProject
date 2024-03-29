package discord.client;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class TextChannel implements Serializable {
    // Fields:
    private String name;
    private final int index;
    private String pinnedMessage;
    private final HashMap<Integer, Boolean> members;
    // maps all the members' IDs to whether they're in this text channel right now (true) or not (false)
    private final ArrayList<ChatMessage> textChannelMessages;
    // holds all the messages exchanged in this text channel
    private final ArrayList<URL> urls;
    private final ArrayList<DownloadableFile> files;

    // Constructors:
    public TextChannel(String name, int index, Set<Integer> membersIDs) {
        this.name = name;
        this.index = index;
        pinnedMessage = "";
        this.members = new HashMap<>();
        for (int member : membersIDs) {
            this.members.put(member, false);
        }
        textChannelMessages = new ArrayList<>();
//        for (String s : messages) {
//            textChannelMessages.add(new TextChannelMessage(s));
//        }
        urls = new ArrayList<>();
        files = new ArrayList<>();
    }

    // Getters:
    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }

    public String getPinnedMessage() {
        return pinnedMessage;
    }

    public HashMap<Integer, Boolean> getMembers() {
        return members;
    }

    public ArrayList<ChatMessage> getTextChannelMessages() {
        return textChannelMessages;
    }

    public ArrayList<String> getMessages() {
        ArrayList<String> messages = new ArrayList<>();
        for (ChatMessage m : textChannelMessages) {
            messages.add(m.getMessage());
        }
        return messages;
    }

    public ArrayList<URL> getUrls() {
        return urls;
    }

    public ArrayList<DownloadableFile> getFiles() {
        return files;
    }

    // Setters:
    public void setName(String name) {
        this.name = name;
    }

    public void setPinnedMessage(String pinnedMessage) {
        this.pinnedMessage = pinnedMessage;
    }



    public void removeMember(int UID) {
        members.remove(UID);
    }
}
