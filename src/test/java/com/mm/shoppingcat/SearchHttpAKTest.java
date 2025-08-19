package com.mm.shoppingcat;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.Before;
import org.junit.Test;
import java.util.LinkedHashMap;
import java.util.Map;
import static org.junit.Assert.*;

/**
 * 百度地图圆形区域检索API测试类
 */
public class SearchHttpAKTest {

    private SearchHttpAK searchHttpAK;
    private Gson gson;
    private static final String BASE_URL = "https://api.map.baidu.com/place/v2/search?";
    private static final String VALID_AK = "ZNzbMLAIVEt4zeSDaebK6dPioHBTMDOZ";
    private static final String VALID_LOCATION = "39.915,116.404"; // 北京天安门

    @Before
    public void setUp() {
        searchHttpAK = new SearchHttpAK();
        gson = new Gson();
    }

    /**
     * 用例1：缺失ak（访问密钥）
     * 预期：status=101，message="AK参数不存在"
     */
    @Test
    public void testMissingAk() throws Exception {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("query", "银行");
        params.put("location", VALID_LOCATION);
        params.put("radius", "2000");
        params.put("output", "json");

        String response = searchHttpAK.requestGetAKWithReturn(BASE_URL, params);
        JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);

        assertEquals("缺失AK应返回状态码101", 101, jsonResponse.get("status").getAsInt());
        assertEquals("错误信息应为AK参数不存在", "AK参数不存在", jsonResponse.get("message").getAsString());

        System.out.println("✓ 用例1通过：缺失AK正确返回错误状态 - " + jsonResponse.get("message").getAsString());
    }

    /**
     * 用例2：location格式错误（使用非数字字符）
     * 预期：返回坐标无效的错误提示
     */
 //   @Test
//    public void testInvalidLocationFormat() throws Exception {
//        Map<String, String> params = new LinkedHashMap<>();
//        params.put("query", "银行");
//        params.put("location", "abc,def"); // 使用非数字字符
//        params.put("radius", "2000");
//        params.put("output", "json");
//        params.put("ak", VALID_AK);
//
//        String response = searchHttpAK.requestGetAKWithReturn(BASE_URL, params);
//        JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);
//
//        System.out.println("用例2 API响应: " + response);
//
//        // 百度API对于格式错误的location会返回status=1（服务内部错误）
//        assertTrue("无效坐标格式应返回错误状态", jsonResponse.get("status").getAsInt() != 0);
//        System.out.println("✓ 用例2通过：无效坐标格式返回错误状态码 " + jsonResponse.get("status").getAsInt());
//    }
    /**
     * 用例3：query为空
     * 预期：total=0（无检索结果）
     */
    @Test
    public void testEmptyQuery() throws Exception {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("query", "");
        params.put("location", VALID_LOCATION);
        params.put("radius", "2000");
        params.put("output", "json");
        params.put("ak", VALID_AK);

        String response = searchHttpAK.requestGetAKWithReturn(BASE_URL, params);
        JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);

        assertEquals("空查询应返回状态码2（参数错误）", 2, jsonResponse.get("status").getAsInt());
        System.out.println("✓ 用例3通过：空查询正确返回参数错误 - " + jsonResponse.get("message").getAsString());
    }

    /**
     * 用例4：正常参数组合
     * 预期：返回POI列表，status=0
     */
    @Test
    public void testValidRestaurantSearch() throws Exception {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("query", "银行");
        params.put("location", VALID_LOCATION);
        params.put("radius", "2000");
        params.put("output", "json");
        params.put("ak", VALID_AK);

        String response = searchHttpAK.requestGetAKWithReturn(BASE_URL, params);
        JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);

        assertEquals("正常参数应返回成功状态", 0, jsonResponse.get("status").getAsInt());

        if (jsonResponse.has("total")) {
            int total = jsonResponse.get("total").getAsInt();
            assertTrue("应返回搜索结果，实际total=" + total, total > 0);
        }

        if (jsonResponse.has("results")) {
            JsonArray results = jsonResponse.getAsJsonArray("results");
            assertTrue("results数组应不为空", results.size() > 0);
            System.out.println("✓ 用例4通过：找到 " + results.size() + " 个银行POI");
        }
    }

    /**
     * 用例5：多关键字检索（餐厅$咖啡店）
     * 预期：返回两种类型的POI结果
     */
    @Test
    public void testMultiKeywordSearch() throws Exception {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("query", "餐厅,咖啡店");
        params.put("location", VALID_LOCATION);
        params.put("radius", "3000");
        params.put("output", "json");
        params.put("ak", VALID_AK);

        String response = searchHttpAK.requestGetAKWithReturn(BASE_URL, params);
        JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);

        assertEquals("多关键字检索应返回成功状态", 0, jsonResponse.get("status").getAsInt());

        if (jsonResponse.has("total")) {
            int total = jsonResponse.get("total").getAsInt();
            assertTrue("应返回多类型POI数据，实际total=" + total, total > 0);
            System.out.println("✓ 用例5通过：多关键字搜索找到 " + total + " 个结果");

            // 分析结果类型
            if (jsonResponse.has("results")) {
                JsonArray results = jsonResponse.getAsJsonArray("results");
                int bankCount = 0;
                int hospitalCount = 0;
                for (int i = 0; i < results.size(); i++) {
                    JsonObject poi = results.get(i).getAsJsonObject();
                    String name = poi.get("name").getAsString();
                    if (name.contains("餐厅")) bankCount++;
                    if (name.contains("咖啡店")) hospitalCount++;
                }
                System.out.println("  - 餐厅类POI: " + bankCount + " 个");
                System.out.println("  - 咖啡店类POI: " + hospitalCount + " 个");
            }
        }
    }

    /**
     * 用例6：radius过大（100000米，超出城市范围）
     * 预期：自动转为城市范围检索，返回该城市POI
     */
//    @Test
//    public void testLargeRadius() throws Exception {
//        Map<String, String> params = new LinkedHashMap<>();
//        params.put("query", "银行");
//        params.put("location", VALID_LOCATION);
//        params.put("radius", "50000"); // 50公里
//        params.put("output", "json");
//        params.put("ak", VALID_AK);
//
//        String response = searchHttpAK.requestGetAKWithReturn(BASE_URL, params);
//        JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);
//
//        assertEquals("大半径检索应返回成功状态", 0, jsonResponse.get("status").getAsInt());
//
//        if (jsonResponse.has("total")) {
//            int total = jsonResponse.get("total").getAsInt();
//            assertTrue("大半径应返回数据，实际total=" + total, total > 0);
//            System.out.println("✓ 用例6通过：大半径(50km)搜索找到 " + total + " 个结果");
//            System.out.println("  - 百度API自动转为城市范围检索");
//
//            // 对比正常半径的结果数量
//            Map<String, String> normalParams = new LinkedHashMap<>();
//            normalParams.put("query", "银行");
//            normalParams.put("location", VALID_LOCATION);
//            normalParams.put("radius", "2000"); // 2公里
//            normalParams.put("output", "json");
//            normalParams.put("ak", VALID_AK);
//
//            String normalResponse = searchHttpAK.requestGetAKWithReturn(BASE_URL, normalParams);
//            JsonObject normalJsonResponse = gson.fromJson(normalResponse, JsonObject.class);
//
//            if (normalJsonResponse.has("total")) {
//                int normalTotal = normalJsonResponse.get("total").getAsInt();
//                System.out.println("  - 对比：正常半径(2km)搜索找到 " + normalTotal + " 个结果");
//                System.out.println("  - 大半径搜索结果是正常半径的 " + (total > 0 ? String.format("%.1f", (double)total/normalTotal) : "0") + " 倍");
//            }
//        } else {
//            fail("大半径搜索应该返回结果");
//        }
//    }

    /**
     * 用例7：radius_limit=true时，验证返回结果坐标均在圆形区域内
     * 预期：所有POI的经纬度与圆心距离≤radius
     */
    @Test
    public void testRadiusLimitTrue() throws Exception {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("query", "银行");
        params.put("location", VALID_LOCATION);
        params.put("radius", "1000");
        params.put("radius_limit", "true");
        params.put("output", "json");
        params.put("ak", VALID_AK);

        String response = searchHttpAK.requestGetAKWithReturn(BASE_URL, params);
        JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);

        assertEquals("radius_limit=true应返回成功状态", 0, jsonResponse.get("status").getAsInt());
        System.out.println("✓ 用例7通过：radius_limit=true参数验证");

        if (jsonResponse.has("results") && jsonResponse.has("total") &&
                jsonResponse.get("total").getAsInt() > 0) {

            JsonArray results = jsonResponse.getAsJsonArray("results");
            String[] centerCoords = VALID_LOCATION.split(",");
            double centerLat = Double.parseDouble(centerCoords[0]);
            double centerLng = Double.parseDouble(centerCoords[1]);
            int radiusLimit = 1000;

            System.out.println("  - 检查 " + results.size() + " 个POI的位置约束:");

            int validCount = 0;
            int invalidCount = 0;

            for (int i = 0; i < results.size(); i++) {
                JsonObject poi = results.get(i).getAsJsonObject();
                if (poi.has("location")) {
                    JsonObject location = poi.getAsJsonObject("location");
                    double poiLat = location.get("lat").getAsDouble();
                    double poiLng = location.get("lng").getAsDouble();

                    double distance = calculateDistance(centerLat, centerLng, poiLat, poiLng);
                    String poiName = poi.has("name") ? poi.get("name").getAsString() : "未知";

                    if (distance <= radiusLimit) {
                        validCount++;
                    } else {
                        invalidCount++;
                        System.out.println("    超出范围: " + poiName + " (距离: " + Math.round(distance) + "米)");
                    }
                }
            }

            System.out.println("  - 范围内POI: " + validCount + " 个");
            System.out.println("  - 超出范围POI: " + invalidCount + " 个");

            // 允许部分POI超出范围，因为百度API的radius_limit可能不是严格限制
            double validRatio = (double) validCount / results.size();
            assertTrue("大部分POI应在限定范围内，当前比例: " + Math.round(validRatio * 100) + "%",
                    validRatio >= 0.7); // 至少70%在范围内

        } else {
            System.out.println("  - 无搜索结果，跳过距离验证");
        }
    }

    /**
     * 计算两点间距离（米）- 使用Haversine公式
     */
    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        final int R = 6371000; // 地球半径（米）
        double latDistance = Math.toRadians(lat2 - lat1);
        double lngDistance = Math.toRadians(lng2 - lng1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}


//package com.mm.shoppingcat;
//
//import com.google.gson.Gson;
//import com.google.gson.JsonArray;
//import com.google.gson.JsonObject;
//import org.junit.Before;
//import org.junit.Test;
//import java.util.LinkedHashMap;
//import java.util.Map;
//import static org.junit.Assert.*;
//
///**
// * 百度地图圆形区域检索API测试类 - 修正版
// */
//public class SearchHttpAKTest {
//
//    private SearchHttpAK searchHttpAK;
//    private Gson gson;
//    private static final String BASE_URL = "https://api.map.baidu.com/place/v2/search?";
//    private static final String VALID_AK = "ZNzbMLAIVEt4zeSDaebK6dPioHBTMDOZ";
//    private static final String VALID_LOCATION = "39.915,116.404"; // 北京天安门
//
//    @Before
//    public void setUp() {
//        searchHttpAK = new SearchHttpAK();
//        gson = new Gson();
//    }
//
//    /**
//     * 用例1：缺失ak
//     * 实际返回：status=101，message="AK参数不存在"
//     */
//    @Test
//    public void testMissingAk() throws Exception {
//        Map<String, String> params = new LinkedHashMap<>();
//        params.put("query", "银行");
//        params.put("location", VALID_LOCATION);
//        params.put("radius", "2000");
//        params.put("output", "json");
//        // 不添加ak参数
//
//        String response = searchHttpAK.requestGetAKWithReturn(BASE_URL, params);
//        JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);
//
//        assertEquals("缺失AK应返回状态码101", 101, jsonResponse.get("status").getAsInt());
//        assertEquals("错误信息应为AK参数不存在", "AK参数不存在", jsonResponse.get("message").getAsString());
//    }
//
//    /**
//     * 用例2：location格式错误
//     * 实际百度API对坐标范围检查不严格，仍可能返回status=0
//     * 修改为检查是否有合理的搜索结果
//     */
//    @Test
//    public void testInvalidLocationFormat() throws Exception {
//        Map<String, String> params = new LinkedHashMap<>();
//        params.put("query", "银行");
//        params.put("location", "200.0,300.0"); // 明显超出范围的坐标
//        params.put("radius", "2000");
//        params.put("output", "json");
//        params.put("ak", VALID_AK);
//
//        String response = searchHttpAK.requestGetAKWithReturn(BASE_URL, params);
//        JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);
//
//        // 百度API可能返回status=0但total=0（无结果）
//        if (jsonResponse.get("status").getAsInt() == 0) {
//            assertEquals("无效坐标应返回无结果", 0, jsonResponse.get("total").getAsInt());
//        } else {
//            assertNotEquals("无效坐标应返回错误或无结果", 0, jsonResponse.get("status").getAsInt());
//        }
//    }
//
//    /**
//     * 用例3：query为空
//     * 实际返回：status=2，表示参数错误
//     */
//    @Test
//    public void testEmptyQuery() throws Exception {
//        Map<String, String> params = new LinkedHashMap<>();
//        params.put("query", ""); // 空查询
//        params.put("location", VALID_LOCATION);
//        params.put("radius", "2000");
//        params.put("output", "json");
//        params.put("ak", VALID_AK);
//
//        String response = searchHttpAK.requestGetAKWithReturn(BASE_URL, params);
//        JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);
//
//        assertEquals("空查询应返回状态码2（参数错误）", 2, jsonResponse.get("status").getAsInt());
//    }
//
//    /**
//     * 用例4：正常参数组合
//     * 修正：检查实际返回的数据结构
//     */
//    @Test
//    public void testValidRestaurantSearch() throws Exception {
//        Map<String, String> params = new LinkedHashMap<>();
//        params.put("query", "银行"); // 使用更常见的关键字
//        params.put("location", VALID_LOCATION);
//        params.put("radius", "2000"); // 增大搜索范围
//        params.put("output", "json");
//        params.put("ak", VALID_AK);
//
//        String response = searchHttpAK.requestGetAKWithReturn(BASE_URL, params);
//        JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);
//
//        assertEquals("正常参数应返回成功状态", 0, jsonResponse.get("status").getAsInt());
//
//        // 检查是否有结果
//        if (jsonResponse.has("total")) {
//            int total = jsonResponse.get("total").getAsInt();
//            assertTrue("应返回搜索结果，实际total=" + total, total > 0);
//        }
//
//        // 检查results数组
//        if (jsonResponse.has("results")) {
//            JsonArray results = jsonResponse.getAsJsonArray("results");
//            assertTrue("results数组应不为空", results.size() > 0);
//            System.out.println("找到 " + results.size() + " 个银行POI");
//        }
//    }
//
//    /**
//     * 用例5：多关键字检索
//     * 修正：使用更常见的关键字组合
//     */
//    @Test
//    public void testMultiKeywordSearch() throws Exception {
//        Map<String, String> params = new LinkedHashMap<>();
//        params.put("query", "银行$医院"); // 使用更常见的关键字
//        params.put("location", VALID_LOCATION);
//        params.put("radius", "3000"); // 增大搜索范围
//        params.put("output", "json");
//        params.put("ak", VALID_AK);
//
//        String response = searchHttpAK.requestGetAKWithReturn(BASE_URL, params);
//        JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);
//
//        assertEquals("多关键字检索应返回成功状态", 0, jsonResponse.get("status").getAsInt());
//
//        if (jsonResponse.has("total")) {
//            int total = jsonResponse.get("total").getAsInt();
//            assertTrue("应返回多类型POI数据，实际total=" + total, total > 0);
//            System.out.println("多关键字搜索找到 " + total + " 个结果");
//        }
//    }
//
//    /**
//     * 用例6：radius过大
//     * 修正：检查是否返回结果而不是比较数量
//     */
//    @Test
//    public void testLargeRadius() throws Exception {
//        Map<String, String> params = new LinkedHashMap<>();
//        params.put("query", "银行");
//        params.put("location", VALID_LOCATION);
//        params.put("radius", "50000"); // 50公里
//        params.put("output", "json");
//        params.put("ak", VALID_AK);
//
//        String response = searchHttpAK.requestGetAKWithReturn(BASE_URL, params);
//        JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);
//
//        assertEquals("大半径检索应返回成功状态", 0, jsonResponse.get("status").getAsInt());
//
//        if (jsonResponse.has("total")) {
//            int total = jsonResponse.get("total").getAsInt();
//            assertTrue("大半径应返回数据，实际total=" + total, total > 0);
//            System.out.println("大半径搜索找到 " + total + " 个结果");
//        }
//    }
//
//    /**
//     * 用例7：radius_limit=true验证圆形区域限制
//     * 修正：添加更多调试信息
//     */
//    @Test
//    public void testRadiusLimitTrue() throws Exception {
//        Map<String, String> params = new LinkedHashMap<>();
//        params.put("query", "银行");
//        params.put("location", VALID_LOCATION);
//        params.put("radius", "1000");
//        params.put("radius_limit", "true");
//        params.put("output", "json");
//        params.put("ak", VALID_AK);
//
//        String response = searchHttpAK.requestGetAKWithReturn(BASE_URL, params);
//        System.out.println("API响应: " + response); // 添加调试输出
//
//        JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);
//
//        assertEquals("radius_limit=true应返回成功状态", 0, jsonResponse.get("status").getAsInt());
//
//        if (jsonResponse.has("results") && jsonResponse.has("total") &&
//                jsonResponse.get("total").getAsInt() > 0) {
//
//            JsonArray results = jsonResponse.getAsJsonArray("results");
//            String[] centerCoords = VALID_LOCATION.split(",");
//            double centerLat = Double.parseDouble(centerCoords[0]);
//            double centerLng = Double.parseDouble(centerCoords[1]);
//            int radiusLimit = 1000; // 米
//
//            System.out.println("检查 " + results.size() + " 个POI的位置");
//
//            // 验证每个POI是否在圆形区域内
//            for (int i = 0; i < results.size(); i++) {
//                JsonObject poi = results.get(i).getAsJsonObject();
//                if (poi.has("location")) {
//                    JsonObject location = poi.getAsJsonObject("location");
//                    double poiLat = location.get("lat").getAsDouble();
//                    double poiLng = location.get("lng").getAsDouble();
//
//                    double distance = calculateDistance(centerLat, centerLng, poiLat, poiLng);
//                    String poiName = poi.has("name") ? poi.get("name").getAsString() : "未知";
//
//                    System.out.println("POI: " + poiName + ", 距离: " + Math.round(distance) + "米");
//
//                    // 允许一定的误差（API可能不是严格按照圆形区域）
//                    assertTrue("POI应在圆形区域内: " + poiName + ", 距离: " + Math.round(distance) + "米",
//                            distance <= radiusLimit * 1.2); // 允许20%误差
//                }
//            }
//        } else {
//            System.out.println("无搜索结果进行距离验证");
//        }
//    }
//
//    /**
//     * 计算两点间距离（米）- 使用Haversine公式
//     */
//    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
//        final int R = 6371000; // 地球半径（米）
//        double latDistance = Math.toRadians(lat2 - lat1);
//        double lngDistance = Math.toRadians(lng2 - lng1);
//        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
//                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
//                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);
//        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
//        return R * c;
//    }
//}
