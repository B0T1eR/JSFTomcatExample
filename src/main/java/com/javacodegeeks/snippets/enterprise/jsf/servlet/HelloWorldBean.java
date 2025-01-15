package com.javacodegeeks.snippets.enterprise.jsf.servlet;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import java.util.ArrayList;

@ManagedBean(name="helloWorldBean")
@RequestScoped
public class HelloWorldBean {
	private String msg;

	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	@PostConstruct
	private void init() {
//		String key = "r8Ipfj1pFXR7m6hGUgmHbQ==";
//		System.setProperty("comp.env.jsf.ClientSideSecretKey",key);
//		System.out.println(key.getBytes());
		msg = "Hello World!! JFS example.. ";
	}

}