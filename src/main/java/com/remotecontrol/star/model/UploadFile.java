package com.remotecontrol.star.model;

import lombok.Data;

import java.util.List;

@Data
public class UploadFile {

    private String name;

    private List<Integer> bytes;

}
