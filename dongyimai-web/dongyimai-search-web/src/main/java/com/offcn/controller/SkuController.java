package com.offcn.controller;

import com.offcn.feign.SearchSkuFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
@RequestMapping(value = "/search")
public class SkuController {

    @Autowired
    private SearchSkuFeign searchSkuFeign;

    /**
     * 搜索
     * @param searchMap
     * @return
     */
    @GetMapping(value = "/list")
    public String search(@RequestParam(required = false) Map<String,String> searchMap, Model model){
        //1.调用搜索微服务的 feign  根据搜索的条件参数 查询 数据
        //调用dongyimai-search-service微服务
        Map resultMap = searchSkuFeign.search(searchMap);
        //2.将数据设置到model中     (模板文件中 根据th:标签数据展示)
        model.addAttribute("result",resultMap);
        //3.设置搜索的条件 回显
        model.addAttribute("searchMap",searchMap);
        //4.记住之前的URL
        //拼接url
        String url = this.setUrl(searchMap);
        model.addAttribute("url",url);
        return "search";
    }

    private String setUrl(Map<String, String> searchMap) {
        String url = "/search/list"; // a/b?id=1&
        if (searchMap != null) {
            url += "?";
            for (Map.Entry<String, String> stringStringEntry : searchMap.entrySet()) {
                //如果是排序 则 跳过 拼接排序的地址 因为有数据
                if(stringStringEntry.getKey().equals("sortField") || stringStringEntry.getKey().equals("sortRule")){
                    continue;
                }
                url += stringStringEntry.getKey() + "=" + stringStringEntry.getValue() + "&";

            }
            if(url.lastIndexOf("&")!=-1)
                url = url.substring(0, url.lastIndexOf("&"));
        }
        return url;
    }
}
