package com.ai.cloud.skywalking.web.service.impl;

import com.ai.cloud.skywalking.web.dao.inter.ICallChainTreeDao;
import com.ai.cloud.skywalking.web.dto.CallChainTree;
import com.ai.cloud.skywalking.web.dto.CallChainTreeNode;
import com.ai.cloud.skywalking.web.service.inter.IAnalysisResultService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by xin on 16-4-25.
 */
@Service
public class AnalysisResultService implements IAnalysisResultService {

    private Logger logger = LogManager.getLogger(AnalysisResultService.class);

    @Autowired
    private ICallChainTreeDao callChainTreeDao;

    @Override
    public CallChainTree fetchAnalysisResult(String treeId, String analyType, String analyDate) throws ParseException, IOException {
        String tableName = null;
        String rowKey = null;
        String loadKey = null;
        if ("MONTH".equals(analyType)) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
            Date date = format.parse(analyDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            rowKey = treeId + "/" + calendar.get(Calendar.YEAR);
            loadKey = (calendar.get(Calendar.MONTH) + 1) + "";
            tableName = "sw-chain-1month-summary";
        } else if ("DAY".equals(analyType)) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            Date date = format.parse(analyDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            rowKey = treeId + "/" + calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1);
            loadKey = calendar.get(Calendar.DAY_OF_MONTH) + "";
            tableName = "sw-chain-1day-summary";
        } else if ("HOUR".equals(analyType)) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd:HH");
            Date date = format.parse(analyDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            rowKey = treeId + "/" + calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1)
                    + "-" + calendar.get(Calendar.DAY_OF_MONTH);
            loadKey = calendar.get(Calendar.HOUR) + "";
            tableName = "sw-chain-1hour-summary";
        }

        if (tableName == null || rowKey == null) {
            throw new RuntimeException("tableName or analyType cannot be find.");
        }

        logger.info("fetchAnalysisResult: tableName :{}, rowKey : {}, loadKey:{}", tableName, rowKey, loadKey);
        CallChainTree callChainTree = callChainTreeDao.queryAnalysisCallTree(tableName, rowKey, loadKey);
        if (callChainTree != null) {
            callChainTree.setTreeId(treeId);
            Collections.sort(callChainTree.getCallChainTreeNodeList(), new Comparator<CallChainTreeNode>() {
                @Override
                public int compare(CallChainTreeNode o1, CallChainTreeNode o2) {
                    return o1.getTraceLevelId().compareTo(o2.getTraceLevelId());
                }
            });
            callChainTree.beautifulViewPointForShow();
        }

        return callChainTree;
    }
}