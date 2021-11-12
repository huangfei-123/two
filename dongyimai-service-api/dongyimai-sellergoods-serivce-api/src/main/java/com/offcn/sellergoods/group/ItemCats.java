package com.offcn.sellergoods.group;

import com.offcn.sellergoods.pojo.ItemCat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.List;

@ApiModel(description = "商品上下级",value = "ItemCats")
public class ItemCats implements Serializable {
    @ApiModelProperty(value = "上下级数组",required = false)
    private List<ItemCat> itemCatList;

    public List<ItemCat> getItemCatList() {
        return itemCatList;
    }

    public void setItemCatList(List<ItemCat> itemCatList) {
        this.itemCatList = itemCatList;
    }
}
