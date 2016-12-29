package net.notfab.discord.selfbot;

import net.dv8tion.jda.MessageHistory;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.exceptions.PermissionException;
import net.dv8tion.jda.hooks.ListenerAdapter;
import net.dv8tion.jda.utils.AvatarUtil;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.List;

/**
 * DiscordSelfBot - http://notfab.net/
 * Created by Fabricio20 on 4/10/2016.
 */
public class CommandListener extends ListenerAdapter {

    private final String LENNY = "( ͡° ͜ʖ ͡°)";
    private final String SHRUG = "¯\\_(ツ)_/¯";
    private boolean isFarmOn = false;

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        if(!e.getAuthor().getId().equals(Main.getInstance().getJDA().getSelfInfo().getId())) {
            return;
        }
        String[] args = e.getMessage().getContent().split(" ");
        String cmd = args[0];
        try {
            Thread.sleep(200); // Sleep So Discord Doesn't Hate On Us
        } catch (InterruptedException ex) {}

        if(cmd.equalsIgnoreCase("/lenny")) {
            e.getMessage().updateMessage(this.LENNY);
        } else if(cmd.equalsIgnoreCase("/shrug")) {
            e.getMessage().updateMessage(this.SHRUG);
        } else if(cmd.equalsIgnoreCase("/idle")) {
            Main.getInstance().setIdle(!Main.getInstance().isIdle());
            Main.getInstance().getJDA().getAccountManager().setIdle(Main.getInstance().isIdle());
            e.getMessage().updateMessage((Main.getInstance().isIdle() ? "Be Right Back" : ":back:"));
        } else if(cmd.equalsIgnoreCase("/stop")) {
            e.getMessage().deleteMessage();
            System.exit(0);
        } else if(cmd.equalsIgnoreCase("/name")) {
            if(args.length == 0) {
                e.getMessage().updateMessage("Error: You didn't specify a name!");
            } else {
                String preChange = Main.getInstance().getJDA().getSelfInfo().getUsername();
                try {
                    Main.getInstance().getJDA().getAccountManager().setUsername(argsToString(args, 1)).update();
                    e.getMessage().updateMessage("**__Username:__** `" + preChange + "` -> `" + argsToString(args, 1) + "`");
                } catch (Exception ex) {
                    e.getMessage().updateMessage("**__Error:__** `>" + ex.getMessage() + "`");
                }
            }
        } else if(cmd.equalsIgnoreCase("/revert")) {
            String preChange = Main.getInstance().getJDA().getSelfInfo().getUsername();
            String originalName = Main.getInstance().getOriginalName();
            try {
                Main.getInstance().getJDA().getAccountManager().setUsername(Main.getInstance().getOriginalName()).update();
                e.getMessage().updateMessage("**__Username:__** `" + preChange + "` -> `" + originalName + "`");
            } catch (Exception ex) {
                e.getMessage().updateMessage("**__Error:__** `>" + ex.getMessage() + "`");
            }
        } else if(cmd.equalsIgnoreCase("/purge")) {
            if(args.length == 0) {
                e.getMessage().updateMessage("**__Error:__** `>No User Selected!`");
            } else if(args.length == 1) {
                if(e.getMessage().getMentionedUsers().size() == 0) {
                    e.getMessage().updateMessage("**__Error:__** `>No User Selected!`");
                    return;
                }
                try {
                    User u = e.getMessage().getMentionedUsers().get(0);
                    deleter(new MessageHistory(e.getJDA(), e.getChannel()).retrieve(100), u);
                } catch (PermissionException ex1) {
                    e.getMessage().updateMessage("**__Error:__** `>I am not allowed to delete this users messages.`");
                } catch (NullPointerException ex2) {
                    e.getMessage().updateMessage("**__Error:__** `>" + e.getMessage() + "`");
                }
            } else {
                Integer amount;
                try {
                    amount = Integer.parseInt(args[1]);
                    if(amount > 100) {
                        e.getMessage().updateMessage("**__Error:__** `>Number Is Too Big! (Max 100)`");
                    }
                } catch (IllegalArgumentException ex) {
                    e.getMessage().updateMessage("**__Error:__** `>That Is Not A Number!`");
                    return;
                }
                if(e.getMessage().getMentionedUsers().size() == 0) {
                    e.getMessage().updateMessage("**__Error:__** `>No User Selected!`");
                    return;
                }
                try {
                    User u = e.getMessage().getMentionedUsers().get(0);
                    deleter(new MessageHistory(e.getJDA(), e.getChannel()).retrieve(amount), u);
                } catch (PermissionException ex1) {
                    e.getMessage().updateMessage("**__Error:__** `>I am not allowed to delete this users messages.`");
                } catch (NullPointerException ex2) {
                    e.getMessage().updateMessage("**__Error:__** `>" + e.getMessage() + "`");
                }
            }
        } else if(cmd.equalsIgnoreCase("/greentext") || cmd.equalsIgnoreCase("/gt")) {
            String rest = argsToString(args, 1);
            e.getMessage().updateMessage("```css\n>" + rest + "```");
        } else if(cmd.equalsIgnoreCase("/lmgtfy")) {
            if(args.length == 0) {
                e.getMessage().updateMessage("**__Error:__** `>You must specify a term!`");
            } else {
                String q = argsToString(args, 1);
                e.getMessage().updateMessage("http://lmgtfy.com/?q=" + q.replace(" ", "+"));
            }
        } else if(cmd.equalsIgnoreCase("/tinyurl")) {
            if(args.length == 0) {
                e.getMessage().updateMessage("**__Error:__** `>You must specify an url!`");
            } else {
                String url = argsToString(args, 1);
                try {
                    Document doc = Jsoup.connect("http://short.notfab.net/index.php")
                            .ignoreContentType(true).ignoreHttpErrors(true).data("URL", url).post();
                    JSONObject o = new JSONObject(doc.text());
                    if(o.has("error")) {
                        e.getMessage().updateMessage("**__Error:__** `>Error while creating tinyurl!`");
                    }
                    e.getMessage().updateMessage("http://short.notfab.net/" + o.getString("UUID"));
                } catch (Exception ex) {
                    e.getMessage().updateMessage("**__Error:__** `>Error while creating tinyurl!`");
                }
            }
        } else if(cmd.equalsIgnoreCase("/help")) {
            StringBuilder sb = new StringBuilder();
            sb.append("```md\n");
            sb.append("/help      | Displays this help message\n");
            sb.append("/lenny     | Pastes lenny\n");
            sb.append("/shrug     | Shrugs\n");
            sb.append("/idle      | Sets IDLE status\n");
            sb.append("/stop      | Shuts down\n");
            sb.append("/name      | Changes name\n");
            sb.append("/revert    | Reverts name\n");
            sb.append("/purge     | Purgues chat (User and/or amount)\n");
            sb.append("/greentext | Greentext\n");
            sb.append("/gt        | Alias for Greentext\n");
            sb.append("/lmgtfy    | Let Me Google That For You\n");
            sb.append("/tinyurl   | Makes an URL smaller\n");
            e.getMessage().updateMessage(sb.toString());
        }
    }

    public String argsToString(String[] args, int i) {
        StringBuilder sb = new StringBuilder();
        for(int a = i; a < args.length; a++) {
            sb.append(" " + args[a]);
        }
        return sb.toString().replaceFirst(" ", "");
    }

    private static void deleter(List<Message> list, User user) {
        if (list == null || user == null)
            return;
        int index = 0;
        Message target;
        while (index < list.size()) {
            target = list.get(index);
            try {
                if (target.getAuthor().getId().equals(user.getId())) {
                    Message m = target;
                    Thread t = new Thread() {
                        public void run() {
                            m.deleteMessage();
                            Thread.currentThread().interrupt();
                        }
                    };
                    t.start();
                }
            } catch (NullPointerException e) {
                Message m = target;
                Thread t = new Thread() {
                    public void run() {
                        m.deleteMessage();
                        Thread.currentThread().interrupt();
                    }
                };
                t.start();
            }
            index++;
        }
    }


}
