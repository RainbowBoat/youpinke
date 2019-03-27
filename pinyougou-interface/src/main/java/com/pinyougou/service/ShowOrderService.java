package com.pinyougou.service;

import java.util.List;
import java.util.Map;

public interface ShowOrderService {

    Map<String, Object> findOrders(String userId, Map<String, String> pageParam);
}
