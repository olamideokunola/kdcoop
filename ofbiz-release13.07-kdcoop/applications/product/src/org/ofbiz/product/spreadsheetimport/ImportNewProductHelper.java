/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/

package org.ofbiz.product.spreadsheetimport;

import java.math.BigDecimal;
import java.util.Map;
import java.sql.Timestamp;
import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;

public class ImportNewProductHelper {

    static String module = ImportNewProductHelper.class.getName();

    // prepare the product map
    public static Map<String, Object> prepareProduct(String productId, String productTypeId,
    		String internalName, String productName, String description, String quantityUomId,
    		String isVirtual, String isVariant) {
        Map<String, Object> fields = FastMap.newInstance();
        fields.put("productId", productId);
        fields.put("productTypeId", productTypeId);
        fields.put("internalName", internalName);
        fields.put("productName", productName);
        fields.put("description", description);
        fields.put("quantityUomId", quantityUomId);
        fields.put("isVirtual", isVirtual);
        fields.put("isVariant", isVariant);
        return fields;
    }

    // prepare the inventoryItem map
    public static Map<String, Object> prepareInventoryItem(String productId,
            BigDecimal quantityOnHand, String inventoryItemId) {
        Map<String, Object> fields = FastMap.newInstance();
        fields.put("inventoryItemId", inventoryItemId);
        fields.put("inventoryItemTypeId", "NON_SERIAL_INV_ITEM");
        fields.put("productId", productId);
        fields.put("ownerPartyId", "Company");
        fields.put("facilityId", "WebStoreWarehouse");
        fields.put("quantityOnHandTotal", quantityOnHand);
        fields.put("availableToPromiseTotal", quantityOnHand);
        return fields;
    }

    // prepare the productCategoryMember map
    public static Map<String, Object> prepareproductCategoryMember(String productCategoryId,
    		String productId, Timestamp fromDate) {
        Map<String, Object> fields = FastMap.newInstance();
        fields.put("productCategoryId", productCategoryId);
        fields.put("productId", productId);
        fields.put("fromDate", fromDate);
        return fields;
    }

    // check if product already exists in database
    public static boolean checkProductExists(String productId,
            Delegator delegator) {
        GenericValue tmpProductGV;
        boolean productExists = false;
        try {
            tmpProductGV = delegator.findOne("Product", UtilMisc
                .toMap("productId", productId), false);
            if (tmpProductGV != null
                    && productId.equals(tmpProductGV.getString("productId")))
                productExists = true;
        } catch (GenericEntityException e) {
            Debug.logError("Problem in reading data of product", module);
        }
        return productExists;
    }

    // check if product category already exists in database
    public static boolean checkProductCategoryExists(String productCategoryId,
            Delegator delegator) {
        GenericValue tmpProductCategoryGV;
        boolean productCategoryExists = false;
        try {
        	tmpProductCategoryGV = delegator.findOne("ProductCategory", UtilMisc
                .toMap("productCategoryId", productCategoryId), false);
            if (tmpProductCategoryGV != null
                    && productCategoryId.equals(tmpProductCategoryGV.getString("productCategoryId")))
            	productCategoryExists = true;
        } catch (GenericEntityException e) {
            Debug.logError("Problem in reading data of product category", module);
        }
        return productCategoryExists;
    }
}
