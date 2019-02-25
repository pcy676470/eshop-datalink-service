package com.roncoo.eshop.datalink.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.roncoo.eshop.datalink.service.EshopProductService;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@RestController
public class DataLinkController {

	@Autowired
	private EshopProductService eshopProductService;
	
	@Autowired
	private JedisPool jedisPool; 
	
	@RequestMapping("/product")
	@ResponseBody
	public String getProduct(Long productId){
		
		// 读redis主集群
		Jedis jedis = jedisPool.getResource();
		String dimProductJSON = jedis.get("dim_"+productId);
		
		if(dimProductJSON == null ||"".equals(dimProductJSON)){//主集群没有数据
			String productDataJson = eshopProductService.findProductById(productId);
			
			if(productDataJson != null || !"".equals(productDataJson)){//数据库有数据
				JSONObject productDataJSONObject = JSONObject.parseObject(productDataJson);
				
				String productPropertyJSONDataJson = eshopProductService.findProductPropertyById(productId);
				if(productPropertyJSONDataJson != null && !"".equals(productPropertyJSONDataJson)){
					productDataJSONObject.put("product_property", JSONObject.parse(productPropertyJSONDataJson));
				}
				
				String productSpecificationDataJSON = eshopProductService.findProductSpecificationById(productId);
				if(productSpecificationDataJSON != null && !"".equals(productSpecificationDataJSON)){
					productDataJSONObject.put("product_specification", JSONObject.parse(productSpecificationDataJSON));
				}
				
				jedis.set("dim_product_" + productId, productDataJSONObject.toJSONString());
				
				return productDataJSONObject.toJSONString();
				
			}
			
		}
		return "";
		
	}
	
}
