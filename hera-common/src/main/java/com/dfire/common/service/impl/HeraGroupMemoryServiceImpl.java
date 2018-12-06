package com.dfire.common.service.impl;

import com.dfire.common.entity.HeraGroup;
import com.dfire.common.entity.Judge;
import com.dfire.logs.HeraLog;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xiaosuda
 * @date 2018/12/5
 */
@Service("heraGroupMemoryService")
public class HeraGroupMemoryServiceImpl extends HeraGroupServiceImpl {

    private volatile Judge judge;

    private final Object lock = new Object();

    private Map<Integer, HeraGroup> memoryJob;

    private Map<Integer, HeraGroup> getMemoryJob() {
        Judge newJudge = heraGroupMapper.selectTableInfo();
        if (judge == null || !newJudge.getCount().equals(judge.getCount()) || !newJudge.getLastModified().equals(judge.getLastModified()) || !newJudge.getMaxId().equals(judge.getMaxId())) {
            synchronized (lock) {
                if (judge == null || !newJudge.getCount().equals(judge.getCount()) || !newJudge.getLastModified().equals(judge.getLastModified()) || !newJudge.getMaxId().equals(judge.getMaxId())) {
                    HeraLog.info("刷新hera_group库");
                    judge = newJudge;
                    List<HeraGroup> all = heraGroupMapper.getAll();
                    Map<Integer, HeraGroup> jobMap = new HashMap<>(all.size());
                    all.forEach(group -> jobMap.put(group.getId(), group));
                    memoryJob = jobMap;
                }
            }
        }
        judge.setStamp(new Date());
        return memoryJob;
    }

    @Override
    public List<HeraGroup> getAll() {
        return new ArrayList<>(getMemoryJob().values());
    }

    @Override
    public HeraGroup findById(Integer id) {
        return getMemoryJob().get(id);
    }

    @Override
    public List<HeraGroup> findByParent(Integer parentId) {
        return getMemoryJob().values().stream().filter(group -> group.getParent().equals(parentId)).collect(Collectors.toList());
    }

    @Override
    public List<HeraGroup> findByIds(List<Integer> list) {
        return getMemoryJob().values().stream().filter(group -> list.contains(group.getId())).collect(Collectors.toList());
    }

    @Override
    public HeraGroup findConfigById(Integer id) {
        return getMemoryJob().get(id);
    }
}
