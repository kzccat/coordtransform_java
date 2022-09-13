# 坐标转换模块 java版（附带标准墨卡托坐标）

提供了百度坐标（BD09）、国测局坐标（火星坐标，GCJ02）、和WGS84坐标系之间的转换。

额外提供了百度坐标到通用墨卡托坐标的转换方式，方便各位应用百度1-18级地图进行像素点到坐标的转化。

无其他依赖。

需要js版本可以移步：https://github.com/wandergis/coordtransform

 python版本：https://github.com/wandergis/coordTransform_py 

 go语言社区版本：https://github.com/qichengzx/coordtransform 

# 方法说明



GCJ02toBD09(double lng_GCJ, double lat_GCJ) # 火星坐标系->百度坐标系
BD09toGCJ02(double lng_BD, double lat_BD)# 百度坐标系->火星坐标系
WGS84toGCJ02(double lng_wgs, double lat_wgs) # WGS84坐标系->火星坐标系
GCJ02toWGS84(double lng_gcj, double lat_gcj) # 火星坐标系->WGS84坐标系
WGS84toMercator(lng, lat) # WGS84坐标系->标准墨卡托坐标系

bdtoMercator(double lng,double lat) # 百度坐标系->标准墨卡托坐标系



