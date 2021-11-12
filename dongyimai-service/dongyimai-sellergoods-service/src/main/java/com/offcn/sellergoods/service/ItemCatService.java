package com.offcn.sellergoods.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.offcn.entity.PageResult;
import com.offcn.sellergoods.group.ItemCats;
import com.offcn.sellergoods.pojo.ItemCat;

import java.util.List;
import java.util.Map;

/****
 * @Author:ujiuye
 * @Description:ItemCat业务层接口
 * @Date 2021/2/1 14:19
 *****/

public interface ItemCatService extends IService<ItemCat> {

    /***
     * ItemCat多条件分页查询
     * @param itemCat
     * @param page
     * @param size
     * @return
     */
    PageResult<ItemCat> findPage(ItemCat itemCat, int page, int size);

    /***
     * ItemCat分页查询
     * @param page
     * @param size
     * @return
     */
    PageResult<ItemCat> findPage(int page, int size);

    /***
     * ItemCat多条件搜索方法
     * @param itemCat
     * @return
     */
    List<ItemCat> findList(ItemCat itemCat);

    /***
     * 删除ItemCat
     * @param id
     */
    void delete(Long id);

    /***
     * 修改ItemCat数据
     * @param itemCat
     */
    void update(ItemCat itemCat);

    /***
     * 新增ItemCat
     * @param itemCat
     */
    void add(ItemCat itemCat);

    /**
     * 根据ID查询ItemCat
     * @param id
     * @return
     */
     ItemCat findById(Long id);

    /***
     * 查询所有ItemCat
     * @return
     */
    List<ItemCat> findAll();

    /**
     * 根据父级ID查询分类列表
     * @param parentId
     * @return
     */
    public List<ItemCat> findByParentId(Long parentId);

    /**
     * 新增商品分类，parent_id=0，商品name为电子竞技,通过name找到电子竞技商品的id，
     * 将该id作为下级商品LOL的parent_id
     * itemCats里面装的是两个上下级商品
     */
    public void addItemCat(ItemCats itemCats);
}
