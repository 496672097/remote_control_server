package com.remotecontrol.star.model;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author ：复目
 * @date ：Created in 2022/1/8 21:20
 */

@Data
@AllArgsConstructor
public class CommandModel {

    /** 目标*/
    private String target;

    /** 回传控制者*/
    private String receiver;

    /** 操作类型*/
    private Integer c_type;

    /** 动作*/
    private Integer action;

    /** 数据*/
    private String data;


}
