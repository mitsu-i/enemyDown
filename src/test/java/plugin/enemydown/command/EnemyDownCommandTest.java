package plugin.enemydown.command;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import plugin.enemydown.Main;

public class EnemyDownCommandTest {

  EnemyDownCommand sut;

  @Mock
  Main main;
  Player player;

  @BeforeEach
  public void before(){
    sut = new EnemyDownCommand(main);
  }

  @Test
  void getDifficultyに渡す引数のargsの最初の文字列がeasyの時にeasyの文字列が変えること() {
    String actual = sut.getDifficulty(player, new String[]{"easy"});
    Assertions.assertEquals("easy", actual);
  }

  @Test
  void getDifficultyに渡す引数のargsの最初の文字列がnormalの時にeasyの文字列が変えること() {
    String actual = sut.getDifficulty(player, new String[]{"normal"});
    Assertions.assertEquals("normal", actual);
  }
}
