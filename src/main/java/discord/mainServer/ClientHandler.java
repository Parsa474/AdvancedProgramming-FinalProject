package discord.mainServer;

import discord.client.*;
import discord.signals.*;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class ClientHandler implements Runnable {
    // Fields:
    public static List<ClientHandler> clientHandlers = Collections.synchronizedList(new ArrayList<>());
    private Model user;
    private final MySocket mySocket;

    // Constructors:
    public ClientHandler(Socket socket) {
        mySocket = new MySocket(socket);
        clientHandlers.add(this);
    }

    // Getters:
    public Model getUser() {
        return user;
    }

    public MySocket getMySocket() {
        return mySocket;
    }

    // Methods:
    @Override
    public void run() {
        while (true) {
            try {
                Action action;
                // the first while loop is for signing up or logging in
                while (user == null) {
                    action = (Action) mySocket.read();
                    if (action instanceof LoginAction || ((action instanceof SignUpOrChangeInfoAction && ((SignUpOrChangeInfoAction) action).getStage() == 5))) {
                        user = (Model) action.act();
                        // write back the Model of the logged in/signed-up user
                        mySocket.write(user);
                    } else {
                        // used for signing up stages before finalizing the registration (validating fields)
                        mySocket.write(action.act());
                    }
                }
                // the second while loop is for any other action after logging in or signing up
                while (user != null) {
                    action = (Action) mySocket.read();
                    if (action instanceof LogoutAction) {
                        handleQuit();
                        user = null;
                    } else {
                        user = MainServer.getUsers().get(user.getUID());
                        mySocket.write(action.act());
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                clientHandlers.remove(this);
                if (user != null) {
                    try {
                        handleQuit();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                mySocket.closeEverything();
                if (user != null) {
                    System.out.println("clientHandler of " + user.getUsername() + " got removed");
                } else {
                    System.out.println("clientHandler of a not logged in client got removed");
                }
                break;
            }
        }
    }

    private void handleQuit() throws IOException {
        user.setStatus(Status.Invisible);
        informRelatedPeople(user);

//        outer: for (Server server : MainServer.getServers().values()) {
//            for (TextChannel textChannel : server.getTextChannels()) {
//                if (textChannel.getMembers().get(user.getUID())) {
//                    textChannel.getMembers().replace(user.getUID(), false);
//                    break outer;
//                }
//            }
//        }

        MainServer.getUsers().replace(user.getUID(), user);
        MainServer.updateDatabase(user);
    }

    public static void informRelatedPeople(Model updatedMe) throws IOException {
        for (ClientHandler ch : clientHandlers) {
            if (ch.user != null) {
                Model user = ch.user;
                Integer UID = user.getUID();
                boolean related = updatedMe.getFriends().contains(UID) ||   // a friend
                        updatedMe.getSentFriendRequests().contains(UID) ||  // someone whom I've sent a friend request to
                        updatedMe.getIncomingFriendRequests().contains(UID);    //someone who has sent a friend request to me
                if (related) {
                    synchronized (ch.getMySocket()) {
                        ch.getMySocket().write(new RelatedUserChangedUpdaterSignal(updatedMe));
                    }
                }
            }
        }
    }

    public static void informServerRelatedPeople(Server server) throws IOException {
        for (ClientHandler ch : clientHandlers) {
            if (ch.user != null) {
                if (server.getMembers().containsKey(ch.user.getUID())) {
                    synchronized (ch.mySocket) {
                        ch.mySocket.write(new RelatedServerChangedUpdaterSignal(server.getUnicode()));
                    }
                }
            }
        }
    }
}
