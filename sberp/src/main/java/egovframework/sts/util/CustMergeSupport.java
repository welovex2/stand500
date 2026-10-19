package egovframework.sts.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import egovframework.sts.dto.CustLadderDTO.BaseRow;

/**
 * 같은 회사가 CMPY_TB 에 여러 줄로 등록된 것을 하나로 합친다.
 *
 * <p>합치는 근거는 둘이다. 사업자등록번호가 같으면 같은 회사로 본다. 사업자번호가 없거나 형식이 아닌 줄은 상호를 정리해서
 * 비교한다. 상호는 근거가 약해서, 같은 상호 안에 서로 다른 사업자번호가 둘 이상 섞여 있으면 그 상호는 합치지 않는다.
 * ("바이오테크", "씨텍" 처럼 흔한 이름이 실제로 다른 회사인 경우가 있다.)
 */
public final class CustMergeSupport {

  private CustMergeSupport() {}

  private static final Pattern BSNS = Pattern.compile("^[0-9]{3}-?[0-9]{2}-?[0-9]{5}$");

  private static final Pattern TRIM_CHARS = Pattern.compile("[\\s.,\\-()\\[\\]&_/]");

  /** 상호에서 떼어낼 조각. 회사 형태를 나타내는 말은 같은 회사를 다르게 보이게 만든다. */
  private static final String[] DROP = {"주식회사", "(주)", "㈜", "(유)", "유한회사", "(재)", "(사)",
      "co.,ltd", "co.ltd", "co., ltd", "company", "corporation", "corp", "ltd", "inc"};

  /** 사업자등록번호를 하이픈 없는 10자리로. 형식이 아니면 null. */
  public static String bsnsKey(String raw) {
    if (raw == null) {
      return null;
    }
    String t = raw.trim();
    if (!BSNS.matcher(t).matches()) {
      return null;
    }
    return t.replace("-", "");
  }

  /** 상호 비교용 키. 회사 형태·공백·기호를 떼고 소문자로. 비교할 게 없으면 빈 문자열. */
  public static String normName(String raw) {
    if (raw == null) {
      return "";
    }
    String s = raw.trim().toLowerCase();
    for (String d : DROP) {
      s = s.replace(d, "");
    }
    return TRIM_CHARS.matcher(s).replaceAll("");
  }

  /**
   * 업체 줄을 같은 회사끼리 묶는다. 반환 순서는 넣은 순서를 따르고, 묶음 안의 순서도 넣은 순서를 따른다.
   */
  public static List<List<BaseRow>> merge(List<BaseRow> rows) {
    List<List<BaseRow>> groups = new ArrayList<List<BaseRow>>();
    if (rows == null || rows.isEmpty()) {
      return groups;
    }

    // 상호 키별로 사업자번호가 몇 종류 섞여 있는지 먼저 센다. 둘 이상이면 그 상호는 합치지 않는다.
    Map<String, Set<String>> bsnsByName = new HashMap<String, Set<String>>();
    for (BaseRow r : rows) {
      String nk = normName(r.getCmpyName());
      if (nk.isEmpty()) {
        continue;
      }
      Set<String> set = bsnsByName.get(nk);
      if (set == null) {
        set = new HashSet<String>();
        bsnsByName.put(nk, set);
      }
      String bk = bsnsKey(r.getBsnsRgnmb());
      if (bk != null) {
        set.add(bk);
      }
    }

    Map<Integer, Integer> parent = new HashMap<Integer, Integer>();
    for (BaseRow r : rows) {
      parent.put(Integer.valueOf(r.getCmpySeq()), Integer.valueOf(r.getCmpySeq()));
    }

    // 사업자번호가 같으면 합친다.
    Map<String, Integer> firstByBsns = new HashMap<String, Integer>();
    for (BaseRow r : rows) {
      String bk = bsnsKey(r.getBsnsRgnmb());
      if (bk == null) {
        continue;
      }
      Integer first = firstByBsns.get(bk);
      if (first == null) {
        firstByBsns.put(bk, Integer.valueOf(r.getCmpySeq()));
      } else {
        union(parent, first.intValue(), r.getCmpySeq());
      }
    }

    // 상호가 같고 사업자번호가 충돌하지 않으면 합친다.
    Map<String, Integer> firstByName = new HashMap<String, Integer>();
    for (BaseRow r : rows) {
      String nk = normName(r.getCmpyName());
      if (nk.isEmpty()) {
        continue;
      }
      Set<String> mixed = bsnsByName.get(nk);
      if (mixed != null && mixed.size() > 1) {
        continue;
      }
      Integer first = firstByName.get(nk);
      if (first == null) {
        firstByName.put(nk, Integer.valueOf(r.getCmpySeq()));
      } else {
        union(parent, first.intValue(), r.getCmpySeq());
      }
    }

    Map<Integer, List<BaseRow>> byRoot = new LinkedHashMap<Integer, List<BaseRow>>();
    for (BaseRow r : rows) {
      Integer root = Integer.valueOf(find(parent, r.getCmpySeq()));
      List<BaseRow> g = byRoot.get(root);
      if (g == null) {
        g = new ArrayList<BaseRow>();
        byRoot.put(root, g);
      }
      g.add(r);
    }
    groups.addAll(byRoot.values());
    return groups;
  }

  static int find(Map<Integer, Integer> parent, int x) {
    Integer key = Integer.valueOf(x);
    Integer up = parent.get(key);
    if (up == null) {
      parent.put(key, key);
      return x;
    }
    int cur = x;
    while (parent.get(Integer.valueOf(cur)).intValue() != cur) {
      int next = parent.get(Integer.valueOf(cur)).intValue();
      parent.put(Integer.valueOf(cur), parent.get(Integer.valueOf(next)));
      cur = next;
    }
    return cur;
  }

  static void union(Map<Integer, Integer> parent, int a, int b) {
    int ra = find(parent, a);
    int rb = find(parent, b);
    if (ra != rb) {
      parent.put(Integer.valueOf(ra), Integer.valueOf(rb));
    }
  }
}
