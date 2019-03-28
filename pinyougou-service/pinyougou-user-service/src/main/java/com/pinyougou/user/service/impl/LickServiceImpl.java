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

    /**
     * 申请当舔狗
     * @param lickerId
     * @param lickedId
     */
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
            makeLickedMsg(lickedId, "你有新的舔狗申请");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据用户名找到对应的舔狗申请
     * @param userId
     * @return
     */
    @Override
    public List<String> findLickers(String userId) {
        try {

            List<String> lickerApplications = (List<String>) redisTemplate.boundHashOps("lickerApplications").get(userId);

            return lickerApplications != null ? lickerApplications : new ArrayList<>();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据用户名找到对应的舔狗
     * @param userId
     * @return
     */
    @Override
    public String findMyLicker(String userId) {
        try {

            return (String) redisTemplate.boundHashOps("theLickerId").get(userId);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 接受这个舔狗
     * @param lickerId
     * @param lickedId
     */
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

            makeLickerMsg(lickerId, "你有一个舔狗申请已被接受");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 拒绝这个舔狗
     * @param lickerId
     * @param lickedId
     */
    @Override
    public void refuseLicker(String lickerId, String lickedId) {
        deleteLickerFromApplyList(lickerId, lickedId);
        makeLickerMsg(lickerId, "你有一个舔狗申请已被拒绝");
    }

    /**
     * 根据舔狗ID找到他所有的licked
     * @param lickerId
     * @return
     */
    @Override
    public List<String> findLickeds(String lickerId) {
        return (List<String>) redisTemplate.boundHashOps("lickedList").get(lickerId);
    }

    /**
     * 删除两个人的舔狗关系
     * @param lickerId
     * @param lickedId
     */
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

        makeLickerMsg(lickerId, "恭喜你当个人了");
        makeLickedMsg(lickedId, "GG, 舔狗关系结束了");

    }

    @Override
    public List<String> getLickedMsg(String lickedId) {

        List<String> lickedMasList = (List<String>) redisTemplate.boundHashOps("lickedMsg").get(lickedId);

        redisTemplate.boundHashOps("lickedMsg").delete(lickedId);

        return lickedMasList;
    }

    @Override
    public List<String> getLickerMsg(String lickerId) {
        List<String> lickerMasList = (List<String>) redisTemplate.boundHashOps("lickedMsg").get(lickerId);

        redisTemplate.boundHashOps("lickedMsg").delete(lickerId);

        return lickerMasList;
    }

    /**
     * 删除舔狗申请
     * @param lickerId
     * @param lickedId
     */
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

    /**
     * 给舔狗留言
     * @param lickerId
     * @param msg
     */
    private void makeLickerMsg(String lickerId, String msg) {
        // 得到舔狗的消息集合
        List<String> lickerMsgList = (List<String>) redisTemplate.boundHashOps("lickerMsg").get(lickerId);
        // 判断集合有效性
        if (lickerMsgList == null || lickerMsgList.size() == 0) {
            lickerMsgList = new ArrayList<>();
        }
        lickerMsgList.add(msg);

        // 将消息存储
        redisTemplate.boundHashOps("lickerMsg").put(lickerId, lickerMsgList);
    }

    /**
     * 给licked留言
     * @param lickedId
     * @param msg
     */
    private void makeLickedMsg(String lickedId, String msg) {
        List<String> lickedMsgList = (List<String>) redisTemplate.boundHashOps("lickedMsg").get(lickedId);
        if (lickedMsgList == null || lickedMsgList.size() == 0) {
            lickedMsgList = new ArrayList<>();
        }
        lickedMsgList.add(msg);
        redisTemplate.boundHashOps("lickedMsg").put(lickedId, lickedMsgList);
    }


}
