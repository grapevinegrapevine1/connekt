package com.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.model.Option;
import com.model.Plan;
import com.model.Store;
import com.properties.Const;
import com.service.OptionService;
import com.service.PlanService;
import com.util.CommonUtil;

@Controller
public class StorePlanController {
	
	@Autowired private PlanService planService;
	@Autowired private OptionService optionService;
	
	@RequestMapping("/store_plan")
	public String disp(Model model, HttpServletRequest req, HttpSession ses) throws Exception {

		// セッション店舗チェック
		String sesErrFoward = CommonUtil.isSesStore(ses);
		if(sesErrFoward != null) return sesErrFoward;
		
		// セッション店舗情報
		Store store = CommonUtil.getSessionStore(ses);
		
		// プラン情報リスト
		List<Plan> plans = planService.find_list(store.getId());
		// オプション情報リスト
		List<Option> options = optionService.find_list(store.getId());
		
		// モデル
		model.addAttribute("plans", plans);
		model.addAttribute("options", options);
		
		// 遷移
		return Const.PAGE_STORE_PlAN;
	}
}
