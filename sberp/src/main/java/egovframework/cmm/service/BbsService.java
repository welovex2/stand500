package egovframework.cmm.service;

import java.util.List;
import java.util.Map;

public interface BbsService {

  public void deleteBoardArticle(BoardVO Board) throws Exception;

  public void deleteGuestList(BoardVO boardVO) throws Exception;

  public String getPasswordInf(BoardVO Board) throws Exception;

  public boolean insertBoardArticle(BoardVO Board) throws Exception;

  // public List<SmsVO> selectSmsList() throws Exception;

  public BoardVO selectBoardArticle(BoardVO boardVO) throws Exception;

  public List<BoardVO> selectBoardMovePN(BoardVO boardVO) throws Exception;

  public List<BoardVO> selectBoardArticleList(ComParam param) throws Exception;
  
  public int selectBoardArticleListCnt(ComParam param) throws Exception;

  public List<BoardVO> selectGuestList(ComParam param) throws Exception;

  public boolean updateBoardArticle(BoardVO Board) throws Exception;
}
