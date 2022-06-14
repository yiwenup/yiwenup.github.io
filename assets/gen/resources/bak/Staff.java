/*
 * Staff.java
 * Copyright © 2016-2020 Hundsun Technologies Inc.
 * All right reserved.
 * 银行企业金融交易银行产品部 交易银行服务平台
 */
package cloud.yiwenup.sample.dao.entity;

/**
 * @Author tbsp
 * @Date 2022-06-13 13:34:45
 * <p>
 * 这是工具生成代码，禁止手工修改
 */
public class Staff {
    /**
     * id 
     */
    private Long id;

    /**
     * staffId 
     */
    private String staffId;

    /**
     * staffName 
     */
    private String staffName;

    /**
     * description 
     */
    private String description;

    /**
     * 获取  字段:staff.id
     *
     * @return staff.id, 
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置  字段:staff.id
     *
     * @param id the value for staff.id, 
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取  字段:staff.staff_id
     *
     * @return staff.staff_id, 
     */
    public String getStaffId() {
        return staffId;
    }

    /**
     * 设置  字段:staff.staff_id
     *
     * @param staffId the value for staff.staff_id, 
     */
    public void setStaffId(String staffId) {
        this.staffId = staffId == null ? null : staffId.trim();
    }

    /**
     * 获取  字段:staff.staff_name
     *
     * @return staff.staff_name, 
     */
    public String getStaffName() {
        return staffName;
    }

    /**
     * 设置  字段:staff.staff_name
     *
     * @param staffName the value for staff.staff_name, 
     */
    public void setStaffName(String staffName) {
        this.staffName = staffName == null ? null : staffName.trim();
    }

    /**
     * 获取  字段:staff.description
     *
     * @return staff.description, 
     */
    public String getDescription() {
        return description;
    }

    /**
     * 设置  字段:staff.description
     *
     * @param description the value for staff.description, 
     */
    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }
}