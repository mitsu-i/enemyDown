package plugin.enemydown.command;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.SplittableRandom;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import plugin.enemydown.Main;
import plugin.enemydown.data.PlayerData;

public class EnemyDownCommand extends BaseCommand implements CommandExecutor, Listener {

  public static final int GAME_TIME = 20;
  public static final String NORMAL = "normal";
  public static final String HARD = "hard";
  public static final String EASY = "easy";
  public static final String NONE = "NONE";
  public static final String LIST = "list";

  private Main main;
  private List<PlayerData> playerDataList = new ArrayList<>();
  private List<Entity> spawnEntityList = new ArrayList<>();
  private int gameTime;
  private String difficulty;


  public EnemyDownCommand(Main main) {
    this.main = main;
  }

  @Override
  public boolean onExecutePlayerCommand(Player player, Command command, String label,
      String[] args) {
    if (args.length == 1 && (LIST.equals(args[0]))) {
      String url = "jdbc:mysql://localhost/spigot_server";
      String user = "root";
      String password = "Sdgosdgo11";
      String sql = "select * from player_score";

      try(Connection con = DriverManager.getConnection(url, user, password)) {
        Statement statement = con.createStatement();
        ResultSet resultset = statement.executeQuery(sql);
        while(resultset.next()){
          int id = resultset.getInt("id");
          String name = resultset.getString("player_name");
          int score = resultset.getInt("score");
          String difficulty = resultset.getString("difficulty");
          DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-mm-dd HH:mm:ss");
          LocalDateTime date = LocalDateTime.parse(resultset.getString("registered_at"),
              formatter);

          player.sendMessage(id + " | " + score + " | " + difficulty  + " | " +date.format(formatter));
        }


      } catch (SQLException e) {
        throw new RuntimeException(e);
      }

      return false;
    }
    difficulty = getDifficulty(player, args);
    if (difficulty.equals(NONE)) {
      return false;
    }
    PlayerData nowPlayer = getPlayerScore(player);
    gameTime = GAME_TIME;
    nowPlayer.setGameTime(GAME_TIME);
    nowPlayer.setScore(0);

    removePotionEffect(player);

    initPlayerStatus(player);

    gamePlay(player, nowPlayer, difficulty);

    return true;
  }

  /**
   * 難易度をコマンドから取得します。
   *
   * @param player
   * @param args
   */
  private String getDifficulty(Player player, String[] args) {
    if (args.length == 1 && (EASY.equals(args[0]) || NORMAL.equals(args[0]) || HARD.equals(
        args[0]))) {
      return args[0];
    } else {
      player.sendMessage("実行できません。コマンド引数の1つ目に難易度の設定が必要です。");
    }
    return NONE;
  }

  @Override
  public boolean onExecuteNPCCommand(CommandSender sender) {
    return false;
  }


  /**
   * 現在、実行しているプレイヤーの情報を取得する。
   *
   * @param player 　コマンドを実行したプレイヤー
   * @return 現在、コマンドを実行しているプレイヤーの情報
   */
  private PlayerData getPlayerScore(Player player) {
    if (playerDataList.isEmpty()) {
      return addNewPlayerData(player);
    } else {
      for (PlayerData playerData : playerDataList) {
        if (!playerData.getPlayerName().equals(player.getName())) {
          return addNewPlayerData(player);
        } else {
          return playerData;
        }
      }
    }
    return null;
  }

  private PlayerData addNewPlayerData(Player player) {
    PlayerData newPlayer = new PlayerData();
    //プレイヤーの名前を取得
    newPlayer.setPlayerName(player.getName());
    //プレイヤー情報をリストに追加
    playerDataList.add(newPlayer);
    return newPlayer;
  }


  @EventHandler
  public void onEnemyDeath(EntityDeathEvent e) {
    LivingEntity enemy = e.getEntity();
    Player player = e.getEntity().getKiller();
    int point = 0;

    boolean isSpawnEnemy = spawnEntityList.stream()
        .anyMatch(entity -> entity.equals(enemy));

    //Objects.isNull(player)→キラーがいなかった場合
    //playerScoreList.isEmpty()→コマンドを誰も実行していなかった場合
    if (Objects.isNull(player) || !isSpawnEnemy) {
      return;
    }

    for (PlayerData playerData : playerDataList) {
      if (playerData.getPlayerName().equals(player.getName())) {

        switch (enemy.getType()) {
          case ZOMBIE -> point = 50;
          case SHEEP -> point = 20;
          case CHICKEN -> point = 10;
        }

        playerData.setScore(playerData.getScore() + point);
        player.sendMessage("敵を倒した！　現在のスコアは" + playerData.getScore() + "点！");
      }
    }
  }

  /**
   * プレイヤーの初期状態、装備を設定する
   *
   * @param player コマンド実行者
   */
  private void initPlayerStatus(Player player) {
    //プレイヤーの空腹度、レベルをセット(初期化)する
    player.setFoodLevel(20);
    player.setLevel(50);

    //プレイヤーのインベントリーを取得する。
    PlayerInventory inventory = player.getInventory();

    //武器情報を取得する
    ItemStack helmet = new ItemStack(Material.NETHERITE_HELMET);
    ItemStack chestPlate = new ItemStack(Material.NETHERITE_CHESTPLATE);
    ItemStack leggings = new ItemStack(Material.NETHERITE_LEGGINGS);
    ItemStack boots = new ItemStack(Material.NETHERITE_BOOTS);
    ItemStack mainHand = new ItemStack(Material.NETHERITE_SWORD);

    //プレイヤー自身に武器をセットする
    inventory.setHelmet(helmet);
    inventory.setChestplate(chestPlate);
    inventory.setLeggings(leggings);
    inventory.setBoots(boots);
    inventory.setItemInMainHand(mainHand);
  }


  /**
   * ランダムで敵を抽選して、その結果の敵を返します。
   *
   * @param difficulty 難易度
   * @return　ランダムの選択された敵
   */
  private EntityType getEnemy(String difficulty) {
    List<EntityType> randomEnemy = switch (difficulty) {
      case NORMAL -> List.of(EntityType.ZOMBIE, EntityType.ZOMBIE_HORSE, EntityType.CHICKEN);
      case HARD -> List.of(EntityType.ZOMBIE, EntityType.ZOMBIE_HORSE);
      default -> List.of(EntityType.SHEEP, EntityType.CHICKEN);
    };

    int random = new SplittableRandom().nextInt(randomEnemy.size());
    return randomEnemy.get(random);
  }

  /**
   * 敵の出現エリアを取得します。 出現エリアはX、Z軸はランダムで、-10～-の値が設定されています。 Y軸はプレイヤーと同じ位置になります。
   *
   * @param player 　コマンドを実行したプレイヤー
   * @return　敵の出現場所
   */
  private Location getLocation(Player player) {
    Location playerLocation = player.getLocation();
    int randomX = new SplittableRandom().nextInt(20) - 10;
    int randomZ = new SplittableRandom().nextInt(20) - 10;

    double x = playerLocation.getX() + randomX;
    double y = playerLocation.getY();
    double z = playerLocation.getZ() + randomZ;

    System.out.println("ランダム変数の値（X,Y)は" + randomX + " " + randomZ);
    return new Location(player.getWorld(), x, y, z);
  }

  /**
   * ゲームを実行します。想定の時間内に敵を倒すとスコアが追加されます。
   *
   * @param player        　コマンドを実行したプレイヤー
   * @param nowPlayerData 　プレイヤーのデータについて
   */
  private void gamePlay(Player player, PlayerData nowPlayerData, String difficulty) {
    Bukkit.getScheduler().runTaskTimer(main, Runnable -> {

      if (gameTime <= 0) {
        Runnable.cancel();
        player.sendTitle("ゲームが終了しました！", nowPlayerData.getPlayerName() +
            " " + nowPlayerData.getScore() + "点！", 0, 30, 0);

        for (Entity enemy : spawnEntityList) {
          enemy.remove();
        }
        spawnEntityList.clear();
        removePotionEffect(player);
        return;
      }

      //敵が出現する場所を取得する
      Location enemySpawnLocation = getLocation(player);

      //ランダムの敵を出現させる
      EntityType spawnEnemy = getEnemy(difficulty);
      Entity spawnEntity = player.getWorld().spawnEntity(enemySpawnLocation, spawnEnemy);
      spawnEntityList.add(spawnEntity);
      String enemyName = spawnEnemy.name();
      System.out.println(enemyName + "が出現した");
      gameTime -= 5;

    }, 0, 5 * 20);
  }

  private static void removePotionEffect(Player player) {
    for (PotionEffect effect : player.getActivePotionEffects()) {
      player.removePotionEffect(effect.getType());
    }
  }
}
