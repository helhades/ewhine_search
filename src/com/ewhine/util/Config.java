package com.ewhine.util;

import java.io.IOException;
import java.util.Properties;

import cn.gov.cbrc.wh.log.Log;
import cn.gov.cbrc.wh.log.LogFactory;



public class Config {
	private static Log log = LogFactory.getLog(Config.class);
	private static Properties props = new Properties();
	static {
		try {
			props.load(Config.class.getResourceAsStream("/config.properties"));
		} catch (IOException e) {
			if(log.isErrorEnabled()) {
				log.error("Read config file error!",e);
			}
		}
	}
	
	public static String getConfig(String name) {
		return props.getProperty(name);
	}
	
	public static void main(String[] args) {
		System.out.println(Config.getConfig("dataset.dir"));
	}

}
