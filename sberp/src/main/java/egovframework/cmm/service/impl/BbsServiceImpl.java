package egovframework.cmm.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import egovframework.cmm.service.BbsMapper;
import egovframework.cmm.service.BbsService;
import egovframework.cmm.service.BoardVO;
import egovframework.cmm.service.ComParam;

@Service("BbsService")
public class BbsServiceImpl implements BbsService {

  @Autowired
  BbsMapper bbsMapper;
  
  @Override
  public void deleteBoardArticle(BoardVO board) throws Exception {
    bbsMapper.deleteBoardArticle(board);
  }

  @Override
  public void deleteGuestList(BoardVO boardVO) throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  public String getPasswordInf(BoardVO board) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  @Transactional
  public boolean insertBoardArticle(BoardVO board) throws Exception {
    // SORT_ORDR는 부모글의 소트 오더와 같게, NTT_NO는 순서대로 부여
    
    int nttId = bbsMapper.selectMaxNttId(board.getBbsId());
    board.setNttId(nttId);
    
    return bbsMapper.insertBoardArticle(board);
  }

  @Override
  @Transactional
  public BoardVO selectBoardArticle(BoardVO boardVO) throws Exception {
    bbsMapper.updateInqireCo(boardVO);
    BoardVO result = bbsMapper.selectBoardArticle(boardVO);
    return result;
  }

  @Override
  public List<BoardVO> selectBoardMovePN(BoardVO boardVO) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean updateBoardArticle(BoardVO board) throws Exception {
    return bbsMapper.updateBoardArticle(board);    
  }

  @Override
  public List<BoardVO> selectBoardArticleList(ComParam param) throws Exception {
    return bbsMapper.selectBoardArticleList(param);
  }

  @Override
  public int selectBoardArticleListCnt(ComParam param) throws Exception {
    return bbsMapper.selectBoardArticleListCnt(param);
  }

  @Override
  public List<BoardVO> selectGuestList(ComParam param) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

}
