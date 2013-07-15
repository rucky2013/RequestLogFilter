package com.qly.b2b.servlet.filter.requestlog;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.util.JSON;
import com.qly.b2b.dao.filter.SystemReturnLog;
import com.qly.b2b.model.log.RequestLogBean;

/**
 * 
 * 说明：请求响应日志过滤器 by guop
 */
public class RequestLogFilter implements Filter {
	final static Logger logger = LoggerFactory
			.getLogger(RequestLogFilter.class);
	/** 不需要校验权限的URL **/
	String[] noCheckPaths = null;
	String contextPath = null;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		String unCheckPaths = filterConfig.getInitParameter("unCheckPaths");
		this.noCheckPaths = unCheckPaths.split(",");
		contextPath = filterConfig.getServletContext().getContextPath();
		logger.info("noCheckPaths----" + noCheckPaths + "contextPath"
				+ contextPath);
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		// 获取请求的url
		String url = req.getServletPath();
		String ip = this.getClientIp(req);
		// 判断url是否验证
		if (!this.isVerification(url)) {
			filterChain.doFilter(request, response);
		} else {
    		// 得到开始处理时间
    		Date startDate = new Date();
    
    		// 获取到传入的参数
    		Map mapParm = req.getParameterMap();
    		String parm = JSON.serialize(mapParm);
    
    		WrapperResponse wrapperResponse = new WrapperResponse(res);
    		// 让服务器将响应内容写到Wrapper中
    		filterChain.doFilter(request, wrapperResponse);
    		// 取出响应内容
    		byte[] content = wrapperResponse.getResponseData();

    		//输出处理后的数据  
    		response.getOutputStream().write(content);
    		response.getOutputStream().flush();
    		response.getOutputStream().close();

    		// 开始封装回调日志记录
    		String xml = new String(content, "UTF-8");
    		Date endDate = new Date();
    		RequestLogBean requestLog = new RequestLogBean();
    		requestLog.setOpenTime(startDate);
    		requestLog.setEndTime(endDate);
    		requestLog.setRequestIp(ip);
    		requestLog.setRequestUrl(url);
    		requestLog.setRequestContent(parm);
    		requestLog.setResponseContent(xml);
    		SystemReturnLog.insertReturnLog(requestLog);
		}
	}

	@Override
	public void destroy() {
	}

	/*
	 * 获的请求端的，真实ip，考虑到在有代理的情况下2013-6-4上午9:35:30 by guop TODO return String
	 */
	public String getClientIp(HttpServletRequest request) {
		// 请求ip
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}

	/**
	 * 
	 * isVerification(判断指定的url是否需要验证，默认需要验证的) (这里描述这个方法适用条件 – 可选)
	 * 
	 * @param path
	 * @return false代表不用验证 true 代表需要进行验证 Boolean
	 * @exception
	 */
	private Boolean isVerification(String path) {
		// 默认需要验证url
		boolean isVerification = true;
		for (String p : noCheckPaths) {
			if (path.indexOf(p) == 0) {
				isVerification = false;
				break;
			}
		}

		// "/view/" 路径下的js ，不校验
		if (path.indexOf("/view/") == 0
				&& (path.indexOf(".js") > 0 || path.indexOf(".js?") > 0)) {
			isVerification = false;
		}

		return isVerification;
	}
}
