package nl.mxndarijn.cluedo.game.events;

//import de.Herbystar.TTA.TTA_Methods;

import io.papermc.paper.event.player.AsyncChatEvent;
import io.papermc.paper.event.player.PlayerSignCommandPreprocessEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.TitlePart;
import nl.mxndarijn.api.mxworld.MxLocation;
import nl.mxndarijn.api.util.Functions;
import nl.mxndarijn.cluedo.Cluedo;
import nl.mxndarijn.cluedo.data.ItemTag;
import nl.mxndarijn.cluedo.data.Role;
import nl.mxndarijn.cluedo.game.Game;
import nl.mxndarijn.cluedo.game.GamePlayer;
import nl.mxndarijn.cluedo.game.UpcomingGameStatus;
import nl.mxndarijn.cluedo.managers.chests.ChestInformation;
import nl.mxndarijn.cluedo.managers.chests.chestattachments.ChestAttachments;
import nl.mxndarijn.cluedo.managers.language.LanguageManager;
import nl.mxndarijn.cluedo.managers.language.LanguageText;
import nl.mxndarijn.cluedo.managers.shulkers.ShulkerInformation;
import nl.mxndarijn.cluedo.map.mapplayer.MapPlayer;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class GamePlayingEvents extends GameEvent {
    private final HashMap<Location, ArmorStand> blocks = new HashMap<>();


    public GamePlayingEvents(Game g, JavaPlugin plugin) {
        super(g, plugin);
    }

    @EventHandler
    public void damage(PlayerArmorStandManipulateEvent e) {
        if (!validateWorld(e.getPlayer().getWorld()))
            return;
        if (Functions.convertComponentToString(e.getRightClicked().customName()).equals("attachment")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void interactShulkerBox(PlayerInteractEvent e) {
        if (game.getGameInfo().getStatus() != UpcomingGameStatus.PLAYING)
            return;
        if (!validateWorld(e.getPlayer().getWorld()))
            return;

        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        assert e.getClickedBlock() != null;
        if (!(e.getClickedBlock().getState() instanceof ShulkerBox shulkerBox)) {
            return;
        }
        Player p = e.getPlayer();
        Optional<GamePlayer> optionalGamePlayer = game.getGamePlayerOfPlayer(p.getUniqueId());
        if (optionalGamePlayer.isEmpty())
            return;

        if (!e.getClickedBlock().getType().equals(optionalGamePlayer.get().getMapPlayer().getColor().getShulkerBlock())) {
            e.setCancelled(true);
            p.sendMessage(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_ONLY_OPEN_OWN_SHULKER));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void changePlayerStateOnShulkerOpen(PlayerInteractEvent e) {
        if (game.getGameInfo().getStatus() != UpcomingGameStatus.PLAYING)
            return;
        if (!validateWorld(e.getPlayer().getWorld()))
            return;

        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        assert e.getClickedBlock() != null;
        if (!(e.getClickedBlock().getState() instanceof ShulkerBox shulkerBox)) {
            return;
        }
        Player p = e.getPlayer();
        Optional<GamePlayer> optionalGamePlayer = game.getGamePlayerOfPlayer(p.getUniqueId());
        if (optionalGamePlayer.isEmpty())
            return;
        if (e.getClickedBlock().getType().equals(optionalGamePlayer.get().getMapPlayer().getColor().getShulkerBlock())) {
            if (e.useInteractedBlock() == Event.Result.ALLOW) {
                Optional<ShulkerInformation> inf = game.getShulkerManager().getShulkerByLocation(MxLocation.getFromLocation(e.getClickedBlock().getLocation()));
                if (inf.isPresent()) {
                    if (inf.get().isStartingRoom()) {
                        optionalGamePlayer.get().setBeginChestOpened(true);
                    }
                    if (!inf.get().isStartingRoom() && !optionalGamePlayer.get().isPeacekeeperChestOpened()) {
                        // Peacekeeper LOOT
                        optionalGamePlayer.get().setPeacekeeperChestOpened(true);
                        if (optionalGamePlayer.get().getMapPlayer().isPeacekeeper()) {
                            optionalGamePlayer.get().givePeacekeeperLoot();
                            game.sendMessageToAll(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_PLAYER_IS_PEACEKEEPER, Collections.singletonList(optionalGamePlayer.get().getMapPlayer().getColor().getColor() + p.getName())));
                            game.sendMessageToAll(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_PEACEKEEPER_KILLS, Collections.singletonList(game.getPeacekeeperKills() + "")));
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void chestAttachmentCanOpenChest(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            assert e.getClickedBlock() != null;
            if (e.getClickedBlock().getType() != Material.CHEST && e.getClickedBlock().getType() != Material.TRAPPED_CHEST) {
                return;
            }
            if(game.getPeacekeeperKills() == 0) {
                e.setCancelled(true);
                return;
            }
            Optional<ChestInformation> inf = game.getChestManager().getChestByLocation(MxLocation.getFromLocation(e.getClickedBlock().getLocation()));
            Optional<GamePlayer> optionalGamePlayer = game.getGamePlayerOfPlayer(e.getPlayer().getUniqueId());
            if (optionalGamePlayer.isPresent()) {
                if (inf.isPresent()) {
                    inf.get().onChestInteract(optionalGamePlayer.get(), e, game, e.getPlayer());

                    if (!inf.get().canOpenChest(optionalGamePlayer.get())) {
                        e.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClickChest(InventoryClickEvent e) {
        if (e.getClickedInventory() == null)
            return;
        Optional<GamePlayer> gamePlayer = game.getGamePlayerOfPlayer(e.getWhoClicked().getUniqueId());
        if (gamePlayer.isEmpty())
            return;
        game.getChestManager().getChests().forEach(chestInformation -> {
            Location l = chestInformation.getLocation().getLocation(e.getWhoClicked().getWorld());
            if (l.getBlock().getState() instanceof Chest c) {
                if (c.getInventory().equals(e.getClickedInventory())) {
                    chestInformation.onChestInventoryClick(gamePlayer.get(), e, game, (Player) e.getWhoClicked());
                }
                if (e.getWhoClicked().getOpenInventory().getTopInventory().equals(c.getInventory()) && chestInformation.containsChestAttachment(ChestAttachments.CHEST_LIMITED_CHOICE)) {
                    if (e.getAction() == InventoryAction.HOTBAR_SWAP || e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY || e.getAction() == InventoryAction.SWAP_WITH_CURSOR) {
                        e.setCancelled(true);
                    }
                }
            }
        });
    }

    @EventHandler
    public void playerKilled(PlayerDeathEvent e) {
        Optional<GamePlayer> gamePlayer = game.getGamePlayerOfPlayer(e.getPlayer().getUniqueId());
        if (gamePlayer.isEmpty())
            return;
        gamePlayer.get().setAlive(false);
        game.addSpectatorSettings(e.getPlayer().getUniqueId(), e.getPlayer().getLocation());

        e.deathMessage(Component.text(""));
        if (e.getEntity().getKiller() != null) {
            Player killer = e.getEntity().getKiller();
            Optional<GamePlayer> optionalKiller = game.getGamePlayerOfPlayer(killer.getUniqueId());
            if (optionalKiller.isPresent()) {
                if (optionalKiller.get().getMapPlayer().isPeacekeeper() && optionalKiller.get().isPeacekeeperChestOpened()) {
                    game.setPeacekeeperKills(game.getPeacekeeperKills() - 1);
                    if (game.getPeacekeeperKills() == 0) {
                        optionalKiller.get().setAlive(false);
                        game.addSpectatorSettings(killer.getUniqueId(), killer.getLocation());
                        game.sendMessageToAll(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_PEACEKEEPER_DISAPPEARED));
                    }
                }
            }
            game.sendMessageToAll(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_PLAYER_KILLED_BY_PLAYER, new ArrayList<>(Arrays.asList(e.getPlayer().getName(), killer.getName()))));
        } else {
            game.sendMessageToAll(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_PLAYER_DIED, Collections.singletonList(e.getPlayer().getName())));
        }
    }

    @EventHandler
    public void breakBlock(BlockBreakEvent e) {
        if (game.getGameInfo().getStatus() != UpcomingGameStatus.PLAYING)
            return;
        if (!validateWorld(e.getPlayer().getWorld()))
            return;

        Optional<GamePlayer> gamePlayer = game.getGamePlayerOfPlayer(e.getPlayer().getUniqueId());
        if (gamePlayer.isEmpty())
            return;

        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void breakBlockCorrector(BlockBreakEvent e) {
        if (!validateWorld(e.getPlayer().getWorld()))
            return;
        if (e.isCancelled())
            return;
        if (blocks.containsKey(e.getBlock().getLocation())) {
            blocks.get(e.getBlock().getLocation()).remove();
            blocks.remove(e.getBlock().getLocation());
        }
    }

    @EventHandler
    public void place(BlockPlaceEvent e) {
        if (game.getGameInfo().getStatus() != UpcomingGameStatus.PLAYING)
            return;
        if (!validateWorld(e.getPlayer().getWorld()))
            return;

        Optional<GamePlayer> gamePlayer = game.getGamePlayerOfPlayer(e.getPlayer().getUniqueId());
        if (gamePlayer.isEmpty())
            return;

        Location location = e.getBlock().getLocation().clone().add(0, -1, 0);
        Material type = location.getBlock().getType();
        Material t = e.getBlock().getType();
        if (type == Material.END_STONE) {
            if (t != Material.GOLD_BLOCK && t != Material.DIAMOND_BLOCK) {
                e.setCancelled(true);
                return;
            }
            if (t != gamePlayer.get().getMapPlayer().getRole().getType()) {
                e.setCancelled(true);
                return;
            }
            List<UUID> list = new ArrayList<>();
            list.addAll(game.getSpectators());
            list.addAll(game.getHosts());
            game.getColors().forEach(c -> {
                if (c.getPlayer().isPresent())
                    list.add(c.getPlayer().get());
            });

            Role role = gamePlayer.get().getMapPlayer().getRole();
            list.forEach(uuid -> {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) {
                    p.sendTitlePart(TitlePart.TITLE, MiniMessage.miniMessage().deserialize(role.getTitle()));
                    p.sendTitlePart(TitlePart.SUBTITLE, MiniMessage.miniMessage().deserialize(role.getSubTitle()));
                }
            });
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                game.setGameStatus(UpcomingGameStatus.FINISHED, Optional.of(role));
            }, 20L * 10L);
            return;
        }
        if(e.getItemInHand().getItemMeta() != null) {
            String data = e.getItemInHand().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, ItemTag.PLACEABLE.getPersistentDataTag()), PersistentDataType.STRING);
            if(data != null && data.equalsIgnoreCase("false")) {
                e.setCancelled(true);
                return;
            }
        }

        if (e.getBlock().getType() == Material.EMERALD_BLOCK || e.getBlock().getType() == Material.DIAMOND_BLOCK || e.getBlock().getType() == Material.GOLD_BLOCK) {
            Location loc = e.getBlock().getLocation().clone().add(0.5, -0.1, 0.5);
            AtomicLong timer = new AtomicLong(5 * 60 * 1000);
            if (e.getBlock().getType() == Material.DIAMOND_BLOCK || e.getBlock().getType() == Material.GOLD_BLOCK) {
                timer.set(60 * 1000);
            }
            AtomicLong currentMillis = new AtomicLong(System.currentTimeMillis());
            ArmorStand ar = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);


            ar.setCustomNameVisible(true);
            ar.setSmall(true);
            ar.setArms(false);
            ar.setBasePlate(false);
            ar.setInvisible(true);
            ar.setInvulnerable(true);
            ar.setGravity(false);
            ar.customName(Component.text("removeableBlock"));
            ar.setCollidable(false);
            AtomicInteger taskID = new AtomicInteger(Integer.MAX_VALUE);
            blocks.put(loc, ar);
            taskID.set(Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                if (!blocks.containsKey(loc)) {
                    Bukkit.getScheduler().cancelTask(taskID.get());
                }
                long now = System.currentTimeMillis();
                long delta = now - currentMillis.get();
                timer.addAndGet(-delta);
                ar.customName(Component.text(ChatColor.AQUA + Functions.formatGameTime(timer.get())));
                if (timer.get() <= 0) {
                    e.getBlock().setType(Material.AIR);
                    ar.remove();
                    Bukkit.getScheduler().cancelTask(taskID.get());
                }
                currentMillis.set(now);
            }, 0L, 10L).getTaskId());
        }


    }

    @EventHandler
    public void interactSettings(PlayerInteractEvent e) {
        if (game.getGameInfo().getStatus() != UpcomingGameStatus.PLAYING)
            return;
        if (!validateWorld(e.getPlayer().getWorld()))
            return;

        Optional<GamePlayer> gamePlayer = game.getGamePlayerOfPlayer(e.getPlayer().getUniqueId());
        if (gamePlayer.isEmpty())
            return;
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK && e.getAction() != Action.PHYSICAL)
            return;
        Material type = e.getClickedBlock().getType();

        if (!game.getInteractionManager().isInteractionWithTypeAllowed(type)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void damageEntity(EntityDamageByEntityEvent e) {
        if (game.getGameInfo().getStatus() != UpcomingGameStatus.PLAYING)
            return;
        if (!validateWorld(e.getEntity().getWorld()))
            return;
        Material type = null;
        if(e.getEntity() instanceof ItemFrame) {
            type = Material.ITEM_FRAME;
        }
        if(e.getEntity() instanceof GlowItemFrame) {
            type = Material.GLOW_ITEM_FRAME;
        }
        if(type != null) {
            if(game.getGamePlayerOfPlayer(e.getDamager().getUniqueId()).isPresent())
                if (!game.getInteractionManager().isInteractionWithTypeAllowed(type)) {
                    e.setCancelled(true);
                }
        }

    }

    @EventHandler
    public void paintingBreak(HangingBreakByEntityEvent e) {
        if (game.getGameInfo().getStatus() != UpcomingGameStatus.PLAYING)
            return;
        if (!validateWorld(e.getEntity().getWorld()))
            return;
        if(game.getGamePlayerOfPlayer(e.getRemover().getUniqueId()).isPresent())
            if(e.getEntity() instanceof ItemFrame || e.getEntity() instanceof GlowItemFrame) {
                e.setCancelled(true);
            }
    }

    @EventHandler
    public void signChangeEvent(SignChangeEvent e) {
        if (game.getGameInfo().getStatus() != UpcomingGameStatus.PLAYING)
            return;
        if (!validateWorld(e.getPlayer().getWorld()))
            return;
        if(game.getGamePlayerOfPlayer(e.getPlayer().getUniqueId()).isPresent())
            e.setCancelled(true);
    }

    @EventHandler
    public void preProcessSignCommand(PlayerSignCommandPreprocessEvent e) {
        if (game.getGameInfo().getStatus() != UpcomingGameStatus.PLAYING)
            return;
        if (!validateWorld(e.getPlayer().getWorld()))
            return;
        if(game.getGamePlayerOfPlayer(e.getPlayer().getUniqueId()).isPresent())
            e.setCancelled(true);
    }

    @EventHandler
    public void onSignClick(PlayerInteractEvent e) {
        if (game.getGameInfo().getStatus() != UpcomingGameStatus.PLAYING)
            return;
        if (!validateWorld(e.getPlayer().getWorld()))
            return;

        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block b = e.getClickedBlock();
        if (b != null) {
            if (b.getType().name().toLowerCase().contains("sign")) {
                if(game.getGamePlayerOfPlayer(e.getPlayer().getUniqueId()).isPresent())
                    e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent e) {
        Player p = (Player) e.getPlayer();
        if (game.getGameInfo().getStatus() != UpcomingGameStatus.PLAYING)
            return;
        if (!validateWorld(p.getWorld()))
            return;

        if (e.getRightClicked() instanceof Lectern) {
            ItemStack item = p.getInventory().getItemInMainHand();

            if (item.getType() == Material.WRITTEN_BOOK || item.getType() == Material.BOOK) {
                if(game.getGamePlayerOfPlayer(p.getUniqueId()).isPresent()) {
                    ItemMeta im = item.getItemMeta();
                    PersistentDataContainer container = im.getPersistentDataContainer();
                    String data = container.get(new NamespacedKey(JavaPlugin.getPlugin(Cluedo.class), "undroppable"), PersistentDataType.STRING);

                    if (data != null && data.equalsIgnoreCase("true"))
                        e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void paintingBreak(PlayerInteractEntityEvent e) {
        if (game.getGameInfo().getStatus() != UpcomingGameStatus.PLAYING)
            return;
        if (!validateWorld(e.getRightClicked().getWorld()))
            return;
        Material type = null;
        if(e.getRightClicked() instanceof ItemFrame) {
            type = Material.ITEM_FRAME;
        }
        if(e.getRightClicked() instanceof GlowItemFrame) {
            type = Material.GLOW_ITEM_FRAME;
        }
        if(type != null) {
            if(game.getGamePlayerOfPlayer(e.getRightClicked().getUniqueId()).isPresent())
                if (!game.getInteractionManager().isInteractionWithTypeAllowed(type)) {
                    e.setCancelled(true);
                }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void chat(AsyncChatEvent e) {
        Player p = e.getPlayer();
        if(e.isCancelled())
            return;
        if (!validateWorld(p.getWorld()))
            return;
        Optional<GamePlayer> optionalGamePlayer = game.getGamePlayerOfPlayer(p.getUniqueId());
        if(optionalGamePlayer.isEmpty())
            return;
        if(!optionalGamePlayer.get().isAlive())
            return;

        e.setCancelled(true);
        GamePlayer gp = optionalGamePlayer.get();
        game.sendMessageToAll(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_CHAT_PLAYER, Arrays.asList(gp.getMapPlayer().getColor().getDisplayName(), p.getName(), Functions.convertComponentToString(e.message()))));
    }

    @EventHandler
    public void chatHost(AsyncChatEvent e) {
        Player p = e.getPlayer();
        if (!validateWorld(p.getWorld()))
            return;
        if(!game.getHosts().contains(p.getUniqueId()))
            return;

        e.setCancelled(true);
        game.sendMessageToAll(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_CHAT_HOST, Arrays.asList(p.getName(), Functions.convertComponentToString(e.message()))));
    }

    @EventHandler
    public void playerDeadEvent(PlayerDeathEvent e) {
        Player p = e.getPlayer();
        if (!validateWorld(e.getPlayer().getWorld()))
            return;
        Optional<GamePlayer> optionalGamePlayer = game.getGamePlayerOfPlayer(p.getUniqueId());
        if(optionalGamePlayer.isEmpty())
            return;

        if (game.getHosts().contains(p.getUniqueId()) &&
                game.getColors().stream().noneMatch(color ->
                        color.getPlayer().filter(uuid -> uuid.equals(p.getUniqueId())).isPresent())) {
            e.setCancelled(true);
        }

        ArrayList<ItemStack> drops = new ArrayList<>(e.getDrops());
        for (ItemStack item : drops) {
            if (item.getItemMeta() == null)
                continue;
            String data = item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, ItemTag.VANISHABLE.getPersistentDataTag()), PersistentDataType.STRING);
            if (data != null && data.equalsIgnoreCase("false"))
                e.getDrops().remove(item);
        }

        if(p.getKiller() == null) return;
        Game game = optionalGamePlayer.get().getGame();
        MapPlayer killer = getMapPlayer(p.getKiller(), game);
        MapPlayer player = getMapPlayer(p, game);

        if(killer == null || player == null) return;
        killer.setRole(player.getRole());
    }

    private MapPlayer getMapPlayer(Player player, Game game) {
        Optional<GamePlayer> gamePlayer = game.getColors().stream().filter(gPlayer -> gPlayer.getPlayer().isPresent() && gPlayer.getPlayer().get() == player.getUniqueId()).findFirst();
        return gamePlayer.map(GamePlayer::getMapPlayer).orElse(null);
    }

    // Rune code
    @EventHandler
    public void onServerCommand(ServerCommandEvent event) {
        CommandSender sender = event.getSender();

        // Alleen command blocks
        if (!(sender instanceof BlockCommandSender)) return;

        BlockCommandSender blockSender = (BlockCommandSender) sender;
        Location loc = blockSender.getBlock().getLocation();

        String command = event.getCommand();

        if (command.contains("@p")) {
            Player nearest = getNearestPlayer(loc);
            if (nearest != null) {
                String newCommand = command.replace("@p", nearest.getName());
                event.setCommand(newCommand);
            } else {
                // Geen speler in deze wereld
                event.setCancelled(true);
            }
        }
    }

    private Player getNearestPlayer(Location loc) {
        Player nearest = null;
        double minDistSq = Double.MAX_VALUE;

        for (Player player : loc.getWorld().getPlayers()) {
            double distSq = player.getLocation().distanceSquared(loc);
            if (distSq < minDistSq) {
                minDistSq = distSq;
                nearest = player;
            }
        }

        return nearest;
    }
}
