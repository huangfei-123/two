package com.offcn.sellergoods.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.offcn.sellergoods.pojo.Brand;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/****
 * @Author:ujiuye
 * @Description:Brand的Dao
 * @Date 2021/2/1 14:19
 *****/
public interface BrandMapper extends BaseMapper<Brand> {
    /**BaseMapper提供的方法我不满意，可以书写自己的方法，然后使用接口对象brandMapper直接调用
     * @return
     */
    @Select("select id,name as text from tb_brand")
    public List<Map> selectOptions();
}
