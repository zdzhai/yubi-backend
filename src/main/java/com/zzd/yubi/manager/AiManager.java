package com.zzd.yubi.manager;

import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.common.BaseResponse;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;
import com.zzd.yubi.common.ErrorCode;
import com.zzd.yubi.exception.BusinessException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author dongdong
 * @Date 2024/5/6 19:55
 */
@Service
public class AiManager {

    @Resource
    private YuCongMingClient yuCongMingClient;

    public String doChat(Long modelId, String message) {
        DevChatRequest devChatRequest = new DevChatRequest();
        devChatRequest.setModelId(modelId);
        devChatRequest.setMessage(message);

        BaseResponse<DevChatResponse> response = yuCongMingClient.doChat(devChatRequest);
        if (response == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI响应错误");
        }
        return response.getData().getContent();
    }
}
