package com.wxp.web;

import com.wxp.NotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @program: prettyblog
 * @description:
 * @author: wangxp
 * @create: 2019-11-02 13:52
 */
@Controller
public class IndexController {

    @GetMapping("/")
    public String index(){
        System.out.println("-------------------Index---------------------");
        return "index";

    }


    @GetMapping("/blog")
    public String blog(){
        System.out.println("-------------------Index---------------------");
        return "blog";

    }
}
