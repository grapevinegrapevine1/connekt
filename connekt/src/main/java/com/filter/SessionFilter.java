package com.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Component;

import com.properties.Const;
import com.util.CommonUtil;

@Component
public class SessionFilter implements Filter {

	@Override
	public void doFilter(ServletRequest _req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest req = (HttpServletRequest) _req;
		String url = (req.getRequestURI());

		HttpSession ses = ((HttpServletRequest) req).getSession();
		if (url.contains(Const.PAGE_LOGIN) || url.contains("login_error") || url.contains("user_create") || url.contains("create_user") || url.contains("store_create") || url.contains("create_store")
				|| url.contains("user_valid_error") || url.contains("store_valid_error")
				|| url.contains("user_forget") || url.contains("user_forget_request") || url.contains("store_forget") || url.contains("store_forget_request") || url.contains(Const.PAGE_USER_FORGET_SEND) || url.contains(Const.PAGE_STORE_FORGET_SEND)
				|| url.contains("cert_user") || url.contains("cert_store") || url.contains("user_recert") || url.contains("store_recert") || url.contains("user_recert_input") || url.contains("store_recert_input")
				|| url.contains("cert_start")
				|| url.endsWith(".css") || url.endsWith(".js") || url.endsWith(".png")|| url.endsWith(".ico")
				|| url.endsWith(".jpg") || url.endsWith(".jpeg") || url.endsWith(".ttf") || url.endsWith(".woff")
				|| url.endsWith(".csv") || url.endsWith(".json")) {

			chain.doFilter(req, res);

			// セッションユーザー情報およびセッション店舗情報が存在しない場合
		} else if (CommonUtil.getSessionUser(ses) == null && CommonUtil.getSessionStore(ses) == null) {
			// ログイン画面へ遷移
			((HttpServletResponse) res).sendRedirect(Const.PAGE_LOGIN);
			
		} else {
			chain.doFilter(req, res);
		}
	}

}
