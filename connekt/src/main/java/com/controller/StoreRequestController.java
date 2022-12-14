package com.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.base.User_count;
import com.base.User_relation;
import com.enm.ResultWithinDate;
import com.form.Store_readForm;
import com.form.Store_requestForm;
import com.model.Store;
import com.model.User;
import com.model.User_option;
import com.model.User_option_count;
import com.model.User_option_count_date;
import com.model.User_plan;
import com.model.User_plan_count;
import com.model.User_plan_count_date;
import com.properties.Const;
import com.repository.User_option_count_dateRepository;
import com.repository.User_plan_count_dateRepository;
import com.service.Non_app_idService;
import com.service.UserService;
import com.service.User_optionService;
import com.service.User_option_countService;
import com.service.User_option_count_dateService;
import com.service.User_planService;
import com.service.User_plan_countService;
import com.service.User_plan_count_dateService;
import com.stripe.Stripe;
import com.stripe.model.PaymentMethodCollection;
import com.util.CommonUtil;
import com.util.StripeUtil;

import jp.fintan.keel.spring.web.token.transaction.TransactionTokenCheck;
import jp.fintan.keel.spring.web.token.transaction.TransactionTokenType;

@Controller
@TransactionTokenCheck("StoreRequestController")
public class StoreRequestController extends BaseStoreController{
	
	@Autowired private UserService userService;
	@Autowired private User_planService user_planService;
	@Autowired private User_optionService user_optionService;
	@Autowired private User_plan_countService user_plan_countService;
	@Autowired private User_option_countService user_option_countService;
	@Autowired private User_plan_count_dateRepository user_plan_count_dateRepository;
	@Autowired private User_option_count_dateRepository user_option_count_dateRepository;
	@Autowired private User_plan_count_dateService user_plan_count_dateService;
	@Autowired private User_option_count_dateService user_option_count_dateService;
	@Autowired private Non_app_idService non_app_idService;
	@Autowired private StripeUtil stripeUtil;
	
	// ?????????????????????
	private static final String REQUEST_USER_ID= "request_user_id";
	
/* ????????????????????? */
	
	/**
	 * ???????????? - ??????????????????????????????
	 */
	@RequestMapping("/store_request")
	@Transactional
	public String checkDisp(@RequestParam(value="user_id",required=true) int user_id, Model model, HttpServletRequest req, HttpSession ses) throws Exception {

		// ?????????????????????????????????
		String sesErrFoward = CommonUtil.isSesStore(ses);
		if(sesErrFoward != null) return sesErrFoward;
		
		// ????????????
		User user = userService.find(user_id);
		// ?????????????????????
		stripeUtil.checkUpdatePlans(user);
		// ??????
		return Const.REDIRECT_HEADER + "/disp_store_request?user_id=" + user_id;
	}
	
	/**
	 * ????????????
	 */
	@TransactionTokenCheck(type = TransactionTokenType.BEGIN)
	@RequestMapping("/disp_store_request")
	public String disp(@RequestParam(value="user_id",required=true) int user_id, Model model, HttpServletRequest req, HttpSession ses) throws Exception {

		// ?????????????????????????????????
		String sesErrFoward = CommonUtil.isSesStore(ses);
		if(sesErrFoward != null) return sesErrFoward;
		
		// Stripe??????
		Stripe.apiKey = Const.STRIPE_API_KEY;
		
		// ???????????????????????????
		Store store = CommonUtil.getSessionStore(ses);
		
		// ????????????
		User user = userService.find(user_id);
		
		// ????????????????????????????????????
		if(user == null) {
			
			// ????????????????????????
			List<String> error_messages = new ArrayList<String>();
			// ????????????????????????
			error_messages.add("???????????????????????????????????????????????????????????????????????????ID???????????????????????????");
			// ???????????????
			req.setAttribute(Const.MSG_ERROR, error_messages);
			
			// ?????????????????????
			return new StoreReadController().disp(model, req, ses);

		// ?????????????????????????????????
		}else {
			
			// ?????????????????????
			User_plan user_plan = user_planService.contain(store.getId(), user_id);
			// ???????????????????????????
			List<User_option> user_options = user_optionService.containByUserId(store.getId(), user_id);
			
			// ?????????????????????????????????
			if(user_plan == null && 0 == user_options.size()) {
				
				// ????????????????????????
				List<String> error_messages = new ArrayList<String>();
				// ????????????????????????
				error_messages.add("????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????");
				// ???????????????
				req.setAttribute(Const.MSG_ERROR, error_messages);
	
				// ?????????????????????
				return new StoreReadController().disp(model, req, ses);
				
			// ??????????????????????????????
			}else {
				
			/* ??????????????????????????? */
				
				// ???????????????
				String planText = stripeUtil.getPlanText(user_plan, user_options);
				
				// ??????????????????
				Store_requestForm store_requestForm = new Store_requestForm();
				store_requestForm.setUser_id(user_id);
				
				// ?????????
				model.addAttribute("user_name", user.getName());
				model.addAttribute("user_plan", user_plan);
				model.addAttribute("user_options", user_options);
				model.addAttribute("store_requestForm", store_requestForm);
				// ???????????????
				req.setAttribute("contract_text", "??????????????????????????????????????????????????????\n" + planText);
				// ???????????????
				ses.setAttribute(REQUEST_USER_ID, user_id);
				
				// ??????
				return Const.PAGE_STORE_REQUEST;
			}
		}
	}
	
/* ???????????????????????? */
	
	/**
	 * ??????????????????????????????????????????????????? - ??????????????????????????????
	 */
	@Transactional
	@PostMapping("/store_request_user")
	public String checkReqNonAppUser(@ModelAttribute @Validated Store_readForm store_readForm, BindingResult bindingResult,Model model, HttpServletRequest req, HttpSession ses) throws Exception {

		// ?????????????????????????????????
		String sesErrFoward = CommonUtil.isSesStore(ses);
		if(sesErrFoward != null) return sesErrFoward;
		
		// Validation??????????????????????????????
		String validMsg = CommonUtil.isValid(bindingResult,req);
		// Validation??????????????????????????????????????????????????????
		if(validMsg != null) return Const.PAGE_STORE_VALID_ERROR;
		
		// ???????????????????????????
		Store store = CommonUtil.getSessionStore(ses);
		// ???????????????
		String name = store_readForm.getFirst_name() + store_readForm.getLast_name();
		
		// ????????????
		User user = userService.findNonAppUser(name, store_readForm.getTel(), store_readForm.getBirth());
		
		// ????????????????????????????????????
		if(user == null) {

			// ??????????????????
			user = new User();
			user.setEmail("baken_non_app_user_" + non_app_idService.createId().getId() + "@baken.sakura.ne.jp");
			user.setName(name);
			user.setPassword(Const.NON_APP_USER_PASSWORD);
			user.setTel(store_readForm.getTel());
			user.setBirth(store_readForm.getBirth());
			
			// ????????????
			String customerId = stripeUtil.createCustomer(user.getEmail(),user.getName());
			user.setStripe_customer_id(customerId);
			
			// ??????????????????
			user = userService.save(user);

			// ??????????????????????????????
			return stripeUtil.createCard_nonAppUser(req, customerId, getUserStoreUrl(name, store_readForm.getTel(), store_readForm.getBirth()) );
			
		// ?????????????????????????????????
		}else {
			
			// ??????ID
			String stripe_customer_id = stripeUtil.getCustomer(user.getStripe_customer_id());
			// ????????????
			PaymentMethodCollection paymentMethods = stripeUtil.getPaymentMethods(stripe_customer_id);
			
			// ?????????????????????????????????????????????????????????????????????????????????????????????????????????
			if(0 == paymentMethods.getData().size()) {
				
				// ??????????????????????????????
				return stripeUtil.createCard_nonAppUser(req, stripe_customer_id, getUserStoreUrl(name, store_readForm.getTel(), store_readForm.getBirth()) );
				
			// ???????????????????????????(???????????????)??????
			}else {
				
				// ?????????????????????
				User_plan user_plan = user_planService.contain(store.getId(), user.getId());
				// ???????????????????????????
				List<User_option> user_options = user_optionService.containByUserId(store.getId(), user.getId());
				
				// ?????????????????????
				stripeUtil.checkUpdatePlans(user);
				
				// ????????????????????????????????????????????????????????? or ?????????????????????????????????
				if((user_plan == null && 0 == user_options.size()) ||!store_readForm.getIs_plan_count()) {
					
					// ?????????????????????
					return Const.REDIRECT_HEADER + getUserStoreUrl(name, store_readForm.getTel(), store_readForm.getBirth());
					
				// ??????????????????????????????????????????????????????????????????
				}else {
					
					// ?????????????????????
					ses.setAttribute(REQUEST_USER_ID, user.getId());
					// ??????
					return Const.REDIRECT_HEADER + "/disp_store_request_user?user_id=" + user.getId();
				}
			}
		}
	}
	
	/**
	 * ???????????????????????????????????????????????????
	 */
	@TransactionTokenCheck(type = TransactionTokenType.BEGIN)
	@GetMapping("/disp_store_request_user")
	public String reqNonAppUser(@RequestParam(name="user_id", required=true) int user_id, Model model, HttpServletRequest req, HttpSession ses) throws Exception {

		// ?????????????????????????????????
		String sesErrFoward = CommonUtil.isSesStore(ses);
		if(sesErrFoward != null) return sesErrFoward;
		
		// ???????????????????????????????????????ID
		int ses_user_id = (int) ses.getAttribute(REQUEST_USER_ID);
		// ???????????????????????????ID?????????????????????ID??????????????????
		if(ses_user_id != user_id) {
			
			// ????????????????????????
			List<String> error_messages = new ArrayList<String>();
			// ????????????????????????
			error_messages.add("???????????????????????????????????????????????????");
			// ???????????????
			req.setAttribute(Const.MSG_ERROR, error_messages);
			
			// ?????????????????????
			return new StoreReadController().disp(model, req, ses);
			
		// ?????????????????????
		}else {
			// ???????????????
			return disp(user_id, model, req, ses);
		}
	}
	
/* ?????? */
	
	/**
	 * ?????????????????????????????????URL??????
	 */
	private static String getUserStoreUrl(String name, String tel, Date birth) {
		return "/disp_search_store_nonAppUser?name=" + name + "&tel=" + tel + "&birth=" + birth.getTime();
	}
	
	/**
	 * ?????????????????????
	 */
	@TransactionTokenCheck
	@Transactional
	@PostMapping("/request_procedure")
	public String updateCount(@ModelAttribute @Validated Store_requestForm store_requestForm, BindingResult bindingResult ,Model model, HttpServletRequest req, HttpSession ses) throws Exception {

		// ?????????????????????????????????
		String sesErrFoward = CommonUtil.isSesStore(ses);
		if(sesErrFoward != null) return sesErrFoward;
		
		// Validation??????????????????????????????
		String validMsg = CommonUtil.isValid(bindingResult,req);
		// Validation??????????????????????????????????????????????????????
		if(validMsg != null) return Const.PAGE_STORE_VALID_ERROR;
		
		// ???????????????????????????????????????ID
		int user_id = (int) ses.getAttribute(REQUEST_USER_ID);
		
		// ????????????????????????ID??????????????????
		if(user_id != store_requestForm.getUser_id()) {
			
			// ????????????????????????
			List<String> error_messages = new ArrayList<String>();
			error_messages.add("??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????");
			// ???????????????
			req.setAttribute(Const.MSG_ERROR, error_messages);
			
			// ??????
			return Const.PAGE_STORE_READ;
			
		// ????????????????????????????????????????????????????????????
		}else if((store_requestForm.getUser_plan_id() == null || "0".equals(store_requestForm.getUser_plan_id())) && 
				 (store_requestForm.getUser_option_id() == null || 0 == store_requestForm.getUser_option_id().size())) {
			
			// ????????????????????????
			List<String> error_messages = new ArrayList<String>();
			error_messages.add("??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????(???????????????????????????????????????????????????????????????????????????????????????)");
			// ???????????????
			req.setAttribute(Const.MSG_ERROR, error_messages);
			
			// ??????
			return disp(user_id, model, req, ses);
			
		// ?????????????????????
		}else {
			
			// ???????????????????????????????????????
			if(store_requestForm.getUser_plan_id() != null && !"0".equals(store_requestForm.getUser_plan_id())) {
				
				// ??????
				String errFoward = countSave(store_requestForm.getUser_plan_id(), user_id, model, req, ses, true);
				// ??????????????????????????????????????????????????????
				if(errFoward != null) return errFoward;
			}
			
			// ??????????????????????????????????????????????????????
			if(store_requestForm.getUser_option_id() != null) {
				
				// ?????????????????????
				for(String user_option_id : store_requestForm.getUser_option_id()) {
					
					// ??????
					String errFoward = countSave(user_option_id, user_id, model, req, ses, false);
					// ??????????????????????????????????????????????????????
					if(errFoward != null) return errFoward;
				}
			}
			
			// ???????????????
			List<String> info_messages = new ArrayList<String>();
			info_messages.add("?????????????????????????????????");
			// ???????????????
			req.setAttribute(Const.MSG_INFO, info_messages);
			
			// ??????
			return disp(user_id, model, req, ses);
		}
	}
	
	/**
	 * ???????????????
	 */
	@SuppressWarnings("unused")
	private String countSave(String user_plan_id, int user_id, Model model, HttpServletRequest req, HttpSession ses, boolean isPlan) throws Exception {

		// ?????????/???????????????ID????????????????????????
		String[] split = user_plan_id.split("-");
		int plan_id = Integer.parseInt(split[0]);
		long start_date = Long.parseLong(split[1]);
		long end_date = Long.parseLong(split[2]);
		
		// ???????????????????????????
		Store store = CommonUtil.getSessionStore(ses);
		
		// ?????????????????????
		User_relation user_relation = isPlan
				? user_planService.containByPlanId(store.getId(), user_id, plan_id)
				: user_optionService.containByPlanId(store.getId(), user_id, plan_id);
		
		// ?????????????????????(true:?????????)
		boolean errUsrPay = stripeUtil.chkErrPaymentIntent(stripeUtil.getSubscription(user_relation.getStripe_subscription_id()));
		// ??????????????????????????????
		if(errUsrPay) {
			// ??????
			return errDisp("????????????????????????????????????????????????????????????", user_id, model, req, ses);
			
		// ??????????????????????????????
		}else{
			
			// ???????????????????????????
			ResultWithinDate result = CommonUtil.chkWithinDate(start_date, end_date);
			// ?????????????????????????????????
			if(result == ResultWithinDate.ERR_PRE) {
				// ??????
				return errDisp("????????????????????????????????????????????????????????????????????????????????????", user_id, model, req, ses);
				
			// ????????????????????????????????????
			}else if(result == ResultWithinDate.ERR_AFT){
				// ??????
				return errDisp("??????????????????????????????????????????????????????????????????????????????????????????????????????????????????", user_id, model, req, ses);
			}
		}
		
		// DB?????????????????????
		User_count user_count;
		// ??????????????????
		if(isPlan) user_count = user_plan_countService.contain(user_id, plan_id, start_date, end_date);
		else user_count = user_option_countService.contain(user_id, plan_id, start_date, end_date);
		
		// ????????????????????????
		boolean usedToday = isPlan ? null != user_plan_count_dateRepository.checkUsedToday(user_count.getId())
									:null != user_option_count_dateRepository.checkUsedToday(user_count.getId());
		
		// ???????????????????????????????????????
		if(usedToday) {
			// ??????
			return errDisp((isPlan ? ((User_plan_count)user_count).getPlan().getName(): ((User_option_count)user_count).getOption().getName()) + "???????????????1?????????????????????????????????????????????????????????????????????????????????", user_id, model, req, ses);
			
		//????????????????????????
		}else {
			
			// ?????????????????????????????????????????????
			if(user_count != null) {
				
				// ?????????/??????????????????????????????????????????????????????
				if(user_count.getBaseCount() <= user_count.getCount()) {
					// ??????
					return errDisp("????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????", user_id, model, req, ses);
					
				// ???????????????????????????
				}else {
					
					// ?????????????????????????????????
					if(isPlan) {
						// ???????????????
						User_plan_count_date user_plan_count_date = new User_plan_count_date();
						user_plan_count_date.setUser_plan_count_id(user_count.getId());
						user_plan_count_date.setUse_date(new Date());
						// ??????
						user_plan_count_dateService.save(user_plan_count_date);
						
					// ???????????????????????????????????????
					}else {
						// ???????????????
						User_option_count_date user_option_count_date = new User_option_count_date();
						user_option_count_date.setUser_option_count_id(user_count.getId());
						user_option_count_date.setUse_date(new Date());
						// ??????
						user_option_count_dateService.save(user_option_count_date);
					}
					
					// ??????
					return null;
				}
				
			// ???????????????????????????????????????????????????
			}else {
				// ??????
				return errDisp("??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????", user_id, model, req, ses);
			}
		}
	}
	
	/**
	 * ?????????????????????
	 */
	private String errDisp(String msg, int user_id, Model model, HttpServletRequest req, HttpSession ses) throws Exception {

		// ????????????????????????
		List<String> error_messages = new ArrayList<String>();
		error_messages.add(msg);
		// ???????????????
		req.setAttribute(Const.MSG_ERROR, error_messages);
		
		// ??????
		return disp(user_id, model, req, ses);
	}
}
