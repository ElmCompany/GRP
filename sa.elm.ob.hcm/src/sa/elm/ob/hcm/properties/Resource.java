package sa.elm.ob.hcm.properties;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Properties;

import org.apache.log4j.Logger;

import sa.elm.ob.utility.util.Utility;

public class Resource {
	private static Resource resource = null;
	private static HashMap<String, Properties> hm = new HashMap<String, Properties>();
	private static Properties defaultProp = null;
	private static Logger log4j = Logger.getLogger(Resource.class);

	public static String getProperty(String propKey, String lang) {
		return getValue(propKey, lang, false);
	}

	public static String getProperty(String propKey, String lang, boolean escape) {
		return getProperty(propKey, lang, escape);
	}

	public static String getDynamicProperty(String propKey, String lang, String args[]) {
		String value = getProperty(propKey, lang);
		if(args != null)
			for (int i = 0; i < args.length; i++) {
				value = value.replace("${" + i + "}", args[i]);
			}

		return value;
	}

	private static String getValue(String propKey, String lang, boolean escape) {
		String val = null;
		if(resource == null) {
			resource = new Resource();
			try {
				defaultProp = new Properties();
				InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream("sa/elm/ob/hcm/properties/applicationresources.properties");
				defaultProp.load(new InputStreamReader(input, "UTF-8"));
				hm.put("en_us", defaultProp);
			}
			catch (Exception e) {
				log4j.error("Exception in Resource", e);
			}
		}

		Properties prop = hm.get(lang.toLowerCase());
		if(prop != null) {
			val = prop.getProperty(propKey);
			return val == null ? "" : (escape ? Utility.escapeHTML(val) : val);
		}
		else {
			try {
				Properties props = new Properties();
				InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream("sa/elm/ob/hcm/properties/applicationresources_" + lang.toLowerCase() + ".properties");
				props.load(new InputStreamReader(input, "UTF-8"));
				hm.put(lang.toLowerCase(), props);
				val = props.getProperty(propKey);
				return val == null ? "" : (escape ? Utility.escapeHTML(val) : val);
			}
			catch (Exception e) {
				log4j.error("Exception in Resource", e);
				val = defaultProp.getProperty(propKey);
				return val == null ? "" : (escape ? Utility.escapeHTML(val) : val);
			}
		}
	}
}