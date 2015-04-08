package com.vs.model;

/**
 * Created by longjianlin on 15/3/26.
 */
public class Organization {
    public String medicalName;          //机构名称
    public String medicalRegInfoId;     //机构id

    @Override
    public String toString() {
        return medicalName;
    }
}
