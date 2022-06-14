/*
 * StaffMapper.java
 * Copyright © 2016-2020 Hundsun Technologies Inc.
 * All right reserved.
 * 银行企业金融交易银行产品部 交易银行服务平台
 */
package cloud.yiwenup.sample.dao.inter;

import cloud.yiwenup.sample.dao.entity.Staff;
import cloud.yiwenup.sample.dao.entity.StaffExample;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

import java.util.List;

/**
 * @Title staff表的Mapper类
 * @Author tbsp
 * @Date 2022-06-13 13:34:45
 * @Description 
 * <p>
 * 这是工具生成代码，禁止手工修改
 */
@Mapper
public interface StaffMapper {
    /**
     * 根据指定的条件获取数据库记录数:staff
     *
     * @param example Example对象
     * @return long 符合条件的记录数
     */
    long countByExample(StaffExample example);

    /**
     * 根据指定的条件删除数据库符合条件的记录:staff
     *
     * @param example Example对象
     * @return int 删除影响的记录数
     */
    int deleteByExample(StaffExample example);

    /**
     * 根据主键删除数据库的记录:staff
     *
     * @param id 
     * @return int 删除影响的记录数
     */
    int deleteByPrimaryKey(Long id);

    /**
     * 新写入数据库记录:staff
     *
     * @param record 实体对象
     * @return int 写入影响的记录数
     */
    int insert(Staff record);

    /**
     * 动态字段,写入数据库记录:staff
     *
     * @param record 实体对象
     * @return int 写入影响的记录数
     */
    int insertSelective(Staff record);

    /**
     * 批量写入数据库记录:staff
     *
     * @param records 记录集
     * @return int 写入影响的记录数
     */
    int insertBatch(@Param("records") List<Staff> records);

    /**
     * 根据指定的条件查询符合条件的数据库记录:staff
     *
     * @param example Example对象
     * @return List<Staff> 符合条件的记录
     */
    List<Staff> selectByExample(StaffExample example);

    /**
     * 根据指定的条件分页查询符合条件的数据库记录:staff
     *
     * @param example   Example对象
     * @param rowBounds 分页对象
     * @return Page<Staff> 符合条件的记录
     */
    Page<Staff> selectByExampleWithPage(@Param("example") StaffExample example, @Param("rowBounds") RowBounds rowBounds);

    /**
     * 根据指定主键获取一条数据库记录:staff
     *
     * @param id 
     * @return Staffstaff 实体对象
     */
    Staff selectByPrimaryKey(Long id);

    /**
     * 动态根据指定的条件来更新符合条件的数据库记录:staff
     *
     * @param record  实体对象
     * @param example Example对象
     * @return int 更新影响的记录数
     */
    int updateByExampleSelective(@Param("record") Staff record, @Param("example") StaffExample example);

    /**
     * 根据指定的条件来更新符合条件的数据库记录:staff
     *
     * @param record  实体对象
     * @param example Example对象
     * @return int 更新影响的记录数
     */
    int updateByExample(@Param("record") Staff record, @Param("example") StaffExample example);

    /**
     * 动态字段,根据主键来更新符合条件的数据库记录:staff
     *
     * @param record 实体对象
     * @return int 更新影响的记录数
     */
    int updateByPrimaryKeySelective(Staff record);

    /**
     * 根据主键来更新符合条件的数据库记录:staff
     *
     * @param record 实体对象
     * @return int 更新影响的记录数
     */
    int updateByPrimaryKey(Staff record);
}