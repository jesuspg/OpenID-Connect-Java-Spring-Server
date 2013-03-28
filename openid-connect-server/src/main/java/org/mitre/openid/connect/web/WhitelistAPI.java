/**
 * 
 */
package org.mitre.openid.connect.web;

import java.security.Principal;
import java.util.Collection;

import org.mitre.openid.connect.model.WhitelistedSite;
import org.mitre.openid.connect.service.WhitelistedSiteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * @author jricher
 *
 */
@Controller
@RequestMapping("/api/whitelist")
@PreAuthorize("hasRole('ROLE_USER')")
public class WhitelistAPI {

	@Autowired
	private WhitelistedSiteService whitelistService;
	
	private static Logger logger = LoggerFactory.getLogger(WhitelistAPI.class);
	
	private Gson gson = new Gson();
	private JsonParser parser = new JsonParser();
	
	/**
	 * Get a list of all whitelisted sites
	 * @param m
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, produces = "application/json")
	public String getAllWhitelistedSites(ModelMap m) {
		
		Collection<WhitelistedSite> all = whitelistService.getAll();
		
		m.put("entity", all);
		
		return "jsonEntityView";
	}
	
	/**
	 * Create a new whitelisted site
	 * @param jsonString
	 * @param m
	 * @param p
	 * @return
	 */
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping(method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public String addNewWhitelistedSite(@RequestBody String jsonString, ModelMap m, Principal p) {
		
		JsonObject json; 
		
		WhitelistedSite whitelist = null; 
		try {
			json = parser.parse(jsonString).getAsJsonObject();
			whitelist = gson.fromJson(json, WhitelistedSite.class);

		} catch (JsonParseException e) {
			logger.error("addNewWhitelistedSite failed due to JsonParseException: " + e.getStackTrace().toString());
			m.addAttribute("code", HttpStatus.BAD_REQUEST);
			m.addAttribute("entity", "An error occurred while processing your request. Contact a system administrator for assistance.");
			return "jsonEntityView";
		} catch (IllegalStateException e) {
			logger.error("addNewWhitelistedSite failed due to IllegalStateException: " + e.getStackTrace().toString());
			m.addAttribute("code", HttpStatus.BAD_REQUEST);
			m.addAttribute("entity", "An error occurred while processing your request. Contact a system administrator for assistance.");
			return "jsonEntityView";
		}
		
		// save the id of the person who created this
		whitelist.setCreatorUserId(p.getName());
		
		WhitelistedSite newWhitelist = whitelistService.saveNew(whitelist);
		
		m.put("entity", newWhitelist);
		
		return "jsonEntityView";
		
	}
	
	/**
	 * Update an existing whitelisted site
	 */
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping(value="/{id}", method = RequestMethod.PUT, consumes = "application/json", produces = "application/json")
	public String updateWhitelistedSite(@PathVariable("id") Long id, @RequestBody String jsonString, ModelMap m, Principal p) {
		
		JsonObject json; 
		
		WhitelistedSite whitelist = null; 
		try {
			json = parser.parse(jsonString).getAsJsonObject();
			whitelist = gson.fromJson(json, WhitelistedSite.class);

		} catch (JsonParseException e) {
			logger.error("updateWhitelistedSite failed due to JsonParseException: " + e.getStackTrace().toString());
			m.put("code", HttpStatus.BAD_REQUEST);
			m.put("entity", "An error occurred while processing your request. Contact a system administrator for assistance.");
			return "jsonEntityView";
		} catch (IllegalStateException e) {
			logger.error("updateWhitelistedSite failed due to IllegalStateException: " + e.getStackTrace().toString());
			m.put("code", HttpStatus.BAD_REQUEST);
			m.put("entity", "An error occurred while processing your request. Contact a system administrator for assistance.");
			return "jsonEntityView";
		}
		
		WhitelistedSite oldWhitelist = whitelistService.getById(id);
		
		if (oldWhitelist == null) {
			logger.error("updateWhitelistedSite failed; whitelist with id " + id + " could not be found.");
			m.put("code", HttpStatus.NOT_FOUND);
			return "httpCodeView";
		} else {
			
			WhitelistedSite newWhitelist = whitelistService.update(oldWhitelist, whitelist);
			
			m.put("entity", newWhitelist);
			
			return "jsonEntityView";
		}
	}
	
	/**
	 * Delete a whitelisted site
	 * 
	 */
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping(value="/{id}", method = RequestMethod.DELETE)
	public String deleteWhitelistedSite(@PathVariable("id") Long id, ModelMap m) {
		WhitelistedSite whitelist = whitelistService.getById(id);
		
		if (whitelist == null) {
			logger.error("deleteWhitelistedSite failed; whitelist with id " + id + " could not be found.");
			m.put("code", HttpStatus.NOT_FOUND);
			m.put("entity", "An error occurred while processing your request. The requested whitelist entry could not be found.");
			return "jsonEntityView";
		} else {
			m.put("code", HttpStatus.OK);
			whitelistService.remove(whitelist);
		}		
		
		return "httpCodeView";
	}
	
	/**
	 * Get a single whitelisted site
	 */
	@RequestMapping(value="/{id}", method = RequestMethod.GET, produces = "application/json")
	public String getWhitelistedSite(@PathVariable("id") Long id, ModelMap m) {
		WhitelistedSite whitelist = whitelistService.getById(id);
		if (whitelist == null) {
			logger.error("getWhitelistedSite failed; whitelist with id " + id + " could not be found.");
			m.put("code", HttpStatus.NOT_FOUND);
			return "httpCodeView";
		} else {
		
			m.put("entity", whitelist);
			
			return "jsonEntityView";
		}
		
	}
}
