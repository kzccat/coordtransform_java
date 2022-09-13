/**
 *
 * -----------------------------------------------------------------------------------------
 * 坐标系    |解释                                                              |使用地图
 * -----------------------------------------------------------------------------------------
 * WGS84    |地球坐标系，国际上通用的坐标系。设备一般包含GPS芯片或者北斗芯片获取          |GPS/谷歌地图卫星
 *          |的经纬度为WGS84地理坐标系,最基础的坐标，谷歌地图在非中国地区使用的坐标系     |
 * -----------------------------------------------------------------------------------------
 * GCJ02    |火星坐标系，是由中国国家测绘局制订的地理信息系统的坐标系统。                 |腾讯(搜搜)地图，
 *          |并要求在中国使用的地图产品使用的都必须是加密后的坐标，                      |阿里云地图，高德地图，
 *          |而这套WGS84加密后的坐标就是gcj02。                                    |谷歌国内地图
 * -----------------------------------------------------------------------------------------
 * BD09     |百度坐标系，百度在GCJ02的基础上进行了二次加密，                           |百度地图
 *          |官方解释是为了进一步保护用户隐私（我差点就信了）                           |
 * -----------------------------------------------------------------------------------------
 * 小众坐标系 |类似于百度地图，在GCJ02基础上使用自己的加密算法进行二次加密的坐标系           |搜狗地图、图吧地图 等
 * -----------------------------------------------------------------------------------------
 * 墨卡托坐标  |墨卡托投影以整个世界范围，赤道作为标准纬线，本初子午线作为中央经线，
 *           |两者交点为坐标原点，向东向北为正，向西向南为负。
 *           |南北极在地图的正下、上方，而东西方向处于地图的正右、左。
 *
 * 你可以通过这个工具类将上述坐标系进行互相转换。
 *
 * 百度地图地图投影采用的依然是Web Mercator投影，地图瓦片的切片规则遵循TMS标准，瓦片坐标原点在经纬度为0的附近，
 * 但却做了一定的偏移处理，经测算此偏移量约为（-865，15850），
 * 即地图瓦片（0, 0）是从Web Mercator投影坐标系的（-865，15850）点开始的。
 *
 * 顺便提供百度地图的地图等级从18级到1级
 * 18级，1个像素代表1米，17级，1个像素代表2米，16级代表4米，依此类推
 * Author: kong
 */
public class Coordtransform {

    public static double baiduChange = (Math.PI * 3000.0) / 180.0;
    public static double ee = 0.00669342162296594323;  //偏心率平方
    public static double a = 6378245.0;//  # 长半轴

    /**
     *     百度坐标系(BD-09)转火星坐标系(GCJ-02)
     *     百度——>谷歌、高德
     *
     * @param lng_BD 百度坐标经度
     * @param lat_BD 百度坐标纬度
     * @return 转换后的坐标列表形式
     */
    public static double[] BD09toGCJ02(double lng_BD, double lat_BD){
        double[] GCJ02 = new double[2];

        double x = lng_BD - 0.0065;
        double y = lat_BD - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * baiduChange);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * baiduChange);
        double gg_lng = z * Math.cos(theta);
        double gg_lat = z * Math.sin(theta);
        GCJ02[0] = gg_lng;
        GCJ02[1] = gg_lat;

        return GCJ02;
    }

    /**
     *     火星坐标系(GCJ-02)转百度坐标系(BD-09)
     *     谷歌、高德——>百度
     * @param lng_GCJ
     * @param lat_GCJ
     * @return 转换后的坐标列表形式
     */
    public static double[] GCJ02toBD09(double lng_GCJ, double lat_GCJ) {
        double[] BD09 = new double[2];
//        """
//                实现GCJ02向BD09坐标系的转换
//                :param lng: GCJ02坐标系下的经度
//                :param lat: GCJ02坐标系下的纬度
//                :return: 转换后的BD09下经纬度
//                """
        double z = Math.sqrt(lng_GCJ * lng_GCJ + lat_GCJ * lat_GCJ) + 0.00002 * Math.sin(lat_GCJ * Math.PI);
        double theta = Math.atan2(lat_GCJ, lng_GCJ) + 0.000003 * Math.cos(lng_GCJ * Math.PI);
        double bd_lng = z * Math.cos(theta) + 0.0065;
        double bd_lat = z * Math.sin(theta) + 0.006;
        BD09[0] = bd_lng;
        BD09[1] = bd_lat;
        return BD09;
    }
    /**
     *     GCJ02(火星坐标系)转GPS84
     * @param lng_gcj 火星坐标系的经度
     * @param lat_gcj 火星坐标系纬度
     * @return 转换后的坐标列表形式
     */
    public static double[] GCJ02toWGS84(double lng_gcj, double lat_gcj){

        double[] wgs84 = new double[2];
        if (outOfChina(lng_gcj, lat_gcj)) {
            return new double[]{lng_gcj,lat_gcj};
        }
//       if out_of_china(lng, lat):
//       return [lng, lat]
        double dlat = transformlat(lng_gcj - 105.0, lat_gcj - 35.0);
        double dlng = transformlng(lng_gcj - 105.0, lat_gcj - 35.0);
        double radlat = lat_gcj / 180.0 * Math.PI;
        double magic = Math.sin(radlat);
        magic = 1 - ee * magic * magic;
        double sqrtmagic = Math.sqrt(magic);
        dlat = (dlat * 180.0) / ((a * (1 - ee)) / (magic * sqrtmagic) * Math.PI);
        dlng = (dlng * 180.0) / (a / sqrtmagic * Math.cos(radlat) * Math.PI);
        double mglat = lat_gcj + dlat;
        double mglng = lng_gcj + dlng;
        return new double[]{lng_gcj * 2 - mglng, lat_gcj * 2 - mglat};
    }

    /**
     *     GPS84转GCJ02(火星坐标系)
     * @param lng_wgs WGS84坐标系的经度
     * @param lat_wgs WGS84坐标系纬度
     * @return 转换后的GCJ02下经纬度
     */
    public static double[] WGS84toGCJ02(double lng_wgs, double lat_wgs) {
        if (outOfChina(lng_wgs, lat_wgs)) {
            return new double[]{lng_wgs,lat_wgs};
        }
        double[] GCJ02 = new double[2];

        double dlat = transformlat(lng_wgs - 105.0, lat_wgs - 35.0);
        double dlng = transformlng(lng_wgs - 105.0, lat_wgs - 35.0);
        double radlat = lat_wgs / 180.0 * Math.PI;
        double magic = Math.sin(radlat);
        magic = 1 - ee * magic * magic;
        double sqrtmagic = Math.sqrt(magic);
        dlat = (dlat * 180.0) / ((a * (1 - ee)) / (magic * sqrtmagic) * Math.PI);
        dlng = (dlng * 180.0) / (a / sqrtmagic * Math.cos(radlat) * Math.PI);
        double gcj_lng = lat_wgs + dlat;
        double gcj_lat = lng_wgs + dlng;
        GCJ02[0] = gcj_lng;
        GCJ02[1] = gcj_lat;
        return GCJ02;
    }
    /**
     *     GPS84 转 墨卡托坐标
     * @param lng GPS84的经度
     * @param lat GPS84纬度
     * @return 转换后的坐标列表形式
     */
    public static double[] WGS84toMercator(double lng,double lat){
        double x = lng * 20037508.342789/180;
        double y = Math.log(Math.tan((90+lat)*Math.PI/360))/(Math.PI/180);
        y = y *20037508.342789/180;
        return new double[]{x, y};
    }


    /**
     * 百度坐标系转成通用墨卡托坐标
     *
     * @param lng
     * @param lat
     * @return
     */
    public static double[] bdtoMercator(double lng,double lat){
        double[] gcj02 = BD09toGCJ02(lng, lat);
        double[] wgs84 = GCJ02toWGS84(gcj02[0], gcj02[1]);
        double[] mercator = WGS84toMercator(wgs84[0], wgs84[1]);
        return mercator;
    }



    private static double transformlat(double lng,double lat){
        double ret = -100.0 + 2.0 * lng + 3.0 * lat + 0.2 * lat * lat +
                0.1 * lng * lat + 0.2 * Math.sqrt(Math.abs(lng));
        ret += (20.0 * Math.sin(6.0 * lng * Math.PI) + 20.0 *
                Math.sin(2.0 * lng * Math.PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(lat * Math.PI) + 40.0 *
                Math.sin(lat / 3.0 * Math.PI)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(lat / 12.0 * Math.PI) + 320 *
                Math.sin(lat * Math.PI / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    private static double transformlng(double lng,double lat) {
        double ret = 300.0 + lng + 2.0 * lat + 0.1 * lng * lng +
                0.1 * lng * lat + 0.1 * Math.sqrt(Math.abs(lng));
        ret += (20.0 * Math.sin(6.0 * lng * Math.PI) + 20.0 *
                Math.sin(2.0 * lng * Math.PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(lng * Math.PI) + 40.0 *
                Math.sin(lng / 3.0 * Math.PI)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(lng / 12.0 * Math.PI) + 300.0 *
                Math.sin(lng / 30.0 * Math.PI)) * 2.0 / 3.0;
        return ret;
    }

    /**
     * 判断是否在国内，不在国内不做偏移
     * @param lng
     * @param lat
     * @return
     */
    private static boolean outOfChina(double lng, double lat) {
        return  !(lng > 73.66 && lng < 135.05 && lat > 3.86 && lat < 53.55);
    }

    public static void main(String[] args) {

    }
