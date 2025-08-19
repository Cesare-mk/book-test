package com.mm.shoppingcat;

import com.google.gson.Gson;
import com.mm.shoppingcat.SearchHttpAK;
import org.junit.Before;
import org.junit.Test;
import java.util.LinkedHashMap;
import java.util.Map;
import static org.junit.Assert.*;

public class BaiduCircleSearchTest {

    private SearchHttpAK searchClient; // 复用前文的HTTP请求工具类
    private Gson gson;
    private static final String BASE_URL = "https://api.map.baidu.com/place/v2/search?";
    private static final String VALID_AK = "ZNzbMLAIVEt4zeSDaebK6dPioHBTMDOZ"; // 替换为实际AK
    private static final String INVALID_AK = "无效AK";
    private static final String VALID_LOCATION = "39.915,116.404"; // 北京天安门经纬度
    private static final String CITY_LEVEL_LOCATION = "39.9042,116.4074"; // 北京中心坐标（用于城市范围校验）

    // API响应数据模型（简化版）
    static class ApiResponse {
        int status;
        String message;
        int total;
        Result[] results; // 用于解析POI详情

        static class Result {
            String name;
            Location location; // POI经纬度

            static class Location {
                double lat; // 纬度
                double lng; // 经度
            }
        }
    }

    @Before
    public void init() {
        searchClient = new SearchHttpAK();
        gson = new Gson();
    }

    // -------------------------- 必选参数校验 --------------------------
    /**
     * 用例1：缺失ak（访问密钥）
     * 预期：status=101，message="AK参数不存在"
     */
    @Test
    public void testMissingAk() throws Exception {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("query", "餐厅");
        params.put("location", VALID_LOCATION);
        params.put("radius", "500");
        params.put("output", "json");
        // 不添加ak参数

        String response = searchClient.requestGetAKWithReturn(BASE_URL, params);
        ApiResponse resp = gson.fromJson(response, ApiResponse.class);

        assertEquals("缺失ak的错误状态码不正确", 101, resp.status);
        assertEquals("缺失ak的错误信息不正确", "AK参数不存在", resp.message);
    }

    /**
     * 用例2：location格式错误（纬度超出±90°）
     * 预期：返回坐标无效的错误提示
     */
    @Test
    public void testInvalidLocation() throws Exception {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("query", "餐厅");
        //params.put("location", "91.123,116.404"); // 纬度91°（超出上限90°）
        params.put("location", "abc,123"); //格式错误（非经纬度数值）
        params.put("radius", "500");
        params.put("output", "json");
        params.put("ak", VALID_AK);

        String response = searchClient.requestGetAKWithReturn(BASE_URL, params);
        ApiResponse resp = gson.fromJson(response, ApiResponse.class);

        assertNotEquals("坐标错误未返回非0状态", 0, resp.status);
        assertTrue("未返回坐标无效提示", resp.message.contains("坐标无效") || resp.message.contains("location参数错误"));
    }

    /**
     * 用例3：query为空
     * 预期：total=0（无检索结果）
     */
    @Test
    public void testEmptyQuery() throws Exception {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("query", ""); // 空查询关键词
        params.put("location", VALID_LOCATION);
        params.put("radius", "500");
        params.put("output", "json");
        params.put("ak", VALID_AK);

        String response = searchClient.requestGetAKWithReturn(BASE_URL, params);
        ApiResponse resp = gson.fromJson(response, ApiResponse.class);

        assertEquals("query为空时状态码错误", 0, resp.status); // 空query不报错，但无结果
        assertEquals("query为空时应返回0结果", 0, resp.total);
    }

    // -------------------------- 功能正确性验证 --------------------------
    /**
     * 用例4：正常参数组合
     * 预期：返回餐厅POI列表，status=0
     */
    @Test
    public void testValidParams() throws Exception {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("query", "餐厅");
        params.put("location", VALID_LOCATION);
        params.put("radius", "500");
        params.put("output", "json");
        params.put("ak", VALID_AK);

        String response = searchClient.requestGetAKWithReturn(BASE_URL, params);
        ApiResponse resp = gson.fromJson(response, ApiResponse.class);

        assertEquals("正常参数组合状态码错误", 0, resp.status);
        assertTrue("未返回餐厅POI数据", resp.total > 0);
        // 验证返回结果中包含"餐厅"相关名称（模糊匹配）
        boolean hasRestaurant = false;
        for (ApiResponse.Result result : resp.results) {
            if (result.name.contains("餐厅") || result.name.contains("餐馆")) {
                hasRestaurant = true;
                break;
            }
        }
        assertTrue("返回结果不含餐厅POI", hasRestaurant);
    }

    /**
     * 用例5：多关键字检索（餐厅$咖啡店）
     * 预期：返回两种类型的POI结果
     */
    @Test
    public void testMultiKeywordQuery() throws Exception {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("query", "餐厅$咖啡店"); // 多关键字用$分隔
        params.put("location", VALID_LOCATION);
        params.put("radius", "1000");
        params.put("output", "json");
        params.put("ak", VALID_AK);

        String response = searchClient.requestGetAKWithReturn(BASE_URL, params);
        ApiResponse resp = gson.fromJson(response, ApiResponse.class);

        assertEquals("多关键字检索状态码错误", 0, resp.status);
        assertTrue("未返回多类型POI数据", resp.total > 0);
        // 验证结果包含两种类型
        boolean hasRestaurant = false;
        boolean hasCafe = false;
        for (ApiResponse.Result result : resp.results) {
            if (result.name.contains("餐厅") || result.name.contains("餐馆")) {
                hasRestaurant = true;
            }
            if (result.name.contains("咖啡") || result.name.contains("cafe")) {
                hasCafe = true;
            }
        }
        assertTrue("返回结果不含餐厅POI", hasRestaurant);
        assertTrue("返回结果不含咖啡店POI", hasCafe);
    }

    // -------------------------- 边界条件测试 --------------------------
    /**
     * 用例6：radius过大（100000米，超出城市范围）
     * 预期：自动转为城市范围检索，返回该城市POI
     */
    @Test
    public void testLargeRadius() throws Exception {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("query", "公园");
        params.put("location", VALID_LOCATION); // 北京坐标
        params.put("radius", "100000"); // 100公里（远超北京城区范围）
        params.put("output", "json");
        params.put("ak", VALID_AK);

        String response = searchClient.requestGetAKWithReturn(BASE_URL, params);
        ApiResponse resp = gson.fromJson(response, ApiResponse.class);

        assertEquals("大半径检索状态码错误", 0, resp.status);
        assertTrue("未返回城市范围POI数据", resp.total > 0);
        // 对比：相同query在城市中心的检索结果应与大半径结果数量接近（均为城市范围）
        Map<String, String> cityParams = new LinkedHashMap<>();
        cityParams.put("query", "公园");
        cityParams.put("location", CITY_LEVEL_LOCATION);
        cityParams.put("radius", "1000"); // 小半径但实际会转为城市检索
        cityParams.put("output", "json");
        cityParams.put("ak", VALID_AK);
        String cityResponse = searchClient.requestGetAKWithReturn(BASE_URL, cityParams);
        ApiResponse cityResp = gson.fromJson(cityResponse, ApiResponse.class);
        assertTrue("大半径未转为城市检索", Math.abs(resp.total - cityResp.total) < 50); // 允许一定误差
    }

    /**
     * 用例7：radius_limit=true时，验证返回结果坐标均在圆形区域内
     * 预期：所有POI的经纬度与圆心距离≤radius
     */
    @Test
    public void testRadiusLimit() throws Exception {
        double centerLat = 39.915; // 圆心纬度
        double centerLng = 116.404; // 圆心经度
        int radius = 500; // 半径500米

        Map<String, String> params = new LinkedHashMap<>();
        params.put("query", "超市");
        params.put("location", centerLat + "," + centerLng);
        params.put("radius", String.valueOf(radius));
        params.put("radius_limit", "true"); // 严格限定范围
        params.put("output", "json");
        params.put("ak", VALID_AK);

        String response = searchClient.requestGetAKWithReturn(BASE_URL, params);
        ApiResponse resp = gson.fromJson(response, ApiResponse.class);

        assertEquals("radius_limit参数状态码错误", 0, resp.status);
        // 计算每个POI与圆心的距离，验证≤radius
        for (ApiResponse.Result result : resp.results) {
            double poiLat = result.location.lat;
            double poiLng = result.location.lng;
            double distance = calculateDistance(centerLat, centerLng, poiLat, poiLng);
            assertTrue("POI超出限定半径范围，距离：" + distance + "米", distance <= radius);
        }
    }

    /**
     * 辅助方法：计算两点经纬度之间的直线距离（单位：米）
     * 基于Haversine公式
     */
    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        final int R = 6371000; // 地球半径（米）
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lng2 - lng1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // 距离（米）
    }
}