package com.pinyougou.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.mapper.LickMapper;
import com.pinyougou.service.LickService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service(interfaceName = "com.pinyougou.service.LickService")
@Transactional
public class LickServiceImpl implements LickService {
    /**
     * lickerApplications 舔狗申请 redisTemplate.boundHashOps("lickerApplications")
     * theLickerId 已经确定的舔狗 redisTemplate.boundHashOps("theLickerId")
     * lickedList 舔狗舔的对象的集合 (List<String>) redisTemplate.boundHashOps("lickedList")
     */

    @Autowired
    private LickMapper lickMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void beALicker(String lickerId, String lickedId) {

        try {

            List<String> lickerApplications = (List<String>) redisTemplate.boundHashOps("lickerApplications").get(lickedId);

            if (lickerApplications != null && lickerApplications.size() > 0) {
                lickerApplications.add(lickerId);
            } else {
                lickerApplications = new ArrayList<>();
                lickerApplications.add(lickerId);
            }

            // 先将舔狗申请保存到被申请对象的Redis中
            redisTemplate.boundHashOps("lickerApplications").put(lickedId, lickerApplications);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public List<String> findLickers(String userId) {
        try {

            List<String> lickerApplications = (List<String>) redisTemplate.boundHashOps("lickerApplications").get(userId);

            return lickerApplications != null ? lickerApplications : new ArrayList<>();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String findMyLicker(String userId) {
        try {

            return (String) redisTemplate.boundHashOps("theLickerId").get(userId);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 接受这个舔狗
    @Override
    public void acceptLicker(String lickerId, String lickedId) {
        // 将当前用户的舔狗设为这个, 然后将当前用户加入这个舔狗的列表, 然后将当前用户的舔狗申请集合中删除掉这个舔狗
        try {

            // 将当前用户的舔狗设为这个
            redisTemplate.boundHashOps("theLickerId").put(lickedId, lickerId);

            // 将当前用户加入这个舔狗的列表
            List<String> lickedList = (List<String>) redisTemplate.boundHashOps("lickedList").get(lickerId);
            if (lickedList != null && lickedList.size() > 0) {
                lickedList.add(lickedId);
            } else {
                lickedList = new ArrayList<>();
                lickedList.add(lickedId);
            }
            redisTemplate.boundHashOps("lickedList").put(lickerId, lickedList);

            // 将当前用户的申请列表中该舔狗删除
            deleteLickerFromApplyList(lickerId, lickedId);

            // 将两个用户的舔狗关系持久化到数据库
            lickMapper.save(lickerId, lickedId);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 拒绝这个舔狗
    @Override
    public void refuseLicker(String lickerId, String lickedId) {
        deleteLickerFromApplyList(lickerId, lickedId);
    }

    // 找到当前用户舔的对象集合
    @Override
    public List<String> findLickeds(String lickerId) {
        return (List<String>) redisTemplate.boundHashOps("lickedList").get(lickerId);
    }

    @Override
    public void brokeUp(String lickerId, String lickedId) {
        // 把licked的licker删除
        redisTemplate.boundHashOps("theLickerId").delete(lickedId);

        // 把licker集合中的licked删除
        List<String> lickedList = (List<String>) redisTemplate.boundHashOps("lickedList").get(lickerId);
        lickedList.remove(lickedId);
        if (lickedList.size() == 0) {
            redisTemplate.boundHashOps("lickedList").delete(lickerId);
        } else {
            redisTemplate.boundHashOps("lickedList").put(lickerId, lickedList);
        }

        // 删除数据库的信息
        lickMapper.brokeUp(lickedId);
    }

    private void deleteLickerFromApplyList(String lickerId, String lickedId) {
        // 将该用户舔狗申请列表中的该舔狗删除
        List<String> lickerApplications = (List<String>) redisTemplate.boundHashOps("lickerApplications").get(lickedId);
        if (lickerApplications != null && lickerApplications.size() > 0) {
            lickerApplications.remove(lickerId);
        }
        // 如果这个舔狗删除后, 当前用户没有舔狗申请了就把redis中的申请集合删除
        if (lickerApplications.size() == 0) {
            redisTemplate.boundHashOps("lickerApplications").delete(lickedId);
        } else {
            redisTemplate.boundHashOps("lickerApplications").put(lickedId, lickerApplications);
        }
    }
}
