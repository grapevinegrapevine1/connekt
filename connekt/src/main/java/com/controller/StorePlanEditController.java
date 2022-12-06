package com.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.base.StoreEditController;
import com.model.Plan;
import com.service.PlanService;
import com.service.User_planService;
import com.util.CommonUtil;
import com.util.MailUtil;
import com.util.NewsUtil;
import com.util.StripeUtil;

import jp.fintan.keel.spring.web.token.transaction.TransactionTokenCheck;
import jp.fintan.keel.spring.web.token.transaction.TransactionTokenType;

@Controller
@TransactionTokenCheck("StorePlanEditController")
public class StorePlanEditController extends BaseStoreController{

	@Autowired private PlanService planService;
	@Autowired private User_planService user_planService;
	@Autowired private StripeUtil stripeUtil;
	@Autowired private MailUtil mailUtil;
	@Autowired private NewsUtil newsUtil;

	@TransactionTokenCheck(type = TransactionTokenType.BEGIN)
	@RequestMapping("/store_plan_edit")
	public String disp(Model model, HttpServletRequest req, HttpSession ses) throws Exception {

		// セッション店舗チェック
		String sesErrFoward = CommonUtil.isSesStore(ses);
		if(sesErrFoward != null) return sesErrFoward;
		
		return new StoreEditController("plan", stripeUtil, mailUtil, newsUtil,planService, user_planService).disp(model, req, ses, true);
	}

	@TransactionTokenCheck
	@Transactional
	@PostMapping("/save_plan")
	public String update(@ModelAttribute @Validated Plan plan, BindingResult bindingResult, Model model, HttpServletRequest req, HttpSession ses) throws Exception {

		// セッション店舗チェック
		String sesErrFoward = CommonUtil.isSesStore(ses);
		if(sesErrFoward != null) return sesErrFoward;
		
		return new StoreEditController("plan", stripeUtil, mailUtil, newsUtil, planService, user_planService).update(plan, bindingResult, model, req, ses, true);
	}

	@TransactionTokenCheck
	@Transactional
	@PostMapping("/delete_plan")
	public String delete(@ModelAttribute @Validated Plan plan, BindingResult bindingResult, Model model, HttpServletRequest req, HttpSession ses) throws Exception {

		// セッション店舗チェック
		String sesErrFoward = CommonUtil.isSesStore(ses);
		if(sesErrFoward != null) return sesErrFoward;
		
		return new StoreEditController("plan", stripeUtil, mailUtil, newsUtil, planService, user_planService).delete(plan, bindingResult, model, req, ses, true);
	}
}
