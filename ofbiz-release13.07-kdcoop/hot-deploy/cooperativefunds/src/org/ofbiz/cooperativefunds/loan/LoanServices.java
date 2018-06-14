package org.ofbiz.cooperativefunds.loan;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilGenerics;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilURL;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

import javolution.util.FastList;
import javolution.util.FastMap;

public class LoanServices {
	
	public static final String module = LoanServices.class.getName();
	public static final String resource = "cooperativefundsUiLabels";
	public static final String resourceError = "CooperativeFundsErrorUiLabels";
	
	public static Map<String, Object> calculateLoanRepayment (DispatchContext dctx, Map<String, ? extends Object> context){
		Delegator delegator = dctx.getDelegator();
		
		String partyId = (String)context.get("partyId");
		Timestamp repaymentDate = (Timestamp)context.get("repaymentDate");
		BigDecimal totalRepaymentAmount = (BigDecimal)context.get("totalRepaymentAmount");
		
		List<GenericValue> loans = FastList.newInstance();
		List<GenericValue> paymentPlan = null;
		
		Map<String, ? extends Object> fields = null;
		GenericValue oVal = null;
		
		List<GenericValue> repayments = FastList.newInstance();
		List<Map<String, Object>> repaymentsList = FastList.newInstance();
		
		//Using partyId, get list of loans belonging to partyId
		EntityCondition conditionMain = EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, partyId);
		
		BigDecimal totalPlannedPrincipalAmount = new BigDecimal (0);
		BigDecimal totalPlannedInterestAmount = new BigDecimal (0);
		BigDecimal totalPlannedPaybackAmount = new BigDecimal (0);
		
		BigDecimal totalActualPrincipalAmount = new BigDecimal (0);
		BigDecimal totalActualInterestAmount = new BigDecimal (0);
		BigDecimal totalActualPaybackAmount = new BigDecimal (0);
		
		totalPlannedPrincipalAmount.setScale(2, BigDecimal.ROUND_HALF_UP);
		totalPlannedInterestAmount.setScale(2, BigDecimal.ROUND_HALF_UP);
		totalPlannedPaybackAmount.setScale(2, BigDecimal.ROUND_HALF_UP);
		
		totalActualPrincipalAmount.setScale(2, BigDecimal.ROUND_HALF_UP);
		totalActualInterestAmount.setScale(2, BigDecimal.ROUND_HALF_UP);
		totalActualPaybackAmount.setScale(2, BigDecimal.ROUND_HALF_UP);
		
		try {
			Debug.logInfo("My Log:  partyId: " + partyId, null);
			
			//loans belonging to partyId
			loans = delegator.findList("Loan", conditionMain, null, null, null, false);
			
			Debug.logInfo("My Log:  Number of loans: " + loans.size(), null);
			
			//Get current payment plan per loan and calculate totalPrincipal and totalInterest
			
			
			for (Object o : loans){
				oVal =(GenericValue) o;
				List<GenericValue> loanPaymentPlanList = delegator.findByAnd("LoanPaymentPlan", UtilMisc.toMap(
	                    "loanId", oVal.get("loanId"),
	                    "statusId", "LOAN_PMTPLAN_APPRVD"), null, false);
				
							
				if(loanPaymentPlanList.size() != 0){
					//For loans with payment plan, do the following
					Debug.logInfo("My Log:  LoanId: " + oVal.get("loanId"), null);

					Debug.logInfo("My Log:  Number of loanPaymentPlanList members: " + loanPaymentPlanList.size(), null);
					GenericValue loanPaymentPlan = loanPaymentPlanList.get(0);
					
					if(loanPaymentPlan.isEmpty() == false){
						//Debug.logInfo("My Log:  Number of loanPaymentPlan size: " + loanPaymentPlan.size(), null);

						totalPlannedPrincipalAmount =totalPlannedPrincipalAmount.add(loanPaymentPlan.getBigDecimal("monthlyPrincipalPaybackAmount"));
						totalPlannedInterestAmount = totalPlannedInterestAmount.add(loanPaymentPlan.getBigDecimal("monthlyInterestPaybackAmount"));
						totalPlannedPaybackAmount = totalPlannedPrincipalAmount.add(totalPlannedInterestAmount);
					}
				}
				
			}
			
			BigDecimal principalRepaymentRatio = new BigDecimal (0);
			principalRepaymentRatio.setScale(2, BigDecimal.ROUND_HALF_UP);
			
			BigDecimal interestRepaymentRatio = new BigDecimal (0);
			interestRepaymentRatio.setScale(2, BigDecimal.ROUND_HALF_UP);
			
			BigDecimal paybackAmountRatio = new BigDecimal (0);
			paybackAmountRatio.setScale(2, BigDecimal.ROUND_HALF_UP);
			
			BigDecimal paybackAmount = new BigDecimal (0);
			paybackAmount.setScale(2, BigDecimal.ROUND_HALF_UP);

			BigDecimal principalAmount = new BigDecimal (0);
			principalAmount.setScale(2, BigDecimal.ROUND_HALF_UP);
			
			BigDecimal interestAmount = new BigDecimal (0);
			interestAmount.setScale(2, BigDecimal.ROUND_HALF_UP);
			
			BigDecimal repaymentAmount = new BigDecimal (0);
			repaymentAmount.setScale(2, BigDecimal.ROUND_HALF_UP);
			
			for (Object o : loans){
				oVal =(GenericValue)o;
				List<GenericValue> loanPaymentPlanList = delegator.findByAnd("LoanPaymentPlan", UtilMisc.toMap(
	                    "loanId", oVal.get("loanId"),
	                    "statusId", "LOAN_PMTPLAN_APPRVD"), null, false);
						
				if(loanPaymentPlanList.size() != 0){
					Debug.logInfo("My Log:  Number of loanPaymentPlanList members: " + loanPaymentPlanList.size(), null);
					GenericValue loanPaymentPlan = loanPaymentPlanList.get(0);

					if(loanPaymentPlan.isEmpty() == false){
						Debug.logInfo("My Log:  Number of loanPaymentPlan size: " + loanPaymentPlan.size(), null);
						Debug.logInfo("My Log:  loanId: " + oVal.get("loanId"), null);
						Debug.logInfo("My Log:  loanPaymentPlanId: " + loanPaymentPlan.get("loanPaymentPlanId"), null);
						Debug.logInfo("My Log:  totalPlannedPrincipalAmount: " + totalPlannedPrincipalAmount, null);
						Debug.logInfo("My Log:  totalPlannedInterestAmount: " + totalPlannedInterestAmount, null);
						Debug.logInfo("My Log:  totalPlannedPaybackAmount: " + totalPlannedPaybackAmount, null);
						Debug.logInfo("My Log:  totalRepaymentAmount: " + totalRepaymentAmount, null);

						paybackAmount = loanPaymentPlan.getBigDecimal("monthlyPrincipalPaybackAmount").add(loanPaymentPlan.getBigDecimal("monthlyInterestPaybackAmount"));	
						principalRepaymentRatio =  loanPaymentPlan.getBigDecimal("monthlyPrincipalPaybackAmount").divide(totalPlannedPaybackAmount,2, BigDecimal.ROUND_HALF_UP);				
						interestRepaymentRatio = loanPaymentPlan.getBigDecimal("monthlyInterestPaybackAmount").divide(totalPlannedPaybackAmount, 2, BigDecimal.ROUND_HALF_UP);
						paybackAmountRatio = paybackAmount.divide(totalPlannedPaybackAmount, 2, BigDecimal.ROUND_HALF_UP);
						
						Debug.logInfo("My Log:  principalRepaymentRatio: " + principalRepaymentRatio, null);
						Debug.logInfo("My Log:  interestRepaymentRatio: " + interestRepaymentRatio, null);
						Debug.logInfo("My Log:  paybackAmount: " + paybackAmount, null);
						Debug.logInfo("My Log:  paybackAmountRatio: " + paybackAmountRatio, null);
						
						repaymentAmount = paybackAmountRatio.multiply(totalRepaymentAmount);
						principalAmount = principalRepaymentRatio.multiply(totalRepaymentAmount);
						interestAmount = repaymentAmount.subtract(principalAmount);
						
						Debug.logInfo("My Log:  repaymentAmount: " + repaymentAmount, null);
						Debug.logInfo("My Log:  principalAmount: " + principalAmount, null);
						Debug.logInfo("My Log:  interestAmount: " + interestAmount, null);

						
						repayments.add(delegator.makeValue("LoanRepaymentLine", UtilMisc.toMap(
								"loanId", oVal.get("loanId"),
								"principalAmount", principalAmount,
								"interestAmount", interestAmount)));
						
						Map<String, Object> newMap = FastMap.newInstance();
						
						totalActualPrincipalAmount =totalActualPrincipalAmount.add(principalAmount);
						totalActualInterestAmount = totalActualInterestAmount.add(interestAmount);
						totalActualPaybackAmount = totalActualPrincipalAmount.add(totalActualInterestAmount);
						
						newMap.put("loanId", oVal.get("loanId"));
						newMap.put("principalAmount", principalAmount);
						newMap.put("interestAmount", interestAmount);
						newMap.put("totalAmount", principalAmount.add(interestAmount));
						newMap.put("partyId", partyId);
						newMap.put("repaymentDate", repaymentDate);
						repaymentsList.add(newMap);
											
					}
				}
			}
						
			
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Debug.logInfo("My Log:  Number of repayments: " + repayments.size(), null);
		Map<String, Object> resultMap = ServiceUtil.returnSuccess();
		resultMap.put("calculatedLoanRepaymentList", repaymentsList);
		resultMap.put("totalPrincipalAmount", totalActualPrincipalAmount);
		resultMap.put("totalInterestAmount", totalActualInterestAmount);
		resultMap.put("totalRepaymentAmount", totalActualPaybackAmount);
		
		return resultMap;
	}
	
	public static Map<String, Object> createLoanRepaymentLines_v1(DispatchContext dctx, Map<String, ? extends Object> context){
		
		Delegator delegator = dctx.getDelegator(); 
		
		
		/** Instance Creation: new instance of LoanRepayment loanRepayment is created */
		
		String loanRepaymentId = delegator.getNextSeqId("LoanRepayment");
		GenericValue loanRepayment = delegator.makeValue("LoanRepayment", UtilMisc.toMap("loanRepaymentId", loanRepaymentId));
		
		
		/** Attribute Modification: attributes of loanRepayment are initialized */
		
		BigDecimal repaymentAmount =  (BigDecimal) context.get("repaymentAmount");
		BigDecimal principalAmount =  (BigDecimal) context.get("principalAmount");
		BigDecimal interestAmount =  (BigDecimal) context.get("interestAmount");
		String currencyUomId =  (String) context.get("currencyUomId");
		Timestamp repaymentDate =  (Timestamp) context.get("repaymentDate");
			
		loanRepayment.put("repaymentAmount", repaymentAmount);
		loanRepayment.put("principalAmount", principalAmount);
		loanRepayment.put("interestAmount", interestAmount);
		loanRepayment.put("currencyUomId", currencyUomId);
		loanRepayment.put("repaymentDate", repaymentDate);
		
		try {
			loanRepayment.create();
		} catch (GenericEntityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		//Checking contents of context
		
		/**for(Object obj : context.keySet()) {
			if(obj.toString()!=null) {
				//obj.toString();
				Debug.logInfo("My Log:  Context elements: " +  obj.toString() , null);
			}
		}*/
		
		for(Entry<String, ? extends Object> e : context.entrySet()) {
			if(e.getKey().toString()!=null) {
				//obj.toString();
				Debug.logInfo("My Log:  Context elements: " +  e.getKey() + " value: " + e.getValue() , null);
			}
		}

		/** Instance Creation: new instances of LoanRepaymentLine were created based on 
		 * number of active loans related to partyId (and stored in a list)
		 */
		
		
		List<GenericValue> loanRepaymentList = (List<GenericValue>) context.get("loanRepaymentList");
		
		if (loanRepaymentList != null) {
			for (GenericValue LoanRepaymentLine : loanRepaymentList) {
				String LoanRepaymentLineId = delegator.getNextSeqId("LoanRepaymentLine");
				LoanRepaymentLine.put("LoanRepaymentLineId", LoanRepaymentLineId);
				try {
					LoanRepaymentLine.create();
					Debug.logInfo("My Log:  LoanRepaymentLine: " + LoanRepaymentLine.getString(LoanRepaymentLineId) , null);
				} catch (GenericEntityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else {
			Debug.logInfo("My Log:  loanRepaymentList is null!" , null);
		}
		
		
		
		
		
		
		
		/**  Attribute Modification: attributes of instances of LoanRepaymentLine are 
		 * calculated (if not already calculated) and initialized.  Attributes to be calculated are principalAmount, interestAmount, installmentSeqId
		 */
		
		
		/** Association Formed: instances of loanPaymentloan were associated with 
		 * loan using corresponding loanId if not already done 
		 */
		
		
		/** Instance Creation: new instance of Payment payment is created */
		/** Attribute Modification: attributes of payment are initialized */
		/** Association Formed: loanPayment is associated with payment using paymentId */
 
		
		Map<String, Object> resultMap = ServiceUtil.returnSuccess();
		resultMap.put("LoanRepaymentList", loanRepaymentList);
		
		/**resultMap.put("totalPrincipalAmount", totalActualPrincipalAmount);
		resultMap.put("totalInterestAmount", totalActualInterestAmount);
		resultMap.put("totalRepaymentAmount", totalActualPaybackAmount);
		*/
		
		return resultMap;
	}
	
	public static Map<String, Object> getActiveLoansByParty(DispatchContext dctx, Map<String, ? extends Object> context){
		
		Delegator delegator = dctx.getDelegator(); 
		String partyId = (String)context.get("partyId");
		List<GenericValue> activeLoans = FastList.newInstance();
		
		//Using partyId, get list of loans belonging to partyId
		EntityCondition conditionMain = EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, partyId);
		
		//loans belonging to partyId
		try {
			//activeLoans = delegator.findList("Loan", conditionMain, null, null, null, false);
			activeLoans = delegator.findByAnd("Loan", UtilMisc.toMap(
			        "partyId", partyId,
			        "statusId", "LOAN_APPRVD"), null, false);
			
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
				
		Map<String, Object> resultMap = ServiceUtil.returnSuccess();
		resultMap.put("partyId", partyId);
		resultMap.put("activeLoans", activeLoans);
		
		Debug.logInfo("My Log:  partyId is: " + partyId , null);
		Debug.logInfo("My Log:  number of activeLoans is: " + activeLoans.size() , null);
		
		return resultMap;
	}
	
	public static Map<String, Object> calculateRepaymentFieldsPerLine(DispatchContext dctx, Map<String, ? extends Object> context){
		
		Delegator delegator = dctx.getDelegator();
		
		List <GenericValue> activeLoans =  (List <GenericValue>) context.get("activeLoans");
		List <GenericValue> loanRepaymentLines = new FastList<GenericValue>();
		BigDecimal totalRepaymentFromPaymentPlans = BigDecimal.ZERO;
		
		BigDecimal repaymentAmount = (BigDecimal) context.get("repaymentAmount");
		BigDecimal principalAmount = BigDecimal.ZERO;
		BigDecimal interestAmount = BigDecimal.ZERO;
		
		String currencyUomId = (String) context.get("currencyUomId");
		
		
		
		for (GenericValue aLoan : activeLoans) {
			try {
				List<GenericValue> loanPaymentPlanList = delegator.findByAnd("LoanPaymentPlan", UtilMisc.toMap(
				        "loanId", aLoan.get("loanId"),
				        "statusId", "LOAN_PMTPLAN_APPRVD"), null, false);
				
				if (loanPaymentPlanList.size() != 0) {
					GenericValue loanPaymentPlan = loanPaymentPlanList.get(0);
					totalRepaymentFromPaymentPlans = totalRepaymentFromPaymentPlans.add(loanPaymentPlan.getBigDecimal("monthlyPaybackAmount"));
				}
				
			} catch (GenericEntityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
		
		for (GenericValue aLoan : activeLoans) {
			try {
				List<GenericValue> loanPaymentPlanList = delegator.findByAnd("LoanPaymentPlan", UtilMisc.toMap(
				        "loanId", aLoan.get("loanId"),
				        "statusId", "LOAN_PMTPLAN_APPRVD")
						, null, false);
				if (loanPaymentPlanList.size() != 0) {
					GenericValue loanPaymentPlan = loanPaymentPlanList.get(0);
					if (loanPaymentPlan != null) {
						
						BigDecimal loanPaymentPlanRepaymentAmount = (BigDecimal) loanPaymentPlan.get("monthlyPaybackAmount");
						BigDecimal ratioPerLoan = loanPaymentPlanRepaymentAmount.divide(totalRepaymentFromPaymentPlans, 5, BigDecimal.ROUND_HALF_UP);
						BigDecimal actualLoanRepaymentAmount = ratioPerLoan.multiply(repaymentAmount);
						BigDecimal loanPaymentPlanPrincipalAmount = (BigDecimal) loanPaymentPlan.get("monthlyPrincipalPaybackAmount");
						BigDecimal principalRatio = loanPaymentPlanPrincipalAmount.divide(loanPaymentPlanRepaymentAmount, 5, BigDecimal.ROUND_HALF_UP);
						BigDecimal actualLoanPrincipalAmount = principalRatio.multiply(actualLoanRepaymentAmount);
						BigDecimal actualLoanInterestAmount = actualLoanRepaymentAmount.subtract(actualLoanPrincipalAmount);
						
						principalAmount = principalAmount.add(actualLoanPrincipalAmount);
						interestAmount = interestAmount.add(actualLoanInterestAmount);
						
						GenericValue loanRepaymentLine = delegator.makeValue("LoanRepaymentLine", UtilMisc.toMap("loanId", aLoan.get("loanId")));
						
						loanRepaymentLine.put("principalAmount", actualLoanPrincipalAmount);
						loanRepaymentLine.put("interestAmount", actualLoanInterestAmount);
						loanRepaymentLine.put("totalAmount", actualLoanPrincipalAmount.add(actualLoanInterestAmount));
						loanRepaymentLine.put("currencyUomId", currencyUomId);
						loanRepaymentLine.put("loanPaymentPlanId", loanPaymentPlan.get("loanPaymentPlanId"));
						
						//String installmentSeqId = (String) context.get("installmentSeqId");
						long installmentSeqId_int;
						
						if (aLoan.getLong("lastInstallmentSeqId") != null) {
							if(loanRepaymentLine.getLong("installmentSeqId") != null) {
								installmentSeqId_int = loanRepaymentLine.getLong("installmentSeqId");
								loanRepaymentLine.set("installmentSeqId", installmentSeqId_int + 1);
							} else {
								loanRepaymentLine.set("installmentSeqId", new Long ("1"));
							}
							
						} else {
							loanRepaymentLine.put("installmentSeqId", new Long ("1"));
						};
						
						loanRepaymentLines.add(loanRepaymentLine);
					}
				}
			} catch (GenericEntityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
		
		GenericValue loanRepayment = delegator.makeValue("LoanRepayment", UtilMisc.toMap("repaymentAmount", repaymentAmount, "principalAmount", principalAmount, "interestAmount", interestAmount));
		
		Map<String, Object> resultMap = ServiceUtil.returnSuccess();
		resultMap.put("loanRepaymentLines", loanRepaymentLines);
		resultMap.put("loanRepayment", loanRepayment);
		
		Debug.logInfo("My Log:  loanRepayment.repaymentAmount is: " + loanRepayment.getBigDecimal("repaymentAmount") , null);
		Debug.logInfo("My Log:  loanRepaymentLines has these number of elements: " + loanRepaymentLines.size() , null);
		
		return resultMap;
	}
	
	public static Map<String, Object> createLoanRepaymentAndLines(DispatchContext dctx, Map<String, ? extends Object> context){
			
		Delegator delegator = dctx.getDelegator();
	
		GenericValue loanRepayment = (GenericValue) context.get("loanRepayment");
		List <GenericValue> loanRepaymentLines = (List <GenericValue>) context.get("loanRepaymentLines");
		
		// Create the loanRepayment record
		try {
			if (loanRepayment.getString("loanRepaymentId") == null) {
				String loanRepaymentId = delegator.getNextSeqId("LoanRepayment");
				loanRepayment.put("loanRepaymentId", loanRepaymentId);
			}
			loanRepayment.put("statusId", "LOAN_REPMT_CRTD");
			loanRepayment.create();
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		// Create the loanRepaymentLines record
		for(GenericValue loanRepaymentLine : loanRepaymentLines) {
			try {
				loanRepaymentLine.put("loanRepaymentId", loanRepayment.getString("loanRepaymentId"));
				loanRepaymentLine.create();
			} catch (GenericEntityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		Map<String, Object> resultMap = ServiceUtil.returnSuccess();
		
		resultMap.put("loanRepaymentLines", loanRepaymentLines);
		resultMap.put("loanRepayment", loanRepayment);
		
		Debug.logInfo("My Log:  loanRepayment.repaymentAmount is: " + loanRepayment.getBigDecimal("repaymentAmount") , null);
		Debug.logInfo("My Log:  loanRepaymentLines has these number of elements: " + loanRepaymentLines.size() , null);
		
		return resultMap;
	}
	
	public static Map<String, Object> createLoanRepaymentLines_v2(DispatchContext dctx, Map<String, ? extends Object> context){
		
		Delegator delegator = dctx.getDelegator();
		
		BigDecimal totalPrincipalAmount = BigDecimal.ZERO;
		BigDecimal totalInterestAmount = BigDecimal.ZERO;
		
		List <GenericValue> loanRepaymentLines = (List <GenericValue>) context.get("loanRepaymentLines");
		
		// Create the loanRepaymentLines record
		for(GenericValue loanRepaymentLine : loanRepaymentLines) {
			try {
				totalPrincipalAmount = totalPrincipalAmount.add(loanRepaymentLine.getBigDecimal("principalAmount"));
				totalInterestAmount = totalInterestAmount.add(loanRepaymentLine.getBigDecimal("interestAmount"));
				loanRepaymentLine.put("loanRepaymentId", loanRepaymentLine.getString("loanRepaymentId"));
				loanRepaymentLine.create();
			} catch (GenericEntityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// Update LoanRepayment fields
		String loanRepaymentId = loanRepaymentLines.get(0).getString("loanRepaymentId");
		Debug.logInfo("My Log:  loanRepaymentLines first element loanRepaymentId: " +  loanRepaymentLines.get(0).getString("loanRepaymentId"), null);
		
		try {
			GenericValue loanRepayment = delegator.findOne("LoanRepayment", UtilMisc.toMap("loanRepaymentId", loanRepaymentId), false);
			loanRepayment.set("principalAmount", totalPrincipalAmount);
			loanRepayment.set("interestAmount", totalInterestAmount);
			loanRepayment.store();
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Debug.logError(e, "Unable to update loanRepayment.", module);
            return(ServiceUtil.returnError(e.getMessage()));
		}
		
		Map<String, Object> resultMap = ServiceUtil.returnSuccess();
		
		resultMap.put("loanRepaymentLines", loanRepaymentLines);
		resultMap.put("loanRepaymentId", loanRepaymentId);

		Debug.logInfo("My Log:  loanRepaymentLines has these number of elements: " + loanRepaymentLines.size() , null);
		
		return resultMap;
	}
	
	public static Map<String, Object> createLoanRepaymentLines(DispatchContext dctx, Map<String, ? extends Object> context){
		Delegator delegator = dctx.getDelegator();
		
		String loanRepaymentId = (String) context.get("loanRepaymentId");
		List<GenericValue> activePartyLoansWithApprovedPaymentPlans = (List<GenericValue>) context.get("activePartyLoansWithApprovedPaymentPlans");
		
		for (GenericValue ln : activePartyLoansWithApprovedPaymentPlans) {
			String loanId = ln.getString("loanId");
					
			try {
				GenericValue loanRepaymentLine = delegator.create("LoanRepaymentLine", UtilMisc.toMap("loanRepaymentId", loanRepaymentId,"loanId", loanId));
				List<GenericValue> pmtPlans = ln.getRelated("LoanPaymentPlan", UtilMisc.toMap("loanId", loanId), null, false);
				
				if (UtilValidate.isEmpty(pmtPlans) == false) {
					BigDecimal principalAmount = pmtPlans.get(0).getBigDecimal("monthlyPrincipalPaybackAmount");
					BigDecimal interestAmount = pmtPlans.get(0).getBigDecimal("monthlyInterestPaybackAmount");
					Long installmentSeqId = null;
					String loanPaymentPlanId = null;
					
					if (ln.getLong("lastInstallmentSeqId") == null) {
						installmentSeqId = (long) 1;
						loanPaymentPlanId = pmtPlans.get(0).getString("loanPaymentPlanId");
					} else {
						installmentSeqId = ln.getLong("lastInstallmentSeqId") + 1;
						loanPaymentPlanId = pmtPlans.get(0).getString("loanPaymentPlanId");
					}
					
					loanRepaymentLine.set("principalAmount", principalAmount);
					loanRepaymentLine.set("interestAmount", interestAmount);
					loanRepaymentLine.set("installmentSeqId", installmentSeqId);
					loanRepaymentLine.set("loanPaymentPlanId", loanPaymentPlanId);
					
					loanRepaymentLine.store();
				}
			} catch (GenericEntityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		List<GenericValue> loanRepaymentLines = null;
		
		try {
			loanRepaymentLines = delegator.findByAnd("LoanRepaymentLine", UtilMisc.toMap("loanRepaymentId", loanRepaymentId), null, false);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Map<String, Object> resultMap = ServiceUtil.returnSuccess();
		resultMap.put("loanRepaymentLines", loanRepaymentLines);
		resultMap.put("loanRepaymentId",loanRepaymentId);
		
		return resultMap;
	}
	
	public static Map<String, Object> createLoanRepayment_v2(DispatchContext dctx, Map<String, ? extends Object> context){
		
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		
		BigDecimal repaymentAmount = (BigDecimal) context.get("repaymentAmount");
		String currencyUomId = (String) context.get("currencyUomId");
		Timestamp repaymentDate = (Timestamp) context.get("repaymentDate");
		
		String loanRepaymentId = (String) context.get("loanRepaymentId");
		String partyId = (String) context.get("partyId");
		
		if (loanRepaymentId == null) {
			loanRepaymentId = delegator.getNextSeqId("LoanRepayment");
		}
		
		GenericValue loanRepayment = delegator.makeValue("LoanRepayment", UtilMisc.toMap("loanRepaymentId", loanRepaymentId));
		
		loanRepayment.put("repaymentAmount", repaymentAmount);
		loanRepayment.put("currencyUomId", currencyUomId);
		loanRepayment.put("repaymentDate", repaymentDate);
		loanRepayment.put("partyId", partyId);
		loanRepayment.put("statusId","LOAN_REPMT_CRTD");
		
		try {
			loanRepayment.create();
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Debug.logError(e, "Unable to create loanRepayment.", module);
            return(ServiceUtil.returnError(e.getMessage()));
		}
		
		//TODO update LoanRepaymentStatus
		
		Map<String, Object> loanRepaymentStatusParams = UtilMisc.toMap(
				"loanRepaymentId", loanRepayment.getString("loanRepaymentId"), 
				"statusId", loanRepayment.getString("statusId"),
				"changeReason", "Creation of new loanRepayment record.",
				"userLogin", userLogin);
		Map<String, Object> loanRepaymentStatusResultMap = FastMap.newInstance();
		

		try {
			loanRepaymentStatusResultMap = dispatcher.runSync("updateLoanRepaymentStatus", loanRepaymentStatusParams);
		} catch (GenericServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Debug.logError(e, "Unable to update LoanRepayment Status.", module);
            return(ServiceUtil.returnError(e.getMessage()));
		}	
		
		Map<String, Object> resultMap = ServiceUtil.returnSuccess();
		resultMap.put("loanRepaymentId", loanRepaymentId);
		resultMap.put("loanRepaymentStatusId", loanRepaymentStatusResultMap.get("loanRepaymentStatusId"));
		
		return resultMap;
	}
	
	// Replace this with another function that does not use lambda function
	//public static Predicate<GenericValue> isFullyPaid() {
       // return p -> p.getLong("noOfInstallments") == p.getLong("lastInstallmentSeqId");
    //}
	
	public static Map<String, Object> createLoanRepayment(DispatchContext dctx, Map<String, ? extends Object> context){
		
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		
		BigDecimal repaymentAmount = (BigDecimal) context.get("repaymentAmount");
		String currencyUomId = (String) context.get("currencyUomId");
		Timestamp repaymentDate = (Timestamp) context.get("repaymentDate");
		
		String loanRepaymentId = (String) context.get("loanRepaymentId");
		String partyId = (String) context.get("partyId");
		
		// Preconditions start
		// -------------------
		// 1. Party has approved and active loans
			GenericValue party = null;
			List<GenericValue> activePartyLoans = FastList.newInstance();;
			try {
				
				party = delegator.findOne("Party", false, UtilMisc.toMap("partyId", partyId));
				activePartyLoans = party.getRelated("Loan", UtilMisc.toMap("partyId", partyId, "statusId", "LOAN_APPRVD"), null, false);
				
				Debug.logInfo("My Log: party: " + partyId , null);
				Debug.logInfo("My Log: activePartyLoans has these number of elements: " + activePartyLoans.size() , null);
				
				if (UtilValidate.isEmpty(activePartyLoans)) {
					Map<String, Object> resultMap = ServiceUtil.returnFailure("No active and approved loans for party: " + partyId);
					return resultMap;
				}
				
			} catch (GenericEntityException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
		
		// 1b. Exclude loans that are fully paid
			//  Find an alternative to this can only work on java 1.8 up
			//activePartyLoans.removeIf(isFullyPaid());
		
		// 2. The loans have approved loan payment plans -> remove all loans without approved payment plans
			List<GenericValue> activePartyLoansWithApprovedPaymentPlans = FastList.newInstance();
			try {
				for(GenericValue apl : activePartyLoans) {
					List<GenericValue> paymentPlans = apl.getRelated("LoanPaymentPlan", UtilMisc.toMap("statusId", "LOAN_PMTPLAN_APPRVD"), null, false);
					
					if (UtilValidate.isEmpty(paymentPlans) == false) {
						activePartyLoansWithApprovedPaymentPlans.add(apl);
					}	
					
				}
				// if there are no approved payment plans return failure
				if (UtilValidate.isEmpty(activePartyLoansWithApprovedPaymentPlans)) {
					Map<String, Object> resultMap = ServiceUtil.returnFailure("No approved paymentplans for loans");
					return resultMap;
				} else {
					Debug.logInfo("My Log: activePartyLoansWithApprovedPaymentPlans has these number of elements after removal of ones with unapproved payment plans: " + activePartyLoansWithApprovedPaymentPlans.size() , null);
				}
			} catch (GenericEntityException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			
		// 3. No unapproved repayments for the party
			try {
				List<GenericValue> unApprovedloanRepayments = party.getRelated("PaidByLoanRepayment", UtilMisc.toMap("partyId", partyId, "statusId", "LOAN_REPMT_CRTD"), null, false);
				
				if (UtilValidate.isEmpty(unApprovedloanRepayments) == false) {
					Debug.logInfo("My Log: unApprovedloanRepayments has these number of elements: " + unApprovedloanRepayments.size() , null);
					
					//remove comments below after completion of this component
					//Map<String, Object> resultMap = ServiceUtil.returnFailure("Unapproved loan repayments exist, kindly approve before proceeding!");
					//return resultMap;
				}	
			} catch (GenericEntityException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			
			
		// 4. Total of all repayment amounts for all payment plans adds up to the repayment amount
			BigDecimal calculatedTotalRepaymentAmount = BigDecimal.ZERO;
			BigDecimal calculatedPrincipalAmount = BigDecimal.ZERO;
			BigDecimal calculatedInterestAmount = BigDecimal.ZERO;
			try {
				for(GenericValue apl : activePartyLoansWithApprovedPaymentPlans) {
					List<GenericValue> paymentPlans = apl.getRelated("LoanPaymentPlan", UtilMisc.toMap("statusId", "LOAN_PMTPLAN_APPRVD"), null, false);
					if (UtilValidate.isEmpty(paymentPlans) == false) {
						Debug.logInfo("My Log: paymentPlans has these number of elements: " + paymentPlans.size() , null);

						//validPaymentPlans.add(paymentPlans.get(0));
						BigDecimal monthlyPaybackAmount  = paymentPlans.get(0).getBigDecimal("monthlyPaybackAmount");
						BigDecimal monthlyPrincipalPaybackAmount  = paymentPlans.get(0).getBigDecimal("monthlyPrincipalPaybackAmount");
						BigDecimal monthlyInterestPaybackAmount  = paymentPlans.get(0).getBigDecimal("monthlyInterestPaybackAmount");
						
						calculatedTotalRepaymentAmount = calculatedTotalRepaymentAmount.add(monthlyPaybackAmount);
						calculatedPrincipalAmount = calculatedPrincipalAmount.add(monthlyPrincipalPaybackAmount);
						calculatedInterestAmount = calculatedInterestAmount.add(monthlyInterestPaybackAmount);
						
					}
				}
				
				Debug.logInfo("My Log: calculatedTotalRepaymentAmount: " + calculatedTotalRepaymentAmount.toString() , null);
				Debug.logInfo("My Log: calculatedPrincipalAmount: " + calculatedPrincipalAmount.toString() , null);
				Debug.logInfo("My Log: calculatedInterestAmount: " + calculatedInterestAmount.toString() , null);

				if (calculatedTotalRepaymentAmount.equals(repaymentAmount) == false) {
					Map<String, Object> resultMap = ServiceUtil.returnFailure("Repayment amount does not match total outstanding repayment per month!");
					return resultMap;	
				}
			} catch (GenericEntityException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}

		// Preconditions end
		// -----------------
		
			
		//Create loanRepayment
		if (loanRepaymentId == null) {
			loanRepaymentId = delegator.getNextSeqId("LoanRepayment");
		}
		
		GenericValue loanRepayment = null;
		
		try {
			loanRepayment = delegator.create("LoanRepayment", UtilMisc.toMap("loanRepaymentId", loanRepaymentId));
		} catch (GenericEntityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			Map<String, Object> resultMap = ServiceUtil.returnError(e1.getMessage());
			return resultMap;	
		}
		
		loanRepayment.set("repaymentAmount", repaymentAmount);
		loanRepayment.set("repaymentDate", repaymentDate);
		loanRepayment.set("principalAmount", calculatedPrincipalAmount);
		loanRepayment.set("interestAmount", calculatedInterestAmount);
		loanRepayment.set("currencyUomId", currencyUomId);
		loanRepayment.set("statusId", "LOAN_REPMT_CRTD");
		
		try {
			loanRepayment.store();
		} catch (GenericEntityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		//TODO update LoanRepaymentStatus
		
		Map<String, Object> loanRepaymentStatusParams = UtilMisc.toMap(
				"loanRepaymentId", loanRepayment.getString("loanRepaymentId"), 
				"statusId", loanRepayment.getString("statusId"),
				"changeReason", "Creation of new loanRepayment record.",
				"userLogin", userLogin);
		Map<String, Object> loanRepaymentStatusResultMap = FastMap.newInstance();
		

		try {
			loanRepaymentStatusResultMap = dispatcher.runSync("updateLoanRepaymentStatus", loanRepaymentStatusParams);
		} catch (GenericServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Debug.logError(e, "Unable to update LoanRepayment Status.", module);
            return(ServiceUtil.returnError(e.getMessage()));
		}	
		
		//Create Loan Repayment Lines
		
		Map<String, Object> createLoanRepaymentLinesMap = UtilMisc.toMap("loanRepaymentId", loanRepaymentId, "activePartyLoansWithApprovedPaymentPlans", activePartyLoansWithApprovedPaymentPlans, "userLogin", userLogin);
		
		try {
			dispatcher.runSync("createLoanRepaymentLines", createLoanRepaymentLinesMap);
		} catch (GenericServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Map<String, Object> resultMap = ServiceUtil.returnSuccess();
		resultMap.put("loanRepaymentId", loanRepaymentId);
		resultMap.put("loanRepaymentStatusId", loanRepaymentStatusResultMap.get("loanRepaymentStatusId"));
		
		return resultMap;
	}


	public static Map<String, Object> updateLoanRepayment(DispatchContext dctx, Map<String, ? extends Object> context){
		
		Delegator delegator = dctx.getDelegator();
		
		BigDecimal repaymentAmount = (BigDecimal) context.get("repaymentAmount");
		BigDecimal principalAmount = (BigDecimal) context.get("principalAmount");
		BigDecimal interestAmount = (BigDecimal) context.get("interestAmount");
		String currencyUomId = (String) context.get("currencyUomId");
		Timestamp repaymentDate = (Timestamp) context.get("repaymentDate");
		String loanRepaymentId = (String) context.get("loanRepaymentId");
		String partyId = (String) context.get("partyId");
		
		GenericValue loanRepayment = null;
				
		try {
			loanRepayment = delegator.findOne("LoanRepayment", UtilMisc.toMap("loanRepaymentId", loanRepaymentId), false);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		if (repaymentAmount != null) {
			loanRepayment.set("repaymentAmount", repaymentAmount);
		}
		if (principalAmount != null) {
			loanRepayment.set("principalAmount", principalAmount);
		}
		if (interestAmount != null) {
			loanRepayment.set("interestAmount", interestAmount);
		}
		if (currencyUomId != null) {
			loanRepayment.set("currencyUomId", currencyUomId);
		}
		if (repaymentDate != null) {
			loanRepayment.set("repaymentDate", repaymentDate);
		}
		if (partyId != null) {
			loanRepayment.set("partyId", partyId);
		}
		
		try {
			loanRepayment.store();
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Map<String, Object> resultMap = ServiceUtil.returnSuccess();
		
		resultMap.put("loanRepaymentId", loanRepaymentId);
		
		return resultMap;
	}
	
	public static Map<String, Object> updateLoanRepaymentStatus(DispatchContext dctx, Map<String, ? extends Object> context){
		
		Delegator delegator = dctx.getDelegator();
		
		String statusId = (String) context.get("statusId");
		String changeReason = (String) context.get("changeReason");
		String loanRepaymentId = (String) context.get("loanRepaymentId");
		Locale locale = (Locale) context.get("locale");
		
		GenericValue userLogin = (GenericValue) context.get("userLogin");	
		String statusUserLogin = (String) userLogin.get("userLoginId");
		String loanRepaymentStatusId = (String) context.get("loanRepaymentStatusId");
		
		if (loanRepaymentId == null) {
            return ServiceUtil.returnError(UtilProperties.getMessage(resourceError,
                    "LoanRepaymentIdNotAvailableForStatusUpdate", locale));
        }
		
		if (loanRepaymentStatusId == null) {
			loanRepaymentStatusId = delegator.getNextSeqId("LoanRepaymentStatus");
		}
		
		GenericValue loanRepaymentStatus = delegator.makeValue("LoanRepaymentStatus", UtilMisc.toMap("loanRepaymentStatusId", loanRepaymentStatusId));
		
		loanRepaymentStatus.put("statusId", statusId);
		loanRepaymentStatus.put("changeReason", changeReason);
		loanRepaymentStatus.put("loanRepaymentId", loanRepaymentId);
		loanRepaymentStatus.put("loanRepaymentStatusId",loanRepaymentStatusId);
		loanRepaymentStatus.put("statusUserLogin",statusUserLogin);
		loanRepaymentStatus.put("statusDate",UtilDateTime.nowTimestamp());
		
		try {
			loanRepaymentStatus.create();
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
		}
		
		GenericValue loanRepayment = null;
				
		try {
			loanRepayment = delegator.findOne("LoanRepayment", UtilMisc.toMap("loanRepaymentId", loanRepaymentId), false);
			loanRepayment.set("statusId", statusId);
			loanRepayment.store();
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		List<GenericValue> loanRepaymentLines = new LinkedList<GenericValue>();
		
		Debug.logInfo("My Log: statusId in module: " + module + " - " + statusId , null);
		
		if(statusId != null) {
			if(statusId.contains("LOAN_REPMT_APPRVD")) {
				//get loanRepaymentLineItems
				Debug.logInfo("My Log: -If Construct- statusId: " + statusId , null);
				
				try {
					loanRepaymentLines = loanRepayment.getRelated("LoanRepaymentLine", null, null, false);
					
					/**
					 * loanRepaymentLines = delegator.findByAnd("LoanRepaymentLine", 
					 *	UtilMisc.toMap("loanRepaymentId",loanRepaymentId), 
					 *		null, 
					 *		false);
					 */
					
				} catch (GenericEntityException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					Debug.logError("Error in getRelated method of loanRepayment GenericValue", module);
				}
				
				Debug.logInfo("My Log:  loanRepaymentLines: " + loanRepaymentLines.toString() , null);
				
				BigDecimal totalPrincipalPaid = BigDecimal.ZERO;
				BigDecimal totalInterestPaid = BigDecimal.ZERO;
				
				//add 
				for(GenericValue loanRepaymentLine : loanRepaymentLines) {
					//update loan fields
					
					try {
						GenericValue loan = delegator.findOne(
								"Loan", 
								UtilMisc.toMap("loanId", loanRepaymentLine.getString("loanId")),
								false);
						
						/** update totalPrincipalPaid by adding loanRepaymentLine.principalAmount to loan.totalPrincipalPaid
						 */
						
						if (loan.getBigDecimal("totalPrincipalPaid") != null) {
							totalPrincipalPaid = loan.getBigDecimal("totalPrincipalPaid").add(loanRepaymentLine.getBigDecimal("principalAmount"));
						} else {
							totalPrincipalPaid = loanRepaymentLine.getBigDecimal("principalAmount");
						}
						
						/** update totalInterestPaid by adding loanRepaymentLine.principalAmount to loan.totalInterestPaid
						 */
												
						if (loan.getBigDecimal("totalInterestPaid") != null) {
							totalInterestPaid = loan.getBigDecimal("totalInterestPaid").add(loanRepaymentLine.getBigDecimal("interestAmount"));
						} else {
							totalInterestPaid = loanRepaymentLine.getBigDecimal("interestAmount");
						}
						
						loan.set("totalPrincipalPaid", totalPrincipalPaid);
						loan.set("totalInterestPaid", totalInterestPaid);
						
						/** calculate balancePrincipalAmount by subtracting loan.totalPrincipalPaid from loan.initialPrincipalAmount
						 */
						loan.set("balancePrincipalAmount", loan.getBigDecimal("initialPrincipalAmount").subtract(loan.getBigDecimal("totalPrincipalPaid")));
						
						
						Debug.logInfo("My Log:  loanId: " + loan.getString("loanId") , null);
						
						/** Attribute Modification: attribute lastInstallmentSeqId of loan is increased by one, 
						 * noOfUnpaidInstallments is reduced by 1.
						 * 
						 */
						
						if(loan.getLong("lastInstallmentSeqId") != null) {
							long lastInstallmentSeqId = loan.getLong("lastInstallmentSeqId");
							Debug.logInfo("My Log:  lastInstallmentSeqId not null: old value is: " + lastInstallmentSeqId, null);
							
							loan.set("lastInstallmentSeqId", lastInstallmentSeqId + 1);	
							
							Debug.logInfo("My Log:  lastInstallmentSeqId not null: new value is: " + lastInstallmentSeqId, null);
						} else {
							loan.set("lastInstallmentSeqId", new Long ("1"));
						}
						
						long noOfUnpaidInstallments = loan.getLong("noOfUnpaidInstallments");
						
						loan.set("noOfUnpaidInstallments", noOfUnpaidInstallments - 1);
						
						Debug.logInfo("My Log: lastInstallmentSeqId: " + loan.getLong("lastInstallmentSeqId") , null);
						
						loan.store();
						
						Debug.logInfo("My Log: lastInstallmentSeqId: " + loan.getLong("lastInstallmentSeqId") , null);

						/** Instance Creation: new instance of Payment payment is created
						 * 
						 */
						
						
					} catch (GenericEntityException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Debug.logError(e.getMessage(), module);
					}
				}
				
				
			}
		}

		
		Map<String, Object> resultMap = ServiceUtil.returnSuccess();
		
		resultMap.put("loanRepaymentStatusId", loanRepaymentStatusId);
		
		
		return resultMap;
	}
	
}
