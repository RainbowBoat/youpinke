package com.pinyougou.service;

import java.util.List;

public interface LickService {

    void beALicker(String lickerId, String lickedId);

    List<String> findLickers(String userId);

    String findMyLicker(String userId);

    void acceptLicker(String lickerId, String lickedId);

    void refuseLicker(String lickerId, String lickedId);

    List<String> findLickeds(String lickerId);

    void brokeUp(String lickerId, String lickedId);

    List<String> getLickedMsg(String lickedId);

    List<String> getLickerMsg(String lickerId);
}
