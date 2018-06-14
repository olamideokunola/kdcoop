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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.sql.Timestamp;

import javolution.util.FastList;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.ServiceUtil;

public class ImportNewProductServices {

    public static String module = ImportNewProductServices.class.getName();
    public static final String resource = "ProductUiLabels";
    
    /**
     * This method is responsible to import spreadsheet data into "Product"
     * entity into database. The method uses the
     * ImportProductHelper class to perform its operation. The method uses "Apache
     * POI" api for importing spreadsheet (xls files) data.
     *
     * Note : Create the spreadsheet directory in the ofbiz home folder and keep
     * your xls files in this folder only.
     *
     * @param dctx the dispatch context
     * @param context the context
     * @return the result of the service execution
     */
    public static Map<String, Object> productCustomImportFromSpreadsheet(DispatchContext dctx, Map<String, ? extends Object> context) {
        Delegator delegator = dctx.getDelegator();
        Locale locale = (Locale) context.get("locale");
        // System.getProperty("user.dir") returns the path upto ofbiz home
        // directory
        String path = System.getProperty("user.dir") + "/spreadsheet";
        List<File> fileItems = FastList.newInstance();

        if (UtilValidate.isNotEmpty(path)) {
            File importDir = new File(path);
            if (importDir.isDirectory() && importDir.canRead()) {
                File[] files = importDir.listFiles();
                // loop for all the containing xls file in the spreadsheet
                // directory
                for (int i = 0; i < files.length; i++) {
                    if (files[i].getName().toUpperCase().endsWith("XLS")) {
                        fileItems.add(files[i]);
                    }
                }
            } else {
                return ServiceUtil.returnError(UtilProperties.getMessage(resource, 
                        "ProductProductImportDirectoryNotFound", locale));
            }
        } else {
            return ServiceUtil.returnError(UtilProperties.getMessage(resource, 
                    "ProductProductImportPathNotSpecified", locale));
        }

        if (fileItems.size() < 1) {
            return ServiceUtil.returnError(UtilProperties.getMessage(resource, 
                    "ProductProductImportPathNoSpreadsheetExists", locale) + path);
        }

        for (File item: fileItems) {
            // read all xls file and create workbook one by one.
            List<Map<String, Object>> products = FastList.newInstance();
            //List<Map<String, Object>> inventoryItems = FastList.newInstance();
            List<Map<String, Object>> productCategoryMembers = FastList.newInstance();
            POIFSFileSystem fs = null;
            HSSFWorkbook wb = null;
            try {
                fs = new POIFSFileSystem(new FileInputStream(item));
                wb = new HSSFWorkbook(fs);
            } catch (IOException e) {
                Debug.logError("Unable to read or create workbook from file", module);
                return ServiceUtil.returnError(UtilProperties.getMessage(resource, 
                        "ProductProductImportCannotCreateWorkbookFromFile", locale));
            }

            // get first sheet
            HSSFSheet sheet = wb.getSheetAt(0);
            int sheetLastRowNumber = sheet.getLastRowNum();
            for (int j = 1; j <= sheetLastRowNumber; j++) {
                HSSFRow row = sheet.getRow(j);
                if (row != null) {
                    // read productId from first column "sheet column index
                    // starts from 0"
                    HSSFCell cell0 = row.getCell(0);
                    cell0.setCellType(HSSFCell.CELL_TYPE_STRING);
                    String productId = cell0.getRichStringCellValue().toString();
                    // read productTypeId from second column
                    HSSFCell cell1 = row.getCell(1);
                    cell1.setCellType(HSSFCell.CELL_TYPE_STRING);
                    String productTypeId = cell1.getRichStringCellValue().toString();     
                    // read internalName from third column
                    HSSFCell cell2 = row.getCell(2);
                    cell2.setCellType(HSSFCell.CELL_TYPE_STRING);
                    String internalName = cell2.getRichStringCellValue().toString();  
                    // read productName from fourth column
                    HSSFCell cell3 = row.getCell(3);
                    cell3.setCellType(HSSFCell.CELL_TYPE_STRING);
                    String productName = cell3.getRichStringCellValue().toString();  
                    // read description from fifth column
                    HSSFCell cell4 = row.getCell(4);
                    cell4.setCellType(HSSFCell.CELL_TYPE_STRING);
                    String description = cell4.getRichStringCellValue().toString();  
                    // read quantityUomId from sixth column
                    HSSFCell cell5 = row.getCell(5);
                    cell5.setCellType(HSSFCell.CELL_TYPE_STRING);
                    String quantityUomId = cell5.getRichStringCellValue().toString();  
                    // read isVirtual from seventh column
                    HSSFCell cell6 = row.getCell(6);
                    cell6.setCellType(HSSFCell.CELL_TYPE_STRING);
                    String isVirtual = cell6.getRichStringCellValue().toString();  
                    // read isVariant from eighth column
                    HSSFCell cell7 = row.getCell(7);
                    cell7.setCellType(HSSFCell.CELL_TYPE_STRING);
                    String isVariant = cell7.getRichStringCellValue().toString();  
                    // read productCategoryId from ninth column
                    HSSFCell cell8 = row.getCell(8);
                    cell8.setCellType(HSSFCell.CELL_TYPE_STRING);
                    String productCategoryId = cell8.getRichStringCellValue().toString();  
                    // read fromDate from tenth column
                    HSSFCell cell9 = row.getCell(9);
                    cell9.setCellType(HSSFCell.CELL_TYPE_STRING);
                    Timestamp fromDate = Timestamp.valueOf(cell9.getRichStringCellValue().toString());
                    
                    // read QOH from ninth column
                    //HSSFCell cell5 = row.getCell(5);
                    //BigDecimal quantityOnHand = BigDecimal.ZERO;
                    //if (cell5 != null && cell5.getCellType() == HSSFCell.CELL_TYPE_NUMERIC)
                    //    quantityOnHand = new BigDecimal(cell5.getNumericCellValue());

                    
                    // check productId if null then skip creating product category item
                    // too.
                    boolean productExists = ImportProductHelper.checkProductExists(productId, delegator);

                    if (productId != null && !productId.trim().equalsIgnoreCase("") && !productExists) {
                        products.add(ImportNewProductHelper.prepareProduct(productId, productTypeId,
                        		internalName, productName, description, quantityUomId,
                        		isVirtual, isVariant));

                        productCategoryMembers.add(ImportNewProductHelper.prepareproductCategoryMember(productCategoryId, productId,
                                    fromDate));
                    }
                    int rowNum = row.getRowNum() + 1;
                    if (row.toString() != null && !row.toString().trim().equalsIgnoreCase("") && productExists) {
                        Debug.logWarning("Row number " + rowNum + " not imported from " + item.getName(), module);
                    }
                }
            }
            // create and store values in "Product" and "ProductCategoryMember" entity
            // in database
            for (int j = 0; j < products.size(); j++) {
                GenericValue productGV = delegator.makeValue("Product", products.get(j));
                GenericValue productCategoryMemberGV = delegator.makeValue("ProductCategoryMember", productCategoryMembers.get(j));
                if (!ImportProductHelper.checkProductExists(productGV.getString("productId"), delegator)) {
                    try {
                        delegator.create(productGV);
                        delegator.create(productCategoryMemberGV);
                    } catch (GenericEntityException e) {
                        Debug.logError("Cannot store product", module);
                        return ServiceUtil.returnError(UtilProperties.getMessage(resource, 
                                "ProductProductImportCannotStoreProduct", locale));
                    }
                }
            }
            int uploadedProducts = products.size() + 1;
            if (products.size() > 0)
                Debug.logInfo("Uploaded " + uploadedProducts + " products from file " + item.getName(), module);
        }
        return ServiceUtil.returnSuccess();
    }
}

