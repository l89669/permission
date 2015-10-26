package com.mengcraft.permission;

import com.mengcraft.permission.entity.PermissionUser;
import com.mengcraft.simpleorm.EbeanHandler;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static java.lang.System.currentTimeMillis;

/**
 * Created on 15-10-20.
 */
public class Commander implements CommandExecutor {

    private static final long DAY_TIME = 86400000;

    private final EbeanHandler db;
    private final Main main;

    public Commander(Main main, EbeanHandler db) {
        this.db = db;
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] arguments) {
        Iterator<String> iterator = Arrays.asList(arguments).iterator();
        if (iterator.hasNext()) {
            return execute(sender,
                    iterator.next(),
                    iterator.hasNext() ? iterator.next() : null,
                    iterator.hasNext() ? Integer.parseInt(iterator.next()) : 0
            );
        } else {
            sender.sendMessage(ChatColor.DARK_RED + "/permission $player $permission $day");
        }
        return false;
    }

    private boolean execute(CommandSender sender, String name, String permission, long time) {
        if (permission == null) {
            List<PermissionUser> permissionList = db.find(PermissionUser.class)
                    .where()
                    .eq("name", name)
                    .findList();
            sender.sendMessage(ChatColor.GOLD + ">>> Player " + name + "'s permission list:");
            for (PermissionUser line : permissionList) {
                sender.sendMessage(ChatColor.GOLD + line.toString());
            }
            sender.sendMessage(ChatColor.GOLD + "<<<");
        } else if (time < 1) {
            return false;
        } else {
            PermissionUser user = db.find(PermissionUser.class)
                    .where()
                    .eq("name", name)
                    .eq("value", permission)
                    .gt("outdated", new Timestamp(currentTimeMillis()))
                    .findUnique();
            if (user == null) {
                sender.sendMessage(ChatColor.GOLD + "Insert new permission!");

                PermissionUser inserted = new PermissionUser();
                inserted.setName(name);
                inserted.setValue(permission);
                inserted.setOutdated(new Timestamp(currentTimeMillis() + time * DAY_TIME));

                db.insert(inserted);
            } else {
                sender.sendMessage(ChatColor.GOLD + "Update exists permission!");

                user.setOutdated(new Timestamp(user.getOutdated().getTime() + time * DAY_TIME));

                db.update(user);
            }
        }
        return true;
    }

}