package com.remotecontrol.star.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Online {

    private String ip;

    private String os;

    private Integer cpu_core;

    private Integer memory_total;

    private Integer memory_free;

    private Integer memory_avail;

    private Long disk_total;

    private Long disk_free;

    private String hostname;

}
