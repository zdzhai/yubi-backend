package com.zzd.yubi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzd.yubi.mapper.ChartMapper;
import com.zzd.yubi.model.entity.Chart;
import com.zzd.yubi.service.ChartService;
import org.springframework.stereotype.Service;

/**
* @author 62618
* @description 针对表【chart(图表信息表)】的数据库操作Service实现
* @createDate 2024-04-22 20:52:05
*/
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
    implements ChartService {

}
