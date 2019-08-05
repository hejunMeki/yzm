/*
 *
 * COPYRIGHT. Shenzhen Qianhai Dianjiang Financial Tech Co., Ltd. 2018. 
 * ALL RIGHTS RESERVED. 
 *
 * No part of this publication may be reproduced, stored in a retrieval system, or transmitted, 
 * on any form or by any means, electronic, mechanical, photocopying, recording,  
 * or otherwise, without the prior written permission of Shenzhen Qianhai Dianjiang Financial Tech Co., Ltd.
 *
 * Amendment History:
 * 
 * Date                   By              Description
 * -------------------    -----------     -------------------------------------------
 * 2018年11月13日    chengcheng.yu         Create the class
*/

package com.myfams.util;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.client.CookieStore;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;


/**
 * httpclient 工具类
 * @Description: 
 * @Date 2018年11月13日 
 * @author stan.c
 * @version 1.0.1
 * 
 */
public class HttpUtil {

	static {
		System.setProperty("jsse.enableSNIExtension", "false");
	}

	/**
	 * 获取httpclient客户端
	 * @return 
	 * @author stan.c
	 * @date 2018年11月13日
	 */
	public static CloseableHttpClient getHttpClient(){
		LaxRedirectStrategy redirectStrategy = new LaxRedirectStrategy(); // 创建客户端
		// 创建ConnectionManager，添加Connection配置信息
		Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory> create()
				.register("http", PlainConnectionSocketFactory.INSTANCE).register("https", trustAllHttpsCertificates())
				.build();
		PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(
				socketFactoryRegistry);
		CookieStore cookieStore = new BasicCookieStore();
		CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(connectionManager)
				.setDefaultCookieStore(cookieStore)// 全国银行间债券报送网址需要https认证
				.setRedirectStrategy(redirectStrategy).build();
		return httpClient;
	}
	
	
	
	private static SSLConnectionSocketFactory trustAllHttpsCertificates() {
		SSLConnectionSocketFactory socketFactory = null;
		TrustManager[] trustAllCerts = new TrustManager[1];
		TrustManager tm = new miTM();
		trustAllCerts[0] = tm;
		SSLContext sc = null;
		try {
			sc = SSLContext.getInstance("TLS");//sc = SSLContext.getInstance("TLS")
			sc.init(null, trustAllCerts, null);
			socketFactory = new SSLConnectionSocketFactory(sc, NoopHostnameVerifier.INSTANCE);
			//HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		}
		return socketFactory;
	}
	
	
	static class miTM implements TrustManager, X509TrustManager {
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}
		
		public void checkServerTrusted(X509Certificate[] certs, String authType) {
			//don't check
		}
		
		public void checkClientTrusted(X509Certificate[] certs, String authType) {
			//don't check
		}
	}
	
}
