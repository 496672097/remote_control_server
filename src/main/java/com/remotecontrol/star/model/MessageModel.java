package com.remotecontrol.star.model;

import lombok.Data;

import java.sql.Timestamp;

/**
 * @author ：复目
 * @date ：Created in 2022/1/9 11:19
 */

@Data
public class MessageModel {

    private String username;

    private String content;

    private Timestamp sendTime;

}
