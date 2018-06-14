package org.ofbiz.cooperativefunds.loan;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.ofbiz.base.conversion.ConversionException;
import org.ofbiz.base.conversion.NumberConverters;
import org.ofbiz.base.conversion.NumberConverters.StringToBigDecimal;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.ObjectType;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilHttp;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntity;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ModelService;
import org.ofbiz.service.ServiceUtil;

import javolution.util.FastList;
import javolution.util.FastMap;

public class LoanEvents {
	public static final String module = LoanEvents.class.getName();
	public static String calculateLoanRepayment(HttpServletRequest request, HttpServletResponse response) {
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
	    final Delegator delegator = (Delegator)request.getAttribute("delegator");
	    GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");
	    Locale locale = UtilHttp.getLocale(request);
	    Map<String, Object> ctx = UtilHttp.getParameterMap(request);
	    String repaymentAmountStr = (String) ctx.get("repaymentAmount");
	    BigDecimal repaymentAmount = null;
	    
		try {
			repaymentAmount = (BigDecimal) ObjectType.simpleTypeConvert(repaymentAmountStr, "BigDecimal", null, locale);
		} catch (GeneralException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    //get active loans into a list using partyId
	    //------------------------------------------------
    	String partyId = (String) ctx.get("partyId");
    	Map<String, Object> getActiveLoansByPartyMap = UtilMisc.toMap("partyId", partyId, "userLogin", userLogin);
    	Map<String, Object> activeLoansMap = null;
    	
    	try {
			activeLoansMap = dispatcher.runSync("getActiveLoansByParty", getActiveLoansByPartyMap);
		} catch (GenericServiceException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			Debug.logError(e1, module);
	        return "error";
		}
    	
    	List<GenericValue> activeLoans = (List<GenericValue>) activeLoansMap.get("activeLoans");
    	
    	//calculate loan repayment fields for each line item
    	//------------------------------------------------
    	
    	Map<String, Object> calculateRepaymentFieldsPerLineMap = UtilMisc.toMap("activeLoans", activeLoans, "repaymentAmount", repaymentAmount, "userLogin", userLogin);
    	Map<String, Object> loanRepaymentLinesMap = null;
    	
    	try {
			loanRepaymentLinesMap = dispatcher.runSync("calculateRepaymentFieldsPerLine", calculateRepaymentFieldsPerLineMap);
		} catch (GenericServiceException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			Debug.logError(e1, module);
	        return "error";
		}	

		
    	//add date & currency to loan repayment
    	//------------------------------------------------
    	
    	GenericValue loanRepayment = (GenericValue) loanRepaymentLinesMap.get("loanRepayment");
    	String currencyUomId = (String) ctx.get("currencyUomId");
    	String repaymentDateStr = (String) ctx.get("repaymentDate");
    	
    	Timestamp repaymentDate = null;
		try {
			repaymentDate = (Timestamp) ObjectType.simpleTypeConvert(repaymentDateStr, "Timestamp", null, locale);
		} catch (GeneralException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	loanRepayment.put("currencyUomId", currencyUomId);
    	loanRepayment.put("repaymentDate", repaymentDate);
    	
    	List <GenericValue> loanRepaymentLines = new FastList<GenericValue>();
    	loanRepaymentLines = (List<GenericValue>) loanRepaymentLinesMap.get("loanRepaymentLines");
    	
    	ctx.put("loanRepayment", loanRepayment);
    	ctx.put("loanRepaymentLines", loanRepaymentLines);
    	ctx.put("partyId", partyId);
    	ctx.put("allParams", UtilHttp.getParameterMap(request));
    	
    	request.setAttribute("loanRepayment", loanRepayment);
    	request.setAttribute("loanRepaymentLines", loanRepaymentLines);
    	request.setAttribute("partyId", partyId);
    	request.setAttribute("allParams", UtilHttp.getParameterMap(request));
    	
    	Debug.logInfo("My Log:  loanRepayment.repaymentAmount is: " + loanRepayment.getBigDecimal("repaymentAmount") , null);
		Debug.logInfo("My Log:  loanRepaymentLines has these number of elements: " + loanRepaymentLines.size() , null);
		Debug.logInfo("My Log:  partyId: " + partyId , null);
		
		return "success";
	}
	
	
	public static String createLoanRepaymentAndLines(HttpServletRequest request, HttpServletResponse response) {
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
	    final Delegator delegator = (Delegator)request.getAttribute("delegator");
	    GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");
	    Locale locale = UtilHttp.getLocale(request);
	    Map<String, Object> ctx = UtilHttp.getParameterMap(request);
	    
	    //Create LoanRepayment and LoanRepayment lines
	    GenericValue loanRepayment = (GenericValue) request.getAttribute("loanRepayment");
	    loanRepayment = (GenericValue) ctx.get("loanRepayment");
	    
	    Debug.logInfo("My Log: While creating LoanRepaymentAndLines loanRepayment.repaymentAmount is: " + loanRepayment.getBigDecimal("repaymentAmount") , null);

	    Collection<Map<String, Object>> multiFormData = UtilHttp.parseMultiFormData(UtilHttp.getParameterMap(request));
	    List<GenericValue> loanRepaymentLines = new LinkedList<GenericValue>();
	    
	    for(Map<String, Object> record : multiFormData) {
	    	GenericValue loanRepaymentLine = delegator.makeValue("LoanRepaymentLine");
	    	loanRepaymentLine.putAll(record);
	    	loanRepaymentLines.add(loanRepaymentLine);
	    }
	    
	    Map<String, Object> createLoanRepaymentAndLinesMap = UtilMisc.toMap("loanRepayment", loanRepayment, "loanRepaymentLines", loanRepaymentLines, "userLogin", userLogin);
	    Map<String, Object> createLoanRepaymentAndLinesResultMap = FastMap.newInstance();
	    
	    try {
	    	createLoanRepaymentAndLinesResultMap = dispatcher.runSync("createLoanRepaymentAndLines", createLoanRepaymentAndLinesMap);
		} catch (GenericServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    loanRepayment = (GenericValue) createLoanRepaymentAndLinesResultMap.get("loanRepayment");
	    loanRepaymentLines = (List<GenericValue>) createLoanRepaymentAndLinesResultMap.get("loanRepaymentLines");
	    
	    request.setAttribute("loanRepayment", loanRepayment);
    	request.setAttribute("loanRepaymentLines", loanRepaymentLines);
    	request.setAttribute("allParams", UtilHttp.getParameterMap(request));
    	request.setAttribute("submit", "Submitted");
	    
    	Debug.logInfo("My Log:  loanRepayment.repaymentAmount is: " + loanRepayment.getBigDecimal("repaymentAmount") , null);
		Debug.logInfo("My Log:  loanRepaymentLines has these number of elements: " + loanRepaymentLines.size() , null);
		
	    return "success";
	}
	
	public static String calculateLoanRepaymentLines(HttpServletRequest request, HttpServletResponse response) {
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
	    final Delegator delegator = (Delegator)request.getAttribute("delegator");
	    GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");
	    Locale locale = UtilHttp.getLocale(request);
	    Map<String, Object> ctx = UtilHttp.getParameterMap(request);
	    
    	// get loan repayment using loanRepaymentId
    	//------------------------------------------------
    	
    	String loanRepaymentId = (String) ctx.get("loanRepaymentId");
    	GenericValue loanRepayment = null;
    	
    	    	
		try {
			loanRepayment = delegator.findOne("LoanRepayment", UtilMisc.toMap("loanRepaymentId", loanRepaymentId), false);
		} catch (GenericEntityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		Debug.logInfo("My Log:  loanRepaymentId parameter is: " + loanRepayment.getString("loanRepaymentId") , null);

	    
	    //String repaymentAmountStr = (String) ctx.get("repaymentAmount");
	    BigDecimal repaymentAmount = loanRepayment.getBigDecimal("repaymentAmount");
	    
	    
	    /**
		try {
			repaymentAmount = (BigDecimal) ObjectType.simpleTypeConvert(repaymentAmountStr, "BigDecimal", null, locale);
		} catch (GeneralException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
	    
	    
	    //get active loans into a list using partyId
	    //------------------------------------------------
    	String partyId = loanRepayment.getString("partyId");
    	Map<String, Object> getActiveLoansByPartyMap = UtilMisc.toMap("partyId", partyId, "userLogin", userLogin);
    	Map<String, Object> activeLoansMap = null;
    	
    	try {
			activeLoansMap = dispatcher.runSync("getActiveLoansByParty", getActiveLoansByPartyMap);
		} catch (GenericServiceException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			Debug.logError(e1, module);
	        return "error";
		}
    	
    	List<GenericValue> activeLoans = (List<GenericValue>) activeLoansMap.get("activeLoans");
    	
    	//calculate loan repayment fields for each line item
    	//------------------------------------------------
    	
    	Map<String, Object> calculateRepaymentFieldsPerLineMap = UtilMisc.toMap("activeLoans", activeLoans, "repaymentAmount", repaymentAmount, "userLogin", userLogin);
    	Map<String, Object> loanRepaymentLinesMap = null;
    	
    	try {
			loanRepaymentLinesMap = dispatcher.runSync("calculateRepaymentFieldsPerLine", calculateRepaymentFieldsPerLineMap);
		} catch (GenericServiceException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			Debug.logError(e1, module);
	        return "error";
		}	
    	
    	
    	GenericValue calculatedLoanRepayment = (GenericValue) loanRepaymentLinesMap.get("loanRepayment");
    	
    	List <GenericValue> loanRepaymentLines = new FastList<GenericValue>();
    	loanRepaymentLines = (List<GenericValue>) loanRepaymentLinesMap.get("loanRepaymentLines");
    	
    	ctx.put("calculatedLoanRepayment", calculatedLoanRepayment);
    	ctx.put("loanRepaymentLines", loanRepaymentLines);
    	ctx.put("partyId", partyId);
    	ctx.put("allParams", UtilHttp.getParameterMap(request));
    	
    	request.setAttribute("calculatedLoanRepayment", calculatedLoanRepayment);
    	request.setAttribute("calculatedLoanRepaymentLines", loanRepaymentLines);
    	request.setAttribute("partyId", partyId);
    	request.setAttribute("allParams", UtilHttp.getParameterMap(request));
    	
    	Debug.logInfo("My Log:  calculatedLoanRepayment.repaymentAmount is: " + calculatedLoanRepayment.getBigDecimal("repaymentAmount") , null);
		Debug.logInfo("My Log:  loanRepaymentLines has these number of elements: " + loanRepaymentLines.size() , null);
		Debug.logInfo("My Log:  partyId: " + partyId , null);
		
		return "success";
	}
	
	public static String createLoanRepaymentLines(HttpServletRequest request, HttpServletResponse response) {
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
	    final Delegator delegator = (Delegator)request.getAttribute("delegator");
	    GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");
	    Locale locale = UtilHttp.getLocale(request);
	    Map<String, Object> ctx = UtilHttp.getParameterMap(request);
	    
	    Collection<Map<String, Object>> multiFormData = UtilHttp.parseMultiFormData(ctx);
	    List<GenericValue> loanRepaymentLines = new LinkedList<GenericValue>();
	    
	    Iterator<Map<String, Object>> multiFormDataItr = multiFormData.iterator();
	    
	    Debug.logInfo("My Log:  multiFormDataItr.next(): " + multiFormDataItr.next() , null);    
	    
	    for(Map<String, Object> record : multiFormData) {
	    	GenericValue loanRepaymentLine = delegator.makeValue("LoanRepaymentLine", UtilMisc.toMap("loanId",  record.get("loanId"), "loanRepaymentId", record.get("loanRepaymentId")));
	    	
	    	NumberConverters.StringToBigDecimal stringToBigDecimal = new NumberConverters.StringToBigDecimal();
	    	NumberConverters.StringToDouble stringToDouble = new NumberConverters.StringToDouble();
	    	NumberConverters.StringToLong stringToLong = new NumberConverters.StringToLong();
	    	
	    	try {
	    		
	    		Debug.logInfo("My Log:  record.get(principalAmount): " + (String) record.keySet().toString() , null);
				Debug.logInfo("My Log:  record.get(interestAmount): " + record.get("interestAmount").getClass().getName() , null);
				Debug.logInfo("My Log:  record.get(totalAmount): " + ((String) record.get("totalAmount")).length(), null);
				Debug.logInfo("My Log:  String to Double: " + stringToDouble.convert(((String) record.get("totalAmount")).replace(",","")), null);
				Debug.logInfo("My Log:  record.get(totalAmount): " + new BigDecimal(stringToDouble.convert(((String) record.get("totalAmount")).replace(",",""))), null);
				
				loanRepaymentLine.put("principalAmount", stringToBigDecimal.convert(((String) record.get("principalAmount")).replace(",","")));
				loanRepaymentLine.put("interestAmount", stringToBigDecimal.convert(((String) record.get("interestAmount")).replace(",","")));
				loanRepaymentLine.put("totalAmount", stringToBigDecimal.convert(((String) record.get("totalAmount")).replace(",","")));
				
				loanRepaymentLine.put("installmentSeqId", stringToLong.convert((String) record.get("installmentSeqId")));
				
			} catch (ConversionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Debug.logError(e, module);
                return "error";
			}
    	
	    	Debug.logInfo("My Log:  record loanId: " + record.get("loanId").getClass().getName() , null);
	    	Debug.logInfo("My Log:  record principalAmount: " + record.get("principalAmount").getClass().getName() , null);
	    	Debug.logInfo("My Log:  record: " + record.toString() , null);
	    	
	    	loanRepaymentLines.add(loanRepaymentLine);
	    }
	    
	    Debug.logInfo("My Log:  loanRepaymentLines has these number of elements: " + loanRepaymentLines.size() , null);
	    
	    Map<String, Object> createLoanRepaymentLinesMap = UtilMisc.toMap("loanRepaymentLines", loanRepaymentLines, "userLogin", userLogin);
	    Map<String, Object> createLoanRepaymentLinesResultMap = FastMap.newInstance();
	    
	    try {
	    	createLoanRepaymentLinesResultMap = dispatcher.runSync("createLoanRepaymentLines", createLoanRepaymentLinesMap);
		} catch (GenericServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    String loanRepaymentId = (String) createLoanRepaymentLinesResultMap.get("loanRepaymentId");
	    loanRepaymentLines = (List<GenericValue>) createLoanRepaymentLinesResultMap.get("loanRepaymentLines");   
	    
	    GenericValue loanRepayment = null;
	    
		try {
			loanRepayment = delegator.findOne("LoanRepayment", UtilMisc.toMap("loanRepaymentId", loanRepaymentId), false);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
    	request.setAttribute("loanRepaymentLines", loanRepaymentLines);
    	request.setAttribute("totalRepaymentAmount", loanRepayment.getBigDecimal("repaymentAmount"));
    	request.setAttribute("totalPrincipalAmount", loanRepayment.getBigDecimal("principalAmount"));
    	request.setAttribute("totalInterestAmount", loanRepayment.getBigDecimal("interestAmount"));
    	request.setAttribute("loanRepaymentId", loanRepaymentId);
    	request.setAttribute("allParams", UtilHttp.getParameterMap(request));
    	request.setAttribute("submit", "Submitted");
	    
		Debug.logInfo("My Log:  loanRepaymentLines has these number of elements: " + loanRepaymentLines.size() , null);
		
	    return "success";
	}
	

}