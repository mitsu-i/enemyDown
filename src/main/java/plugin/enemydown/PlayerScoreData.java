package plugin.enemydown;

import java.io.InputStream;
import java.util.List;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import plugin.enemydown.mapper.PlayerScoreMapper;
import plugin.enemydown.mapper.data.PlayerScore;

/**
 *
 */
public class PlayerScoreData {

  private SqlSessionFactory sqlSessionFactory;
  private PlayerScoreMapper mapper;


  public PlayerScoreData() {
    try {
      InputStream inputStream = Resources.getResourceAsStream("mybatis-config.xml");
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
      SqlSession session = sqlSessionFactory.openSession(true);
       this.mapper = session.getMapper(PlayerScoreMapper.class);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * player_scoreテーブルから情報を取得する
   * @return
   */
  public List<PlayerScore> selectList() {
      return mapper.selectList();
  }

  /**
   * プレイヤースコアテーブルにスコア情報を登録する
   * @param playerScore
   */
  public void insert(PlayerScore playerScore){
          mapper.insert(playerScore);
  }

}
