package com.zzd.yubi.controller;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zzd.yubi.common.BaseResponse;
import com.zzd.yubi.common.ErrorCode;
import com.zzd.yubi.common.ResultUtils;
import com.zzd.yubi.exception.BusinessException;
import com.zzd.yubi.exception.ThrowUtils;
import com.zzd.yubi.manager.AiManager;
import com.zzd.yubi.model.dto.chart.ChartQueryRequest;
import com.zzd.yubi.model.dto.chart.GenChartByAiRequest;
import com.zzd.yubi.model.entity.Chart;
import com.zzd.yubi.model.entity.User;
import com.zzd.yubi.model.vo.BiResponseVO;
import com.zzd.yubi.service.ChartService;
import com.zzd.yubi.service.UserService;
import com.zzd.yubi.utils.ExcelUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * 图表接口
 *
 * @author dongdong
 * @date
 */
@RestController
@RequestMapping("/chart")
@Slf4j
public class ChartController {

    @Resource
    private ChartService chartService;
    @Resource
    private UserService userService;

    @Resource
    private AiManager aiManager;


    // region 增删改查

    // endregion

    /**
     * 用户上传分析图表
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @PostMapping("/gen")
    public BaseResponse<BiResponseVO> genChartByAi(@RequestPart("file") MultipartFile multipartFile,
                                             GenChartByAiRequest genChartByAiRequest,
                                             HttpServletRequest request) {

        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();

        //校验
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "分析目标为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR, "名称过长");

        //用户必须先登录bi系统
        User loginUser = userService.getLoginUser(request);

        //校验文件大小和类型
        validFile(multipartFile);

        StringBuilder userInput = new StringBuilder();
        //userInput.append("现在你是一个数据分析师，接下来我会给你我的分析目标和原始数据，请告诉我分析结论。").append("\n");
        userInput.append("分析需求：").append("\n");

        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += "，请使用" + chartType;
        }
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");
        //用户上传的压缩后的数据
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        userInput.append(csvData).append("\n");

        Long aiModelId = 1787445601903431682L;

        //拿到返回结果
        String answer = aiManager.doChat(aiModelId, userInput.toString());
        String[] split = answer.split("【【【【");

        if (split.length < 3) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成错误");
        }
        String genChart = split[1].trim();
        String genResult = split[2].trim();
        //插入到数据库
        Chart chart = new Chart();
        chart.setName(name);
        chart.setGoal(goal);
        chart.setChartData(csvData);
        chart.setChartType(chartType);
        chart.setGenChart(genChart);
        chart.setGenResult(genResult);
        chart.setUserId(loginUser.getId());
        boolean save = chartService.save(chart);
        ThrowUtils.throwIf(!save, ErrorCode.SYSTEM_ERROR, "图表保存失败");

        //返回给前端
        BiResponseVO biResponseVO = new BiResponseVO();
        biResponseVO.setGenChart(genChart);
        biResponseVO.setGenResult(genResult);
        biResponseVO.setChartId(chart.getId());
        return ResultUtils.success(biResponseVO);
    }

    /**
     * 校验文件
     *
     * @param multipartFile
     */
    private void validFile(MultipartFile multipartFile) {
        // 文件大小
        long fileSize = multipartFile.getSize();
        // 文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        final long ONE_M = 1024 * 1024L;
        if (fileSize > ONE_M) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过 1M");
        }
        if (!Arrays.asList("xls", "xlsx").contains(fileSuffix)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
        }
    }

    private Wrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest) {
        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>();

        return null;
    }

}
