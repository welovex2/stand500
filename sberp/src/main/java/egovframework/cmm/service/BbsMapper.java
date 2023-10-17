package egovframework.cmm.service;

import java.util.List;
import egovframework.rte.psl.dataaccess.mapper.Mapper;

@Mapper("BbsMapper")
public interface BbsMapper {

  public void deleteBoardArticle(BoardVO board) throws Exception;

  public void deleteGuestList(BoardVO boardVO) throws Exception;

  public String getPasswordInf(BoardVO board) throws Exception;

  public int selectMaxNttId(String bbsId)  throws Exception;
  
  public boolean insertBoardArticle(BoardVO board) throws Exception;

  // public List<SmsVO> selectSmsList() throws Exception;

  public void updateInqireCo(BoardVO boardVO) throws Exception;
  
  public BoardVO selectBoardArticle(BoardVO boardVO) throws Exception;

  public List<BoardVO> selectBoardMovePN(BoardVO boardVO) throws Exception;

  public List<BoardVO> selectBoardArticleList(ComParam param) throws Exception;
  
  public int selectBoardArticleListCnt(ComParam param) throws Exception;

  public List<BoardVO> selectGuestList(ComParam param) throws Exception;

  public boolean updateBoardArticle(BoardVO board) throws Exception;

}
