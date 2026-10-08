package cn.xinyue_neko.plugins.BrutalClean;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.plugin.java.JavaPlugin;

public class BrutalClean extends JavaPlugin {

    private int taskId = -1;
    private final String prefix = "[暴力扫地机] ";

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // 从配置文件读取清理间隔（秒），默认 300 秒
        long intervalSeconds = getConfig().getLong("clean-interval-seconds", 300);
        long intervalTicks = intervalSeconds * 20L;

        // 启动定时清理任务，20 tick = 1 秒
        taskId = Bukkit.getScheduler().runTaskTimer(
                this,
                this::cleanAllWorlds,
                intervalTicks,   // 首次延迟
                intervalTicks    // 后续间隔
        ).getTaskId();

        getLogger().info("BrutalCleaner 已启用，清理间隔: " + intervalSeconds + " 秒");
    }

    @Override
    public void onDisable() {
        // 取消定时任务
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
        getLogger().info("BrutalCleaner 已禁用。");
    }

    // ==================== 核心清理逻辑 ====================

    /**
     * 遍历所有世界，清理除玩家和村民外的所有实体。
     */
    private void cleanAllWorlds() {
        int totalRemoved = 0;

        for (World world : Bukkit.getWorlds()) {
            totalRemoved += cleanWorld(world);
        }

        if (totalRemoved > 0) {
            String msg = "本次清理共移除 " + totalRemoved + " 个实体。";
            getServer().getConsoleSender().sendMessage("本次清理共移除 " + totalRemoved + " 个实体。");
            Bukkit.broadcastMessage(prefix + ChatColor.RED + msg);
        }
    }

    /**
     * 清理单个世界中除玩家和村民外的所有实体。
     *
     * @param world 目标世界
     * @return 移除的实体数量
     */
    private int cleanWorld(World world) {
        int removed = 0;

        for (Entity entity : new java.util.ArrayList<>(world.getEntities())) {
            if (shouldKeep(entity)) {
                continue;
            }

            if (entity instanceof Item || entity.getType() == EntityType.DROPPED_ITEM) {
                entity.remove();
                removed++;
                continue;
            }

            entity.remove();
            removed++;
        }



        return removed;
    }

    /**
     * 判断实体是否应该保留（不被清理）。
     */
    private boolean shouldKeep(Entity entity) {

        return entity instanceof Player ||
                entity instanceof Minecart ||
                entity instanceof Boat ||
                entity instanceof ItemFrame ||
                entity instanceof Painting ||
                entity.getType() == EntityType.VILLAGER;

    }

    // ==================== 命令处理 ====================

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("brutalclean")) {
            return false;
        }

        // 权限检查（plugin.yml 中已声明，此处再校验一次）
        if (!sender.hasPermission("brutalcleaner.clean")) {
            sender.sendMessage("§c你没有权限使用此命令。");
            return true;
        }

        sender.sendMessage("§e正在执行暴力清理...");
        cleanAllWorlds();
        sender.sendMessage("§a清理完成。");

        return true;
    }
}